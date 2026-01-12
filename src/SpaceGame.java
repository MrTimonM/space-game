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
    private static final int TILE_SIZE = 16;
    
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
    private boolean gameOver;
    private boolean gameWon;
    private int gameTime;
    private boolean bossDefeated;
    private int bossSpawnDelay;
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
                    break;
                }
            }
        }
        
        // Check collisions between player bullets and boss
        if (boss != null) {
            for (int i = bullets.size() - 1; i >= 0; i--) {
                if (i >= bullets.size()) continue;
                Bullet bullet = bullets.get(i);
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
        player.keyPressed(e);
        
        // Restart game on R key
        if (e.getKeyCode() == KeyEvent.VK_R && (gameOver || gameWon)) {
            restartGame();
        }
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

// Player class
class Player {
    int x, y;
    int width = 96;   // 4 tiles * 16 * 1.5
    int height = 72;  // 3 tiles * 16 * 1.5
    int speed = 5;
    boolean left, right, up, down;
    BufferedImage spriteSheet;
    BufferedImage exhaustSheet;
    ArrayList<PlayerPart> parts;
    ArrayList<ExhaustPart> exhaustParts;
    
    public Player(int x, int y, BufferedImage sheet, BufferedImage exhaustSheet) {
        this.x = x;
        this.y = y;
        this.spriteSheet = sheet;
        this.exhaustSheet = exhaustSheet;
        this.parts = new ArrayList<>();
        this.exhaustParts = new ArrayList<>();
        createPlayerParts();
        createExhaustParts();
    }
    
    private void createExhaustParts() {
        // Main left exhaust (connects directly to bottom of plane at y=48)
        exhaustParts.add(new ExhaustPart(16, 48, 48, 96, 16, 16));   // left upper
        exhaustParts.add(new ExhaustPart(16, 64, 48, 112, 16, 16));  // left down
        
        // Main right exhaust (connects directly to bottom of plane at y=48)
        exhaustParts.add(new ExhaustPart(32, 48, 64, 96, 16, 16));   // right upper
        exhaustParts.add(new ExhaustPart(32, 64, 64, 112, 16, 16));  // right down
    }
    
    private void createPlayerParts() {
        // Blue plane - 4x3 grid (12 parts)
        // Row 1
        parts.add(new PlayerPart(0, 0, 0, 16, 16, 16));     // 1
        parts.add(new PlayerPart(16, 0, 16, 16, 16, 16));   // 2
        parts.add(new PlayerPart(32, 0, 32, 16, 16, 16));   // 3
        parts.add(new PlayerPart(48, 0, 48, 16, 16, 16));   // 4
        
        // Row 2
        parts.add(new PlayerPart(0, 16, 0, 32, 16, 16));    // 5
        parts.add(new PlayerPart(16, 16, 16, 32, 16, 16));  // 6
        parts.add(new PlayerPart(32, 16, 32, 32, 16, 16));  // 7
        parts.add(new PlayerPart(48, 16, 48, 32, 16, 16));  // 8
        
        // Row 3
        parts.add(new PlayerPart(0, 32, 0, 48, 16, 16));    // 9
        parts.add(new PlayerPart(16, 32, 16, 48, 16, 16));  // 10
        parts.add(new PlayerPart(32, 32, 32, 48, 16, 16));  // 11
        parts.add(new PlayerPart(48, 32, 48, 48, 16, 16));  // 12
    }
    
    public void update() {
        if (left) x -= speed;
        if (right) x += speed;
        if (up) y -= speed;
        if (down) y += speed;
        
        // Keep player in bounds
        if (x < 0) x = 0;
        if (x > 800 - width) x = 800 - width;
        if (y < 0) y = 0;
        if (y > 600 - height) y = 600 - height;
    }
    
    public void draw(Graphics2D g) {
        // Draw exhaust only when moving (behind the ship)
        if (exhaustSheet != null && (left || right || up || down)) {
            for (ExhaustPart exhaust : exhaustParts) {
                try {
                    BufferedImage sprite = exhaustSheet.getSubimage(
                        exhaust.srcX, exhaust.srcY, exhaust.srcW, exhaust.srcH
                    );
                    int scaledW = (int)(exhaust.srcW * 1.5);
                    int scaledH = (int)(exhaust.srcH * 1.5);
                    int scaledX = (int)(exhaust.offsetX * 1.5);
                    int scaledY = (int)(exhaust.offsetY * 1.5);
                    g.drawImage(sprite, x + scaledX, y + scaledY, scaledW, scaledH, null);
                } catch (Exception e) {
                    // Skip if sprite extraction fails
                }
            }
        }
        
        // Draw ship
        if (spriteSheet == null) {
            // Fallback drawing
            g.setColor(Color.CYAN);
            g.fillRect(x, y, width, height);
            g.setColor(Color.WHITE);
            g.drawRect(x, y, width, height);
            return;
        }
        
        for (PlayerPart part : parts) {
            try {
                BufferedImage sprite = spriteSheet.getSubimage(
                    part.srcX, part.srcY, part.srcW, part.srcH
                );
                int scaledW = (int)(part.srcW * 1.5);
                int scaledH = (int)(part.srcH * 1.5);
                int scaledX = (int)(part.offsetX * 1.5);
                int scaledY = (int)(part.offsetY * 1.5);
                g.drawImage(sprite, x + scaledX, y + scaledY, scaledW, scaledH, null);
            } catch (Exception e) {
                // Fallback if sprite extraction fails
                g.setColor(Color.CYAN);
                int scaledW = (int)(part.srcW * 1.5);
                int scaledH = (int)(part.srcH * 1.5);
                int scaledX = (int)(part.offsetX * 1.5);
                int scaledY = (int)(part.offsetY * 1.5);
                g.fillRect(x + scaledX, y + scaledY, scaledW, scaledH);
            }
        }
    }
    
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) left = true;
        if (key == KeyEvent.VK_RIGHT) right = true;
        if (key == KeyEvent.VK_UP) up = true;
        if (key == KeyEvent.VK_DOWN) down = true;
    }
    
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) left = false;
        if (key == KeyEvent.VK_RIGHT) right = false;
        if (key == KeyEvent.VK_UP) up = false;
        if (key == KeyEvent.VK_DOWN) down = false;
    }
}

