package ru.hollowhorizon.hc.client.utils;

import java.util.ArrayList;
import java.util.List;

public class NeuralNetwork {

    Matrix weights_ih, weights_ho, bias_h, bias_o;
    double l_rate = 0.01;
    boolean useMultiThreading = false;

    public NeuralNetwork(int i, int h, int o) {
        weights_ih = new Matrix(h, i);
        weights_ho = new Matrix(o, h);

        bias_h = new Matrix(h, 1);
        bias_o = new Matrix(o, 1);

    }

    public NeuralNetwork(int i, int h, int o, boolean useMultiThreading) {
        weights_ih = new Matrix(h, i);
        weights_ho = new Matrix(o, h);

        bias_h = new Matrix(h, 1);
        bias_o = new Matrix(o, 1);

        this.useMultiThreading = useMultiThreading;

    }

    public NeuralNetwork(int i, int h, int o, double l_rate) {
        weights_ih = new Matrix(h, i);
        weights_ho = new Matrix(o, h);

        bias_h = new Matrix(h, 1);
        bias_o = new Matrix(o, 1);

        this.l_rate = l_rate;

    }

    public NeuralNetwork(int i, int h, int o, double l_rate, boolean useMultiThreading) {
        weights_ih = new Matrix(h, i);
        weights_ho = new Matrix(o, h);

        bias_h = new Matrix(h, 1);
        bias_o = new Matrix(o, 1);

        this.l_rate = l_rate;
        this.useMultiThreading = useMultiThreading;

    }

    public List<Double> predict(double[] X) {
        Matrix input = Matrix.fromArray(X);
        Matrix hidden = Matrix.multiply(weights_ih, input, useMultiThreading);
        hidden.add(bias_h);
        hidden.sigmoid();

        Matrix output = Matrix.multiply(weights_ho, hidden, useMultiThreading);
        output.add(bias_o);
        output.sigmoid();

        return output.toArray();
    }

    public void fit(double[][] X, double[][] Y, int epochs) {
        for (int i = 0; i < epochs; i++) {
            int sampleN = (int) (Math.random() * X.length);
            this.train(X[sampleN], Y[sampleN], false);
        }
    }

    public void fit(double[][] X, double[][] Y, int epochs, int verbose) {
        switch (verbose) {

            case 0: {
                System.out.println("Staring training with " + epochs + " epochs");
                long start = System.currentTimeMillis();
                for (int i = 0; i < epochs; i++) {
                    int sampleN = (int) (Math.random() * X.length);
                    this.train(X[sampleN], Y[sampleN], i + 1 == epochs);
                }
                long end = System.currentTimeMillis();
                long elapsedTime = end - start;
                System.out.println("Training took : " + (elapsedTime / 1000) + "s");

                break;
            }

            case 1: {
                System.out.println("Staring training with " + epochs + " epochs");
                long start = System.currentTimeMillis();
                for (int i = 0; i < epochs; i++) {
                    System.out.println("Epoch: " + (i + 1));
                    int sampleN = (int) (Math.random() * X.length);
                    this.train(X[sampleN], Y[sampleN], true);
                }
                long end = System.currentTimeMillis();
                long elapsedTime = end - start;
                System.out.println("Training took : " + (elapsedTime / 1000) + "s");

                break;
            }
        }

    }

    public void train(double[] X, double[] Y, Boolean showLoss) {
        Matrix input = Matrix.fromArray(X);
        Matrix hidden = Matrix.multiply(weights_ih, input, useMultiThreading);
        hidden.add(bias_h);
        hidden.sigmoid();

        Matrix output = Matrix.multiply(weights_ho, hidden, useMultiThreading);
        output.add(bias_o);
        output.sigmoid();

        Matrix target = Matrix.fromArray(Y);

        Matrix error = Matrix.subtract(target, output);
        Matrix gradient = output.dsigmoid();
        gradient.multiply(error);
        gradient.multiply(l_rate);

        if (showLoss)
            printLoss(error);

        Matrix hidden_T = Matrix.transpose(hidden);
        Matrix who_delta = Matrix.multiply(gradient, hidden_T, useMultiThreading);

        weights_ho.add(who_delta);
        bias_o.add(gradient);

        Matrix who_T = Matrix.transpose(weights_ho);
        Matrix hidden_errors = Matrix.multiply(who_T, error, useMultiThreading);

        Matrix h_gradient = hidden.dsigmoid();
        h_gradient.multiply(hidden_errors);
        h_gradient.multiply(l_rate);

        Matrix i_T = Matrix.transpose(input);
        Matrix wih_delta = Matrix.multiply(h_gradient, i_T, useMultiThreading);

        weights_ih.add(wih_delta);
        bias_h.add(h_gradient);

    }

    private void printLoss(Matrix error) {
        double avg = 0.0;

        for (int i = 0; i < error.rows; i++) {
            for (int j = 0; j < error.cols; j++) {
                avg += error.data[i][j];
            }
        }

        System.out.print("Average Error: " + avg + "\n");
    }

}

