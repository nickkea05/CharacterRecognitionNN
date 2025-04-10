package tests;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An enhanced viewer for EMNIST images that allows browsing through multiple images
 * and testing different orientations to check for orientation issues.
 */
public class EMNISTBrowserViewer extends JFrame {
    private static final int IMAGE_SIZE = 28;
    private static final int SCALE = 10;
    private static final int MAX_IMAGES = 100; // Limit how many images to load initially
    
    private List<EMNISTImage> images;
    private int currentIndex = 0;
    private int orientation = 0; // 0=normal, 1=90°, 2=180°, 3=270°
    private JLabel imageLabel;
    private JLabel infoLabel;
    
    public EMNISTBrowserViewer() {
        super("EMNIST Image Browser");
        
        // Set up the UI
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Image display
        imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(IMAGE_SIZE * SCALE, IMAGE_SIZE * SCALE));
        add(imageLabel, BorderLayout.CENTER);
        
        // Info panel at the bottom
        infoLabel = new JLabel();
        add(infoLabel, BorderLayout.SOUTH);
        
        // Control panel at the top
        JPanel controlPanel = new JPanel();
        JButton prevButton = new JButton("Previous");
        JButton nextButton = new JButton("Next");
        JButton rotateButton = new JButton("Rotate");
        
        prevButton.addActionListener(e -> showPreviousImage());
        nextButton.addActionListener(e -> showNextImage());
        rotateButton.addActionListener(e -> rotateImage());
        
        controlPanel.add(prevButton);
        controlPanel.add(nextButton);
        controlPanel.add(rotateButton);
        add(controlPanel, BorderLayout.NORTH);
        
        // Set up keyboard shortcuts
        setupKeyboardShortcuts();
        
        // Load images
        try {
            images = loadImages("dataset/archive/emnist-mnist-test.csv", MAX_IMAGES);
            if (!images.isEmpty()) {
                showImage(0);
            } else {
                infoLabel.setText("No images loaded");
            }
        } catch (IOException e) {
            infoLabel.setText("Error loading images: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Finalize frame
        pack();
        setLocationRelativeTo(null);
    }
    
    private void setupKeyboardShortcuts() {
        // Left and right arrow keys for navigation
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "previous");
        getRootPane().getActionMap().put("previous", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPreviousImage();
            }
        });
        
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "next");
        getRootPane().getActionMap().put("next", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showNextImage();
            }
        });
        
        // R key for rotation
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "rotate");
        getRootPane().getActionMap().put("rotate", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rotateImage();
            }
        });
    }
    
    private void showPreviousImage() {
        if (images.size() > 0) {
            currentIndex = (currentIndex - 1 + images.size()) % images.size();
            showImage(currentIndex);
        }
    }
    
    private void showNextImage() {
        if (images.size() > 0) {
            currentIndex = (currentIndex + 1) % images.size();
            showImage(currentIndex);
        }
    }
    
    private void rotateImage() {
        orientation = (orientation + 1) % 4;
        showImage(currentIndex);
    }
    
    private void showImage(int index) {
        if (index < 0 || index >= images.size()) return;
        
        EMNISTImage image = images.get(index);
        BufferedImage bufferedImage = createBufferedImage(image);
        
        // Apply orientation
        if (orientation > 0) {
            bufferedImage = rotateBufferedImage(bufferedImage, orientation * 90);
        }
        
        // Scale the image
        BufferedImage scaledImage = new BufferedImage(
                IMAGE_SIZE * SCALE, IMAGE_SIZE * SCALE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaledImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                          RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(bufferedImage, 0, 0, IMAGE_SIZE * SCALE, IMAGE_SIZE * SCALE, null);
        g.dispose();
        
        // Update the UI
        imageLabel.setIcon(new ImageIcon(scaledImage));
        infoLabel.setText(String.format("Image %d/%d, Label: %d, Orientation: %d°", 
                          index + 1, images.size(), image.label, orientation * 90));
    }
    
    private BufferedImage createBufferedImage(EMNISTImage image) {
        BufferedImage bufferedImage = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, 
                                                       BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < IMAGE_SIZE; y++) {
            for (int x = 0; x < IMAGE_SIZE; x++) {
                // MNIST pixel values are 0-255, with 0 being white and 255 being black
                // We invert the colors to make the digits black on white background
                int pixelValue = 255 - image.pixels[y][x];
                int rgb = new Color(pixelValue, pixelValue, pixelValue).getRGB();
                bufferedImage.setRGB(x, y, rgb);
            }
        }
        
        return bufferedImage;
    }
    
    private BufferedImage rotateBufferedImage(BufferedImage image, int degrees) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        BufferedImage rotatedImage = new BufferedImage(width, height, image.getType());
        Graphics2D g = rotatedImage.createGraphics();
        
        g.translate(width / 2, height / 2);
        g.rotate(Math.toRadians(degrees));
        g.translate(-width / 2, -height / 2);
        g.drawImage(image, 0, 0, null);
        g.dispose();
        
        return rotatedImage;
    }
    
    private List<EMNISTImage> loadImages(String csvFilePath, int maxImages) throws IOException {
        List<EMNISTImage> imageList = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            int count = 0;
            
            while ((line = br.readLine()) != null && count < maxImages) {
                String[] values = line.split(",");
                
                EMNISTImage image = new EMNISTImage();
                image.label = Integer.parseInt(values[0]);
                image.pixels = new int[IMAGE_SIZE][IMAGE_SIZE];
                
                for (int i = 0; i < IMAGE_SIZE; i++) {
                    for (int j = 0; j < IMAGE_SIZE; j++) {
                        int pixelValue = Integer.parseInt(values[i * IMAGE_SIZE + (IMAGE_SIZE - 1 - j) + 1]);
                        image.pixels[IMAGE_SIZE - 1 - j][i] = pixelValue;
                    }
                }
                
                imageList.add(image);
                count++;
            }
        }
        
        return imageList;
    }
    
    /**
     * Simple class to hold an EMNIST image and its label
     */
    private static class EMNISTImage {
        int label;
        int[][] pixels;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EMNISTBrowserViewer viewer = new EMNISTBrowserViewer();
            viewer.setVisible(true);
        });
    }
} 