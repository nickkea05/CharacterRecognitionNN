package neuralnetwork;

import java.io.IOException;
import java.util.Arrays;

/**
 * Specialized class for training neural networks on the EMNIST dataset.
 */
public class EMNISTTrainer {
    // Network configuration defaults
    private static final int INPUT_NODES = 28 * 28;  // 28x28 pixels
    private static final int HIDDEN_NODES = 256;     // Single hidden layer
    private static final int OUTPUT_NODES = 10;      // 10 digits (0-9)
    
    // Training parameters
    private static final double INITIAL_LEARNING_RATE = 0.4;  // High initial learning rate
    private static final double MIN_LEARNING_RATE = 0.01;     // Minimum learning rate
    private static final double DECAY_FACTOR = 0.8;           // How much to decay learning rate
    private static final double[] ACCURACY_THRESHOLDS = {0.8, 0.85, 0.9, 0.95}; // Accuracy thresholds for decay
    private static final int BATCH_SIZE = 128;                // Balanced batch size
    private static final int MAX_EPOCHS = 200;                // Maximum number of epochs
    private static final double TARGET_ACCURACY = 0.99;       // Target accuracy (99%)
    private static final int PATIENCE = 10;                   // Stop after 10 epochs with no improvement
    
    // Progress callback interface for UI updates
    public interface ProgressCallback {
        void onEpochStart(int currentEpoch, int totalEpochs);
        void onEpochComplete(int currentEpoch, int totalEpochs, double accuracy);
    }
    
    /**
     * Train a network on the EMNIST dataset with default parameters
     * @param network The neural network to train
     * @param trainingSamples Number of training samples to use
     * @param testSamples Number of test samples to use
     * @return The final accuracy achieved
     * @throws IOException If data files cannot be loaded
     */
    public static double trainNetwork(NeuralNetwork network, int trainingSamples, int testSamples) 
            throws IOException {
        return trainNetwork(network, trainingSamples, testSamples, null);
    }
    
    /**
     * Train a network on the EMNIST dataset with default parameters
     * @param network The neural network to train
     * @param trainingSamples Number of training samples to use
     * @param testSamples Number of test samples to use
     * @param callback Optional callback for progress updates
     * @return The final accuracy achieved
     * @throws IOException If data files cannot be loaded
     */
    public static double trainNetwork(NeuralNetwork network, int trainingSamples, int testSamples, 
                                      ProgressCallback callback) throws IOException {
        // Load training and test data
        System.out.println("Loading EMNIST data...");
        DataPoint[][] data = DataLoader.loadTrainTestData(
            "dataset/archive/emnist-mnist-train.csv",
            "dataset/archive/emnist-mnist-test.csv",
            trainingSamples,
            testSamples,
            OUTPUT_NODES
        );
        
        DataPoint[] trainingData = data[0];
        DataPoint[] testData = data[1];
        
        // Train the network
        System.out.println("Starting network training...");
        return trainNetworkWithData(network, trainingData, testData, callback);
    }
    
