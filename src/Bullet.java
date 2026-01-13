import java.awt.*;
import java.awt.image.BufferedImage;

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
