import java.io.*;
import java.util.*;

public class a2 
{
    /**
     * This function reads a CSV file and turns it into a 2D array of doubles.
     * It skips the header and looks for missing values (represented by "?").
     * If it finds a missing value, it'll replace it with Double.NaN (Not a Number).
     */
    public static double[][] readCSVFile(String path) throws IOException 
    {
        // Open the file for reading
        BufferedReader br = new BufferedReader(new FileReader(path));
        List<double[]> data = new ArrayList<>(); // This will store all the rows of data
        br.readLine(); // Skip the header line
        String dataSegment = br.readLine(); // Start reading the data

        // Keep reading until you hit the end of the file
        while (dataSegment != null) 
        {
            // Split the line by commas to get each feature's value
            String[] dataVal = dataSegment.split(",");
            double[] dataRow = new double[dataVal.length]; // This will store one row of data

            // Loop through each value in the row
            for (int i = 0; i < dataVal.length; i++) 
            {
                // If the value is "?", mark it as missing (NaN), otherwise convert it to a
                // double
                dataRow[i] = dataVal[i].equals("?") ? Double.NaN : Double.parseDouble(dataVal[i]);
            }
            data.add(dataRow); // Add this row to the list
            dataSegment = br.readLine(); // Move on to the next line
        }

        br.close(); // Make sure to close the file when done
        // Return the list of rows as a 2D array
        return data.toArray(new double[0][0]);
    }

    /**
     * This method fills in the missing values (NaN) by calculating the mean for
     * each feature (column).
     * If a value is missing in a row, it'll be replaced with the average of all of
     * the other non-missing values in that column.
     */
    public static double[][] meanImputation(double[][] data) 
    {
        int features = data[0].length; // The number of columns (features)
        double[] means = new double[features]; // This will hold the mean for each column
        int[] counts = new int[features]; // This keeps track of how many non-missing values we have for each column

        // Calculate the mean for each column
        for (int j = 0; j < features; j++) 
        {
            double sum = 0;
            for (double[] row : data) 
            {
                // Add up all the non-missing values
                if (!Double.isNaN(row[j])) 
                {
                    sum += row[j];
                    counts[j]++; // Keep track of how many values we used for the mean
                }
            }
            means[j] = sum / counts[j];
        }

        // Create a new dataset where missing values are replaced by the mean
        double[][] imputedData = new double[data.length][features];
        for (int i = 0; i < data.length; i++) 
        {
            for (int j = 0; j < features; j++) 
            {
                // If the value is missing, use the mean; otherwise, keep the original value
                imputedData[i][j] = Double.isNaN(data[i][j]) ? means[j] : data[i][j];
            }
        }

        return imputedData; // Return the filled dataset
    }

    /**
     * This function calculates the Manhattan distance between two rows (or
     * objects).
     * Manhattan distance is basically the sum of the absolute differences between
     * the two rows.
     * If one of the values is missing, we'll make it further by adding 1 to the
     * distance.
     */
    public static double calculateManhattanDistance(double[] obj1, double[] obj2) 
    {
        double distance = 0; // Initialize the distance
        for (int i = 0; i < obj1.length; i++) 
        {
            // If both values are present, calculate their absolute difference
            if (!Double.isNaN(obj1[i]) && !Double.isNaN(obj2[i])) 
            {
                distance += Math.abs(obj1[i] - obj2[i]);
            } else 
            {
                // If one of the values is missing, add a 1 to the distance
                distance += 1;
            }
        }
        return distance; // Return the calculated Manhattan distance
    }

    /**
     * This method performs Hot Deck Imputation to fill in missing values.
     * For each row with missing data, it finds the most similar row (using
     * Manhattan distance) that has no missing data.
     * The missing values in the row are replaced with values from the most similar
     * row.
     */
    public static double[][] hotDeckImputation(double[][] data) 
    {
        int rows = data.length; // Number of rows in the dataset
        int features = data[0].length; // Number of columns (features)
        double[][] imputedData = new double[rows][features]; // This will store the imputed dataset

        // Loop through each row in the dataset
        for (int i = 0; i < rows; i++) 
        {
            // Copy the row as is
            System.arraycopy(data[i], 0, imputedData[i], 0, features);

            // Check if the row contains any missing data
            boolean missingValue = false;
            for (double value : data[i]) 
            {
                if (Double.isNaN(value)) 
                {
                    missingValue = true; // This row has missing data
                    break;
                }
            }

            // If there are missing values, find the most similar row with no missing values
            if (missingValue) 
            {
                double[] mostSimilarRow = null; // This will hold the most similar row
                double minDistance = Double.MAX_VALUE; // Start with the maximum possible distance

                // Compare this row to every other row
                for (int j = 0; j < rows; j++) 
                {
                    // We skip comparing a row to itself and rows that have missing values
                    if (i != j && !rowHasMissingValues(data[j])) 
                    {
                        double distance = calculateManhattanDistance(data[i], data[j]); // Calculate the distance
                        if (distance < minDistance) 
                        {
                            minDistance = distance;
                            mostSimilarRow = data[j]; // Save the most similar row
                        }
                    }
                }

                // Replace the missing values with the values from the most similar row
                if (mostSimilarRow != null) 
                {
                    for (int k = 0; k < features; k++) 
                    {
                        // If there's a missing value, impute it with the value from the similar row
                        if (Double.isNaN(data[i][k])) 
                        {
                            imputedData[i][k] = mostSimilarRow[k];
                        }
                    }
                }
            }
        }

        return imputedData; // Return the imputed dataset
    }

