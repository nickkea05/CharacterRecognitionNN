package neuralnetwork;

import java.io.IOException;

/**
 * Application for training and testing a neural network on EMNIST data
 */
public class EMNISTTrainer {
    // Default paths to EMNIST data files
    private static final String TRAIN_FILE = "dataset/archive/emnist-mnist-train.csv";
    private static final String TEST_FILE = "dataset/archive/emnist-mnist-test.csv";
    
    // For digit classification (0-9), we have 10 classes
    private static final int NUM_CLASSES = 10;
    
    // Network configuration
    private static final int[] LAYER_SIZES = {784, 128, 64, NUM_CLASSES};
    
    // Training hyperparameters
    private static final double LEARNING_RATE = 0.01;
    private static final int BATCH_SIZE = 100;
    private static final int EPOCHS = 5;
    
    public static void main(String[] args) {
        try {
            System.out.println("Loading EMNIST data...");
            // Load a limited number of samples for demonstration (adjust as needed)
            DataPoint[][] data = DataLoader.loadTrainTestData(
                TRAIN_FILE, 
                TEST_FILE, 
                10000,  // Load 10,000 training samples
                1000,   // Load 1,000 test samples
                NUM_CLASSES
            );
            
            DataPoint[] trainingData = data[0];
            DataPoint[] testData = data[1];
            
            System.out.printf("Loaded %d training samples and %d test samples\n", 
                              trainingData.length, testData.length);
            
            // Create and train the neural network
            NeuralNetwork network = new NeuralNetwork(LAYER_SIZES);
            
            System.out.println("Starting training...");
            trainNetwork(network, trainingData);
            
            // Evaluate the network
            System.out.println("Evaluating network on test data...");
            double accuracy = evaluateNetwork(network, testData);
            System.out.printf("Test accuracy: %.2f%%\n", accuracy * 100);
            
        } catch (IOException e) {
            System.err.println("Error loading EMNIST data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Trains the neural network using mini-batch gradient descent
     * Steps for each epoch:
     * 1. Divides training data into mini-batches
     * 2. Trains the network on each batch
     * 3. Calculates and reports the average cost
     * This approach balances training speed and stability
     */
    private static void trainNetwork(NeuralNetwork network, DataPoint[] trainingData) {
        int numBatches = (trainingData.length + BATCH_SIZE - 1) / BATCH_SIZE;
        
        for (int epoch = 0; epoch < EPOCHS; epoch++) {
            System.out.printf("Epoch %d/%d\n", epoch + 1, EPOCHS);
            double totalCost = 0;
            
            for (int batch = 0; batch < numBatches; batch++) {
                int startIdx = batch * BATCH_SIZE;
                int endIdx = Math.min(startIdx + BATCH_SIZE, trainingData.length);
                int batchSize = endIdx - startIdx;
                
                // Create a batch of the appropriate size
                DataPoint[] batchData = new DataPoint[batchSize];
                System.arraycopy(trainingData, startIdx, batchData, 0, batchSize);
                
                // Train on this batch
                network.Learn(batchData, LEARNING_RATE);
                
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
        }
    }
    
    /**
     * Evaluates the network on test data and returns accuracy
     * Process for evaluation:
     * 1. Runs each test example through the network
     * 2. Compares the network's prediction to the true label
     * 3. Calculates percentage of correct predictions
     * This provides a measure of how well the network generalizes
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
} 