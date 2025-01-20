package km.algorithms;

import km.model.TSPProblem;

import java.util.*;

public class GeneticAlgorithm extends Algorithm {

    private final TSPProblem problem;
    private final int populationSize;
    private final double mutationRate;
    private final double crossoverRate;
    private final long stopTime;
    private final String mutationMethod;

    private long bestSolutionTime;

    public GeneticAlgorithm(TSPProblem problem, int populationSize, double mutationRate, double crossoverRate, long stopTime, String mutationMethod) {
        this.problem = problem;
        this.populationSize = populationSize;
        this.mutationRate = mutationRate;
        this.crossoverRate = crossoverRate;
        this.stopTime = stopTime * 1000;
        this.mutationMethod = mutationMethod;
    }

    @Override
    public List<Integer> solve(int optimalSolution) {
        List<List<Integer>> population = initializePopulationGreedy(); // Inicjalizacja populacji za pomocą metody zachłannej
        List<Integer> bestSolution = null;
        int bestDistance = Integer.MAX_VALUE;
        bestSolutionTime = 0; // Resetowanie czasu znalezienia najlepszego rozwiązania

        long startTime = System.currentTimeMillis();
        long startTimeNano = System.nanoTime();

        while (System.currentTimeMillis() - startTime < stopTime) {
            List<List<Integer>> newPopulation = new ArrayList<>();

            for (int i = 0; i < populationSize; i++) {
                List<Integer> parent1 = selectParent(population); // wybór pierwszego rodzica
                List<Integer> parent2 = selectParent(population); // wybór drugiego rodzica

                List<Integer> child;
                if (Math.random() < crossoverRate) { // Sprawdzenie, czy zachodzi krzyżowanie
                    child = crossover(parent1, parent2); // Tworzenie potomka przez krzyżowanie
                } else {
                    child = new ArrayList<>(parent1); // Brak krzyżowania - kopiowanie pierwszego rodzica
                }

                if (Math.random() < mutationRate) { // Sprawdzenie, czy zachodzi mutacja
                    mutate(child); // Mutacja potomka
                }

                newPopulation.add(child); // Dodanie potomka do nowej populacji

                int childDistance = calculateTotalDistance(child); // Obliczenie odległości dla potomka
                if (childDistance < bestDistance) { // Sprawdzenie, czy potomek jest lepszy od dotychczasowego najlepszego rozwiązania
                    bestDistance = childDistance; // Aktualizacja najlepszej odległości
                    bestSolution = new ArrayList<>(child); // Aktualizacja najlepszego rozwiązania
                    bestSolutionTime = System.nanoTime() - startTimeNano; // Zapis czasu znalezienia najlepszego rozwiązania
                }
            }

            population = newPopulation; // Aktualizacja populacji do nowej generacji
        }

        // Wyświetlenie informacji o najlepszym rozwiązaniu
        System.out.printf("Najlepsze rozwiązanie znaleziono po czasie: %d ns\n", bestSolutionTime);
        System.out.printf("Najlepsza odległość: %d\n", bestDistance);
        return bestSolution; // Zwrócenie najlepszego rozwiązania
    }

    public long getBestSolutionTime() {
        return bestSolutionTime;
    }

