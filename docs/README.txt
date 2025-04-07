Neural Network Drawing Recognition System
=======================================

This project implements an interactive neural network system that can recognize handwritten digits and letters drawn by users on a grid interface. The system provides a simple and intuitive way to test the capabilities of a custom-built neural network.

Features:
---------
- Interactive drawing grid for input
- Real-time recognition of drawn digits and letters
- Visual feedback on recognition confidence
- Training capabilities with user-drawn examples
- Performance metrics and visualization tools

How to Use:
----------
1. Launch the application
2. Draw a digit (0-9) or letter (A-Z) on the grid
3. Click "Recognize" to see the network's prediction
4. View the confidence level of the prediction
5. Clear the grid to draw a new input

Technical Details:
----------------
The system uses a custom-built neural network with:
- Input layer processing 28x28 pixel grid
- Two hidden layers for feature extraction
- Output layer for classification
- Real-time processing and visualization

Requirements:
------------
- Python 3.8 or higher
- NumPy for numerical computations
- Matplotlib for visualization
- Web framework (Flask/Streamlit) for the interface

Getting Started:
---------------
1. Install the required dependencies
2. Run the main application
3. Open the web interface in your browser
4. Start drawing and testing the network!

Note: The neural network improves its accuracy as it processes more examples. You can contribute to its training by drawing examples and providing feedback on the predictions.

