import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

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
