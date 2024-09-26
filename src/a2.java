import java.io.*;
import java.util.*;

public class a2 
{
    public static double[][] readCSVFile(String path) throws IOException 
    {
        BufferedReader br = new BufferedReader(new FileReader(path));
        List<double[]> data = new ArrayList<>();
        br.readLine(); // Skip header
        String dataSegment = br.readLine();

        while (dataSegment != null) 
        {
            String[] dataVal = dataSegment.split(",");
            double[] dataRow = new double[dataVal.length];

            for (int i = 0; i < dataVal.length; i++) 
            {
                dataRow[i] = dataVal[i].equals("?") ? Double.NaN : Double.parseDouble(dataVal[i]);
            }
            data.add(dataRow);
            dataSegment = br.readLine();
        }

        br.close();
        return data.toArray(new double[0][0]);
    }

    public static double[][] meanImputation(double[][] data) 
    {
        int features = data[0].length;
        double[] means = new double[features];
        int[] counts = new int[features];

        for (int j = 0; j < features; j++) 
        {
            double sum = 0;
            for (double[] row : data) 
            {
                if (!Double.isNaN(row[j])) 
                {
                    sum += row[j];
                    counts[j]++;
                }
            }
            means[j] = sum / counts[j];
        }

        double[][] imputedData = new double[data.length][features];
        for (int i = 0; i < data.length; i++) 
        {
            for (int j = 0; j < features; j++) 
            {
                imputedData[i][j] = Double.isNaN(data[i][j]) ? means[j] : data[i][j];
            }
        }

        return imputedData;
    }

    public static double calculateManhattanDistance(double[] obj1, double[] obj2) 
    {
        double distance = 0;
        for (int i = 0; i < obj1.length; i++) 
        {
            if (!Double.isNaN(obj1[i]) && !Double.isNaN(obj2[i])) 
            {
                distance += Math.abs(obj1[i] - obj2[i]);
            } 
            else 
            {
                distance += 1; // Penalize missing values
            }
        }
        return distance;
    }

    public static double[][] hotDeckImputation(double[][] data) 
    {
        int rows = data.length;
        int features = data[0].length;
        double[][] imputedData = new double[rows][features];

        for (int i = 0; i < rows; i++) 
        {
            System.arraycopy(data[i], 0, imputedData[i], 0, features); // Copy existing values

            boolean missingValue = false;
            for (double value : data[i]) 
            {
                if (Double.isNaN(value)) 
                {
                    missingValue = true;
                    break;
                }
            }

            if (missingValue) 
            {
                double[] mostSimilarRow = null;
                double minDistance = Double.MAX_VALUE;

                for (int j = 0; j < rows; j++) 
                {
                    if (i != j && !rowHasMissingValues(data[j])) 
                    { // Find rows without missing values
                        double distance = calculateManhattanDistance(data[i], data[j]);
                        if (distance < minDistance) 
                        {
                            minDistance = distance;
                            mostSimilarRow = data[j];
                        }
                    }
                }

                // Impute missing values with values from the most similar row
                if (mostSimilarRow != null) 
                {
                    for (int k = 0; k < features; k++) 
                    {
                        if (Double.isNaN(data[i][k])) 
                        {
                            imputedData[i][k] = mostSimilarRow[k]; // Use the most similar row's value
                        }
                    }
                }
            }
        }

        return imputedData;
    }

    public static boolean rowHasMissingValues(double[] row) 
    {
        for (double value : row) 
        {
            if (Double.isNaN(value)) 
            {
                return true;
            }
        }
        return false;
    }

    public static double calculateMAE(double[][] imputedData, double[][] completeData) 
    {
        double sumError = 0;
        int count = 0;

        for (int i = 0; i < imputedData.length; i++) 
        {
            for (int j = 0; j < imputedData[0].length; j++) 
            {
                if (Double.isNaN(imputedData[i][j]))
                    continue;

                sumError += Math.abs(imputedData[i][j] - completeData[i][j]);
                count++;
            }
        }

        return sumError / count;
    }

    public static void main(String[] args) throws IOException 
    {
        double[][] complete = readCSVFile("lib" + File.separator + "dataset_complete.csv");
        double[][] missing01 = readCSVFile("lib" + File.separator + "dataset_missing01.csv");
        double[][] missing10 = readCSVFile("lib" + File.separator + "dataset_missing10.csv");

        // Mean imputation for 1% missing data
        long startTime = System.currentTimeMillis();
        double[][] imputedMissing01Mean = meanImputation(missing01);
        long endTime = System.currentTimeMillis();
        System.out.println("MAE_01_mean = " + calculateMAE(imputedMissing01Mean, complete));
        System.out.println("Runtime_01_mean = " + (endTime - startTime) + " ms");

        // Mean imputation for 10% missing data
        startTime = System.currentTimeMillis();
        double[][] imputedMissing10Mean = meanImputation(missing10);
        endTime = System.currentTimeMillis();
        System.out.println("MAE_10_mean = " + calculateMAE(imputedMissing10Mean, complete));
        System.out.println("Runtime_10_mean = " + (endTime - startTime) + " ms");

        // Hot deck imputation for 1% missing data
        startTime = System.currentTimeMillis();
        double[][] imputedMissing01HotDeck = hotDeckImputation(missing01);
        endTime = System.currentTimeMillis();
        System.out.println("MAE_01_hotDeck = " + calculateMAE(imputedMissing01HotDeck, complete));
        System.out.println("Runtime_01_hotDeck = " + (endTime - startTime) + " ms");

        // Hot deck imputation for 10% missing data
        startTime = System.currentTimeMillis();
        double[][] imputedMissing10HotDeck = hotDeckImputation(missing10);
        endTime = System.currentTimeMillis();
        System.out.println("MAE_10_hotDeck = " + calculateMAE(imputedMissing10HotDeck, complete));
        System.out.println("Runtime_10_hotDeck = " + (endTime - startTime) + " ms");
    }
}
