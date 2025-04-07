package neuralnetwork;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for loading EMNIST data from CSV files
 * Applies necessary transformations to ensure correct image orientation
 */
public class DataLoader {
    // Standard size for EMNIST images (28x28 pixels)
    private static final int IMAGE_SIZE = 28;
    
    /**
     * Loads EMNIST data from a CSV file with proper orientation
     * For each image:
     * 1. Reads the label (first value in CSV row)
     * 2. Applies transformations for correct orientation
     * 3. Creates a DataPoint with normalized pixel values
     */
    public static DataPoint[] loadEMNISTData(String csvFilePath, int maxSamples, int numClasses) throws IOException {
        List<DataPoint> dataPoints = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            int count = 0;
            
            while ((line = br.readLine()) != null && (maxSamples <= 0 || count < maxSamples)) {
                String[] values = line.split(",");
                
                // First value is the label
                int label = Integer.parseInt(values[0]);
                
                // Create and populate the 2D pixel array with proper orientation
                int[][] pixels = new int[IMAGE_SIZE][IMAGE_SIZE];
                for (int i = 0; i < IMAGE_SIZE; i++) {
                    for (int j = 0; j < IMAGE_SIZE; j++) {
                        // Apply the transformation for correct orientation
                        // This matches the transformation in EMNISTBrowserViewer
                        int pixelValue = Integer.parseInt(values[i * IMAGE_SIZE + (IMAGE_SIZE - 1 - j) + 1]);
                        pixels[IMAGE_SIZE - 1 - j][i] = pixelValue;
                    }
                }
                
                // Create a DataPoint with the correctly oriented image
                dataPoints.add(DataPoint.fromPixelArray(pixels, label, numClasses));
                count++;
            }
        }
        
        return dataPoints.toArray(new DataPoint[0]);
    }
    
    /**
     * Splits data into training and testing sets
     * Divides data according to the provided ratio:
     * 1. First portion becomes training data
     * 2. Remaining portion becomes testing data
     * This allows for easy cross-validation setup
     */
    public static DataPoint[][] splitTrainTest(DataPoint[] data, double trainingRatio) {
        int trainingSize = (int)(data.length * trainingRatio);
        int testingSize = data.length - trainingSize;
        
        DataPoint[] trainingData = new DataPoint[trainingSize];
        DataPoint[] testingData = new DataPoint[testingSize];
        
        // Copy data into training set
        System.arraycopy(data, 0, trainingData, 0, trainingSize);
        
        // Copy remaining data into testing set
        System.arraycopy(data, trainingSize, testingData, 0, testingSize);
        
        return new DataPoint[][] { trainingData, testingData };
    }
    
    /**
     * Helper method to load both training and testing data
     * Loads data from separate files for training and testing:
     * 1. Loads training data with specified sample limit
     * 2. Loads testing data with specified sample limit
     * 3. Returns both datasets as a 2D array for convenience
     */
    public static DataPoint[][] loadTrainTestData(
            String trainFilePath, 
            String testFilePath, 
            int maxTrainSamples, 
            int maxTestSamples, 
            int numClasses) throws IOException {
        
        DataPoint[] trainData = loadEMNISTData(trainFilePath, maxTrainSamples, numClasses);
        DataPoint[] testData = loadEMNISTData(testFilePath, maxTestSamples, numClasses);
        
        return new DataPoint[][] { trainData, testData };
    }
} 