// Rock class
class Rock {
    int x, y;
    int level; // 1 = small, 2 = medium, 3 = big
    int speed;
    int width, height;
    int health;
    BufferedImage asteroidSheet;
    ArrayList<RockPart> parts;
    
    public Rock(int x, int y, int level, BufferedImage sheet) {
        this.x = x;
        this.y = y;
        this.level = level;
        this.asteroidSheet = sheet;
        this.speed = 1 + new Random().nextInt(2); // Slower: 1-2 instead of 2-4
        this.parts = new ArrayList<>();
        
        // Set health based on rock size
        if (level == 1) {
            this.health = 3; // Small rock
        } else if (level == 2) {
            this.health = 4; // Medium rock
        } else {
            this.health = 5; // Big rock
        }
        
        createRockParts();
        calculateDimensions();
    }
    
    private void createRockParts() {
        if (level == 1) {
            // Small rock - single 16x16 sprite
            parts.add(new RockPart(0, 0, 32, 16, 16, 16));
            
        } else if (level == 2) {
            // Medium rock - 2x2 grid (4 parts)
            parts.add(new RockPart(0, 0, 16, 32, 16, 16));     // left up
            parts.add(new RockPart(16, 0, 32, 32, 16, 16));    // right up
            parts.add(new RockPart(0, 16, 16, 48, 16, 16));    // left down
            parts.add(new RockPart(16, 16, 32, 48, 16, 16));   // right down
            
        } else if (level == 3) {
            // Big rock - 3x3 grid (9 parts)
            // Top row
            parts.add(new RockPart(0, 0, 0, 64, 16, 16));      // x
            parts.add(new RockPart(16, 0, 16, 64, 16, 16));    // y
            parts.add(new RockPart(32, 0, 32, 64, 16, 16));    // z
            // Middle row
            parts.add(new RockPart(0, 16, 0, 80, 16, 16));     // a
            parts.add(new RockPart(16, 16, 16, 80, 16, 16));   // b
            parts.add(new RockPart(32, 16, 32, 80, 16, 16));   // c
            // Bottom row
            parts.add(new RockPart(0, 32, 0, 96, 16, 16));     // q
            parts.add(new RockPart(16, 32, 16, 96, 16, 16));   // w
            parts.add(new RockPart(32, 32, 32, 96, 16, 16));   // e
        }
    }
    
