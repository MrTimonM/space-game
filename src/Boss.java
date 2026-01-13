import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

// Boss class
class Boss {
    int x, y;
    int width = 144;  // 3 tiles * 16 * 3 scale
    int height = 192; // 4 tiles * 16 * 3 scale
    int health = 120;
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
        int state;
        if (health > 100) {
            state = 1;
        } else if (health > 80) {
            state = 2;
        } else if (health > 60) {
            state = 3;
        } else if (health > 40) {
            state = 4;
        } else if (health > 20) {
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
            int barWidth = 48 * 2;
            int startX = centerX - barWidth / 2;
            
            // Left part
            BufferedImage leftSprite = allSheet.getSubimage(srcX, 0, 16, 16);
            g.drawImage(leftSprite, startX, barY, 32, 32, null);
            
            // Middle part
            BufferedImage midSprite = allSheet.getSubimage(srcX + 16, 0, 16, 16);
            g.drawImage(midSprite, startX + 32, barY, 32, 32, null);
            
            // Right part
            int rightSrcX = srcX + 32;
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
