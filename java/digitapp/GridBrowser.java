package digitapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A simple browser to test the DrawableGrid functionality
 */
public class GridBrowser extends JFrame {
    private DrawableGrid grid;
    private JLabel infoLabel;
    
    public GridBrowser() {
        super("DrawableGrid Browser");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create the grid
        grid = new DrawableGrid();
        add(grid, BorderLayout.CENTER);
        
        // Create control panel
        JPanel controlPanel = new JPanel();
        JButton clearButton = new JButton("Clear");
        JButton printButton = new JButton("Print Values");
        
        clearButton.addActionListener(e -> grid.clear());
        printButton.addActionListener(e -> printPixelValues());
        
        controlPanel.add(clearButton);
        controlPanel.add(printButton);
        add(controlPanel, BorderLayout.SOUTH);
        
        // Create info label
        infoLabel = new JLabel("Draw in the grid above");
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(infoLabel, BorderLayout.NORTH);
        
        // Set up keyboard shortcuts
        setupKeyboardShortcuts();
        
        // Finalize frame
        pack();
        setLocationRelativeTo(null);
    }
    
    private void setupKeyboardShortcuts() {
        // C key for clear
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), "clear");
        getRootPane().getActionMap().put("clear", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                grid.clear();
            }
        });
        
        // P key for print values
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "print");
        getRootPane().getActionMap().put("print", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                printPixelValues();
            }
        });
    }
    
    private void printPixelValues() {
        int[][] values = grid.getPixelValues();
        StringBuilder sb = new StringBuilder();
        sb.append("Pixel Values (28x28):\n");
        
        for (int y = 0; y < values.length; y++) {
            for (int x = 0; x < values[y].length; x++) {
                sb.append(String.format("%3d ", values[y][x]));
            }
            sb.append("\n");
        }
        
        System.out.println(sb.toString());
        infoLabel.setText("Values printed to console");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GridBrowser browser = new GridBrowser();
            browser.setVisible(true);
        });
    }
} 