    private void calculateDimensions() {
        if (level == 1) {
            width = 16 * SpaceGame.ROCK_SCALE;
            height = 16 * SpaceGame.ROCK_SCALE;
        } else if (level == 2) {
            width = 32 * SpaceGame.ROCK_SCALE;
            height = 32 * SpaceGame.ROCK_SCALE;
        } else {
            width = 48 * SpaceGame.ROCK_SCALE;
            height = 48 * SpaceGame.ROCK_SCALE;
        }
    }
    
    public void update() {
        y += speed;
    }
    
    public void draw(Graphics2D g) {
        if (asteroidSheet == null) {
            // Fallback drawing
            g.setColor(Color.GRAY);
            g.fillRect(x, y, width, height);
            return;
        }
        
        for (RockPart part : parts) {
            try {
                BufferedImage sprite = asteroidSheet.getSubimage(
                    part.srcX, part.srcY, part.srcW, part.srcH
                );
                int scaledW = part.srcW * SpaceGame.ROCK_SCALE;
                int scaledH = part.srcH * SpaceGame.ROCK_SCALE;
                int scaledX = part.offsetX * SpaceGame.ROCK_SCALE;
                int scaledY = part.offsetY * SpaceGame.ROCK_SCALE;
                g.drawImage(sprite, x + scaledX, y + scaledY, scaledW, scaledH, null);
            } catch (Exception e) {
                // Fallback if sprite extraction fails
                g.setColor(Color.GRAY);
                int scaledW = part.srcW * SpaceGame.ROCK_SCALE;
                int scaledH = part.srcH * SpaceGame.ROCK_SCALE;
                int scaledX = part.offsetX * SpaceGame.ROCK_SCALE;
                int scaledY = part.offsetY * SpaceGame.ROCK_SCALE;
                g.fillRect(x + scaledX, y + scaledY, scaledW, scaledH);
            }
        }
    }
}

// Helper class to store rock part information
class RockPart {
    int offsetX, offsetY; // Position relative to rock's x, y
    int srcX, srcY;       // Source position in sprite sheet
    int srcW, srcH;       // Source width and height
    
    public RockPart(int offsetX, int offsetY, int srcX, int srcY, int srcW, int srcH) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.srcX = srcX;
        this.srcY = srcY;
        this.srcW = srcW;
        this.srcH = srcH;
    }
}

// Helper class to store player part information
class PlayerPart {
    int offsetX, offsetY; // Position relative to player's x, y
    int srcX, srcY;       // Source position in sprite sheet
    int srcW, srcH;       // Source width and height
    
    public PlayerPart(int offsetX, int offsetY, int srcX, int srcY, int srcW, int srcH) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.srcX = srcX;
        this.srcY = srcY;
        this.srcW = srcW;
        this.srcH = srcH;
    }
}

// Helper class to store exhaust part information
class ExhaustPart {
    int offsetX, offsetY; // Position relative to player's x, y
    int srcX, srcY;       // Source position in sprite sheet
    int srcW, srcH;       // Source width and height
    
    public ExhaustPart(int offsetX, int offsetY, int srcX, int srcY, int srcW, int srcH) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.srcX = srcX;
        this.srcY = srcY;
        this.srcW = srcW;
        this.srcH = srcH;
    }
}

// Bullet class
class Bullet {
    int x, y;
    int speed = 10;
    BufferedImage bulletSheet;
    
    public Bullet(int x, int y, BufferedImage sheet) {
        this.x = x;
        this.y = y;
        this.bulletSheet = sheet;
    }
    
    public void update() {
        y -= speed; // Move upward
    }
    
