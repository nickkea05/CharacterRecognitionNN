package tests;

import javax.swing.*;

import neuralnetwork.DataPoint;
import neuralnetwork.NeuralNetwork;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class NetworkTesterViewer extends JFrame 
{
    // Constants for image display
    private static final int IMAGE_SIZE = 28;
    private static final int SCALE = 10;
    
    // Network and data
    private final NeuralNetwork network;
    private final List<TestImage> testImages;
    private int currentIndex = 0;
    
    // UI components
    private JLabel imageLabel;
    private JLabel infoLabel;
    private JLabel predictionLabel;
    
    public NetworkTesterViewer(NeuralNetwork network, DataPoint[] testData) {
        super("Neural Network Test Viewer");
        this.network = network;
        this.testImages = createTestImages(testData);
        
        // Set up the UI
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Image display
        imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(IMAGE_SIZE * SCALE, IMAGE_SIZE * SCALE));
        add(imageLabel, BorderLayout.CENTER);
        
        // Info panel at the bottom
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoLabel = new JLabel();
        predictionLabel = new JLabel();
        infoPanel.add(infoLabel);
        infoPanel.add(predictionLabel);
        add(infoPanel, BorderLayout.SOUTH);
        
        // Control panel at the top
        JPanel controlPanel = new JPanel();
        JButton prevButton = new JButton("Previous");
        JButton nextButton = new JButton("Next");
        
        prevButton.addActionListener(e -> showPreviousImage());
        nextButton.addActionListener(e -> showNextImage());
        
        controlPanel.add(prevButton);
        controlPanel.add(nextButton);
        add(controlPanel, BorderLayout.NORTH);
        
        // Show first image
        if (!testImages.isEmpty()) {
            showImage(0);
        } else {
            infoLabel.setText("No test images available");
        }
        
        // Finalize frame
        pack();
        setLocationRelativeTo(null);
    }
    
    private List<TestImage> createTestImages(DataPoint[] testData) {
        List<TestImage> images = new ArrayList<>();
        for (DataPoint dataPoint : testData) {
            TestImage image = new TestImage();
            image.label = dataPoint.label;
            image.pixels = new int[IMAGE_SIZE][IMAGE_SIZE];
            
            // Convert normalized inputs back to pixel values
            for (int i = 0; i < IMAGE_SIZE; i++) {
                for (int j = 0; j < IMAGE_SIZE; j++) {
                    int index = i * IMAGE_SIZE + j;
                    image.pixels[i][j] = (int)(dataPoint.inputs[index] * 255);
                }
            }
            
            images.add(image);
        }
        return images;
    }
    
    private void showPreviousImage() {
        if (testImages.size() > 0) {
            currentIndex = (currentIndex - 1 + testImages.size()) % testImages.size();
            showImage(currentIndex);
        }
    }
    
    private void showNextImage() {
        if (testImages.size() > 0) {
            currentIndex = (currentIndex + 1) % testImages.size();
            showImage(currentIndex);
        }
    }
    
    private void showImage(int index) {
        if (index < 0 || index >= testImages.size()) return;
        
        TestImage image = testImages.get(index);
        
        // Create and display the image
        BufferedImage bufferedImage = createBufferedImage(image);
        BufferedImage scaledImage = new BufferedImage(
                IMAGE_SIZE * SCALE, IMAGE_SIZE * SCALE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaledImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                          RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(bufferedImage, 0, 0, IMAGE_SIZE * SCALE, IMAGE_SIZE * SCALE, null);
        g.dispose();
        
        // Get network prediction
        double[] inputs = new double[IMAGE_SIZE * IMAGE_SIZE];
        for (int i = 0; i < IMAGE_SIZE; i++) {
            for (int j = 0; j < IMAGE_SIZE; j++) {
                inputs[i * IMAGE_SIZE + j] = image.pixels[i][j] / 255.0; // Normalize to 0-1
            }
        }
        int prediction = network.Classify(inputs);
        
        // Update the UI
        imageLabel.setIcon(new ImageIcon(scaledImage));
        infoLabel.setText(String.format("Image %d/%d, Actual Label: %d", 
                          index + 1, testImages.size(), image.label));
        predictionLabel.setText(String.format("Network Prediction: %d (Correct: %s)", 
                          prediction, prediction == image.label ? "Yes" : "No"));
    }
    
    private BufferedImage createBufferedImage(TestImage image) {
        BufferedImage bufferedImage = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, 
                                                       BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < IMAGE_SIZE; y++) {
            for (int x = 0; x < IMAGE_SIZE; x++) {
                int pixelValue = 255 - image.pixels[y][x]; // Invert colors for display
                int rgb = new Color(pixelValue, pixelValue, pixelValue).getRGB();
                bufferedImage.setRGB(x, y, rgb);
            }
        }
        
        return bufferedImage;
    }
    
    private static class TestImage {
        int label;
        int[][] pixels;
    }
} 