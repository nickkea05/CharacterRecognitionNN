package neuralnetwork;

import java.util.Random;

public class Layer
{
    // Number of nodes in the input and output of this layer
    public int numNodesIn, numNodesOut;

    // Stores gradients for weights during backpropagation
    // Used to update weights during learning
    public double[][] costGradientW;
    
    // Stores gradients for biases during backpropagation
    // Used to update biases during learning
    public double[] costGradientB;
    
    // Stores the connection strengths between nodes
    // weights[i][j] is the connection from input node i to output node j
    public double[][] weights;

    // Stores the bias value for each output node
    // biases[i] is added to the weighted sum for output node i
    public double[] biases;
    
    // Stores the inputs to this layer (outputs from previous layer)
    // Needed for gradient calculations during backpropagation
    public double[] inputs;
    
    // Stores the weighted inputs (before activation function)
    // Needed for derivative calculations during backpropagation
    public double[] weightedInputs;
    
    // Stores the activations (outputs after activation function)
    // These are the actual outputs of this layer
    public double[] activations;

    // Create the layer
    public Layer(int numNodesIn, int numNodesOut) {
        costGradientW = new double[numNodesIn][numNodesOut];
        weights = new double[numNodesIn][numNodesOut];
        costGradientB = new double[numNodesOut];
        biases = new double[numNodesOut];
        this.numNodesOut = numNodesOut;
        this.numNodesIn = numNodesIn;
        InitializeRandomWeights();
    }

    /**
     * Processes inputs through this layer to produce outputs
     * For each output node:
     * 1. Takes inputs from previous layer
     * 2. Multiplies each by its connection weight
     * 3. Adds them together with the bias
     */
    public double[] CalculateOutputs(double[] inputs) {
        // Store inputs for use in backpropagation
        this.inputs = inputs;
        
        // Initialize arrays to store intermediate values
        weightedInputs = new double[numNodesOut];
        activations = new double[numNodesOut];

        for (int nodeOut = 0; nodeOut < numNodesOut; nodeOut++) {
            double weightedInput = biases[nodeOut];
            for (int nodeIn = 0; nodeIn < numNodesIn; nodeIn++) {
                weightedInput += inputs[nodeIn] * weights[nodeIn][nodeOut];
            }
            // Store both the weighted input (before activation) and the activation (after function)
            weightedInputs[nodeOut] = weightedInput;
            activations[nodeOut] = ActivationFunction(weightedInput);
        }

        return activations;
    }

    /**
     * Calculate node values for the output layer using the expected outputs
     * Node values are used in backpropagation to update weights and biases
     * Combines the partial derivatives of cost with respect to activation
     * and activation with respect to weighted input
     */
    public double[] CalculateOutputLayerNodeValues(double[] expectedOutputs) {
        double[] nodeValues = new double[expectedOutputs.length];
        
        for (int i = 0; i < nodeValues.length; i++) {
            // Evaluate partial derivs for current node: cost/activation & activation/weightedInput
            double costDerivative = NodeCostDerivative(activations[i], expectedOutputs[i]);
            double activationDerivative = ActivationFunctionDerivative(weightedInputs[i]);
            nodeValues[i] = activationDerivative * costDerivative;
        }

        return nodeValues;
    }

    public double[] CalculateHiddenLayerNodeValues(Layer oldLayer, double[] oldNodeValues) {
        double[] newNodeValues = new double[numNodesOut];

        for (int newNodeIndx = 0; newNodeIndx < newNodeValues.length; newNodeIndx++) {
            double newNodeValue = 0;
            for (int oldNodeIndx = 0; oldNodeIndx < oldNodeValues.length; oldNodeIndx++) {
                // Get the partial deriv of the weight input with repsect to input
                double weightInpDeriv = oldLayer.weights[newNodeIndx][oldNodeIndx];
                newNodeValue += weightInpDeriv * oldNodeValues[oldNodeIndx];
            }
            newNodeValue *= ActivationFunctionDerivative(weightedInputs[newNodeIndx]);
            newNodeValues[newNodeIndx] = newNodeValue;
        }

        return newNodeValues;
    }

    // sigmoid activation function (ReLu is other alternative to create non-linearity)
    public double ActivationFunction(double weightedInput) {
        return 1 / (1 + Math.exp(-weightedInput));
    }

    //Partial derivative of the activation function with respect to the weighted input
    public double ActivationFunctionDerivative(double weightedInput) {
        double sigmoid = ActivationFunction(weightedInput);
        return sigmoid * (1 - sigmoid);
    }

    /**
     * Calculates the error/cost for a single node by comparing output with expected value
     * Uses squared error (output - expectedOutput)² which:
     * 1. Always gives a positive value
     * 2. Penalizes larger errors more heavily
     * 3. Has a simple derivative for gradient descent
     */
    public double NodeCost(double output, double expectedOutput) {
        double error = output - expectedOutput;
        return error * error;
    }

    // Partial derivative of the cost function with respect to the output
    public double NodeCostDerivative(double output, double expectedOutput) {
        return 2 * (output - expectedOutput);
    }
    
    /**
     * Update the weights and biases based on the cost gradients (gradient descent)
     * Reduces the cost/error by moving in the direction opposite to the gradient
     * The learn rate controls how big of a step to take during learning
     */
    public void ApplyGradients(double learnRate) {
        for (int nodeOut = 0; nodeOut < numNodesOut; nodeOut++) {
            biases[nodeOut] -= costGradientB[nodeOut] * learnRate;
            for (int nodeIn = 0; nodeIn < numNodesIn; nodeIn++) {       
                weights[nodeIn][nodeOut] -= costGradientW[nodeIn][nodeOut] * learnRate;
            }
        }
    }

    /**
     * Updates the gradients for weights and biases using node values from backpropagation
     * The gradients are accumulated (added) for batch learning
     * @param nodeValues The node values calculated during backpropagation
     */
    public void UpdateGradients(double[] nodeValues) {
        for (int nodeOut = 0; nodeOut < numNodesOut; nodeOut++) {
            for (int nodeIn = 0; nodeIn < numNodesIn; nodeIn++) {
                // Eval the partial deriv of cost with respect to the weight
                double derivCostOvrWeight = inputs[nodeIn] * nodeValues[nodeOut];

                // The costGradientW array stores the partial dervs for each weight. The deriv is being added to the
                // array because we want to calculate the average gradient across all training samples.
                costGradientW[nodeIn][nodeOut] += derivCostOvrWeight;
            }

            // Eval the partial deriv of cost with respect to the bias
            double derivCostOvrBias = 1 * nodeValues[nodeOut];
            costGradientB[nodeOut] += derivCostOvrBias;
        }
    }
    
    /**
     * Initialize weights to small random values to break symmetry
     * Allows neurons to learn different features during training
     */
    public void InitializeRandomWeights() {
        Random random = new Random();
        for (int i = 0; i < numNodesIn; i++) {
            for (int j = 0; j < numNodesOut; j++) {
                double randomValue = random.nextDouble() * 2 - 1;
                weights[i][j] = randomValue / Math.sqrt(numNodesIn);
            }
        }   
    }
} 