    public void draw(Graphics2D g) {
        if (bulletSheet == null) {
            // Fallback drawing
            g.setColor(Color.YELLOW);
            g.fillRect(x, y, 8, 12);
            return;
        }
        
        try {
            // Get bullet sprite at 176,112, 16x16
            BufferedImage sprite = bulletSheet.getSubimage(176, 112, 16, 16);
            g.drawImage(sprite, x, y, 16, 16, null);
        } catch (Exception e) {
            // Fallback if sprite extraction fails
            g.setColor(Color.YELLOW);
            g.fillRect(x, y, 8, 12);
        }
    }
}

// Enemy class
class Enemy {
    int x, y;
    int width = 64;  // 2x2 grid * 16 * 2 scale
    int height = 64;
    int speed;
    int movePattern;
    int moveCounter = 0;
    BufferedImage enemySheet;
    ArrayList<EnemyPart> parts;
    
    public Enemy(int x, int y, BufferedImage sheet, int movePattern) {
        this.x = x;
        this.y = y;
        this.enemySheet = sheet;
        this.movePattern = movePattern;
        this.speed = 1 + new Random().nextInt(2);
        this.parts = new ArrayList<>();
        createEnemyParts();
    }
    
    private void createEnemyParts() {
        // Enemy 2x2 grid (scaled 2x)
        parts.add(new EnemyPart(0, 0, 192, 112, 16, 16));      // 1 top-left
        parts.add(new EnemyPart(32, 0, 208, 112, 16, 16));     // 2 top-right
        parts.add(new EnemyPart(0, 32, 192, 128, 16, 16));     // 3 bottom-left
        parts.add(new EnemyPart(32, 32, 208, 128, 16, 16));    // 4 bottom-right
    }
    
    public void update() {
        moveCounter++;
        
        // Different movement patterns
        if (movePattern == 1) {
            // Straight down
            y += speed;
        } else if (movePattern == 2) {
            // Zig-zag
            y += speed;
            x += (int)(Math.sin(moveCounter * 0.1) * 3);
        }
    }
    
    public void shoot(ArrayList<EnemyBullet> bullets, BufferedImage bulletSheet) {
        // Shoot from center of enemy
        bullets.add(new EnemyBullet(x + width / 2 - 8, y + height, bulletSheet));
    }
    
    public void draw(Graphics2D g) {
        if (enemySheet == null) {
            // Fallback drawing
            g.setColor(Color.RED);
            g.fillRect(x, y, width, height);
            return;
        }
        
        for (EnemyPart part : parts) {
            try {
                BufferedImage sprite = enemySheet.getSubimage(
                    part.srcX, part.srcY, part.srcW, part.srcH
                );
                int scaledW = part.srcW * 2;
                int scaledH = part.srcH * 2;
                g.drawImage(sprite, x + part.offsetX, y + part.offsetY, scaledW, scaledH, null);
            } catch (Exception e) {
                // Fallback if sprite extraction fails
                g.setColor(Color.RED);
                g.fillRect(x + part.offsetX, y + part.offsetY, part.srcW * 2, part.srcH * 2);
            }
        }
    }
}

// Enemy bullet class
class EnemyBullet {
    int x, y;
    int speed = 5;
    BufferedImage bulletSheet;
    
    public EnemyBullet(int x, int y, BufferedImage sheet) {
        this.x = x;
        this.y = y;
        this.bulletSheet = sheet;
    }
    
    public void update() {
        y += speed; // Move downward
    }
    
    public void draw(Graphics2D g) {
        if (bulletSheet == null) {
            // Fallback drawing
            g.setColor(Color.RED);
            g.fillRect(x, y, 8, 12);
            return;
        }
        
        try {
            // Get enemy bullet sprite at 176,144, 16x16
            BufferedImage sprite = bulletSheet.getSubimage(176, 144, 16, 16);
            g.drawImage(sprite, x, y, 16, 16, null);
        } catch (Exception e) {
            // Fallback if sprite extraction fails
            g.setColor(Color.RED);
            g.fillRect(x, y, 8, 12);
        }
    }
}

// Helper class to store enemy part information
class EnemyPart {
    int offsetX, offsetY;
    int srcX, srcY;
    int srcW, srcH;
    
    public EnemyPart(int offsetX, int offsetY, int srcX, int srcY, int srcW, int srcH) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.srcX = srcX;
        this.srcY = srcY;
        this.srcW = srcW;
        this.srcH = srcH;
    }
}

