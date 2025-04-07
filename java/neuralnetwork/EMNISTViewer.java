package neuralnetwork;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * A simple test program to read and display a single EMNIST image from a CSV file
 * This helps verify if there are any orientation issues with the images
 */
public class EMNISTViewer {
    
    public static void main(String[] args) {
        // Path to the EMNIST CSV file - we'll use a small test file
        String csvFilePath = "dataset/archive/emnist-mnist-test.csv";
        
        try {
            // Read first image from CSV file
            System.out.println("Reading image from CSV file...");
            EMNISTImage image = readImageFromCSV(csvFilePath);
            System.out.println("Label: " + image.label);
            
            // Display the image
            displayImage(image);
            
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Reads the first image from the CSV file
     * @param csvFilePath Path to the CSV file
     * @return An object containing the image data and label
     */
    private static EMNISTImage readImageFromCSV(String csvFilePath) throws IOException {
        EMNISTImage image = new EMNISTImage();
        
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            // Read the first line (skip header if exists)
            String line = br.readLine();
            
            // Split the line by comma
            String[] values = line.split(",");
            
            // The first value is the label
            image.label = Integer.parseInt(values[0]);
            
            // The remaining 784 values are pixel values (28x28)
            image.pixels = new int[28][28];
            for (int i = 0; i < 28; i++) {
                for (int j = 0; j < 28; j++) {
                    // CSV values are in row-major order
                    // +1 to skip the label
                    int pixelValue = Integer.parseInt(values[i * 28 + j + 1]);
                    
                    // Store the original values without transformation
                    image.pixels[i][j] = pixelValue;
                }
            }
        }
        
        return image;
    }
    
    /**
     * Displays the image in a JFrame window
     * @param image The EMNIST image to display
     */
    private static void displayImage(EMNISTImage image) {
        // Create a BufferedImage from the pixel data
        BufferedImage bufferedImage = new BufferedImage(28, 28, BufferedImage.TYPE_INT_RGB);
        
        // Fill the BufferedImage with the pixel data
        for (int y = 0; y < 28; y++) {
            for (int x = 0; x < 28; x++) {
                // MNIST pixel values are 0-255, with 0 being white and 255 being black
                // We invert the colors to make the digits black on white background
                int pixelValue = 255 - image.pixels[y][x];
                int rgb = new Color(pixelValue, pixelValue, pixelValue).getRGB();
                bufferedImage.setRGB(x, y, rgb);
            }
        }
        
        // Scale the image to make it more visible (4x larger)
        BufferedImage scaledImage = new BufferedImage(28 * 10, 28 * 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaledImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(bufferedImage, 0, 0, 28 * 10, 28 * 10, null);
        g.dispose();
        
        // Create and show the JFrame
        JFrame frame = new JFrame("EMNIST Image Viewer - Label: " + image.label);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(new JLabel(new ImageIcon(scaledImage)), BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    /**
     * Simple class to hold an EMNIST image and its label
     */
    private static class EMNISTImage {
        int label;
        int[][] pixels;
    }
} 