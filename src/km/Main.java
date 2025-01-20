package km;

import km.data.ConfigLoader;
import km.data.CSVWriter;
import km.model.TSPProblem;
import km.algorithms.GeneticAlgorithm;

import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        CSVWriter csvWriter = null;

        try {
            Map<String, Integer> optimalDistances = Map.of(
                    "ftv47.atsp", 1776,
                    "ftv170.atsp", 2755,
                    "rbg403.atsp", 2465
            );

            String[] inputFiles = {"ftv47.atsp", "ftv170.atsp", "rbg403.atsp"};

            // Wczytanie konfiguracji
            ConfigLoader configLoader = new ConfigLoader("pea4_config.txt");
            String inputFilePath = configLoader.getProperty("inputData");
            String outputFilePath = configLoader.getProperty("outputFile");
            int populationSize = configLoader.getIntProperty("populationSize");
            double mutationRate = configLoader.getDoubleProperty("mutationRate");
            double crossoverRate = configLoader.getDoubleProperty("crossoverRate");
            String mutationMethod = configLoader.getProperty("mutationMethod");
            int stopTime = configLoader.getIntProperty("stopTime");
            int testMode = configLoader.getIntProperty("testMode");
            String changedValues = configLoader.getProperty("changedValues");

            // Inicjalizacja zapisu do pliku
            csvWriter = new CSVWriter();
            csvWriter.setFilePath(outputFilePath);
            csvWriter.writeRecordHeader("Plik", "Metoda mutacji", "Populacja", "Iteracja", "Najlepsza odleglosc", "Blad wzgledny (%)", "Czas znalezienia najlepszego rozwiazania (ns)", "Czas wykonania (ns)", "Najlepsza sciezka");

            // Tryb testowy lub właściwy do badania
            if (testMode == 1) {
                System.out.println("Uruchamianie algorytmu genetycznego w trybie testowym...");
                runTestMode(csvWriter, inputFilePath, populationSize, mutationRate, crossoverRate, stopTime, mutationMethod, optimalDistances);
            } else {
                System.out.println("Uruchamianie algorytmu genetycznego w trybie standardowym...");
                runStandardMode(csvWriter, inputFiles, populationSize, mutationRate, crossoverRate, stopTime, changedValues, mutationMethod, optimalDistances);
            }

        } catch (IOException e) {
            System.err.println("Błąd podczas wczytywania konfiguracji lub danych: " + e.getMessage());
        } finally {
            closeCSVWriter(csvWriter);
        }
    }

    private static void runTestMode(CSVWriter csvWriter, String inputFilePath, int populationSize, double mutationRate, double crossoverRate, int stopTime, String mutationMethod, Map<String, Integer> optimalDistances) throws IOException {
        Integer optimalDistance = optimalDistances.get(inputFilePath);
        if (optimalDistance == null) {
            throw new IllegalArgumentException("Nieznane optymalne rozwiązanie dla pliku: " + inputFilePath);
        }

        TSPProblem problem = TSPProblem.loadFromFile(inputFilePath);
        runGeneticAlgorithmOnce(problem, csvWriter, inputFilePath, populationSize, mutationRate, crossoverRate, stopTime, mutationMethod, optimalDistance);
    }

    private static void runStandardMode(CSVWriter csvWriter, String[] inputFiles, int populationSize, double mutationRate, double crossoverRate, int stopTime, String changedValues, String mutationMethod, Map<String, Integer> optimalDistances) throws IOException {
        int[] populationSizes = {50, 100, 200};
        double[] mutationRates = {0.02, 0.05, 0.10};
        String[] mutationMethods = {"swap", "invert"};

        if ("mutation".equalsIgnoreCase(changedValues)) {
            for (String method : mutationMethods) {
                System.out.printf("Testowanie dla metody mutacji: %s\n", method);
                for (double rate : mutationRates) {
                    System.out.printf("Testowanie dla współczynnika mutacji: %.2f\n", rate);
                    for (String inputFile : inputFiles) {
                        executeAlgorithm(csvWriter, inputFile, populationSize, rate, crossoverRate, stopTime, method, optimalDistances);
                    }
                }
            }
        } else if ("population".equalsIgnoreCase(changedValues)) {
            for (String method : mutationMethods) {
                System.out.printf("Testowanie dla metody mutacji: %s\n", method);
                for (int size : populationSizes) {
                    System.out.printf("Testowanie dla wielkości populacji: %d\n", size);
                    for (String inputFile : inputFiles) {
                        executeAlgorithm(csvWriter, inputFile, size, mutationRate, crossoverRate, stopTime, method, optimalDistances);
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Nieznana wartość dla changedValues: " + changedValues);
        }
    }

    private static void executeAlgorithm(CSVWriter csvWriter, String inputFile, int populationSize, double mutationRate, double crossoverRate, int stopTime, String mutationMethod, Map<String, Integer> optimalDistances) throws IOException {
        Integer optimalDistance = optimalDistances.get(inputFile);
        if (optimalDistance == null) {
            throw new IllegalArgumentException("Nieznane optymalne rozwiązanie dla pliku: " + inputFile);
        }

        TSPProblem problem = TSPProblem.loadFromFile(inputFile);
        stopTime = switch (inputFile) {
            case "ftv47.atsp" -> 60;
            case "ftv170.atsp" -> 120;
            case "rbg403.atsp" -> 180;
            default -> stopTime;
        };

        runGeneticAlgorithmMultipleTimes(problem, csvWriter, inputFile, populationSize, mutationRate, crossoverRate, stopTime, mutationMethod, optimalDistance, 10);
    }

    private static void closeCSVWriter(CSVWriter csvWriter) {
        if (csvWriter != null) {
            try {
                csvWriter.close();
            } catch (IOException e) {
                System.err.println("Błąd podczas zamykania pliku CSV: " + e.getMessage());
            }
        }
    }

    private static void runGeneticAlgorithmOnce(TSPProblem problem, CSVWriter csvWriter, String inputFilePath, int populationSize, double mutationRate, double crossoverRate, int stopTime, String mutationMethod, int optimalDistance) throws IOException {
        GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(problem, populationSize, mutationRate, crossoverRate, stopTime, mutationMethod);

        long startTime = System.nanoTime();
        List<Integer> bestSolution = geneticAlgorithm.solve(optimalDistance);
        long elapsedTime = System.nanoTime() - startTime;

        int bestDistance = calculateTotalDistance(bestSolution, problem);
        double relativeError = calculateRelativeError(bestDistance, optimalDistance);

        System.out.printf("Najlepsze rozwiazanie: %s\n", bestSolution);
        System.out.printf("Najlepsza odleglosc = %d, Blad wzgledny = %.2f%%, Czas wykonania = %d ns\n", bestDistance, relativeError, elapsedTime);

        csvWriter.writeRecord(inputFilePath, mutationMethod, populationSize, 1, bestDistance, relativeError, elapsedTime, elapsedTime, bestSolution.toString());
    }

    private static void runGeneticAlgorithmMultipleTimes(TSPProblem problem, CSVWriter csvWriter, String inputFilePath, int populationSize, double mutationRate, double crossoverRate, int stopTime, String mutationMethod, int optimalDistance, int runs) throws IOException {
        int bestOverallDistance = Integer.MAX_VALUE;
        List<Integer> bestOverallPath = null;

        double totalRelativeError = 0.0;
        long totalExecutionTimeNs = 0;
        long totalBestSolutionTimeNs = 0;

        for (int run = 1; run <= runs; run++) {
            GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(problem, populationSize, mutationRate, crossoverRate, stopTime, mutationMethod);

            long startTime = System.nanoTime();
            List<Integer> bestSolution = geneticAlgorithm.solve(optimalDistance);
            long elapsedTime = System.nanoTime() - startTime;

            int bestDistance = calculateTotalDistance(bestSolution, problem);
            double relativeError = calculateRelativeError(bestDistance, optimalDistance);

            long bestSolutionTimeNs = geneticAlgorithm.getBestSolutionTime();
            totalBestSolutionTimeNs += bestSolutionTimeNs;
            totalRelativeError += relativeError;
            totalExecutionTimeNs += elapsedTime;

            System.out.printf("Iteracja %d: Najlepsza odleglosc = %d, Blad wzgledny = %.2f%%, Czas znalezienia najlepszego rozwiazania = %d ns, Czas wykonania = %d ns\n", run, bestDistance, relativeError, bestSolutionTimeNs, elapsedTime);

            csvWriter.writeRecord(inputFilePath, mutationMethod, populationSize, run, bestDistance, relativeError, bestSolutionTimeNs, elapsedTime, "-");

            if (bestDistance < bestOverallDistance) {
                bestOverallDistance = bestDistance;
                bestOverallPath = new ArrayList<>(bestSolution);
            }
        }

        double averageRelativeError = totalRelativeError / runs;
        double averageExecutionTimeNs = (double) totalExecutionTimeNs / runs;
        double averageBestSolutionTimeNs = (double) totalBestSolutionTimeNs / runs;

        System.out.printf("Sredni blad wzgledny = %.2f%%, Sredni czas znalezienia najlepszego rozwiazania = %.2f ns, Sredni czas wykonania = %.2f ns\n", averageRelativeError, averageBestSolutionTimeNs, averageExecutionTimeNs);

        csvWriter.writeRecord(inputFilePath, mutationMethod, populationSize, -1, bestOverallDistance, -1, (long) averageBestSolutionTimeNs, (long) averageExecutionTimeNs, bestOverallPath.toString());
        csvWriter.writeAverageRecord(inputFilePath, mutationMethod, populationSize, averageRelativeError, averageBestSolutionTimeNs, averageExecutionTimeNs);
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