    private List<List<Integer>> initializePopulationGreedy() {
        List<List<Integer>> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            population.add(generateGreedySolution());
        }
        return population;
    }

    private List<Integer> generateGreedySolution() {
        int citiesCount = problem.getCitiesCount();
        List<Integer> solution = new ArrayList<>();
        boolean[] visited = new boolean[citiesCount];

        Random random = new Random();
        int currentCity = random.nextInt(citiesCount);
        solution.add(currentCity);
        visited[currentCity] = true;

        for (int i = 1; i < citiesCount; i++) {
            int nearestCity = -1;
            int shortestDistance = Integer.MAX_VALUE;

            for (int nextCity = 0; nextCity < citiesCount; nextCity++) {
                if (!visited[nextCity]) {
                    int distance = problem.getDistance(currentCity, nextCity);
                    if (distance < shortestDistance) {
                        nearestCity = nextCity;
                        shortestDistance = distance;
                    }
                }
            }

            solution.add(nearestCity);
            visited[nearestCity] = true;
            currentCity = nearestCity;
        }

        return solution;
    }

    private List<Integer> selectParent(List<List<Integer>> population) {
        Random random = new Random();
        return population.get(random.nextInt(population.size())); // Losowy wybór rodzica z populacji
    }

    // Krzyżowanie dwóch rodziców za pomocą algorytmu OX (Order Crossover)
    private List<Integer> crossover(List<Integer> parent1, List<Integer> parent2) {
        int size = parent1.size(); // Rozmiar chromosomu
        Random random = new Random();
        int start = random.nextInt(size); // Punkt początkowy fragmentu do skopiowania
        int end = random.nextInt(size); // Punkt końcowy fragmentu do skopiowania

        if (start > end) { // Upewnienie się, że start <= end
            int temp = start;
            start = end;
            end = temp;
        }

        List<Integer> child = new ArrayList<>(Collections.nCopies(size, -1));
        for (int i = start; i <= end; i++) {
            child.set(i, parent1.get(i)); // Skopiowanie fragmentu od pierwszego rodzica
        }

        int currentIndex = 0; // Indeks dla dodawania elementów od drugiego rodzica
        for (int i = 0; i < size; i++) {
            int city = parent2.get(i); // Pobranie miasta z drugiego rodzica
            if (!child.contains(city)) { // Sprawdzenie, czy miasto nie zostało już dodane
                while (child.get(currentIndex) != -1) { // Szukanie wolnego miejsca w dziecku
                    currentIndex++;
                }
                child.set(currentIndex, city); // Dodanie miasta do dziecka
            }
        }

        return child; // Zwrócenie wygenerowanego dziecka
    }

    // Mutacja potomka
    private void mutate(List<Integer> solution) {
        if ("swap".equalsIgnoreCase(mutationMethod)) {
            swapMutation(solution);
        } else if ("invert".equalsIgnoreCase(mutationMethod)) {
            invertMutation(solution);
        } else {
            throw new IllegalArgumentException("Nieznana metoda mutacji: " + mutationMethod);
        }
    }

    private void swapMutation(List<Integer> solution) {
        Random random = new Random();
        int i = random.nextInt(solution.size());
        int j = random.nextInt(solution.size());
        Collections.swap(solution, i, j);
    }

    private void invertMutation(List<Integer> solution) {
        Random random = new Random();
        int i = random.nextInt(solution.size()); // Losowy indeks początkowy
        int j = random.nextInt(solution.size()); // Losowy indeks końcowy
        if (i > j) { // Upewnienie się, że i <= j
            int temp = i;
            i = j;
            j = temp;
        }
        while (i < j) { // Odwracanie kolejności fragmentu
            Collections.swap(solution, i, j); // Zamiana elementów na przeciwnych końcach fragmentu
            i++;
            j--;
        }
    }

    // Obliczanie całkowitej odległości trasy
    private int calculateTotalDistance(List<Integer> solution) {
        int distance = 0; // Suma odległości
        for (int i = 0; i < solution.size() - 1; i++) {
            distance += problem.getDistance(solution.get(i), solution.get(i + 1)); // Dodanie odległości między kolejnymi miastami
        }
        distance += problem.getDistance(solution.get(solution.size() - 1), solution.get(0)); // Dodanie odległości powrotnej do miasta początkowego
        return distance; // Zwrócenie całkowitej odległości
    }

    // Generowanie losowego rozwiązania (alternatywna metoda inicjalizacji populacji)
    private List<Integer> generateRandomSolution(int citiesCount) {
        List<Integer> solution = new ArrayList<>(); // Lista przechowująca miasta w losowej kolejności
        for (int i = 0; i < citiesCount; i++) {
            solution.add(i); // Dodanie wszystkich miast do listy
        }
        Collections.shuffle(solution); // Wymieszanie miast w losowej kolejności
        return solution; // Zwrócenie losowego rozwiązania
    }
}
