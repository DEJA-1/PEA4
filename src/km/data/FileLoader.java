package km.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileLoader {
    public static int[][] loadMatrixFromFile(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        int citiesCount = Integer.parseInt(reader.readLine().trim());
        int[][] matrix = new int[citiesCount][citiesCount];

        for (int i = 0; i < citiesCount; i++) {
            String[] line = reader.readLine().trim().split("\\s+");
            for (int j = 0; j < citiesCount; j++) {
                matrix[i][j] = Integer.parseInt(line[j]);
            }
        } // Wczytywanie danych z pliku, parsowanie tekstu na liczbę (typ Int)
        reader.close();
        return matrix;
    }
}
