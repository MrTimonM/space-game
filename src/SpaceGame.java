import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class SpaceGame extends JPanel implements ActionListener, KeyListener {
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    
    private Timer gameTimer;
    private Player player;
    private ArrayList<Rock> rocks;
    private ArrayList<Bullet> bullets;
    private ArrayList<Enemy> enemies;
    private ArrayList<EnemyBullet> enemyBullets;
    private ArrayList<Explosion> explosions;
    private Boss boss;
    private ArrayList<BossBullet> bossBullets;
    private ArrayList<SubEnemy> subEnemies;
    private Random random;
    private BufferedImage asteroidSheet;
    private BufferedImage backgroundSheet;
    private BufferedImage playerSheet;
    private BufferedImage enemySheet;
    private BufferedImage exhaustSheet;
    private BufferedImage bulletSheet;
    private BufferedImage uiSheet;
    private BufferedImage explosionSheet;
    private BufferedImage allSheet;
    private ArrayList<HealthPowerUp> healthPowerUps;
    private double backgroundOffsetY;
    private int spawnTimer;
    private int enemySpawnTimer;
    private int killCount;
    private int autoFireTimer;
    private int lives;
    private int invincibilityTimer;
    private int score;
    private boolean gameOver;
    private boolean gameWon;
    private int gameTime;
    private boolean bossDefeated;
    private int bossSpawnDelay;
    private GameState gameState;
    private Clip musicClip;
    private Clip bossMusicClip;
    public static final int ROCK_SCALE = 2;  // Double the rock size
    
    public SpaceGame() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        setDoubleBuffered(true); // Enable double buffering for smoother rendering
        
        random = new Random();
        rocks = new ArrayList<>();
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        enemyBullets = new ArrayList<>();
        explosions = new ArrayList<>();
        bossBullets = new ArrayList<>();
        subEnemies = new ArrayList<>();
        healthPowerUps = new ArrayList<>();
        boss = null;
        bossDefeated = false;
        gameWon = false;
        gameTime = 0;
        bossSpawnDelay = 0;
        score = 0;
        gameState = GameState.PLAYING;
        backgroundOffsetY = 0;
        spawnTimer = 0;
        enemySpawnTimer = 0;
        killCount = 0;
        autoFireTimer = 0;
        lives = 10; // 5 hearts × 2 lives each
        invincibilityTimer = 0;
        gameOver = false;
        
        loadImages();
        
        player = new Player(WINDOW_WIDTH / 2 - 32, WINDOW_HEIGHT - 100, playerSheet, exhaustSheet);
        
        playMusic();
        
        gameTimer = new Timer(20, this); // 50 FPS for better performance and slower gameplay
        gameTimer.start();
    }
    
    private void loadImages() {
        try {
            asteroidSheet = ImageIO.read(new File("../Assets/Asteroids-0001.png"));
            backgroundSheet = ImageIO.read(new File("../Assets/Background_Full-0001.png"));
            playerSheet = ImageIO.read(new File("../Assets/SpaceShips_Player-0001.png"));
            enemySheet = ImageIO.read(new File("../Assets/SpaceShips_Enemy-0001.png"));
            exhaustSheet = ImageIO.read(new File("../Assets/Exhaust-0001.png"));
            bulletSheet = ImageIO.read(new File("../Assets/Bullets-0001.png"));
            uiSheet = ImageIO.read(new File("../Assets/UI_sprites-0001.png"));
            explosionSheet = ImageIO.read(new File("../Assets/Explosion-0001.png"));
            allSheet = ImageIO.read(new File("../Assets/All.png"));
        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void playMusic() {
        try {
            // Load theme music
            File musicFile = new File("../Assets/theme.wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
            musicClip = AudioSystem.getClip();
            musicClip.open(audioStream);
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            musicClip.start();
            
            // Load boss music (will be played when boss appears)
            File bossMusicFile = new File("../Assets/boss music.wav");
            AudioInputStream bossAudioStream = AudioSystem.getAudioInputStream(bossMusicFile);
            bossMusicClip = AudioSystem.getClip();
            bossMusicClip.open(bossAudioStream);
        } catch (Exception e) {
            System.err.println("Error loading music: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void switchToBossMusic() {
        try {
            if (musicClip != null && musicClip.isRunning()) {
                musicClip.stop();
            }
            if (bossMusicClip != null) {
                bossMusicClip.setFramePosition(0);
                bossMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                bossMusicClip.start();
            }
        } catch (Exception e) {
            System.err.println("Error switching to boss music: " + e.getMessage());
        }
    }
    
    private void switchToThemeMusic() {
        try {
            if (bossMusicClip != null && bossMusicClip.isRunning()) {
                bossMusicClip.stop();
            }
            if (musicClip != null) {
                musicClip.setFramePosition(0);
                musicClip.loop(Clip.LOOP_CONTINUOUSLY);
                musicClip.start();
            }
        } catch (Exception e) {
            System.err.println("Error switching to theme music: " + e.getMessage());
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }
    
    private void update() {
        if (gameState != GameState.PLAYING) {
            return; // Don't update if not playing
        }
        
        if (gameOver || gameWon) {
            return; // Don't update if game is over or won
        }
        
        // Update game time (50 FPS, so 50 frames = 1 second)
        gameTime++;
        
        // Trigger boss spawn sequence at 30 seconds (30 * 50 = 1500 frames)
        if (gameTime == 1500 && boss == null && !bossDefeated) {
            // Start clearing enemies and rocks with explosions
            clearEnemiesForBoss();
            bossSpawnDelay = 50; // 1 second delay before boss appears
        }
        
        // Spawn boss after delay
        if (bossSpawnDelay > 0) {
            bossSpawnDelay--;
            if (bossSpawnDelay == 0) {
                spawnBoss();
            }
        }
        
        // Update invincibility timer
        if (invincibilityTimer > 0) {
            invincibilityTimer--;
        }
        
        // Update player
        player.update();
        
        // Auto fire
        autoFireTimer++;
        if (autoFireTimer > 30) { // Fire every ~0.6 seconds
            autoFireTimer = 0;
            shootBullets();
        }
        
        // Update background scroll
        backgroundOffsetY += 1.5;
        if (backgroundOffsetY >= WINDOW_HEIGHT) {
            backgroundOffsetY = 0;
        }
        
        // Update rocks
        for (int i = rocks.size() - 1; i >= 0; i--) {
            Rock rock = rocks.get(i);
            rock.update();
            
            // Remove rocks that are off screen
            if (rock.y > WINDOW_HEIGHT + 100) {
                rocks.remove(i);
            }
        }
        
        // Update bullets
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.update();
            
            // Remove bullets that are off screen
            if (bullet.y < -50) {
                bullets.remove(i);
            }
        }
        
        // Limit bullets to prevent lag
        while (bullets.size() > 30) {
            bullets.remove(0);
        }
        
        // Update explosions
        for (int i = explosions.size() - 1; i >= 0; i--) {
            Explosion explosion = explosions.get(i);
            explosion.update();
            if (explosion.isFinished()) {
                explosions.remove(i);
            }
        }
        
        // Update boss if active
        if (boss != null) {
            boss.update();
            
            // Boss shoots bullets frequently (increased rate)
            if (random.nextInt(30) < 1 && bossBullets.size() < 30) { // Fire more often, limit to 30 boss bullets
                boss.shoot(bossBullets, bulletSheet);
            }
            
            // Boss throws sub-enemies occasionally
            if (random.nextInt(240) < 1 && subEnemies.size() < 5) { // Less frequent, max 5 sub-enemies
                boss.throwSubEnemy(subEnemies, enemySheet);
            }
        }
        
        // Update boss bullets
        for (int i = bossBullets.size() - 1; i >= 0; i--) {
            BossBullet bullet = bossBullets.get(i);
            bullet.update();
            
            // Remove bullets that are off screen
            if (bullet.y > WINDOW_HEIGHT + 50) {
                bossBullets.remove(i);
            }
        }
        
        // Limit boss bullets to prevent lag
        while (bossBullets.size() > 35) {
            bossBullets.remove(0);
        }
        
        // Update sub-enemies
        for (int i = subEnemies.size() - 1; i >= 0; i--) {
            SubEnemy subEnemy = subEnemies.get(i);
            subEnemy.update();
            
            // Remove sub-enemies that are off screen
            if (subEnemy.y > WINDOW_HEIGHT + 100) {
                subEnemies.remove(i);
            }
        }
        
        // Update health power-ups
        for (int i = healthPowerUps.size() - 1; i >= 0; i--) {
            HealthPowerUp powerUp = healthPowerUps.get(i);
            powerUp.update();
            
            // Remove power-ups that are off screen
            if (powerUp.y > WINDOW_HEIGHT + 50) {
                healthPowerUps.remove(i);
            }
        }
        
        // Spawn health power-ups randomly (only when boss is not active)
        if (boss == null && random.nextInt(1200) < 1) { // Much rarer spawn
            int x = random.nextInt(WINDOW_WIDTH - 50) + 25;
            healthPowerUps.add(new HealthPowerUp(x, -30, allSheet));
        }
        
        // Update enemies (only if boss is not present)
        if (boss == null) {
            for (int i = enemies.size() - 1; i >= 0; i--) {
                Enemy enemy = enemies.get(i);
                enemy.update();
                
                // Enemy shoots occasionally (reduced frequency)
                if (random.nextInt(200) < 2) { // 1% chance per frame (was 2%)
                    enemy.shoot(enemyBullets, bulletSheet);
                }
                
                // Remove enemies that are off screen
                if (enemy.y > WINDOW_HEIGHT + 100) {
                    enemies.remove(i);
                }
            }
        }
        
        // Update enemy bullets
        for (int i = enemyBullets.size() - 1; i >= 0; i--) {
            EnemyBullet bullet = enemyBullets.get(i);
            bullet.update();
            
            // Remove bullets that are off screen
            if (bullet.y > WINDOW_HEIGHT + 50) {
                enemyBullets.remove(i);
            }
        }
        
        // Check collisions between player bullets and rocks
        for (int i = bullets.size() - 1; i >= 0; i--) {
            if (i >= bullets.size()) continue;
            Bullet bullet = bullets.get(i);
            for (int j = rocks.size() - 1; j >= 0; j--) {
                Rock rock = rocks.get(j);
                if (checkCollision(bullet.x, bullet.y, 16, 16, rock.x, rock.y, rock.width, rock.height)) {
                    bullets.remove(i);
                    rock.health--;
                    if (rock.health <= 0) {
                        explosions.add(new Explosion(rock.x, rock.y, rock.level, explosionSheet));
                        // Add points based on rock level
                        if (rock.level == 1) score += 15;      // Small rock
                        else if (rock.level == 2) score += 25; // Medium rock
                        else if (rock.level == 3) score += 50; // Big rock
                        rocks.remove(j);
                    }
                    break;
                }
            }
        }
        
        // Check collisions between player bullets and enemies
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            for (int j = enemies.size() - 1; j >= 0; j--) {
                Enemy enemy = enemies.get(j);
                if (checkCollision(bullet.x, bullet.y, 16, 16, enemy.x, enemy.y, enemy.width, enemy.height)) {
                    bullets.remove(i);
                    enemies.remove(j);
                    killCount++;
                    score += 10; // Enemy destroyed
                    break;
                }
            }
        }
        
        // Check collisions between player bullets and boss
        if (boss != null) {
            for (int i = bullets.size() - 1; i >= 0; i--) {
                if (i >= bullets.size()) continue;
                Bullet bullet = bullets.get(i);
                if (boss == null) break; // Boss was destroyed, exit loop
                if (checkCollision(bullet.x, bullet.y, 16, 16, boss.x, boss.y, boss.width, boss.height)) {
                    bullets.remove(i);
                    boss.health--;
                    if (boss.health <= 0) {
                        // Boss defeated!
                        explosions.add(new BossExplosion(boss.x, boss.y, explosionSheet));
                        boss = null;
                        bossDefeated = true;
                        gameWon = true;
                        // Switch back to theme music
                        switchToThemeMusic();
                        break; // Exit loop after boss is defeated
                    }
                }
            }
        }
        
        // Check collisions between player bullets and sub-enemies
        for (int i = bullets.size() - 1; i >= 0; i--) {
            if (i >= bullets.size()) continue;
            Bullet bullet = bullets.get(i);
            for (int j = subEnemies.size() - 1; j >= 0; j--) {
                SubEnemy subEnemy = subEnemies.get(j);
                if (checkCollision(bullet.x, bullet.y, 16, 16, subEnemy.x, subEnemy.y, subEnemy.width, subEnemy.height)) {
                    bullets.remove(i);
                    subEnemies.remove(j);
                    killCount++;
                    score += 10; // Sub-enemy destroyed (same points as regular enemy)
                    break;
                }
            }
        }
        
        // Check collisions between player and enemies
        if (invincibilityTimer == 0) {
            // Check collision with boss
            if (boss != null) {
                if (checkCollision(player.x, player.y, player.width, player.height, boss.x, boss.y, boss.width, boss.height)) {
                    loseLife();
                }
            }
            
            // Check collision with sub-enemies
            for (int i = subEnemies.size() - 1; i >= 0; i--) {
                SubEnemy subEnemy = subEnemies.get(i);
                if (checkCollision(player.x, player.y, player.width, player.height, subEnemy.x, subEnemy.y, subEnemy.width, subEnemy.height)) {
                    subEnemies.remove(i);
                    loseLife();
                    break;
                }
            }
            
            for (int i = enemies.size() - 1; i >= 0; i--) {
                Enemy enemy = enemies.get(i);
                if (checkCollision(player.x, player.y, player.width, player.height, enemy.x, enemy.y, enemy.width, enemy.height)) {
                    enemies.remove(i);
                    loseLife();
                    break;
                }
            }
            
            // Check collisions between player and rocks
            for (int i = rocks.size() - 1; i >= 0; i--) {
                Rock rock = rocks.get(i);
                if (checkCollision(player.x, player.y, player.width, player.height, rock.x, rock.y, rock.width, rock.height)) {
                    rocks.remove(i);
                    loseLife();
                    break;
                }
            }
            
            // Check collisions between player and enemy bullets
            for (int i = enemyBullets.size() - 1; i >= 0; i--) {
                EnemyBullet bullet = enemyBullets.get(i);
                if (checkCollision(player.x, player.y, player.width, player.height, bullet.x, bullet.y, 16, 16)) {
                    enemyBullets.remove(i);
                    loseLife();
                    break;
                }
            }
            
            // Check collisions between player and boss bullets
            for (int i = bossBullets.size() - 1; i >= 0; i--) {
                BossBullet bullet = bossBullets.get(i);
                if (checkCollision(player.x, player.y, player.width, player.height, bullet.x, bullet.y, 16, 16)) {
                    bossBullets.remove(i);
                    loseLife();
                    break;
                }
            }
            
            // Check collisions between player and health power-ups
            for (int i = healthPowerUps.size() - 1; i >= 0; i--) {
                HealthPowerUp powerUp = healthPowerUps.get(i);
                if (checkCollision(player.x, player.y, player.width, player.height, powerUp.x, powerUp.y, 32, 32)) {
                    healthPowerUps.remove(i);
                    // Add life, max 10 (5 hearts)
                    if (lives < 10) {
                        lives++;
                    }
                    break;
                }
            }
        }
        
        // Spawn new rocks (not when boss is active or spawning)
        if (boss == null && bossSpawnDelay == 0) {
            spawnTimer++;
            if (spawnTimer > 120 && rocks.size() < 10) { // Spawn every ~2 seconds, max 10 rocks
                spawnTimer = 0;
                spawnRock();
            }
        }
        
        // Spawn new enemies (not when boss is active or spawning)
        if (boss == null && bossSpawnDelay == 0) {
            enemySpawnTimer++;
            if (enemySpawnTimer > 120 && enemies.size() < 8) { // Slower spawn, max 8 enemies
                enemySpawnTimer = 0;
                spawnEnemy();
            }
        }
    }
    
    private boolean checkCollision(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
    }
    
    private void loseLife() {
        lives--;
        invincibilityTimer = 60; // 1 second of invincibility
        if (lives <= 0) {
            gameOver = true;
            // Switch to theme music on game over
            switchToThemeMusic();
        }
    }
    
    private void spawnEnemy() {
        int pattern = random.nextInt(5);
        
        switch(pattern) {
            case 0: // Single enemy from top
                enemies.add(new Enemy(random.nextInt(WINDOW_WIDTH - 50), -50, enemySheet, 1));
                break;
            case 1: // Two enemies side by side
                enemies.add(new Enemy(random.nextInt(WINDOW_WIDTH / 2), -50, enemySheet, 1));
                enemies.add(new Enemy(WINDOW_WIDTH / 2 + random.nextInt(WINDOW_WIDTH / 2 - 50), -50, enemySheet, 1));
                break;
            case 2: // Three enemies in a row
                int startX = random.nextInt(WINDOW_WIDTH / 2);
                for (int i = 0; i < 3; i++) {
                    enemies.add(new Enemy(startX + i * 60, -50 - i * 30, enemySheet, 1));
                }
                break;
            case 3: // V formation
                int centerX = WINDOW_WIDTH / 2;
                enemies.add(new Enemy(centerX, -50, enemySheet, 1));
                enemies.add(new Enemy(centerX - 60, -80, enemySheet, 1));
                enemies.add(new Enemy(centerX + 60, -80, enemySheet, 1));
                break;
            case 4: // Diagonal line
                int diagX = random.nextInt(WINDOW_WIDTH / 2);
                for (int i = 0; i < 3; i++) {
                    enemies.add(new Enemy(diagX + i * 50, -50 - i * 40, enemySheet, 2));
                }
                break;
        }
    }
    
    private void clearEnemiesForBoss() {
        // Create explosions for all existing enemies
        for (Enemy enemy : enemies) {
            explosions.add(new Explosion(enemy.x, enemy.y, 1, explosionSheet));
        }
        enemies.clear();
        
        // Create explosions for all existing rocks
        for (Rock rock : rocks) {
            explosions.add(new Explosion(rock.x, rock.y, rock.level, explosionSheet));
        }
        rocks.clear();
        
        // Clear bullets
        enemyBullets.clear();
    }
    
    private void spawnBoss() {
        // Spawn boss in the center top
        boss = new Boss(WINDOW_WIDTH / 2 - 72, -150, enemySheet);
        boss.setAllSheet(allSheet);
        
        // Switch to boss music
        switchToBossMusic();
    }
    
    private void spawnRock() {
        int level = random.nextInt(3) + 1; // 1, 2, or 3
        int x = random.nextInt(WINDOW_WIDTH - 100) + 50;
        rocks.add(new Rock(x, -50, level, asteroidSheet));
    }
    
    private void shootBullets() {
        // Left bullet - fires from position 16,32 on the spaceship
        int leftX = player.x + (int)(16 * 1.5);
        int leftY = player.y + (int)(32 * 1.5);
        bullets.add(new Bullet(leftX, leftY, bulletSheet));
        
        // Right bullet - fires from position 32,32 on the spaceship
        int rightX = player.x + (int)(32 * 1.5);
        int rightY = player.y + (int)(32 * 1.5);
        bullets.add(new Bullet(rightX, rightY, bulletSheet));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Draw scrolling background
        drawBackground(g2d);
        
        // Draw rocks
        for (Rock rock : rocks) {
            rock.draw(g2d);
        }
        
        // Draw explosions
        for (Explosion explosion : explosions) {
            explosion.draw(g2d);
        }
        
        // Draw boss
        if (boss != null) {
            boss.draw(g2d);
        }
        
        // Draw sub-enemies
        for (SubEnemy subEnemy : subEnemies) {
            subEnemy.draw(g2d);
        }
        
        // Draw enemies
        for (Enemy enemy : enemies) {
            enemy.draw(g2d);
        }
        
        // Draw bullets
        for (Bullet bullet : bullets) {
            bullet.draw(g2d);
        }
        
        // Draw enemy bullets
        for (EnemyBullet bullet : enemyBullets) {
            bullet.draw(g2d);
        }
        
        // Draw boss bullets
        for (BossBullet bullet : bossBullets) {
            bullet.draw(g2d);
        }
        
        // Draw health power-ups
        for (HealthPowerUp powerUp : healthPowerUps) {
            powerUp.draw(g2d);
        }
        
        // Draw player
        player.draw(g2d);
        
        // Draw lives
        drawLives(g2d);
        
        // Draw score in top right corner
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.setColor(Color.WHITE);
        String scoreText = "Score: " + score;
        int scoreWidth = g2d.getFontMetrics().stringWidth(scoreText);
        g2d.drawString(scoreText, WINDOW_WIDTH - scoreWidth - 20, 30);
        
        // Draw game over message
        if (gameOver) {
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            g2d.setColor(Color.RED);
            g2d.drawString("GAME OVER", WINDOW_WIDTH / 2 - 150, WINDOW_HEIGHT / 2);
            g2d.setFont(new Font("Arial", Font.PLAIN, 24));
            g2d.setColor(Color.WHITE);
            g2d.drawString("Final Kills: " + killCount, WINDOW_WIDTH / 2 - 80, WINDOW_HEIGHT / 2 + 50);
            g2d.drawString("Press R to Restart", WINDOW_WIDTH / 2 - 100, WINDOW_HEIGHT / 2 + 90);
        }
        
        // Draw win message
        if (gameWon) {
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            g2d.setColor(Color.GREEN);
            g2d.drawString("YOU WIN!", WINDOW_WIDTH / 2 - 130, WINDOW_HEIGHT / 2);
            g2d.setFont(new Font("Arial", Font.PLAIN, 24));
            g2d.setColor(Color.WHITE);
            g2d.drawString("Boss Defeated!", WINDOW_WIDTH / 2 - 80, WINDOW_HEIGHT / 2 + 50);
            g2d.drawString("Press R to Play Again", WINDOW_WIDTH / 2 - 110, WINDOW_HEIGHT / 2 + 90);
        }
    }
    
    private void drawBackground(Graphics2D g2d) {
        if (backgroundSheet == null) return;
        
        // Scale background to fill entire screen width and loop vertically
        int bgHeight = WINDOW_HEIGHT;
        int bgWidth = WINDOW_WIDTH;
        
        // Draw two copies for seamless vertical scrolling
        int y1 = (int)backgroundOffsetY;
        int y2 = (int)(backgroundOffsetY - bgHeight);
        
        g2d.drawImage(backgroundSheet, 0, y1, bgWidth, bgHeight, null);
        g2d.drawImage(backgroundSheet, 0, y2, bgWidth, bgHeight, null);
    }
    
    private void drawLives(Graphics2D g2d) {
        if (uiSheet == null) return;
        
        int heartSize = 32; // Double size from 16 to 32
        int startX = 10; // Top-left corner
        int startY = 10;
        
        // Calculate how many hearts to show
        int fullHearts = lives / 2;  // Number of red hearts
        int hasPartialHeart = lives % 2; // 1 if odd number of lives, 0 if even
        int totalHeartsToShow = fullHearts + hasPartialHeart;
        
        // Draw only the hearts that should be visible
        for (int i = 0; i < totalHeartsToShow; i++) {
            try {
                BufferedImage heart;
                if (i < fullHearts) {
                    // Full heart at 0,80
                    heart = uiSheet.getSubimage(0, 80, 16, 16);
                } else {
                    // Gray heart at 16,80 (for the partial heart)
                    heart = uiSheet.getSubimage(16, 80, 16, 16);
                }
                g2d.drawImage(heart, startX + (i * 36), startY, heartSize, heartSize, null);
            } catch (Exception e) {
                // Fallback - draw colored rectangles
                g2d.setColor(i < fullHearts ? Color.RED : Color.DARK_GRAY);
                g2d.fillRect(startX + (i * 36), startY, heartSize, heartSize);
            }
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        // Restart from game over or game won
        if ((gameOver || gameWon) && key == KeyEvent.VK_R) {
            restartGame();
            return;
        }
        
        player.keyPressed(e);
    }
    
    private void restartGame() {
        // Reset all game state
        rocks.clear();
        bullets.clear();
        enemies.clear();
        enemyBullets.clear();
        explosions.clear();
        bossBullets.clear();
        subEnemies.clear();
        healthPowerUps.clear();
        boss = null;
        bossDefeated = false;
        gameWon = false;
        gameOver = false;
        gameTime = 0;
        killCount = 0;
        lives = 10;
        invincibilityTimer = 0;
        spawnTimer = 0;
        enemySpawnTimer = 0;
        bossSpawnDelay = 0;
        gameState = GameState.PLAYING;
        
        // Reset player position
        player.x = WINDOW_WIDTH / 2 - 32;
        player.y = WINDOW_HEIGHT - 100;
        
        // Make sure theme music is playing
        switchToThemeMusic();
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        player.keyReleased(e);
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Space Game");
        SpaceGame game = new SpaceGame();
        
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
