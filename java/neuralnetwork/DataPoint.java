package neuralnetwork;

/**
 * Represents a single data point for neural network training
 * Enhanced to handle EMNIST image data (28x28 pixel images)
 */
public class DataPoint {
    // The flattened and normalized input values to feed into the neural network
    public double[] inputs;
    
    // The expected output values for this training example (one-hot encoded)
    public double[] expectedOutputs;
    
    // The original label for this example
    public int label;
    
    /**
     * Creates a new data point for neural network training
     * Simply stores the provided inputs and expected outputs
     * Used when data is already pre-processed
     */
    public DataPoint(double[] inputs, double[] expectedOutputs) {
        this.inputs = inputs;
        this.expectedOutputs = expectedOutputs;
    }
    
    /**
     * Creates a new data point from an EMNIST image
     * Performs multiple transformations:
     * 1. Flattens 2D array of pixels into 1D array
     * 2. Normalizes pixel values from 0-255 to 0-1 range
     * 3. Creates one-hot encoded output from the label
     */
    public DataPoint(int[][] pixels, int label, int numClasses) {
        this.label = label;
        
        // Convert 2D pixel array to 1D input array (flattening)
        inputs = new double[pixels.length * pixels[0].length];
        int index = 0;
        for (int i = 0; i < pixels.length; i++) {
            for (int j = 0; j < pixels[0].length; j++) {
                // Normalize pixel values from 0-255 to 0-1 range
                inputs[index++] = pixels[i][j] / 255.0;
            }
        }
        
        // Create one-hot encoded outputs (1 for correct class, 0 for others)
        expectedOutputs = new double[numClasses];
        for (int i = 0; i < numClasses; i++) {
            expectedOutputs[i] = (i == label) ? 1.0 : 0.0;
        }
    }
    
    /**
     * Static utility method to convert an image pixel array and label to a DataPoint
     * Provides a convenient way to create DataPoints from pixel arrays
     * Especially useful when loading data from external sources
     */
    public static DataPoint fromPixelArray(int[][] pixels, int label, int numClasses) {
        return new DataPoint(pixels, label, numClasses);
    }
    
    /**
     * Static utility method to create a batch of DataPoints from pixel arrays and labels
     * Batch processing is more efficient than creating individual DataPoints
     * Used when loading multiple examples in one operation
     */
    public static DataPoint[] fromPixelArrays(int[][][] pixelArrays, int[] labels, int numClasses) {
        DataPoint[] dataPoints = new DataPoint[pixelArrays.length];
        for (int i = 0; i < pixelArrays.length; i++) {
            dataPoints[i] = fromPixelArray(pixelArrays[i], labels[i], numClasses);
        }
        return dataPoints;
    }
} 