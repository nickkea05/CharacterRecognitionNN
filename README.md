# Character Recognition Neural Network

A simple Java implementation of a neural network for EMNIST character recognition with a drawing interface. This project provides a practical demonstration of neural network concepts by recognizing handwritten digits and characters (later).

## Features

- **Fully-connected Neural Network**: Implementation of a multi-layer network trained on the EMNIST dataset
- **Interactive Drawing Interface**: Draw digits with your mouse and see real-time predictions
- **Live Training Visualization**: Watch predictions improve as the network trains
- **Customizable Architecture**: Easily modify network parameters and architecture

![Application Demo](docs/screenshots/demo.gif)

## About This Project

This project is my first experience with machine learning and neural networks. Built entirely from scratch in Java without using any ML libraries, it has been extremely helpful with understanding the fundamentals of neural networks.

Current performance:
- Up to 95% accuracy on digit recognition
- Working on optimizing training parameters and network structure
- Challenges with certain digits (7s and 9s) that have similar features

I'm continuing to refine the network architecture and training approach to improve accuracy while maintaining reasonable training times.

## How It Works

This project implements a very simple neural network from scratch in Java. It uses:

- **Backpropagation** for learning through gradient descent
- **Sigmoid activation** functions for introducing non-linearity
- **EMNIST dataset** for training digit and character recognition
- **Swing UI** for a simple interactive interface

## Future Improvements

- Improve accuracy for commonly confused digits (5s and 9s)
- Add support for alphabetic characters
- Implement convolutional layers for better performance
- Utilize GPU acceleration for faster training

## Acknowledgments

- The EMNIST dataset for providing training data
- Inspired by [Sebastian Lague's Neural Network Experiments](https://github.com/SebLague/Neural-Network-Experiments) and his [video tutorial](https://www.youtube.com/watch?v=hfMk-kjRv4c)


