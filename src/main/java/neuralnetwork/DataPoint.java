package neuralnetwork;

/**
 * Represents a single data point for neural network training
 * Contains input values and their corresponding expected outputs
 */
public class DataPoint {
    // The input values to feed into the neural network
    public double[] inputs;
    
    // The expected output values for this training example
    public double[] expectedOutputs;
    
    /**
     * Creates a new data point for neural network training
     * @param inputs The input values to feed into the network
     * @param expectedOutputs The expected output values (correct answers)
     */
    public DataPoint(double[] inputs, double[] expectedOutputs) {
        this.inputs = inputs;
        this.expectedOutputs = expectedOutputs;
    }
} 