package digitapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * A grid for drawing digits that maintains a 28x28 internal representation
 * while providing a 280x280 drawing surface for user interaction.
 */
public class DrawableGrid extends JPanel {
    // Physical dimensions of the drawing surface are determined by container
    private static final int CELL_SIZE = 10;  // Each cell is 10x10 pixels
    
    // Internal representation dimensions
    private static final int INTERNAL_SIZE = 28;
    
    // Drawing state
    private float[][] pixelValues;  // Using float for smoother intensity calculations
    private boolean isDrawing = false;
    private Point lastPoint = null;
    
    // Drawing parameters
    private static final float BRUSH_RADIUS = 2f;  // In internal coordinates
    private static final float SMOOTHING = 0.72f;  // Smoothing factor for transitions
    
    public DrawableGrid() {
        // Let the layout manager determine size
        setBackground(Color.BLACK);
        pixelValues = new float[INTERNAL_SIZE][INTERNAL_SIZE];
        
        // Set up mouse listeners for drawing
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                isDrawing = true;
                if (SwingUtilities.isLeftMouseButton(e)) {
                    Point internalPoint = screenToInternal(e.getPoint());
                    lastPoint = internalPoint;
                    updateStroke(internalPoint);
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    clear(); // Right click to erase
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                isDrawing = false;
                lastPoint = null;
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDrawing && SwingUtilities.isLeftMouseButton(e)) {
                    Point internalPoint = screenToInternal(e.getPoint());
                    updateStroke(internalPoint);
                }
            }
        });
    }
    
    private Point screenToInternal(Point screenPoint) {
        // Convert screen coordinates to internal 28x28 grid
        return new Point(
            screenPoint.x * INTERNAL_SIZE / getWidth(),
            screenPoint.y * INTERNAL_SIZE / getHeight()
        );
    }
    
    private void updateStroke(Point currentPoint) {
        if (lastPoint == null) {
            applyBrush(currentPoint.x, currentPoint.y);
        } else {
            // Draw a smooth line from lastPoint to currentPoint
            float dx = currentPoint.x - lastPoint.x;
            float dy = currentPoint.y - lastPoint.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            
            if (distance > 0) {
                float steps = Math.max(distance * 2, 1);
                for (float i = 0; i <= steps; i++) {
                    float t = i / steps;
                    float x = lastPoint.x + dx * t;
                    float y = lastPoint.y + dy * t;
                    applyBrush(x, y);
                }
            }
        }
        
        lastPoint = currentPoint;
        repaint();
    }
    
    private void applyBrush(float centerX, float centerY) {
        // Calculate affected area
        int minX = Math.max(0, (int)(centerX - BRUSH_RADIUS - 1));
        int maxX = Math.min(INTERNAL_SIZE - 1, (int)(centerX + BRUSH_RADIUS + 1));
        int minY = Math.max(0, (int)(centerY - BRUSH_RADIUS - 1));
        int maxY = Math.min(INTERNAL_SIZE - 1, (int)(centerY + BRUSH_RADIUS + 1));
        
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                float dx = x - centerX;
                float dy = y - centerY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                
                if (distance < BRUSH_RADIUS) {
                    // Calculate intensity using smoothstep
                    float t = 1 - smoothstep(BRUSH_RADIUS * (1 - SMOOTHING), BRUSH_RADIUS, distance);
                    // Convert to 0-255 range and update max value
                    float intensity = t * 255;
                    pixelValues[y][x] = Math.max(pixelValues[y][x], intensity);
                }
            }
        }
    }
    
    private float smoothstep(float min, float max, float t) {
        t = clamp((t - min) / (max - min), 0, 1);
        return t * t * (3 - 2 * t);
    }
    
    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Clears the drawing surface
     */
    public void clear() {
        for (int y = 0; y < INTERNAL_SIZE; y++) {
            for (int x = 0; x < INTERNAL_SIZE; x++) {
                pixelValues[y][x] = 0;
            }
        }
        repaint();
    }
    
    /**
     * Gets the current 28x28 pixel values
     * @return A 28x28 array of pixel intensities (0-255)
     * Note: Values are in the format expected by the network (255=black, 0=white)
     */
    public int[][] getPixelValues() {
        int[][] result = new int[INTERNAL_SIZE][INTERNAL_SIZE];
        for (int y = 0; y < INTERNAL_SIZE; y++) {
            for (int x = 0; x < INTERNAL_SIZE; x++) {
                result[y][x] = (int) pixelValues[y][x];
            }
        }
        return result;
    }
    
    /**
     * Displays a visual representation of the current pixel values in a dialog.
     * This is useful for debugging and verifying the grid is being updated correctly.
     */
    public void showPixelValues() {
        // Get the current pixel values
        int[][] pixels = getPixelValues();
        
        // Create a BufferedImage to visualize the grid with higher contrast
        BufferedImage img = new BufferedImage(INTERNAL_SIZE * 10, INTERNAL_SIZE * 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        
        // Fill background with black (inverted)
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        
        // Draw each pixel with appropriate grayscale color
        for (int y = 0; y < INTERNAL_SIZE; y++) {
            for (int x = 0; x < INTERNAL_SIZE; x++) {
                int value = pixels[y][x];
                if (value > 0) {
                    // Use white values instead of black (inverted)
                    g.setColor(new Color(value, value, value));
                    g.fillRect(x * 10, y * 10, 10, 10);
                }
            }
        }
        
        // Draw grid lines to separate pixels
        g.setColor(Color.DARK_GRAY);
        for (int i = 0; i <= INTERNAL_SIZE; i++) {
            g.drawLine(i * 10, 0, i * 10, img.getHeight());
            g.drawLine(0, i * 10, img.getWidth(), i * 10);
        }
        
        g.dispose();
        
        // Also create a string representation for text-based viewing
        StringBuilder textRepresentation = new StringBuilder("28x28 Pixel Values (0-255):\n");
        for (int y = 0; y < INTERNAL_SIZE; y++) {
            for (int x = 0; x < INTERNAL_SIZE; x++) {
                int value = pixels[y][x];
                if (value == 0) {
                    textRepresentation.append("   ");
                } else if (value < 10) {
                    textRepresentation.append("  ").append(value);
                } else if (value < 100) {
                    textRepresentation.append(" ").append(value);
                } else {
                    textRepresentation.append(value);
                }
                textRepresentation.append(" ");
            }
            textRepresentation.append("\n");
        }
        
        // Create a panel with both image and text representation
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(new ImageIcon(img)), BorderLayout.CENTER);
        
        JTextArea textArea = new JTextArea(textRepresentation.toString());
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(800, 200));
        panel.add(scrollPane, BorderLayout.SOUTH);
        
        // Show in a dialog
        JOptionPane.showMessageDialog(this, panel, "28x28 Grid Visualization", JOptionPane.PLAIN_MESSAGE);
    }
    
    /**
     * Creates a scaled BufferedImage representation of the grid 
     * that matches the format expected by the neural network
     */
    public BufferedImage createScaledImage() {
        int[][] pixels = getPixelValues();
        BufferedImage img = new BufferedImage(INTERNAL_SIZE, INTERNAL_SIZE, BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < INTERNAL_SIZE; y++) {
            for (int x = 0; x < INTERNAL_SIZE; x++) {
                int pixelValue = 255 - pixels[y][x];
                int rgb = new Color(pixelValue, pixelValue, pixelValue).getRGB();
                img.setRGB(x, y, rgb);
            }
        }
        
        return img;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        int width = getWidth();
        int height = getHeight();
        
        // Calculate cell size based on smaller dimension (for square grid)
        int usableSize = Math.min(width, height);
        int cellSize = usableSize / INTERNAL_SIZE;
        
        // Calculate offset to center grid
        int offsetX = (width - cellSize * INTERNAL_SIZE) / 2;
        int offsetY = (height - cellSize * INTERNAL_SIZE) / 2;
        
        // Draw grid and pixels
        Graphics2D g2d = (Graphics2D) g;
        
        // First draw background grid
        g2d.setColor(Color.DARK_GRAY);
        
        // Draw vertical lines
        for (int x = 0; x <= INTERNAL_SIZE; x++) {
            g2d.drawLine(
                offsetX + x * cellSize, 
                offsetY, 
                offsetX + x * cellSize, 
                offsetY + INTERNAL_SIZE * cellSize
            );
        }
        
        // Draw horizontal lines
        for (int y = 0; y <= INTERNAL_SIZE; y++) {
            g2d.drawLine(
                offsetX, 
                offsetY + y * cellSize,
                offsetX + INTERNAL_SIZE * cellSize,
                offsetY + y * cellSize
            );
        }
        
        // Draw pixels
        for (int y = 0; y < INTERNAL_SIZE; y++) {
            for (int x = 0; x < INTERNAL_SIZE; x++) {
                float value = pixelValues[y][x];
                if (value > 0) {
                    // Invert: Use WHITE for drawing instead of gray values
                    int grayValue = (int) value;
                    g2d.setColor(new Color(255, 255, 255, grayValue)); // Use white with appropriate alpha
                    g2d.fillRect(offsetX + x * cellSize, offsetY + y * cellSize, cellSize, cellSize);
                }
            }
        }
    }
} 