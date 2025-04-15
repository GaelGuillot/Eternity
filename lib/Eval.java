import java.io.*;
import java.util.*;

public class Eval {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java Eval <bench_file> <solution_file> <output_file>");
            System.exit(1);
        }

        String benchFilePath = args[0];
        String solutionFilePath = args[1];
        String outputFilePath = args[2];

        List<List<Integer>> pieces = new ArrayList<>();
        List<Pair<Integer, Integer>> solution = new ArrayList<>();
        int width = 0, height = 0, fitness = 0;

        // Load bench file
        try (BufferedReader benchReader = new BufferedReader(new FileReader(benchFilePath))) {
            String[] dimensions = benchReader.readLine().split(" ");
            width = Integer.parseInt(dimensions[0]);
            height = Integer.parseInt(dimensions[1]);

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

        // Print pieces
        for (int i = 0; i < pieces.size(); i++) {
            System.out.println("Piece " + i + ": " + pieces.get(i));
        }

        // Load solution file
        try (BufferedReader solutionReader = new BufferedReader(new FileReader(solutionFilePath))) {
            for (int i = 0; i < width * height; i++) {
                String[] pair = solutionReader.readLine().split(" ");
                int pieceId = Integer.parseInt(pair[0]);
                int rotation = Integer.parseInt(pair[1]);
                solution.add(new Pair<>(pieceId, rotation));
            }
        } catch (IOException e) {
            System.err.println("Error reading solution file: " + e.getMessage());
            System.exit(1);
        }

        // Print solution
        System.out.println("Solution:");
        System.out.println("---------");
        for (int i = 0; i < solution.size(); i++) {
            System.out.printf("%03d -> %d %d%n", i, solution.get(i).first, solution.get(i).second);
        }

        // Verify borders
        if (!verifyBorders(pieces, solution, width, height)) {
            System.exit(1);
        }

        // Evaluate horizontal matches
        System.out.println("\nVerif horizontal:");
        System.out.println("-----------------");
        for (int i = 0; i < width * height - 1; i++) {
            if ((i % width) != (width - 1)) {
                System.out.print(i + ", " + (i + 1));
                if (pieces.get(solution.get(i).first).get((3 - solution.get(i).second + 4) % 4)
                        .equals(pieces.get(solution.get(i + 1).first).get((5 - solution.get(i + 1).second + 4) % 4))) {
                    fitness++;
                    System.out.println(" -> ok");
                } else {
                    System.out.println(" -> not ok");
                }
            }
        }

        // Evaluate vertical matches
        System.out.println("\nVerif vertical:");
        System.out.println("---------------");
        for (int i = 0; i < width * (height - 1); i++) {
            System.out.print(i + ", " + (i + width));
            if (pieces.get(solution.get(i).first).get((4 - solution.get(i).second + 4) % 4)
                    .equals(pieces.get(solution.get(i + width).first).get((6 - solution.get(i + width).second + 4) % 4))) {
                fitness++;
                System.out.println(" -> ok");
            } else {
                System.out.println(" -> not ok");
            }
        }

        // Print fitness
        System.out.println("\nFitness: " + fitness);

        // Write fitness to output file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, true))) {
            writer.write(fitness + "\n");
        } catch (IOException e) {
            System.err.println("Error writing to output file: " + e.getMessage());
        }
    }

    private static boolean verifyBorders(List<List<Integer>> pieces, List<Pair<Integer, Integer>> solution, int width, int height) {
        // Verify top border
        for (int i = 0; i < width; i++) {
            if (pieces.get(solution.get(i).first).get((6 - solution.get(i).second + 4) % 4) != 0) {
                System.out.println("Top border -> INVALID");
                return false;
            }
        }
        System.out.println("Top border -> VALID");

        // Verify bottom border
        for (int i = width * (height - 1); i < width * height; i++) {
            if (pieces.get(solution.get(i).first).get((4 - solution.get(i).second + 4) % 4) != 0) {
                System.out.println("Bottom border -> INVALID");
                return false;
            }
        }
        System.out.println("Bottom border -> VALID");

        // Verify left border
        for (int i = 0; i < width * height; i += width) {
            if (pieces.get(solution.get(i).first).get((5 - solution.get(i).second + 4) % 4) != 0) {
                System.out.println("Left border -> INVALID");
                return false;
            }
        }
        System.out.println("Left border -> VALID");

        // Verify right border
        for (int i = width - 1; i < width * height; i += width) {
            if (pieces.get(solution.get(i).first).get((3 - solution.get(i).second + 4) % 4) != 0) {
                System.out.println("Right border -> INVALID");
                return false;
            }
        }
        System.out.println("Right border -> VALID");

        return true;
    }

    // Helper class to represent a pair of integers
    static class Pair<F, S> {
        F first;
        S second;

        Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }
    }
}