// Explosion class
class Explosion {
    int x, y;
    int level;
    int frame;
    int maxFrames;
    BufferedImage explosionSheet;
    ArrayList<ExplosionPart> parts;
    
    public Explosion(int x, int y, int level, BufferedImage sheet) {
        this.x = x;
        this.y = y;
        this.level = level;
        this.explosionSheet = sheet;
        this.frame = 0;
        this.maxFrames = 10; // Animation lasts 10 frames
        this.parts = new ArrayList<>();
        createExplosionParts();
    }
    
    protected void createExplosionParts() {
        if (level == 1) {
            // Small rock explosion: 16,48, 16x16
            parts.add(new ExplosionPart(0, 0, 16, 48, 16, 16));
        } else if (level == 2) {
            // Medium rock explosion: 48,48, 16x16
            parts.add(new ExplosionPart(0, 0, 48, 48, 16, 16));
        } else if (level == 3) {
            // Big rock explosion: 4x4 grid
            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 4; col++) {
                    int srcX = 64 + (col * 16);
                    int srcY = 16 + (row * 16);
                    int offsetX = col * 16 * SpaceGame.ROCK_SCALE;
                    int offsetY = row * 16 * SpaceGame.ROCK_SCALE;
                    parts.add(new ExplosionPart(offsetX, offsetY, srcX, srcY, 16, 16));
                }
            }
        }
    }
    
    public void update() {
        frame++;
    }
    
    public boolean isFinished() {
        return frame >= maxFrames;
    }
    
    public void draw(Graphics2D g) {
        if (explosionSheet == null) return;
        
        // Fade effect based on frame
        float alpha = 1.0f - (frame / (float)maxFrames);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        
        for (ExplosionPart part : parts) {
            try {
                BufferedImage sprite = explosionSheet.getSubimage(
                    part.srcX, part.srcY, part.srcW, part.srcH
                );
                
                if (level == 1) {
                    g.drawImage(sprite, x + part.offsetX, y + part.offsetY, 16 * SpaceGame.ROCK_SCALE, 16 * SpaceGame.ROCK_SCALE, null);
                } else if (level == 2) {
                    g.drawImage(sprite, x + part.offsetX, y + part.offsetY, 32 * SpaceGame.ROCK_SCALE, 32 * SpaceGame.ROCK_SCALE, null);
                } else {
                    g.drawImage(sprite, x + part.offsetX, y + part.offsetY, 16 * SpaceGame.ROCK_SCALE, 16 * SpaceGame.ROCK_SCALE, null);
                }
            } catch (Exception e) {
                // Skip if sprite extraction fails
            }
        }
        
        // Reset composite
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
}

// Helper class for explosion parts
class ExplosionPart {
    int offsetX, offsetY;
    int srcX, srcY;
    int srcW, srcH;
    
    public ExplosionPart(int offsetX, int offsetY, int srcX, int srcY, int srcW, int srcH) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.srcX = srcX;
        this.srcY = srcY;
        this.srcW = srcW;
        this.srcH = srcH;
    }
}

// Boss class
class Boss {
    int x, y;
    int width = 144;  // 3 tiles * 16 * 3 scale
    int height = 192; // 4 tiles * 16 * 3 scale
    int health = 150;
    int speed = 2;
    int moveCounter = 0;
    BufferedImage enemySheet;
    BufferedImage allSheet;
    ArrayList<BossPart> parts;
    Random random;
    
    public Boss(int x, int y, BufferedImage sheet) {
        this.x = x;
        this.y = y;
        this.enemySheet = sheet;
        this.parts = new ArrayList<>();
        this.random = new Random();
        createBossParts();
    }
    
    public void setAllSheet(BufferedImage allSheet) {
        this.allSheet = allSheet;
    }
    
