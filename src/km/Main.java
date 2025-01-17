package km;

import km.data.ConfigLoader;
import km.data.CSVWriter;
import km.model.TSPProblem;
import km.algorithms.GeneticAlgorithm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        CSVWriter csvWriter = null;

        try {
            // Mapa najlepszych znanych rozwiązań dla plików
            Map<String, Integer> optimalDistances = new HashMap<>();
            optimalDistances.put("ftv47.atsp", 1776);
            optimalDistances.put("ftv170.atsp", 2755);
            optimalDistances.put("rbg403.atsp", 2465);

            // Wczytanie konfiguracji
            ConfigLoader configLoader = new ConfigLoader("pea4_config.txt");
            String inputFilePath = configLoader.getProperty("inputData");
            String outputFilePath = configLoader.getProperty("outputFile");
            int populationSize = configLoader.getIntProperty("populationSize");
            double mutationRate = configLoader.getDoubleProperty("mutationRate");
            int stopTime = configLoader.getIntProperty("stopTime"); // w sekundach
            int testMode = configLoader.getIntProperty("testMode");

            // Znalezienie optymalnego rozwiązania dla danego pliku
            Integer optimalDistance = optimalDistances.get(inputFilePath);
            if (optimalDistance == null) {
                throw new IllegalArgumentException("Nieznane optymalne rozwiązanie dla pliku: " + inputFilePath);
            }

            // Inicjalizacja CSVWriter
            csvWriter = new CSVWriter();
            csvWriter.setFilePath(outputFilePath);
            csvWriter.writeRecordHeader("Plik", "Iteracja", "Najlepsza odleglosc", "Blad wzgledny (%)", "Czas znalezienia najlepszego rozwiazania (ms)", "Czas wykonania (ms)", "Najlepsza sciezka");

            // Wczytanie problemu TSP
            TSPProblem problem = TSPProblem.loadFromFile(inputFilePath);

            if (testMode == 1) {
                // Tryb testowy: uruchamiany raz
                System.out.println("Uruchamianie algorytmu genetycznego w trybie testowym...");
                runGeneticAlgorithmOnce(problem, csvWriter, inputFilePath, populationSize, mutationRate, stopTime, optimalDistance);
            } else {
                // Standardowy tryb: uruchamiany 10 razy
                System.out.println("Uruchamianie algorytmu genetycznego w trybie standardowym...");
                runGeneticAlgorithmMultipleTimes(problem, csvWriter, inputFilePath, populationSize, mutationRate, stopTime, optimalDistance, 10);
            }

        } catch (IOException e) {
            System.err.println("Błąd podczas wczytywania konfiguracji lub danych: " + e.getMessage());
        } finally {
            if (csvWriter != null) {
                try {
                    csvWriter.close();
                } catch (IOException e) {
                    System.err.println("Błąd podczas zamykania pliku CSV: " + e.getMessage());
                }
            }
        }
    }

    private static void runGeneticAlgorithmOnce(TSPProblem problem, CSVWriter csvWriter, String inputFilePath, int populationSize, double mutationRate, int stopTime, int optimalDistance) throws IOException {
        GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(problem, populationSize, mutationRate, stopTime);

        long startTime = System.currentTimeMillis();
        List<Integer> bestSolution = geneticAlgorithm.solve(optimalDistance);
        long elapsedTime = System.currentTimeMillis() - startTime;

        int bestDistance = calculateTotalDistance(bestSolution, problem);
        double relativeError = calculateRelativeError(bestDistance, optimalDistance);

        System.out.printf("Najlepsze rozwiazanie: %s\n", bestSolution);
        System.out.printf("Najlepsza odleglosc = %d, Blad wzgledny = %.2f%%, Czas wykonania = %d ms\n", bestDistance, relativeError, elapsedTime);

        csvWriter.writeRecord(inputFilePath, 1, bestDistance, relativeError, elapsedTime, elapsedTime, bestSolution.toString());
    }

    private static void runGeneticAlgorithmMultipleTimes(TSPProblem problem, CSVWriter csvWriter, String inputFilePath, int populationSize, double mutationRate, int stopTime, int optimalDistance, int runs) throws IOException {
        int bestOverallDistance = Integer.MAX_VALUE;
        List<Integer> bestOverallPath = null;

        double totalRelativeError = 0.0;
        long totalExecutionTimeMs = 0;
        long totalBestSolutionTimeMs = 0; // Suma czasu znalezienia najlepszego rozwiązania

        for (int run = 1; run <= runs; run++) {
            GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(problem, populationSize, mutationRate, stopTime);

            long startTime = System.currentTimeMillis();
            List<Integer> bestSolution = geneticAlgorithm.solve(optimalDistance);
            long elapsedTime = System.currentTimeMillis() - startTime;

            int bestDistance = calculateTotalDistance(bestSolution, problem);
            double relativeError = calculateRelativeError(bestDistance, optimalDistance);

            long bestSolutionTimeMs = geneticAlgorithm.getBestSolutionTime();
            totalBestSolutionTimeMs += bestSolutionTimeMs;

            totalRelativeError += relativeError;
            totalExecutionTimeMs += elapsedTime;

            System.out.printf("Iteracja %d: Najlepsza odleglosc = %d, Blad wzgledny = %.2f%%, Czas znalezienia najlepszego rozwiazania = %d ms, Czas wykonania = %d ms\n",
                    run, bestDistance, relativeError, bestSolutionTimeMs, elapsedTime);

            csvWriter.writeRecord(inputFilePath, run, bestDistance, relativeError, bestSolutionTimeMs, elapsedTime, "-");

            if (bestDistance < bestOverallDistance) {
                bestOverallDistance = bestDistance;
                bestOverallPath = new ArrayList<>(bestSolution);
            }
        }

        double averageRelativeError = totalRelativeError / runs;
        double averageBestSolutionTimeMs = (double) totalBestSolutionTimeMs / runs; // Średni czas znalezienia najlepszego rozwiązania
        double averageExecutionTimeMs = (double) totalExecutionTimeMs / runs;

        System.out.printf("Sredni blad wzgledny = %.2f%%, Sredni czas znalezienia najlepszego rozwiazania = %.2f ms, Sredni czas wykonania = %.2f ms\n",
                averageRelativeError, averageBestSolutionTimeMs, averageExecutionTimeMs);

// Konwersja do long dla zapisu w pliku CSV
        csvWriter.writeRecord(inputFilePath, -1, bestOverallDistance, -1,
                (long) averageBestSolutionTimeMs, (long) averageExecutionTimeMs, bestOverallPath.toString());
        csvWriter.writeAverageRecord(inputFilePath, averageRelativeError, averageBestSolutionTimeMs * 1_000_000, averageExecutionTimeMs);

    }


    private static int calculateTotalDistance(List<Integer> solution, TSPProblem problem) {
        int distance = 0;
        for (int i = 0; i < solution.size() - 1; i++) {
            distance += problem.getDistance(solution.get(i), solution.get(i + 1));
        }
        distance += problem.getDistance(solution.get(solution.size() - 1), solution.get(0));
        return distance;
    }

    private static double calculateRelativeError(int foundDistance, int optimalDistance) {
        return ((double) (foundDistance - optimalDistance) / optimalDistance) * 100;
    }
}
