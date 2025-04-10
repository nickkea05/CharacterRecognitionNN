package digitapp;

import neuralnetwork.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Handles neural network processing and prediction of drawn digits.
 * Features:
 * 1. Uses EMNISTTrainer to train a neural network
 * 2. Continuously processes DrawableGrid input
 * 3. Provides real-time predictions as user draws
 * 4. Displays top 5 predictions with confidence percentages
 */
public class Guess {
    // Network configuration
    private static final int INPUT_NODES = 28 * 28;  // 28x28 pixels
    private static final int HIDDEN_NODES = 256;     // Single hidden layer
    private static final int OUTPUT_NODES = 10;      // 10 digits (0-9)
    
    // Data loading parameters
    private static final int TRAINING_SAMPLES = 40000;  // 40,000 training samples
    private static final int TEST_SAMPLES = 5000;       // 5,000 test samples
    
    // GUI update frequency
    private static final int UPDATE_DELAY_MS = 100;  // Update predictions every 100ms
    
    // Prediction display
    private static final String[] DIGIT_NAMES = {
        "Zero", "One", "Two", "Three", "Four", 
        "Five", "Six", "Seven", "Eight", "Nine"
    };
    
    private NeuralNetwork network;
    private DrawableGrid drawableGrid;
    private JPanel predictionPanel;
    private Timer updateTimer;
    private boolean isNetworkReady = false;
    
    // Stores current predictions
    private int[] topPredictions;
    private double[] predictionConfidences;
    
    // Training status label reference
    private JLabel statusLabel;
    