    private void createBossParts() {
        // Boss 3x4 grid (12 parts) - scaled 3x
        // Row 1
        parts.add(new BossPart(0, 0, 32, 0, 16, 16));      // 1
        parts.add(new BossPart(48, 0, 48, 0, 16, 16));     // 2
        parts.add(new BossPart(96, 0, 64, 0, 16, 16));     // 3
        
        // Row 2
        parts.add(new BossPart(0, 48, 32, 16, 16, 16));    // 4
        parts.add(new BossPart(48, 48, 48, 16, 16, 16));   // 5
        parts.add(new BossPart(96, 48, 64, 16, 16, 16));   // 6
        
        // Row 3
        parts.add(new BossPart(0, 96, 32, 32, 16, 16));    // 7
        parts.add(new BossPart(48, 96, 48, 32, 16, 16));   // 8
        parts.add(new BossPart(96, 96, 64, 32, 16, 16));   // 9
        
        // Row 4 (bullet firing positions)
        parts.add(new BossPart(0, 144, 32, 48, 16, 16));   // 10
        parts.add(new BossPart(48, 144, 48, 48, 16, 16));  // 11
        parts.add(new BossPart(96, 144, 64, 48, 16, 16));  // 12
    }
    
    public void update() {
        moveCounter++;
        
        // Move down slowly until reaching top quarter of screen
        if (y < 50) {
            y += speed;
        } else {
            // Horizontal movement pattern
            x += (int)(Math.sin(moveCounter * 0.02) * 3);
            
            // Keep boss in bounds
            if (x < 0) x = 0;
            if (x > 800 - width) x = 800 - width;
        }
    }
    
    public void shoot(ArrayList<BossBullet> bullets, BufferedImage bulletSheet) {
        // Fire from positions 10, 11, 12 (bottom row)
        // Position 10 - left
        bullets.add(new BossBullet(x + 24, y + 144 + 48, bulletSheet));
        // Position 11 - center
        bullets.add(new BossBullet(x + 72, y + 144 + 48, bulletSheet));
        // Position 12 - right
        bullets.add(new BossBullet(x + 120, y + 144 + 48, bulletSheet));
    }
    
    public void throwSubEnemy(ArrayList<SubEnemy> subEnemies, BufferedImage enemySheet) {
        // Spawn sub-enemy from boss position
        subEnemies.add(new SubEnemy(x + width / 2 - 32, y + height, enemySheet));
    }
    
    public void draw(Graphics2D g) {
        if (enemySheet == null) {
            // Fallback drawing
            g.setColor(Color.MAGENTA);
            g.fillRect(x, y, width, height);
            return;
        }
        
        for (BossPart part : parts) {
            try {
                BufferedImage sprite = enemySheet.getSubimage(
                    part.srcX, part.srcY, part.srcW, part.srcH
                );
                int scaledW = part.srcW * 3;
                int scaledH = part.srcH * 3;
                g.drawImage(sprite, x + part.offsetX, y + part.offsetY, scaledW, scaledH, null);
            } catch (Exception e) {
                // Fallback if sprite extraction fails
                g.setColor(Color.MAGENTA);
                g.fillRect(x + part.offsetX, y + part.offsetY, part.srcW * 3, part.srcH * 3);
            }
        }
        
        // Draw sprite-based health bar
        drawHealthBar(g);
    }
    
