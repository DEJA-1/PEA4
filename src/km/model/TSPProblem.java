package km.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TSPProblem {
    private int[][] distanceMatrix;

    public TSPProblem(int[][] distanceMatrix) {
        this.distanceMatrix = distanceMatrix;
    }

    public static TSPProblem loadFromFile(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        int dimension = 0; // Zmienna przechowująca liczbę miast (wymiar macierzy)
        boolean matrixSection = false; // Flaga wskazująca rozpoczęcie sekcji macierzy odległości
        int[][] matrix = null; // Macierz odległości
        int row = 0, col = 0; // Wskaźniki aktualnego wiersza i kolumny macierzy

        while ((line = reader.readLine()) != null) {
            line = line.trim(); // Usunięcie białych znaków na początku i końcu linii

            // Odczytanie wymiaru macierzy z nagłówka DIMENSION
            if (line.startsWith("DIMENSION:")) {
                dimension = Integer.parseInt(line.split(":")[1].trim()); // Parsowanie liczby miast
                matrix = new int[dimension][dimension]; // Inicjalizacja macierzy odległości
            }

            // Znalezienie sekcji EDGE_WEIGHT_SECTION, która zawiera dane macierzy
            if (line.equals("EDGE_WEIGHT_SECTION")) {
                matrixSection = true; // Ustawienie flagi na true, aby zacząć przetwarzanie macierzy
                continue;
            }

            // Przetwarzanie sekcji macierzy odległości
            if (matrixSection) {
                if (line.equals("EOF")) { // Koniec pliku
                    break;
                }

                String[] tokens = line.split("\\s+"); // Podział linii na liczby oddzielone spacjami

                // Iteracja po liczbach w bieżącej linii
                for (String token : tokens) {
                    if (col == dimension) { // Jeśli kolumna osiągnie wymiar, przejdź do nowego wiersza
                        col = 0;
                        row++;
                    }

                    if (row >= dimension) { // Sprawdzenie, czy liczba wierszy nie przekracza wymiaru
                        throw new IOException("Za dużo wierszów w EDGE_WEIGHT_SECTION, oczekiwano:  " + dimension);
                    }

                    matrix[row][col++] = Integer.parseInt(token); // Przypisanie wartości do macierzy i przesunięcie kolumny
                }
            }
        }

        reader.close(); // Zamknięcie pliku po zakończeniu przetwarzania

        // Sprawdzenie, czy macierz została poprawnie wczytana
        if (matrix == null) {
            throw new IOException("Nieprawidłowy format pliku");
        }

        // Sprawdzenie, czy macierz ma dokładnie wymiar dimension x dimension
        if (row != dimension - 1 || col != dimension) {
            throw new IOException("Niekompletna macierz, oczekiwano: " + dimension + "x" + dimension + ", ale jest wiersz=" + row + " i kolumna=" + col);
        }

        return new TSPProblem(matrix); // Zwrócenie obiektu TSPProblem z wczytaną macierzą odległości
    }

    public int getCitiesCount() {
        return distanceMatrix.length; // Zwraca liczbę miast (wymiar macierzy)
    }

    public int[][] getDistanceMatrix() {
        return distanceMatrix; // Zwraca macierz odległości
    }

    public int getDistance(int from, int to) {
        return distanceMatrix[from][to]; // Zwraca odległość między dwoma miastami
    }
}
