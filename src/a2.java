import java.io.*;
import java.util.*;

public class a2 
{
    public static double[][] readCSVFile(String path) throws IOException 
    {
        BufferedReader br = new BufferedReader(new FileReader(path));
        List<double[]> data = new ArrayList<>();
        br.readLine();
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

    public static void main(String[] args) throws IOException 
    {
        double[][] complete = readCSVFile("src\\dataset_complete.csv");
        double[][] missing01 = readCSVFile("src\\dataset_missing01.csv");
        double[][] missing10 = readCSVFile("src\\dataset_missing10.csv");

        for (int i = 0; i < complete.length; i++) 
        {
            System.out.print(complete[i]);
        }

    }
}