    private void drawHealthBar(Graphics2D g) {
        if (allSheet == null) {
            // Fallback to simple health bar
            g.setColor(Color.RED);
            g.fillRect(x, y - 20, width, 10);
            g.setColor(Color.GREEN);
            int healthWidth = (int)((health / 150.0) * width);
            g.fillRect(x, y - 20, healthWidth, 10);
            return;
        }
        
        // Determine which health bar state to show (1-6)
        // State 1: 150-126 health (full)
        // State 2: 125-101 health
        // State 3: 100-76 health
        // State 4: 75-51 health
        // State 5: 50-26 health
        // State 6: 25-1 health (almost dead)
        int state;
        if (health > 125) {
            state = 1;
        } else if (health > 100) {
            state = 2;
        } else if (health > 75) {
            state = 3;
        } else if (health > 50) {
            state = 4;
        } else if (health > 25) {
            state = 5;
        } else {
            state = 6;
        }
        
        // Get the starting X position for the health bar sprites
        int srcX;
        switch (state) {
            case 1: srcX = 624; break; // Full health
            case 2: srcX = 672; break;
            case 3: srcX = 720; break;
            case 4: srcX = 768; break;
            case 5: srcX = 816; break;
            case 6: srcX = 864; break;
            default: srcX = 624; break;
        }
        
        try {
            // Draw 3-part health bar centered above the boss
            int barY = y - 40;
            int centerX = x + width / 2;
            int barWidth = 48 * 2; // 3 sprites * 16 * 2 scale = 96 total width
            int startX = centerX - barWidth / 2;
            
            // Left part
            BufferedImage leftSprite = allSheet.getSubimage(srcX, 0, 16, 16);
            g.drawImage(leftSprite, startX, barY, 32, 32, null);
            
            // Middle part
            BufferedImage midSprite = allSheet.getSubimage(srcX + 16, 0, 16, 16);
            g.drawImage(midSprite, startX + 32, barY, 32, 32, null);
            
            // Right part (note: state 4 middle has typo in original spec, using +32 offset)
            int rightSrcX = (state == 4) ? srcX + 32 : srcX + 32;
            BufferedImage rightSprite = allSheet.getSubimage(rightSrcX, 0, 16, 16);
            g.drawImage(rightSprite, startX + 64, barY, 32, 32, null);
        } catch (Exception e) {
            // Fallback if sprite extraction fails
            g.setColor(Color.RED);
            g.fillRect(x, y - 20, width, 10);
            g.setColor(Color.GREEN);
            int healthWidth = (int)((health / 150.0) * width);
            g.fillRect(x, y - 20, healthWidth, 10);
        }
    }
}

// Helper class for boss parts
class BossPart {
    int offsetX, offsetY;
    int srcX, srcY;
    int srcW, srcH;
    
    public BossPart(int offsetX, int offsetY, int srcX, int srcY, int srcW, int srcH) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.srcX = srcX;
        this.srcY = srcY;
        this.srcW = srcW;
        this.srcH = srcH;
    }
}

// Boss bullet class
class BossBullet {
    int x, y;
    int speed = 4;
    BufferedImage bulletSheet;
    int bulletType;
    Random random;
    
    public BossBullet(int x, int y, BufferedImage sheet) {
        this.x = x;
        this.y = y;
        this.bulletSheet = sheet;
        this.random = new Random();
        this.bulletType = random.nextInt(6); // 6 different bullet types
    }
    
    public void update() {
        y += speed; // Move downward
    }
    
    public void draw(Graphics2D g) {
        if (bulletSheet == null) {
            // Fallback drawing
            g.setColor(Color.ORANGE);
            g.fillRect(x, y, 16, 16);
            return;
        }
        
        try {
            BufferedImage sprite;
            // Select bullet sprite based on type
            switch (bulletType) {
                case 0:
                    sprite = bulletSheet.getSubimage(176, 16, 16, 16);
                    break;
                case 1:
                    sprite = bulletSheet.getSubimage(176, 48, 16, 16);
                    break;
                case 2:
                    sprite = bulletSheet.getSubimage(176, 176, 16, 16);
                    break;
                case 3:
                    sprite = bulletSheet.getSubimage(48, 16, 16, 16);
                    break;
                case 4:
                    sprite = bulletSheet.getSubimage(48, 48, 16, 16);
                    break;
                case 5:
                    sprite = bulletSheet.getSubimage(48, 176, 16, 16);
                    break;
                default:
                    sprite = bulletSheet.getSubimage(176, 16, 16, 16);
            }
            g.drawImage(sprite, x, y, 16, 16, null);
        } catch (Exception e) {
            // Fallback if sprite extraction fails
            g.setColor(Color.ORANGE);
            g.fillRect(x, y, 16, 16);
        }
    }
}

// Sub-enemy class
class SubEnemy {
    int x, y;
    int width = 64;  // 2 tiles * 16 * 2 scale
    int height = 32; // 1 tile * 16 * 2 scale
    int speed = 3;
    BufferedImage enemySheet;
    ArrayList<SubEnemyPart> parts;
    
    public SubEnemy(int x, int y, BufferedImage sheet) {
        this.x = x;
        this.y = y;
        this.enemySheet = sheet;
        this.parts = new ArrayList<>();
        createSubEnemyParts();
    }
    