    /**
     * Creates a new Guess instance
     * @param drawableGrid The drawing grid to get input from
     * @param predictionPanel Panel where prediction results will be displayed
     */
    public Guess(DrawableGrid drawableGrid, JPanel predictionPanel) {
        this.drawableGrid = drawableGrid;
        this.predictionPanel = predictionPanel;
        
        // Initialize prediction arrays
        topPredictions = new int[5];  // Top 5 predictions
        predictionConfidences = new double[5];  // Confidence for each prediction
        
        // Create the network
        network = new NeuralNetwork(new int[] {
            INPUT_NODES,
            HIDDEN_NODES,
            OUTPUT_NODES
        });
        
        // Initialize prediction panel with "Training..." message
        setupTrainingMessage();
        
        // Set up timer for continuous prediction updates
        updateTimer = new Timer(UPDATE_DELAY_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updatePrediction();
            }
        });
        updateTimer.start();
    }
    
    private void setupTrainingMessage() {
        SwingUtilities.invokeLater(() -> {
            predictionPanel.removeAll();
            
            JLabel trainingLabel = new JLabel("Training network...");
            trainingLabel.setForeground(Color.WHITE);
            trainingLabel.setFont(new Font(trainingLabel.getFont().getName(), Font.BOLD, 18));
            trainingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel targetLabel = new JLabel("Target: 99% accuracy");
            targetLabel.setForeground(Color.LIGHT_GRAY);
            targetLabel.setFont(new Font(targetLabel.getFont().getName(), Font.PLAIN, 14));
            targetLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            statusLabel = new JLabel("Starting...");
            statusLabel.setForeground(Color.LIGHT_GRAY);
            statusLabel.setFont(new Font(statusLabel.getFont().getName(), Font.PLAIN, 14));
            statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            predictionPanel.add(Box.createVerticalGlue());
            predictionPanel.add(trainingLabel);
            predictionPanel.add(Box.createVerticalStrut(10));
            predictionPanel.add(targetLabel);
            predictionPanel.add(Box.createVerticalStrut(20));
            predictionPanel.add(statusLabel);
            predictionPanel.add(Box.createVerticalGlue());
            
            predictionPanel.revalidate();
            predictionPanel.repaint();
        });
    }
    
    /**
     * Check if the network is trained and ready for predictions
     */
    public boolean isNetworkReady() {
        return isNetworkReady;
    }
    
    /**
     * Initiates training of the neural network using EMNISTTrainer
     */
    public void trainNetwork() {
        // Start neural network training in a separate thread
        new Thread(() -> {
            try {
                // Create a progress callback for training updates
                EMNISTTrainer.ProgressCallback progressCallback = new EMNISTTrainer.ProgressCallback() {
                    @Override
                    public void onEpochStart(int currentEpoch, int totalEpochs) {
                        updateStatus(String.format("Epoch %d/%d - Starting...", currentEpoch, totalEpochs));
                    }

                    @Override
                    public void onEpochComplete(int currentEpoch, int totalEpochs, double accuracy) {
                        updateStatus(String.format("Epoch %d/%d - Accuracy: %.2f%%", 
                                                  currentEpoch, totalEpochs, accuracy * 100));
                    }
                };
                
                // Use EMNISTTrainer to train our network
                updateStatus("Loading training data...");
                double accuracy = EMNISTTrainer.trainNetwork(network, TRAINING_SAMPLES, TEST_SAMPLES, progressCallback);
                
                updateStatus(String.format("Training complete! Accuracy: %.2f%%", accuracy * 100));
                System.out.println("Training complete! Final accuracy: " + (accuracy * 100) + "%");
                isNetworkReady = true;
                
            } catch (IOException e) {
                System.err.println("Error loading training data: " + e.getMessage());
                e.printStackTrace();
                
                // Show error in prediction panel
                SwingUtilities.invokeLater(() -> {
                    predictionPanel.removeAll();
                    
                    JLabel errorLabel = new JLabel("Error loading data");
                    errorLabel.setForeground(Color.RED);
                    errorLabel.setFont(new Font(errorLabel.getFont().getName(), Font.BOLD, 16));
                    errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    
                    predictionPanel.add(Box.createVerticalGlue());
                    predictionPanel.add(errorLabel);
                    predictionPanel.add(Box.createVerticalGlue());
                    
                    predictionPanel.revalidate();
                    predictionPanel.repaint();
                });
                
            } catch (Exception e) {
                System.err.println("Unexpected error: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
    
    /**
     * Updates the training status displayed in the UI
     */
    private void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText(status);
                predictionPanel.revalidate();
                predictionPanel.repaint();
            }
        });
    }
    
    /**
     * Updates prediction based on current drawing
     * Called periodically by the timer
     */
    private void updatePrediction() {
        // Get current drawing as a 28x28 pixel grid
        int[][] pixelValues = drawableGrid.getPixelValues();
        
        // Skip prediction if there's nothing drawn
        boolean hasDrawing = false;
        for (int i = 0; i < pixelValues.length && !hasDrawing; i++) {
            for (int j = 0; j < pixelValues[i].length && !hasDrawing; j++) {
                if (pixelValues[i][j] > 0) {
                    hasDrawing = true;
                }
            }
        }
        
        if (!hasDrawing) {
            // Clear predictions if nothing is drawn
            for (int i = 0; i < 5; i++) {
                topPredictions[i] = -1;
                predictionConfidences[i] = 0.0;
            }
            displayPredictions();
            return;
        }
        
        // Convert to a DataPoint
        DataPoint inputData = convertDrawingToDataPoint(pixelValues);
        
        // Run through neural network to get output values
        double[] outputs = network.CalculateOutputs(inputData.inputs);
        
        // Find top 5 predictions
        findTopPredictions(outputs);
        
        // Update UI with predictions
        displayPredictions();
    }
    
    /**
     * Converts a 28x28 drawing to a DataPoint the network can process
     */
    private DataPoint convertDrawingToDataPoint(int[][] pixelValues) {
        // Create input array
        double[] inputs = new double[INPUT_NODES];
        
        // Flatten and normalize pixel values
        int index = 0;
        for (int i = 0; i < 28; i++) {
            for (int j = 0; j < 28; j++) {
                // Normalize pixel values from 0-255 to 0-1 range
                inputs[index++] = pixelValues[i][j] / 255.0;
            }
        }
        
        // Create dummy expected outputs (all zeros since we don't know the expected digit)
        double[] expectedOutputs = new double[OUTPUT_NODES];
        
        return new DataPoint(inputs, expectedOutputs);
    }
    
    /**
     * Finds the top 5 predictions from network outputs
     */
    private void findTopPredictions(double[] outputs) {
        // Reset arrays
        for (int i = 0; i < 5; i++) {
            topPredictions[i] = -1;
            predictionConfidences[i] = 0.0;
        }
        
        // Find top 5 values
        for (int i = 0; i < outputs.length; i++) {
            // Check if this output is higher than any in our top 5
            for (int j = 0; j < 5; j++) {
                if (outputs[i] > predictionConfidences[j]) {
                    // Shift lower values down
                    for (int k = 4; k > j; k--) {
                        topPredictions[k] = topPredictions[k - 1];
                        predictionConfidences[k] = predictionConfidences[k - 1];
                    }
                    // Insert new value
                    topPredictions[j] = i;
                    predictionConfidences[j] = outputs[i];
                    break;
                }
            }
        }
    }
    
    /**
     * Updates the UI with current predictions
     */
    private void displayPredictions() {
        SwingUtilities.invokeLater(() -> {
            // Don't clear the entire panel if we're still training
            if (!isNetworkReady) {
                // Just update the prediction area without affecting the training messages
                // Find or create the prediction area
                JPanel predictionsContainer = null;
                for (Component comp : predictionPanel.getComponents()) {
                    if (comp instanceof JPanel && comp.getName() != null && 
                        comp.getName().equals("predictionsContainer")) {
                        predictionsContainer = (JPanel) comp;
                        break;
                    }
                }
                
                if (predictionsContainer == null) {
                    // Create a new container for predictions
                    predictionsContainer = new JPanel();
                    predictionsContainer.setName("predictionsContainer");
                    predictionsContainer.setLayout(new BoxLayout(predictionsContainer, BoxLayout.Y_AXIS));
                    predictionsContainer.setBackground(new Color(30, 30, 40));
                    
                    // Add it to the top of the panel
                    predictionPanel.add(predictionsContainer, 0);
                }
                
                // Clear and update only the predictions container
                predictionsContainer.removeAll();
                
                // Add predictions if we have any
                boolean hasValidPredictions = false;
                for (int i = 0; i < 5; i++) {
                    if (topPredictions[i] >= 0) {
                        hasValidPredictions = true;
                        JPanel predictionRow = createPredictionRow(i);
                        predictionsContainer.add(predictionRow);
                        predictionsContainer.add(Box.createVerticalStrut(5));
                    }
                }
                
                if (hasValidPredictions) {
                    JLabel trainingNote = new JLabel("(Training in progress)");
                    trainingNote.setForeground(Color.GRAY);
                    trainingNote.setFont(new Font(trainingNote.getFont().getName(), Font.ITALIC, 10));
                    trainingNote.setAlignmentX(Component.CENTER_ALIGNMENT);
                    predictionsContainer.add(trainingNote);
                }
                
                predictionsContainer.revalidate();
                predictionsContainer.repaint();
            } else {
                // If training is done, use the original full panel update
                predictionPanel.removeAll();
                predictionPanel.setLayout(new BoxLayout(predictionPanel, BoxLayout.Y_AXIS));
                
                // Add spacing at the top
                predictionPanel.add(Box.createVerticalStrut(20));
                
                // Add a row for each of the top 5 predictions
                for (int i = 0; i < 5; i++) {
                    if (topPredictions[i] >= 0) {
                        JPanel predictionRow = createPredictionRow(i);
                        predictionPanel.add(predictionRow);
                        predictionPanel.add(Box.createVerticalStrut(10)); // Space between rows
                    }
                }
                
                // Add glue at the bottom to push everything up
                predictionPanel.add(Box.createVerticalGlue());
                
                predictionPanel.revalidate();
                predictionPanel.repaint();
            }
        });
    }
    
    /**
     * Helper method to create a prediction row
     */
    private JPanel createPredictionRow(int index) {
        JPanel predictionRow = new JPanel();
        predictionRow.setLayout(new BorderLayout(10, 0));
        predictionRow.setBackground(new Color(30, 30, 40)); // Match background color
        
        // Create label with digit name
        JLabel digitLabel = new JLabel(DIGIT_NAMES[topPredictions[index]]);
        digitLabel.setFont(new Font(digitLabel.getFont().getName(), Font.PLAIN, 16));
        
        // Set the top prediction to white, others to gray
        if (index == 0) {
            digitLabel.setForeground(Color.WHITE);
        } else {
            digitLabel.setForeground(Color.GRAY);
        }
        
        // Create percentage label
        JLabel percentLabel = new JLabel(String.format("%.2f%%", predictionConfidences[index] * 100));
        percentLabel.setFont(new Font(percentLabel.getFont().getName(), Font.PLAIN, 16));
        
        if (index == 0) {
            percentLabel.setForeground(Color.WHITE);
        } else {
            percentLabel.setForeground(Color.GRAY);
        }
        
        // Add components to row
        predictionRow.add(digitLabel, BorderLayout.WEST);
        predictionRow.add(percentLabel, BorderLayout.EAST);
        
        return predictionRow;
    }
    
    /**
     * Stops prediction updates
     * Call this when closing the application
     */
    public void shutdown() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
    }
} 