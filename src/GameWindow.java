import javax.swing.*;
import java.awt.*;

public class GameWindow extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private MenuScreen menuScreen;
    private SpaceGame spaceGame;
    
    public GameWindow() {
        super("VOID - Main Menu");
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Create menu screen
        menuScreen = new MenuScreen(this);
        mainPanel.add(menuScreen, "MENU");
        
        add(mainPanel);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
        
        // Start with menu
        showMenu();
    }
    
    public void showMenu() {
        setTitle("VOID - Main Menu");
        
        // Stop and cleanup old game if it exists
        if (spaceGame != null) {
            spaceGame.cleanup();
            mainPanel.remove(spaceGame);
            spaceGame = null;
        }
        
        // Stop old menu music if exists
        if (menuScreen != null) {
            menuScreen.stopMusic();
            mainPanel.remove(menuScreen);
        }
        
        // Create fresh menu screen
        menuScreen = new MenuScreen(this);
        mainPanel.add(menuScreen, "MENU");
        
        cardLayout.show(mainPanel, "MENU");
        menuScreen.requestFocusInWindow();
    }
    
    public void startGame() {
        setTitle("VOID - Space Combat");
        
        // Stop menu music before starting game
        if (menuScreen != null) {
            menuScreen.stopMusic();
        }
        
        // Remove old game if exists
        if (spaceGame != null) {
            spaceGame.cleanup();
            mainPanel.remove(spaceGame);
        }
        
        // Create new game
        spaceGame = new SpaceGame(this);
        mainPanel.add(spaceGame, "GAME");
        
        cardLayout.show(mainPanel, "GAME");
        spaceGame.requestFocusInWindow();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GameWindow();
        });
    }
}