    private void createSubEnemyParts() {
        // Sub-enemy 2x1 grid (2 parts) - scaled 2x
        parts.add(new SubEnemyPart(0, 0, 208, 224, 16, 16));   // 1
        parts.add(new SubEnemyPart(32, 0, 224, 224, 16, 16));  // 2
    }
    
    public void update() {
        y += speed; // Move downward
    }
    
    public void draw(Graphics2D g) {
        if (enemySheet == null) {
            // Fallback drawing
            g.setColor(Color.YELLOW);
            g.fillRect(x, y, width, height);
            return;
        }
        
        for (SubEnemyPart part : parts) {
            try {
                BufferedImage sprite = enemySheet.getSubimage(
                    part.srcX, part.srcY, part.srcW, part.srcH
                );
                int scaledW = part.srcW * 2;
                int scaledH = part.srcH * 2;
                g.drawImage(sprite, x + part.offsetX, y + part.offsetY, scaledW, scaledH, null);
            } catch (Exception e) {
                // Fallback if sprite extraction fails
                g.setColor(Color.YELLOW);
                g.fillRect(x + part.offsetX, y + part.offsetY, part.srcW * 2, part.srcH * 2);
            }
        }
    }
}

// Helper class for sub-enemy parts
class SubEnemyPart {
    int offsetX, offsetY;
    int srcX, srcY;
    int srcW, srcH;
    
    public SubEnemyPart(int offsetX, int offsetY, int srcX, int srcY, int srcW, int srcH) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.srcX = srcX;
        this.srcY = srcY;
        this.srcW = srcW;
        this.srcH = srcH;
    }
}

// Boss explosion class - 4x4 grid scaled 4 times
class BossExplosion extends Explosion {
    
    public BossExplosion(int x, int y, BufferedImage sheet) {
        super(x, y, 4, sheet); // Use level 4 for boss explosion
        this.maxFrames = 20; // Longer animation
    }
    
    @Override
    protected void createExplosionParts() {
        // Boss explosion: 4x4 grid scaled 4x
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                int srcX = 64 + (col * 16);
                int srcY = 16 + (row * 16);
                int offsetX = col * 16 * 4;
                int offsetY = row * 16 * 4;
                parts.add(new ExplosionPart(offsetX, offsetY, srcX, srcY, 16, 16));
            }
        }
    }
    
    @Override
    public void draw(Graphics2D g) {
        if (explosionSheet == null) return;
        
        // Fade effect based on frame
        float alpha = 1.0f - (frame / (float)maxFrames);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        
        for (ExplosionPart part : parts) {
            try {
                BufferedImage sprite = explosionSheet.getSubimage(
                    part.srcX, part.srcY, part.srcW, part.srcH
                );
                // Scale 4x for boss explosion
                g.drawImage(sprite, x + part.offsetX, y + part.offsetY, 16 * 4, 16 * 4, null);
            } catch (Exception e) {
                // Skip if sprite extraction fails
            }
        }
        
        // Reset composite
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
}

// Health Power-Up class
class HealthPowerUp {
    int x, y;
    int speed = 2;
    BufferedImage allSheet;
    
    public HealthPowerUp(int x, int y, BufferedImage sheet) {
        this.x = x;
        this.y = y;
        this.allSheet = sheet;
    }
    
    public void update() {
        y += speed; // Move downward slowly
    }
    
    public void draw(Graphics2D g) {
        if (allSheet == null) {
            // Fallback drawing
            g.setColor(Color.GREEN);
            g.fillRect(x, y, 32, 32);
            g.setColor(Color.WHITE);
            g.drawString("+", x + 12, y + 22);
            return;
        }
        
        try {
            // Health power-up sprite at 64,112, 16x16
            BufferedImage sprite = allSheet.getSubimage(64, 112, 16, 16);
            // Scale 2x for visibility
            g.drawImage(sprite, x, y, 32, 32, null);
        } catch (Exception e) {
            // Fallback if sprite extraction fails
            g.setColor(Color.GREEN);
            g.fillRect(x, y, 32, 32);
            g.setColor(Color.WHITE);
            g.drawString("+", x + 12, y + 22);
        }
    }
}
