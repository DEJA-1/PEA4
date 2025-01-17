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
    private final long stopTime; // czas wykonania w sekundach

    public GeneticAlgorithm(TSPProblem problem, int populationSize, double mutationRate, long stopTime) {
        this.problem = problem;
        this.populationSize = populationSize;
        this.mutationRate = mutationRate;
        this.stopTime = stopTime * 1000; // Konwersja na milisekundy
    }

    private long bestSolutionTime;

    @Override
    public List<Integer> solve(int optimalSolution) {
        List<List<Integer>> population = initializePopulation();
        List<Integer> bestSolution = null;
        int bestDistance = Integer.MAX_VALUE;
        bestSolutionTime = 0; // Reset czasu

        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < stopTime) {
            List<List<Integer>> newPopulation = new ArrayList<>();

            for (int i = 0; i < populationSize; i++) {
                List<Integer> parent1 = selectParent(population);
                List<Integer> parent2 = selectParent(population);
                List<Integer> child = crossover(parent1, parent2);

                if (Math.random() < mutationRate) {
                    mutate(child);
                }

                newPopulation.add(child);

                int childDistance = calculateTotalDistance(child);
                if (childDistance < bestDistance) {
                    bestDistance = childDistance;
                    bestSolution = new ArrayList<>(child);
                    bestSolutionTime = System.currentTimeMillis() - startTime; // Zapis czasu znalezienia najlepszego rozwiązania
                }
            }

            population = newPopulation;
        }

        System.out.printf("Najlepsze rozwiązanie znaleziono po czasie: %d ms\n", bestSolutionTime);
        System.out.printf("Najlepsza odległość: %d\n", bestDistance);
        return bestSolution;
    }

    public long getBestSolutionTime() {
        return bestSolutionTime;
    }


    private List<List<Integer>> initializePopulation() {
        List<List<Integer>> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            population.add(generateRandomSolution(problem.getCitiesCount()));
        }
        return population;
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
        Random random = new Random();
        int index1 = random.nextInt(solution.size());
        int index2 = random.nextInt(solution.size());
        Collections.swap(solution, index1, index2);
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
