import java.awt.*;
import java.awt.image.BufferedImage;

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
