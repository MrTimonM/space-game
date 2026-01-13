import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

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
