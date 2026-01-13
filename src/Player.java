import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

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
