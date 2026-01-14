import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class MenuScreen extends JPanel implements KeyListener {
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    
    private enum MenuState {
        MAIN_MENU,
        CONTROLS,
        CREDITS,
        EXIT_CONFIRM
    }
    
    private MenuState currentState = MenuState.MAIN_MENU;
    private int selectedOption = 0;
    private String[] mainMenuOptions = {"PLAY GAME", "CONTROLS", "MUSIC VOLUME", "CREDITS", "EXIT"};
    private int exitConfirmSelection = 0; // 0 = No, 1 = Yes
    private BufferedImage backgroundImage;
    private Font customFont;
    private Font titleFont;
    private Font optionFont;
    private boolean gameStarted = false;
    private float musicVolume = 0.7f; // 70% volume
    private Clip musicClip;
    private GameWindow gameWindow;
    
    // Animation
    private float titleGlow = 0.0f;
    private float glowDirection = 0.02f;
    private Timer animationTimer;
    
    public MenuScreen(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        
        loadAssets();
        startAnimation();
        playMenuMusic();
    }
    
    private void loadAssets() {
        try {
            // Load background image
            backgroundImage = ImageIO.read(new File("../Assets/home.png"));
            
            // Load custom font
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, new File("../Assets/VT323-Regular.ttf"));
            titleFont = baseFont.deriveFont(Font.BOLD, 72f);
            optionFont = baseFont.deriveFont(Font.PLAIN, 32f);
            customFont = baseFont.deriveFont(Font.PLAIN, 24f);
            
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback fonts
            titleFont = new Font("Monospaced", Font.BOLD, 72);
            optionFont = new Font("Monospaced", Font.PLAIN, 32);
            customFont = new Font("Monospaced", Font.PLAIN, 24);
        }
    }
    
    private void playMenuMusic() {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("../Assets/theme.wav"));
            musicClip = AudioSystem.getClip();
            musicClip.open(audioInputStream);
            
            // Set volume
            FloatControl volumeControl = (FloatControl) musicClip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log(musicVolume) / Math.log(10.0) * 20.0);
            volumeControl.setValue(dB);
            
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            musicClip.start();
        } catch (Exception e) {
            System.out.println("Could not load menu music: " + e.getMessage());
        }
    }
    
    private void startAnimation() {
        animationTimer = new Timer(50, e -> {
            titleGlow += glowDirection;
            if (titleGlow >= 1.0f || titleGlow <= 0.0f) {
                glowDirection = -glowDirection;
            }
            repaint();
        });
        animationTimer.start();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        switch (currentState) {
            case MAIN_MENU:
                drawMainMenu(g2d);
                break;
            case CONTROLS:
                drawControls(g2d);
                break;
            case CREDITS:
                drawCredits(g2d);
                break;
            case EXIT_CONFIRM:
                drawExitConfirm(g2d);
                break;
        }
    }
    
    private void drawMainMenu(Graphics2D g2d) {
        // Draw background
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, null);
        } else {
            // Fallback gradient background
            GradientPaint gradient = new GradientPaint(0, 0, new Color(10, 10, 30), 
                                                        0, WINDOW_HEIGHT, new Color(30, 10, 50));
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        }
        
        // Semi-transparent overlay for better text visibility
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        // Draw title "VOID" with glow effect
        g2d.setFont(titleFont);
        String title = "V O I D";
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (WINDOW_WIDTH - fm.stringWidth(title)) / 2;
        int titleY = 150;
        
        // Glow effect
        int alphaValue = Math.min(255, Math.max(0, (int)(100 * titleGlow)));
        for (int i = 5; i > 0; i--) {
            int glowAlpha = Math.min(255, Math.max(0, (int)(50 * titleGlow / i)));
            g2d.setColor(new Color(138, 43, 226, glowAlpha));
            g2d.drawString(title, titleX - i, titleY - i);
            g2d.drawString(title, titleX + i, titleY + i);
        }
        
        // Main title
        g2d.setColor(new Color(200, 150, 255));
        g2d.drawString(title, titleX, titleY);
        
        // Subtitle
        g2d.setFont(customFont);
        String subtitle = "~ Journey Through the Cosmos ~";
        fm = g2d.getFontMetrics();
        int subtitleX = (WINDOW_WIDTH - fm.stringWidth(subtitle)) / 2;
        g2d.setColor(new Color(150, 150, 200));
        g2d.drawString(subtitle, subtitleX, titleY + 40);
        
        // Menu options
        g2d.setFont(optionFont);
        int startY = 280;
        int spacing = 60;
        
        for (int i = 0; i < mainMenuOptions.length; i++) {
            int y = startY + i * spacing;
            boolean isSelected = (i == selectedOption);
            
            String option = mainMenuOptions[i];
            fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(option);
            
            // For selected music volume, position text to accommodate volume bar
            int x;
            if (option.equals("MUSIC VOLUME") && isSelected) {
                int totalWidth = textWidth + 150;
                x = (WINDOW_WIDTH - totalWidth) / 2;
            } else {
                x = (WINDOW_WIDTH - textWidth) / 2;
            }
            
            if (isSelected) {
                // Selected option - draw background box
                int boxWidth = textWidth + 40;
                if (option.equals("MUSIC VOLUME")) {
                    boxWidth = textWidth + 190;
                }
                
                g2d.setColor(new Color(138, 43, 226, 100));
                g2d.fillRoundRect(x - 20, y - 35, boxWidth, 50, 10, 10);
                
                // Border
                g2d.setColor(new Color(200, 150, 255));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(x - 20, y - 35, boxWidth, 50, 10, 10);
                
                // Arrow indicator
                g2d.setColor(new Color(255, 200, 100));
                g2d.fillPolygon(new int[]{x - 40, x - 50, x - 40}, 
                               new int[]{y - 15, y - 10, y - 5}, 3);
                
                // Text
                g2d.setColor(new Color(255, 255, 255));
                g2d.drawString(option, x, y);
                
                // Only show volume bar when MUSIC VOLUME is selected
                if (option.equals("MUSIC VOLUME")) {
                    drawVolumeBar(g2d, x + textWidth + 20, y - 20);
                }
            } else {
                // Unselected option
                g2d.setColor(new Color(150, 150, 180));
                g2d.drawString(option, x, y);
            }
        }
        
        // Footer
        g2d.setFont(customFont);
        g2d.setColor(new Color(100, 100, 120));
        String footer = "(C) 2026 | Use UP/DOWN arrows to navigate | ENTER to select | ESC to go back";
        fm = g2d.getFontMetrics();
        int footerX = (WINDOW_WIDTH - fm.stringWidth(footer)) / 2;
        g2d.drawString(footer, footerX, WINDOW_HEIGHT - 30);
    }
    
    private void drawVolumeBar(Graphics2D g2d, int x, int y) {
        int barWidth = 120;
        int barHeight = 20;
        int filledWidth = (int)(barWidth * musicVolume);
        
        // Background
        g2d.setColor(new Color(50, 50, 70));
        g2d.fillRect(x, y, barWidth, barHeight);
        
        // Filled portion
        g2d.setColor(new Color(100, 200, 100));
        g2d.fillRect(x, y, filledWidth, barHeight);
        
        // Border
        g2d.setColor(new Color(150, 150, 180));
        g2d.drawRect(x, y, barWidth, barHeight);
    }
    
    private void drawControls(Graphics2D g2d) {
        // Background
        GradientPaint gradient = new GradientPaint(0, 0, new Color(10, 10, 30), 
                                                    0, WINDOW_HEIGHT, new Color(30, 10, 50));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        // Title
        g2d.setFont(titleFont.deriveFont(48f));
        g2d.setColor(new Color(200, 150, 255));
        String title = "CONTROLS";
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (WINDOW_WIDTH - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, 100);
        
        // Controls info
        g2d.setFont(optionFont);
        g2d.setColor(new Color(200, 200, 220));
        
        String[][] controls = {
            {"LEFT / RIGHT ARROW", "Move Left / Right"},
            {"SPACE", "Fire Weapon"},
            {"ESC", "Pause Game"},
            {"", ""},
            {"Objective:", "Destroy all enemies and defeat the boss!"},
            {"", "Collect health power-ups to survive!"}
        };
        
        int startY = 180;
        int spacing = 50;
        
        for (int i = 0; i < controls.length; i++) {
            int y = startY + i * spacing;
            
            if (controls[i][0].isEmpty()) continue;
            
            // Key
            g2d.setColor(new Color(255, 200, 100));
            g2d.drawString(controls[i][0], 150, y);
            
            // Description
            g2d.setColor(new Color(200, 200, 220));
            g2d.drawString(controls[i][1], 320, y);
        }
        
        // Back instruction
        g2d.setFont(customFont);
        g2d.setColor(new Color(150, 150, 180));
        String back = "Press ESC or BACKSPACE to return";
        fm = g2d.getFontMetrics();
        int backX = (WINDOW_WIDTH - fm.stringWidth(back)) / 2;
        g2d.drawString(back, backX, WINDOW_HEIGHT - 50);
    }
    
    private void drawCredits(Graphics2D g2d) {
        // Background
        GradientPaint gradient = new GradientPaint(0, 0, new Color(10, 10, 30), 
                                                    0, WINDOW_HEIGHT, new Color(30, 10, 50));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        // Title
        g2d.setFont(titleFont.deriveFont(48f));
        g2d.setColor(new Color(200, 150, 255));
        String title = "CREDITS";
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (WINDOW_WIDTH - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, 100);
        
        // Credits
        g2d.setFont(optionFont);
        
        String[][] credits = {
            {"GAME DESIGN & DEVELOPMENT", ""},
            {"Your Name", "Lead Developer"},
            {"", ""},
            {"SPECIAL THANKS", ""},
            {"Java Swing Framework", "UI Framework"},
            {"OpenGameArt.org", "Asset Resources"},
            {"", ""},
            {"POWERED BY", ""},
            {"Java & Swing", "2026"}
        };
        
        int startY = 180;
        int spacing = 45;
        
        for (int i = 0; i < credits.length; i++) {
            int y = startY + i * spacing;
            
            if (credits[i][0].isEmpty()) continue;
            
            boolean isHeader = credits[i][1].isEmpty();
            
            if (isHeader) {
                g2d.setColor(new Color(255, 200, 100));
                g2d.setFont(optionFont.deriveFont(Font.BOLD));
            } else {
                g2d.setColor(new Color(200, 200, 220));
                g2d.setFont(optionFont);
            }
            
            String text = credits[i][0];
            fm = g2d.getFontMetrics();
            int textX = (WINDOW_WIDTH - fm.stringWidth(text)) / 2;
            g2d.drawString(text, textX, y);
            
            if (!isHeader && !credits[i][1].isEmpty()) {
                g2d.setFont(customFont);
                g2d.setColor(new Color(150, 150, 180));
                String subtitle = credits[i][1];
                fm = g2d.getFontMetrics();
                int subtitleX = (WINDOW_WIDTH - fm.stringWidth(subtitle)) / 2;
                g2d.drawString(subtitle, subtitleX, y + 25);
            }
        }
        
        // Back instruction
        g2d.setFont(customFont);
        g2d.setColor(new Color(150, 150, 180));
        String back = "Press ESC or BACKSPACE to return";
        fm = g2d.getFontMetrics();
        int backX = (WINDOW_WIDTH - fm.stringWidth(back)) / 2;
        g2d.drawString(back, backX, WINDOW_HEIGHT - 50);
    }
    
    private void drawExitConfirm(Graphics2D g2d) {
        // Background
        GradientPaint gradient = new GradientPaint(0, 0, new Color(10, 10, 30), 
                                                    0, WINDOW_HEIGHT, new Color(30, 10, 50));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        // Semi-transparent overlay
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        // Warning box
        int boxWidth = 500;
        int boxHeight = 250;
        int boxX = (WINDOW_WIDTH - boxWidth) / 2;
        int boxY = (WINDOW_HEIGHT - boxHeight) / 2;
        
        g2d.setColor(new Color(40, 20, 60, 220));
        g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 20, 20);
        
        g2d.setColor(new Color(200, 150, 255));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 20, 20);
        
        // Title
        g2d.setFont(titleFont.deriveFont(40f));
        g2d.setColor(new Color(255, 200, 100));
        String title = "EXIT GAME?";
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (WINDOW_WIDTH - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, boxY + 80);
        
        // Message
        g2d.setFont(customFont);
        g2d.setColor(new Color(200, 200, 220));
        String msg = "Are you sure you want to exit?";
        fm = g2d.getFontMetrics();
        int msgX = (WINDOW_WIDTH - fm.stringWidth(msg)) / 2;
        g2d.drawString(msg, msgX, boxY + 120);
        
        // Options
        g2d.setFont(optionFont);
        String[] options = {"NO", "YES"};
        int optionsY = boxY + 180;
        int spacing = 150;
        
        for (int i = 0; i < options.length; i++) {
            String option = options[i];
            fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(option);
            int x = boxX + 125 + i * spacing;
            boolean isSelected = (i == exitConfirmSelection);
            
            if (isSelected) {
                // Selected
                g2d.setColor(new Color(138, 43, 226, 150));
                g2d.fillRoundRect(x - 15, optionsY - 30, textWidth + 30, 45, 10, 10);
                
                g2d.setColor(new Color(255, 255, 255));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(x - 15, optionsY - 30, textWidth + 30, 45, 10, 10);
                
                g2d.setColor(Color.WHITE);
                g2d.drawString(option, x, optionsY);
            } else {
                // Unselected
                g2d.setColor(new Color(150, 150, 180));
                g2d.drawString(option, x, optionsY);
            }
        }
        
        // Instructions
        g2d.setFont(customFont.deriveFont(18f));
        g2d.setColor(new Color(120, 120, 140));
        String inst = "LEFT/RIGHT to select | ENTER to confirm | ESC to cancel";
        fm = g2d.getFontMetrics();
        int instX = (WINDOW_WIDTH - fm.stringWidth(inst)) / 2;
        g2d.drawString(inst, instX, boxY + boxHeight - 30);
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (currentState == MenuState.EXIT_CONFIRM) {
            // Exit confirmation screen
            switch (key) {
                case KeyEvent.VK_LEFT:
                    exitConfirmSelection = 0; // NO
                    break;
                case KeyEvent.VK_RIGHT:
                    exitConfirmSelection = 1; // YES
                    break;
                case KeyEvent.VK_ENTER:
                    if (exitConfirmSelection == 1) {
                        System.exit(0);
                    } else {
                        currentState = MenuState.MAIN_MENU;
                        exitConfirmSelection = 0;
                    }
                    break;
                case KeyEvent.VK_ESCAPE:
                    currentState = MenuState.MAIN_MENU;
                    exitConfirmSelection = 0;
                    break;
            }
        } else if (currentState == MenuState.MAIN_MENU) {
            switch (key) {
                case KeyEvent.VK_UP:
                    selectedOption = (selectedOption - 1 + mainMenuOptions.length) % mainMenuOptions.length;
                    break;
                case KeyEvent.VK_DOWN:
                    selectedOption = (selectedOption + 1) % mainMenuOptions.length;
                    break;
                case KeyEvent.VK_LEFT:
                    if (mainMenuOptions[selectedOption].equals("MUSIC VOLUME")) {
                        musicVolume = Math.max(0.0f, musicVolume - 0.1f);
                        updateMusicVolume();
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (mainMenuOptions[selectedOption].equals("MUSIC VOLUME")) {
                        musicVolume = Math.min(1.0f, musicVolume + 0.1f);
                        updateMusicVolume();
                    }
                    break;
                case KeyEvent.VK_ENTER:
                    handleMenuSelection();
                    break;
            }
        } else {
            // In sub-menus (Controls, Credits)
            if (key == KeyEvent.VK_ESCAPE || key == KeyEvent.VK_BACK_SPACE) {
                currentState = MenuState.MAIN_MENU;
            }
        }
        
        repaint();
    }
    
    private void updateMusicVolume() {
        if (musicClip != null && musicClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl volumeControl = (FloatControl) musicClip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log(Math.max(0.01, musicVolume)) / Math.log(10.0) * 20.0);
            volumeControl.setValue(dB);
        }
    }
    
    private void handleMenuSelection() {
        String selected = mainMenuOptions[selectedOption];
        
        switch (selected) {
            case "PLAY GAME":
                if (musicClip != null) {
                    musicClip.stop();
                }
                if (animationTimer != null) {
                    animationTimer.stop();
                }
                if (gameWindow != null) {
                    gameWindow.startGame();
                }
                break;
            case "CONTROLS":
                currentState = MenuState.CONTROLS;
                break;
            case "MUSIC VOLUME":
                // Volume already adjusted with arrow keys
                break;
            case "CREDITS":
                currentState = MenuState.CREDITS;
                break;
            case "EXIT":
                currentState = MenuState.EXIT_CONFIRM;
                exitConfirmSelection = 0; // Default to NO
                break;
        }
    }
    
    public boolean isGameStarted() {
        return gameStarted;
    }
    
    public float getMusicVolume() {
        return musicVolume;
    }
    
    public void stopMusic() {
        if (musicClip != null && musicClip.isRunning()) {
            musicClip.stop();
            musicClip.close();
        }
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    @Override
    public void keyReleased(KeyEvent e) {}
}
