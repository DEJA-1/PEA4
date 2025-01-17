package km.algorithms;

import km.model.TSPProblem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GeneticAlgorithm extends Algorithm {
    private final TSPProblem problem;
    private final int populationSize;
    private final double mutationRate;
    private final double crossoverRate;
    private final long stopTime;
    private final String mutationMethod;


    public GeneticAlgorithm(TSPProblem problem, int populationSize, double mutationRate, double crossoverRate, long stopTime, String mutationMethod) {
        this.problem = problem;
        this.populationSize = populationSize;
        this.mutationRate = mutationRate;
        this.crossoverRate = crossoverRate;
        this.stopTime = stopTime * 1000;
        this.mutationMethod = mutationMethod;
    }

    private long bestSolutionTime;

    @Override
    public List<Integer> solve(int optimalSolution) {
        List<List<Integer>> population = initializePopulationGreedy();
        List<Integer> bestSolution = null;
        int bestDistance = Integer.MAX_VALUE;
        bestSolutionTime = 0; // Reset czasu

        long startTime = System.currentTimeMillis();
        long startTimeNano = System.nanoTime();

        while (System.currentTimeMillis() - startTime < stopTime) {
            List<List<Integer>> newPopulation = new ArrayList<>();

            for (int i = 0; i < populationSize; i++) {
                List<Integer> parent1 = selectParent(population);
                List<Integer> parent2 = selectParent(population);
                List<Integer> child;

                if (Math.random() < crossoverRate) {
                    child = crossover(parent1, parent2);
                } else {
                    child = new ArrayList<>(parent1);
                }


                if (Math.random() < mutationRate) {
                    mutate(child);
                }

                newPopulation.add(child);

                int childDistance = calculateTotalDistance(child);
                if (childDistance < bestDistance) {
                    bestDistance = childDistance;
                    bestSolution = new ArrayList<>(child);
                    bestSolutionTime = System.nanoTime() - startTimeNano; // Zapis czasu znalezienia najlepszego rozwiązania
                }
            }

            population = newPopulation;
        }

        System.out.printf("Najlepsze rozwiązanie znaleziono po czasie: %d ns\n", bestSolutionTime);
        System.out.printf("Najlepsza odległość: %d\n", bestDistance);
        return bestSolution;
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
        return population.get(random.nextInt(population.size()));
    }

    private List<Integer> crossover(List<Integer> parent1, List<Integer> parent2) {
        int size = parent1.size();
        Random random = new Random();
        int start = random.nextInt(size);
        int end = random.nextInt(size);

        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        List<Integer> child = new ArrayList<>(Collections.nCopies(size, -1));
        for (int i = start; i <= end; i++) {
            child.set(i, parent1.get(i));
        }

        int currentIndex = 0;
        for (int i = 0; i < size; i++) {
            int city = parent2.get(i);
            if (!child.contains(city)) {
                while (child.get(currentIndex) != -1) {
                    currentIndex++;
                }
                child.set(currentIndex, city);
            }
        }

        return child;
    }

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
        int i = random.nextInt(solution.size());
        int j = random.nextInt(solution.size());
        if (i > j) {
            int temp = i;
            i = j;
            j = temp;
        }
        while (i < j) {
            Collections.swap(solution, i, j);
            i++;
            j--;
        }
    }

    private int calculateTotalDistance(List<Integer> solution) {
        int distance = 0;
        for (int i = 0; i < solution.size() - 1; i++) {
            distance += problem.getDistance(solution.get(i), solution.get(i + 1));
        }
        distance += problem.getDistance(solution.get(solution.size() - 1), solution.get(0));
        return distance;
    }

    private List<Integer> generateRandomSolution(int citiesCount) {
        List<Integer> solution = new ArrayList<>();
        for (int i = 0; i < citiesCount; i++) {
            solution.add(i);
        }
        Collections.shuffle(solution);
        return solution;
    }
}
