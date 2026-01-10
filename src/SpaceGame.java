import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
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
    private Random random;
    private BufferedImage asteroidSheet;
    private BufferedImage backgroundSheet;
    private BufferedImage playerSheet;
    private BufferedImage exhaustSheet;
    private BufferedImage bulletSheet;
    private double backgroundOffsetY;
    private int spawnTimer;
    public static final int ROCK_SCALE = 2;  // Double the rock size
    
    public SpaceGame() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        
        random = new Random();
        rocks = new ArrayList<>();
        bullets = new ArrayList<>();
        backgroundOffsetY = 0;
        spawnTimer = 0;
        
        loadImages();
        
        player = new Player(WINDOW_WIDTH / 2 - 32, WINDOW_HEIGHT - 100, playerSheet, exhaustSheet);
        
        gameTimer = new Timer(16, this); // ~60 FPS
        gameTimer.start();
    }
    
    private void loadImages() {
        try {
            asteroidSheet = ImageIO.read(new File("../Assets/Asteroids-0001.png"));
            backgroundSheet = ImageIO.read(new File("../Assets/Background_Full-0001.png"));
            playerSheet = ImageIO.read(new File("../Assets/SpaceShips_Player-0001.png"));
            exhaustSheet = ImageIO.read(new File("../Assets/Exhaust-0001.png"));
            bulletSheet = ImageIO.read(new File("../Assets/Bullets-0001.png"));
        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }
    
    private void update() {
        // Update player
        player.update();
        
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
        
        // Spawn new rocks
        spawnTimer++;
        if (spawnTimer > 40) { // Spawn every ~0.6 seconds
            spawnTimer = 0;
            spawnRock();
        }
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
        
        // Draw bullets
        for (Bullet bullet : bullets) {
            bullet.draw(g2d);
        }
        
        // Draw player
        player.draw(g2d);
        
        // Draw UI
        g2d.setColor(Color.WHITE);
        g2d.drawString("Rocks: " + rocks.size(), 10, 20);
        g2d.drawString("Use Arrow Keys to Move", 10, 40);
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
    
    @Override
    public void keyPressed(KeyEvent e) {
        player.keyPressed(e);
        
        // Shoot bullets on space bar
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            shootBullets();
        }
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
    BufferedImage asteroidSheet;
    ArrayList<RockPart> parts;
    
    public Rock(int x, int y, int level, BufferedImage sheet) {
        this.x = x;
        this.y = y;
        this.level = level;
        this.asteroidSheet = sheet;
        this.speed = 2 + new Random().nextInt(3);
        this.parts = new ArrayList<>();
        
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