class Matrix {
    double[][] data;
    int rows, cols;

    public Matrix(int rows, int cols) {
        data = new double[rows][cols];
        this.rows = rows;
        this.cols = cols;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                data[i][j] = Math.random() * 2 - 1;
            }
        }
    }

    public void print() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(this.data[i][j] + " ");
            }
            System.out.println();
        }
    }

    public void add(int scaler) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                this.data[i][j] += scaler;
            }

        }
    }

    public void add(Matrix m) {
        if (cols != m.cols || rows != m.rows) {
            System.out.println("Shape Mismatch");
            return;
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                this.data[i][j] += m.data[i][j];
            }
        }
    }

    public static Matrix fromArray(double[] x) {
        Matrix temp = new Matrix(x.length, 1);
        for (int i = 0; i < x.length; i++)
            temp.data[i][0] = x[i];
        return temp;

    }

    public List<Double> toArray() {
        List<Double> temp = new ArrayList<Double>();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                temp.add(data[i][j]);
            }
        }
        return temp;
    }

    public static Matrix subtract(Matrix a, Matrix b) {
        Matrix temp = new Matrix(a.rows, a.cols);
        for (int i = 0; i < a.rows; i++) {
            for (int j = 0; j < a.cols; j++) {
                temp.data[i][j] = a.data[i][j] - b.data[i][j];
            }
        }
        return temp;
    }

    public static Matrix transpose(Matrix a) {
        Matrix temp = new Matrix(a.cols, a.rows);
        for (int i = 0; i < a.rows; i++) {
            for (int j = 0; j < a.cols; j++) {
                temp.data[j][i] = a.data[i][j];
            }
        }
        return temp;
    }

    // TODO Add Multi-threading
    public static Matrix multiply(Matrix a, Matrix b, boolean useMultiThreading) {
        Matrix temp = new Matrix(a.rows, b.cols);
        if (!useMultiThreading) {
            for (int i = 0; i < temp.rows; i++) {
                for (int j = 0; j < temp.cols; j++) {
                    double sum = 0;
                    for (int k = 0; k < a.cols; k++) {
                        sum += a.data[i][k] * b.data[k][j];
                    }
                    temp.data[i][j] = sum;
                }
            }

        } else {

            // return ParallelMatrixMultiplication.multiply(a, b);
            ParallelThreadsCreator.multiply(a, b, temp);

        }

        return temp;
    }

    public void multiply(Matrix a) {
        for (int i = 0; i < a.rows; i++) {
            for (int j = 0; j < a.cols; j++) {
                this.data[i][j] *= a.data[i][j];
            }
        }

    }

    public void multiply(double a) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                this.data[i][j] *= a;
            }
        }

    }

    public void sigmoid() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++)
                this.data[i][j] = 1 / (1 + Math.exp(-this.data[i][j]));
        }

    }

    public Matrix dsigmoid() {
        Matrix temp = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++)
                temp.data[i][j] = this.data[i][j] * (1 - this.data[i][j]);
        }
        return temp;

    }
}

class ParallelThreadsCreator {

    // creating 10 threads and waiting for them to complete then again repeat steps.
    public static void multiply(Matrix matrix1, Matrix matrix2, Matrix result) {
        List<Thread> threads = new ArrayList<>();
        int rows1 = matrix1.rows;
        for (int i = 0; i < rows1; i++) {
            RowMultiplyWorker task = new RowMultiplyWorker(result, matrix1, matrix2, i);
            Thread thread = new Thread(task);
            thread.start();
            threads.add(thread);
            if (threads.size() % 10 == 0) {
                waitForThreads(threads);
            }
        }
    }

    private static void waitForThreads(List<Thread> threads) {
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        threads.clear();
    }
}

class WorkerThread extends Thread {
    private int row;
    private int col;
    private int[][] A;
    private int[][] B;
    private int[][] C;

    public WorkerThread(int row, int col, int[][] A, int[][] B, int[][] C) {
        this.row = row;
        this.col = col;
        this.A = A;
        this.B = B;
        this.C = C;
    }

    public void run() {
        C[row][col] = (A[row][0] * B[0][col]) + (A[row][1] * B[1][col]);
    }
}

class RowMultiplyWorker implements Runnable {

    private final Matrix result;
    private Matrix matrix1;
    private Matrix matrix2;
    private final int row;

    public RowMultiplyWorker(Matrix result, Matrix matrix1, Matrix matrix2, int row) {
        this.result = result;
        this.matrix1 = matrix1;
        this.matrix2 = matrix2;
        this.row = row;
    }

    @Override
    public void run() {

        for (int i = 0; i < matrix2.data[0].length; i++) {
            result.data[row][i] = 0;
            for (int j = 0; j < matrix1.data[row].length; j++) {
                result.data[row][i] += matrix1.data[row][j] * matrix2.data[j][i];

            }

        }

    }

}
