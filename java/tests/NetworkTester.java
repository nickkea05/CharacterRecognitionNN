package tests;

import java.io.IOException;
import javax.swing.SwingUtilities;

import neuralnetwork.DataLoader;
import neuralnetwork.DataPoint;
import neuralnetwork.NeuralNetwork;

public class NetworkTester 
{
    // Network configuration for digit recognition (MNIST)
    private static final int INPUT_NODES = 28 * 28;  // 28x28 pixels
    private static final int HIDDEN_NODES = 256;     // Single hidden layer
    private static final int OUTPUT_NODES = 10;      // 10 digits (0-9)
    
    // Training parameters
    private static final double INITIAL_LEARNING_RATE = 0.4;  // High initial learning rate
    private static final double MIN_LEARNING_RATE = 0.01;     // Minimum learning rate
    private static final double DECAY_FACTOR = 0.8;           // How much to decay learning rate
    private static final double[] ACCURACY_THRESHOLDS = {0.8, 0.85, 0.9, 0.95}; // Accuracy thresholds for decay
    private static final int BATCH_SIZE = 128;                // Balanced batch size
    private static final int MAX_EPOCHS = 200;
    private static final double TARGET_ACCURACY = 0.99;       // Target 99% accuracy
    private static final int PATIENCE = 10;                   // Reasonable patience
    
    // Data loading parameters
    private static final int TRAINING_SAMPLES = 20000;  // More training data
    private static final int TEST_SAMPLES = 2000;
    
    public static void main(String[] args) {
        try {
            System.out.println("Starting neural network test...");
            
            // Create the neural network with single hidden layer
            NeuralNetwork network = new NeuralNetwork(new int[] {
                INPUT_NODES,
                HIDDEN_NODES,
                OUTPUT_NODES
            });
            
            // Load training and test data
            System.out.println("Loading data...");
            DataPoint[][] data = DataLoader.loadTrainTestData(
                "dataset/archive/emnist-mnist-train.csv",
                "dataset/archive/emnist-mnist-test.csv",
                TRAINING_SAMPLES,
                TEST_SAMPLES,
                OUTPUT_NODES
            );
            
            DataPoint[] trainingData = data[0];
            DataPoint[] testData = data[1];
            
            System.out.printf("Loaded %d training samples and %d test samples\n",
                            trainingData.length, testData.length);
            
            // Train the network
            System.out.println("Training network...");
            trainNetwork(network, trainingData, testData);
            
            // Show the test results in a viewer
            System.out.println("Launching test viewer...");
            SwingUtilities.invokeLater(() -> {
                NetworkTesterViewer viewer = new NetworkTesterViewer(network, testData);
                viewer.setVisible(true);
            });
            
        } catch (IOException e) {
            System.err.println("Error during testing: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void trainNetwork(NeuralNetwork network, DataPoint[] trainingData, DataPoint[] testData) {
        int numBatches = (trainingData.length + BATCH_SIZE - 1) / BATCH_SIZE;
        double bestAccuracy = 0;
        int epochsWithoutImprovement = 0;
        double currentLearningRate = INITIAL_LEARNING_RATE;
        int currentThresholdIndex = 0;
        
        for (int epoch = 0; epoch < MAX_EPOCHS; epoch++) {
            System.out.printf("\nEpoch %d/%d (learning rate: %.5f)\n", 
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
                double batchCost = 0;
                try {
                    batchCost = network.Cost(batchData);
                } catch (Exception e) {
                    // If network.Cost throws an exception, calculate a simple average cost manually
                    for (DataPoint dp : batchData) {
                        double[] outputs = network.CalculateOutputs(dp.inputs);
                        double sampleCost = 0;
                        for (int i = 0; i < outputs.length; i++) {
                            double error = outputs[i] - dp.expectedOutputs[i];
                            sampleCost += error * error; // Square error
                        }
                        batchCost += sampleCost / outputs.length;
                    }
                    batchCost /= batchData.length;
                }
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
            
            if (accuracy >= TARGET_ACCURACY) {
                System.out.println("\nReached target accuracy! Stopping training.");
                break;
            }
            
            if (epochsWithoutImprovement >= PATIENCE) {
                System.out.println("\nNo improvement for " + PATIENCE + " epochs. Stopping training.");
                break;
            }
        }
    }
    
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
} 