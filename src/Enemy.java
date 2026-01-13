import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

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
