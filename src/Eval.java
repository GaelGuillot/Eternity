import java.io.*;
import java.util.*;

public class Eval {
    private List<List<Integer>> pieces;
    private int width;
    private int height;
    public int count = 0;

    public Eval(String benchFilePath) {
        try (BufferedReader benchReader = new BufferedReader(new FileReader(benchFilePath))) {
            String[] dimensions = benchReader.readLine().split(" ");
            width = Integer.parseInt(dimensions[0]);
            height = Integer.parseInt(dimensions[1]);

            pieces = new ArrayList<>();
            for (int i = 0; i < width * height; i++) {
                String[] edges = benchReader.readLine().split(" ");
                List<Integer> piece = new ArrayList<>();
                for (String edge : edges) {
                    piece.add(Integer.parseInt(edge));
                }
                pieces.add(piece);
            }
        } catch (IOException e) {
            System.err.println("Error reading bench file: " + e.getMessage());
            System.exit(1);
        }
    }

    public int evaluateSolution(List<Pair<Integer, Integer>> solution) {
        count++;
        if (!verifyBorders(solution)) {
            return -1;
        }

        int fitness = 0;

        // Evaluate horizontal matches
        for (int i = 0; i < width * height - 1; i++) {
            if ((i % width) != (width - 1)) {
                if (pieces.get(solution.get(i).first).get((3 - solution.get(i).second + 4) % 4)
                        .equals(pieces.get(solution.get(i + 1).first).get((5 - solution.get(i + 1).second + 4) % 4))) {
                    fitness++;
                }
            }
        }

        // Evaluate vertical matches
        for (int i = 0; i < width * (height - 1); i++) {
            if (pieces.get(solution.get(i).first).get((4 - solution.get(i).second + 4) % 4)
                    .equals(pieces.get(solution.get(i + width).first).get((6 - solution.get(i + width).second + 4) % 4))) {
                fitness++;
            }
        }

        return fitness;
    }

    private boolean verifyBorders(List<Pair<Integer, Integer>> solution) {
        // Verify top border
        for (int i = 0; i < width; i++) {
            if (pieces.get(solution.get(i).first).get((6 - solution.get(i).second + 4) % 4) != 0) {
                return false;
            }
        }

        // Verify bottom border
        for (int i = width * (height - 1); i < width * height; i++) {
            if (pieces.get(solution.get(i).first).get((4 - solution.get(i).second + 4) % 4) != 0) {
                return false;
            }
        }

        // Verify left border
        for (int i = 0; i < width * height; i += width) {
            if (pieces.get(solution.get(i).first).get((5 - solution.get(i).second + 4) % 4) != 0) {
                return false;
            }
        }

        // Verify right border
        for (int i = width - 1; i < width * height; i += width) {
            if (pieces.get(solution.get(i).first).get((3 - solution.get(i).second + 4) % 4) != 0) {
                return false;
            }
        }

        return true;
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java Eval <bench_file> <solution_file> <output_file>");
            System.exit(1);
        }

        String benchFilePath = args[0];
        String solutionFilePath = args[1];
        String outputFilePath = args[2];

        Eval evaluator = new Eval(benchFilePath);
        List<Pair<Integer, Integer>> solution = new ArrayList<>();

        // Load solution file
        try (BufferedReader solutionReader = new BufferedReader(new FileReader(solutionFilePath))) {
            for (int i = 0; i < evaluator.width * evaluator.height; i++) {
                String[] pair = solutionReader.readLine().split(" ");
                int pieceId = Integer.parseInt(pair[0]);
                int rotation = Integer.parseInt(pair[1]);
                solution.add(new Pair<>(pieceId, rotation));
            }
        } catch (IOException e) {
            System.err.println("Error reading solution file: " + e.getMessage());
            System.exit(1);
        }

        int fitness = evaluator.evaluateSolution(solution);
        System.out.println("Fitness: " + fitness);

        // Write fitness to output file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, true))) {
            writer.write(fitness + "\n");
        } catch (IOException e) {
            System.err.println("Error writing to output file: " + e.getMessage());
        }
    }

    // Helper class to represent a pair of integers
    // static class Pair<F, S> {
    //     F first;
    //     S second;

    //     Pair(F first, S second) {
    //         this.first = first;
    //         this.second = second;
    //     }
    // }
}