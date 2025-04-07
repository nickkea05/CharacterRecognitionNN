package neuralnetwork;

public class NeuralNetwork
{
    // Array of layers that make up the network
    // Each layer connects to the next one in sequence
    Layer[] layers;

    /**
     * Creates a neural network with the specified layer sizes
     * @param layerSizes Array where each number represents how many nodes are in that layer
     * Example: [3, 4, 2] creates a network with:
     * - 3 input nodes
     * - 4 nodes in hidden layer
     * - 2 output nodes
     */
    public NeuralNetwork(int[] layerSizes) {
        layers = new Layer[layerSizes.length - 1];
        for (int i = 0; i < layers.length; i++) {
            layers[i] = new Layer(layerSizes[i], layerSizes[i + 1]);
        }
    }

    /**
     * Processes input values through all layers to produce final output
     * The output of each layer becomes the input for the next layer
     */
    public double[] CalculateOutputs(double[] inputs) {
        for (Layer layer : layers) {
            inputs = layer.CalculateOutputs(inputs);
        }
        return inputs;
    }

    /**
     * Classifies input by finding which output node has the highest value
     * Returns the index of the most activated output node
     */
    public int Classify(double[] inputs) {
        double[] outputs = CalculateOutputs(inputs);
        return IndexOfMaxValue(outputs);
    }

    /**
     * Helper method to find the index of the highest value in an array
     * Used to determine which output node is most activated
     */
    private int IndexOfMaxValue(double[] array) {
        int maxIndex = 0;
        double maxValue = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxValue) {
                maxValue = array[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    /**
     * Calculates the total cost/error for a single data point
     * Sums the cost across all output nodes to get total error
     * Lower cost means better predictions
     */
    double Cost(DataPoint dataPoint) {
        double[] outputs = CalculateOutputs(dataPoint.inputs);
        Layer outputLayer = layers[layers.length - 1];
        double totalCost = 0;

        for (int i = 0; i < outputs.length; i++) {
            totalCost += outputLayer.NodeCost(outputs[i], dataPoint.expectedOutputs[i]);
        }

        return totalCost;
    }

    /**
     * Calculates the average cost across a batch of data points
     * This represents how well the network performs overall
     * Used to track learning progress and evaluate model quality
     */
    double Cost(DataPoint[] data) {
        double totalCost = 0;

        for (DataPoint dataPoint : data) {
            totalCost += Cost(dataPoint);
        }

        return totalCost / data.length;
    }
    
    /**
     * Trains the neural network using backpropagation
     * Updates weights and biases to minimize prediction error
     * @param trainingData Array of input/expected output pairs
     * @param learnRate Learning rate controls size of weight/bias adjustments
     */
    public void Learn(DataPoint[] trainingData, double learnRate) {
        // Uses backpropagation to calculate the cost gradient for each layer
        // This is done for each data point in the training set, then the gradients are added up
        // and the average gradient is used to update the weights and biases.
        for (DataPoint dataPoint : trainingData) {
            UpdateAllGradients(dataPoint);
        }
        
        // Gradient descent: Update weights and biases based on the average gradient across all data points
        ApplyAllGradients(learnRate / trainingData.length);

        // Clear the gradients for the next iteration
        ClearAllGradients();
    }

    /**
     * Apply the calculated gradients to all layers
     * @param learnRate Learning rate to control weight/bias update magnitude
     */
    void ApplyAllGradients(double learnRate)
    {
        for (Layer layer : layers)
        {
            layer.ApplyGradients(learnRate);
        }
    }

    /**
     * Clears all gradients in preparation for the next batch of training data
     * This prevents accumulated gradients from affecting future training
     */
    void ClearAllGradients() {
        for (Layer layer : layers) {
            // Reset all weight gradients to zero
            for (int nodeIn = 0; nodeIn < layer.numNodesIn; nodeIn++) {
                for (int nodeOut = 0; nodeOut < layer.numNodesOut; nodeOut++) {
                    layer.costGradientW[nodeIn][nodeOut] = 0;
                }
            }
            
            // Reset all bias gradients to zero
            for (int nodeOut = 0; nodeOut < layer.numNodesOut; nodeOut++) {
                layer.costGradientB[nodeOut] = 0;
            }
        }
    }

    /**
     * Updates gradients for all layers using backpropagation
     * @param dataPoint The training example to calculate gradients for
     */
    void UpdateAllGradients(DataPoint dataPoint) {
        // Run the inputs through the network. During this process, the cost gradient for each node is calculated
        // and each layer will store the values needed to update the weights and biases.
        CalculateOutputs(dataPoint.inputs);

        //BACKPROPAGATION TECHNIQUE
        // 1. Calculate the node values for the output layer
        // 2. Update the gradients for the output layer
        // 3. Calculate the node values for the hidden layer
        // 4. Update the gradients for the hidden layer (repeat for each hidden layer)

        // Update gradients for the output layer
        Layer outputLayer = layers[layers.length - 1];
        double[] nodeValues = outputLayer.CalculateOutputLayerNodeValues(dataPoint.expectedOutputs);
        outputLayer.UpdateGradients(nodeValues);

        // Update gradients for the hidden layer
        for (int i = layers.length - 2; i >= 0; i--) {
            Layer hiddenLayer = layers[i];
            nodeValues = hiddenLayer.CalculateHiddenLayerNodeValues(layers[i + 1], nodeValues);
            hiddenLayer.UpdateGradients(nodeValues);
        }
    }
} 