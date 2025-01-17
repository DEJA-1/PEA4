package km.data;

import java.io.FileWriter;
import java.io.IOException;

public class CSVWriter {
    private FileWriter writer;

    public CSVWriter() {
        this.writer = null;
    }

    // Ustawia ścieżkę do pliku i inicjalizuje nagłówki kolumn
    public void setFilePath(String filePath) throws IOException {
        if (writer != null) {
            writer.close();
        }
        writer = new FileWriter(filePath);
    }

    // Dodanie nagłówka do pliku CSV
    public void writeRecordHeader(String... headers) throws IOException {
        for (int i = 0; i < headers.length; i++) {
            writer.write(headers[i]);
            if (i < headers.length - 1) {
                writer.write(", ");
            }
        }
        writer.write("\n");
    }

    // Zapisuje wiersz z wynikami, uwzględniając czas znalezienia najlepszego rozwiązania
    public void writeRecord(String file, int run, int bestDistance, double relativeError, long bestSolutionTimeMs, long executionTimeMs, String bestPath) throws IOException {
        writer.write(String.format("%s, %d, %d, %.2f, %d, %d, %s\n", file, run, bestDistance, relativeError, bestSolutionTimeMs, executionTimeMs, bestPath));
    }

    // Zapisuje średnie wartości dla pliku
    public void writeAverageRecord(String file, double averageRelativeError, double averageBestSolutionTimeNs, double averageExecutionTimeMs) throws IOException {
        writer.write(String.format("%s, -, -, %.2f, %.2f, %.2f, -\n", file, averageRelativeError, averageBestSolutionTimeNs / 1_000_000, averageExecutionTimeMs));
    }

    // Zamknięcie strumienia
    public void close() throws IOException {
        if (writer != null) {
            writer.flush();
            writer.close();
        }
    }
}