    /**
     * Helper method that checks if a row has any missing values.
     */
    public static boolean rowHasMissingValues(double[] row) 
    {
        // Loop through the row to see if any value is missing
        for (double value : row) 
        {
            if (Double.isNaN(value)) 
            {
                return true; // If a value is missing, return true
            }
        }
        return false; // Otherwise, return false
    }

    /**
     * This function calculates the Mean Absolute Error (MAE) between two datasets:
     * the imputed one and the complete one.
     * It measures how far the imputed values are from the true values in the
     * complete dataset.
     */
    public static double calculateMAE(double[][] imputedData, double[][] completeData) 
    {
        double sumError = 0; // This will hold the sum of the absolute errors
        int count = 0; // This will keep track of how many values we're comparing

        // Loop through each value in the datasets
        for (int i = 0; i < imputedData.length; i++) 
        {
            for (int j = 0; j < imputedData[0].length; j++) 
            {
                // We only care about comparing values that aren't missing
                if (!Double.isNaN(imputedData[i][j])) 
                {
                    // Add the absolute difference between the imputed value and the complete value
                    sumError += Math.abs(imputedData[i][j] - completeData[i][j]);
                    count++; // Keep track of how many comparisons we make
                }
            }
        }

        // Return the mean of the absolute differences (total error divided by number of
        // comparisons)
        return sumError / count;
    }

    /**
     * This method saves a 2D array into a CSV file.
     */
    public static void saveCSVFile(String fileName, double[][] data) throws IOException 
    {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        for (double[] row : data) 
        {
            for (int i = 0; i < row.length; i++) 
            {
                writer.write(Double.isNaN(row[i]) ? "NaN" : Double.toString(row[i]));
                if (i < row.length - 1) 
                {
                    writer.write(",");
                }
            }
            writer.newLine();
        }
        writer.close();
    }

    /**
     * The main function
     */
    public static void main(String[] args) throws IOException
    {
        // Load the datasets
        double[][] complete = readCSVFile("lib" + File.separator + "dataset_complete.csv");
        double[][] missing01 = readCSVFile("lib" + File.separator + "dataset_missing01.csv");
        double[][] missing10 = readCSVFile("lib" + File.separator + "dataset_missing10.csv");

        String vNumber = "V12345678"; // Replace with your actual V number

        // Mean imputation for 1% missing data
        long startTime = System.currentTimeMillis();
        double[][] imputedMissing01Mean = meanImputation(missing01);
        long endTime = System.currentTimeMillis();
        System.out.println("MAE_01_mean = " + calculateMAE(imputedMissing01Mean, complete));
        System.out.println("Runtime_01_mean = " + (endTime - startTime) + " ms");
        saveCSVFile("lib" + File.separator + vNumber + "_missing01_imputed_mean.csv", imputedMissing01Mean);

        // Mean imputation for 10% missing data
        startTime = System.currentTimeMillis();
        double[][] imputedMissing10Mean = meanImputation(missing10);
        endTime = System.currentTimeMillis();
        System.out.println("MAE_10_mean = " + calculateMAE(imputedMissing10Mean, complete));
        System.out.println("Runtime_10_mean = " + (endTime - startTime) + " ms");
        saveCSVFile("lib" + File.separator + vNumber + "_missing10_imputed_mean.csv", imputedMissing10Mean);

        // Hot deck imputation for 1% missing data
        startTime = System.currentTimeMillis();
        double[][] imputedMissing01HotDeck = hotDeckImputation(missing01);
        endTime = System.currentTimeMillis();
        System.out.println("MAE_01_hd = " + calculateMAE(imputedMissing01HotDeck, complete));
        System.out.println("Runtime_01_hd = " + (endTime - startTime) + " ms");
        saveCSVFile("lib" + File.separator + vNumber + "_missing01_imputed_hd.csv", imputedMissing01HotDeck);

        // Hot deck imputation for 10% missing data
        startTime = System.currentTimeMillis();
        double[][] imputedMissing10HotDeck = hotDeckImputation(missing10);
        endTime = System.currentTimeMillis();
        System.out.println("MAE_10_hd = " + calculateMAE(imputedMissing10HotDeck, complete));
        System.out.println("Runtime_10_hd = " + (endTime - startTime) + " ms");
        saveCSVFile("lib" + File.separator + vNumber + "_missing10_imputed_hd.csv", imputedMissing10HotDeck);
    }
}