    /**
     * Train a network with the provided training and test data
     * @param network The neural network to train
     * @param trainingData Array of training data points
     * @param testData Array of test data points
     * @param callback Optional callback for progress updates
     * @return The final accuracy achieved
     */
    public static double trainNetworkWithData(NeuralNetwork network, DataPoint[] trainingData, 
                                             DataPoint[] testData, ProgressCallback callback) {
        int numBatches = (trainingData.length + BATCH_SIZE - 1) / BATCH_SIZE;
        double bestAccuracy = 0;
        int epochsWithoutImprovement = 0;
        double currentLearningRate = INITIAL_LEARNING_RATE;
        int currentThresholdIndex = 0;
        
        System.out.printf("Training with %d samples, testing with %d samples\n", 
                        trainingData.length, testData.length);
        System.out.printf("Target accuracy: %.1f%%, Patience: %d epochs\n", 
                        TARGET_ACCURACY * 100, PATIENCE);
        
        for (int epoch = 0; epoch < MAX_EPOCHS; epoch++) {
            // Notify about epoch start
            if (callback != null) {
                callback.onEpochStart(epoch + 1, MAX_EPOCHS);
            }
            
            System.out.printf("Epoch %d/%d (learning rate: %.5f)\n", 
                           epoch + 1, MAX_EPOCHS, currentLearningRate);
            double totalCost = 0;
            
            // Train on all batches
            for (int batch = 0; batch < numBatches; batch++) {
                int startIdx = batch * BATCH_SIZE;
                int endIdx = Math.min(startIdx + BATCH_SIZE, trainingData.length);
                int batchSize = endIdx - startIdx;
                
                // Create a batch of the appropriate size
                DataPoint[] batchData = new DataPoint[batchSize];
                System.arraycopy(trainingData, startIdx, batchData, 0, batchSize);
                
                // Train on this batch with current learning rate
                network.Learn(batchData, currentLearningRate);
                
                // Calculate and accumulate cost for this batch
                double batchCost = network.Cost(batchData);
                totalCost += batchCost * batchSize;
                
                if (batch % 10 == 0) {
                    System.out.printf("  Batch %d/%d, Cost: %.4f\n",
                                    batch + 1, numBatches, batchCost);
                }
            }
            
            // Calculate average cost for the epoch
            double avgCost = totalCost / trainingData.length;
            System.out.printf("  Average cost: %.4f\n", avgCost);
            
            // Evaluate on test set
            double accuracy = evaluateNetwork(network, testData);
            System.out.printf("  Test accuracy: %.2f%%\n", accuracy * 100);
            
            // Notify about epoch completion
            if (callback != null) {
                callback.onEpochComplete(epoch + 1, MAX_EPOCHS, accuracy);
            }
            
            // Check if we should reduce the learning rate based on accuracy thresholds
            if (currentThresholdIndex < ACCURACY_THRESHOLDS.length && 
                accuracy >= ACCURACY_THRESHOLDS[currentThresholdIndex]) {
                currentLearningRate = Math.max(currentLearningRate * DECAY_FACTOR, MIN_LEARNING_RATE);
                System.out.printf("  Reached %.1f%% accuracy - reducing learning rate to %.5f\n", 
                               ACCURACY_THRESHOLDS[currentThresholdIndex] * 100, currentLearningRate);
                currentThresholdIndex++;
            }
            
            // Check for early stopping
            if (accuracy > bestAccuracy) {
                bestAccuracy = accuracy;
                epochsWithoutImprovement = 0;
            } else {
                epochsWithoutImprovement++;
                // If we're stuck, also try reducing the learning rate
                if (epochsWithoutImprovement % 5 == 0 && currentLearningRate > MIN_LEARNING_RATE) {
                    currentLearningRate = Math.max(currentLearningRate * DECAY_FACTOR, MIN_LEARNING_RATE);
                    System.out.printf("  No improvement for %d epochs - reducing learning rate to %.5f\n", 
                                   epochsWithoutImprovement, currentLearningRate);
                }
            }
            
            // Check if we have reached target accuracy
            if (accuracy >= TARGET_ACCURACY) {
                System.out.println("Reached target accuracy! Stopping training.");
                return accuracy;
            }
            
            // Check for early stopping
            if (epochsWithoutImprovement >= PATIENCE) {
                System.out.println("No improvement for " + PATIENCE + " epochs. Stopping training.");
                return bestAccuracy;
            }
        }
        
        System.out.println("Reached maximum epochs. Final accuracy: " + (bestAccuracy * 100) + "%");
        return bestAccuracy;
    }
    
    /**
     * Evaluates the network on test data
     * @return Accuracy (0.0-1.0)
     */
    private static double evaluateNetwork(NeuralNetwork network, DataPoint[] testData) {
        int correct = 0;
        
        for (DataPoint dataPoint : testData) {
            int prediction = network.Classify(dataPoint.inputs);
            if (prediction == dataPoint.label) {
                correct++;
            }
        }
        
        return (double) correct / testData.length;
    }
    
    /**
     * Creates a confusion matrix for the network on test data
     * Shows how many of each digit were classified as each other digit
     * @return The confusion matrix
     */
    public static int[][] createConfusionMatrix(NeuralNetwork network, DataPoint[] testData) {
        int[][] confusionMatrix = new int[OUTPUT_NODES][OUTPUT_NODES];
        
        for (DataPoint dataPoint : testData) {
            int actual = dataPoint.label;
            int predicted = network.Classify(dataPoint.inputs);
            
            confusionMatrix[actual][predicted]++;
        }
        
        return confusionMatrix;
    }
    
    /**
     * Prints a confusion matrix in a readable format
     */
    public static void printConfusionMatrix(int[][] confusionMatrix) {
        System.out.println("\nConfusion Matrix (rows=actual, cols=predicted):");
        
        // Print header
        System.out.print("   ");
        for (int i = 0; i < confusionMatrix.length; i++) {
            System.out.printf("%4d", i);
        }
        System.out.println("\n   " + "----".repeat(confusionMatrix.length));
        
        // Print rows
        for (int i = 0; i < confusionMatrix.length; i++) {
            System.out.printf("%2d |", i);
            for (int j = 0; j < confusionMatrix[i].length; j++) {
                System.out.printf("%4d", confusionMatrix[i][j]);
            }
            System.out.println();
        }
    }
} 