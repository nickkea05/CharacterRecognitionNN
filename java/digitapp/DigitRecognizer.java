package digitapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Simple digit recognition app similar to the sample image
 */
public class DigitRecognizer extends JFrame {
    private DrawableGrid drawableGrid;
    private JPanel predictionPanel;
    private Guess guessEngine;
    
    public DigitRecognizer() {
        super("Digit Recognizer");
        
        // Set up the frame with dark background
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(new Color(30, 30, 40));
        
        // Create left panel (drawing area)
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        
        // Create the drawing grid with white background
        drawableGrid = new DrawableGrid();
        leftPanel.add(drawableGrid, BorderLayout.CENTER);
        
        // Create the prediction panel with a fixed size and dark background
        predictionPanel = new JPanel();
        predictionPanel.setBackground(new Color(30, 30, 40));
        predictionPanel.setLayout(new BoxLayout(predictionPanel, BoxLayout.Y_AXIS));
        predictionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        predictionPanel.setPreferredSize(new Dimension(200, 600)); // Match sample image width
        
        // Add components to frame (no spacing between areas)
        add(leftPanel, BorderLayout.CENTER);
        add(predictionPanel, BorderLayout.EAST);
        
        // Add keyboard shortcut for clearing (C key)
        KeyStroke clearKey = KeyStroke.getKeyStroke(KeyEvent.VK_C, 0);
        getRootPane().registerKeyboardAction(e -> drawableGrid.clear(), 
                                           clearKey, 
                                           JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        // Add mouse instructions at the bottom
        JPanel instructionPanel = new JPanel();
        instructionPanel.setBackground(new Color(30, 30, 40));
        instructionPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        
        JLabel instructionsLabel = new JLabel("Left mouse: Draw  |  Right mouse: Erase  |  C: Clear");
        instructionsLabel.setForeground(Color.LIGHT_GRAY);
        instructionPanel.add(instructionsLabel);
        
        add(instructionPanel, BorderLayout.SOUTH);
        
        // Initialize the guess engine and start training immediately
        guessEngine = new Guess(drawableGrid, predictionPanel);
        
        // Set size and center on screen
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Start training immediately
        startTraining();
    }
    
    /**
     * Starts the neural network training process immediately
     */
    private void startTraining() {
        // Start training network without requiring button press
        guessEngine.trainNetwork();
    }
    
    @Override
    public void dispose() {
        // Shut down the guess engine properly
        if (guessEngine != null) {
            guessEngine.shutdown();
        }
        super.dispose();
    }
    
    /**
     * Main entry point for the application
     */
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new DigitRecognizer().setVisible(true);
        });
    }
} 