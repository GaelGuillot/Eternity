import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.io.FileWriter;

public class Main {
    static int width  = 16;
    static int height = 16;
    static int progressBarWidth = 100;
    
    static String filePath = "lib/benchs/pieces_set/pieces_" + String.format("%02d", width) + "x" + String.format("%02d", height) + ".txt";
    static String benchmarkPath = "lib/benchs/benchEternity2WithoutHint.txt";
    static String solutionPath = "lib/solutions/solution_" + String.format("%02d", width) + "x" + String.format("%02d", height) + ".txt";
    
    static List<Piece> initPieces = new ArrayList<>(); 
    
    public static void main(String[] args) throws Exception {
        int populationSize = 20;
        double elitismRate = 0.1;
        double mutationRate = 0.4;
        int totalIterations = 500000;

        // Parse the initial board:
        // try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
        try (BufferedReader br = new BufferedReader(new FileReader(benchmarkPath))) {
            String line;
            line = br.readLine();
            String[] dimensions = line.split(" ");
            width = Integer.parseInt(dimensions[0]);
            height = Integer.parseInt(dimensions[1]);
            int id = 0;
            while ((line = br.readLine()) != null) {
                String[] stringValues = line.split(" ");
                int[] values = new int[4];
                for (int i = 0; i < 4; i++) {
                    values[i] = Integer.parseInt(stringValues[i]);
                }
                Piece.PieceType type;
                if (values[0] == 0){
                    if (values[1] == 0) {
                        type = Piece.PieceType.CORNER;
                    }
                    else {
                        type = Piece.PieceType.BORDER;
                    }
                }
                else {
                    type = Piece.PieceType.CENTER;
                }
                initPieces.add(new Piece(id, values, type));
                id++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        int maxScore = width*(width-1) + height*(height-1);
        
        // Run 100 times and calculate mean

        double totalScore = 0;

        double[] scores = new double[7];
        
        for (int j = 1; j < 8; j++) {
            System.out.println("Population size: " + 50*j);
            totalScore = 0;
            for (int i = 0; i < 50; i++) {
                System.out.println("Iteration " + (i + 1) + "/50");
                int bestScore = run(j*50, elitismRate, mutationRate, totalIterations);
                totalScore += bestScore;
            }
            System.out.println("Mean score: " + totalScore / 50);
            scores[j-1] = totalScore / 50;
        }

        System.out.println("Scores: " + Arrays.toString(scores));

        // run(populationSize, elitismRate, mutationRate, totalIterations);
        
    }

    public static int run(int populationSize, double elitismRate, double mutationRate, int totalIterations){
        List<Piece> pieces = new Init(initPieces, width, height).shuffle();
        Solution solution = new Solution(pieces);
        Eval eval = new Eval(benchmarkPath);

        int maxScore = width*(width-1) + height*(height-1);

        List<Chicken> population = new ArrayList<>();

        int elitism = (int) (populationSize * elitismRate);

        for (int i=0;i<=populationSize;i++){
            pieces = new Init(initPieces, width, height).shuffle();
            solution = new Solution(pieces);
            population.add(new Chicken(pieces, eval.evaluateSolution(solution.getSolution())));
        }

        // Start timer
        long startTime = System.nanoTime();
        boolean bestSolutionFound = false;

        List<Integer> bestScores = new ArrayList<>();
        
        while (eval.count < totalIterations && !bestSolutionFound) {
            // Get weighted population
            List<Chicken> weightedPopulation = ChickenSorter.getWeightedPopulation(population);

            // Apply crossover
            List<Chicken> children = ChickenSorter.crossover(weightedPopulation, width, height);
            ChickenSorter.mutate(children, mutationRate);
            bestSolutionFound = ChickenSorter.recalculateScores(eval, children, maxScore);

            // Apply elitism
            List<Chicken> newPopulation = new ArrayList<>();
            List<Chicken> bestChildren = ChickenSorter.getTopNChickens(children, populationSize - elitism);
            List<Chicken> bestParents = ChickenSorter.getTopNChickens(population, elitism);

            newPopulation.addAll(bestChildren);
            newPopulation.addAll(bestParents);

            population = newPopulation;

            // Update progress bar
            int progress = (int)((eval.count * progressBarWidth) / totalIterations);
            System.out.print("\r[");

            // Calculate and display elapsed time
            long elapsedTime = System.nanoTime() - startTime;
            long hours = TimeUnit.NANOSECONDS.toHours(elapsedTime);
            long minutes = TimeUnit.NANOSECONDS.toMinutes(elapsedTime) % 60;
            long seconds = TimeUnit.NANOSECONDS.toSeconds(elapsedTime) % 60;
            String timeString = String.format(" %02d:%02d:%02d", hours, minutes, seconds);

            for (int i = 0; i < progressBarWidth; i++) {
                if (i < progress) System.out.print("=");
                else if (i == progress) System.out.print(">");
                else System.out.print(" ");
            }
            System.out.print("] " + (eval.count * 100 / totalIterations) + "%" + timeString);

            Chicken bestChicken = population.get(0);
            for (Chicken chicken : population) {
                if (chicken.getScore() > bestChicken.getScore()) {
                    bestChicken = chicken;
                }
            }
            bestScores.add(bestChicken.getScore());
        }
        System.out.println(); // New line after progress bar completes

        System.out.println("Total invalid solutions: " + ChickenSorter.totalInvalidSolutions);

        // Save scores to CSV
        String csvPath = "lib/scores/scores_" + "p" + populationSize + "_i" + totalIterations + 
                        "_e" + (int)(elitismRate*100) + "_m" + (int)(mutationRate*100) + ".csv";
        try (FileWriter writer = new FileWriter(csvPath)) {
            for (int score : bestScores) {
                writer.write(score + "\n");
            }
            System.out.println("Scores saved to: " + csvPath);
        } catch (IOException e) {
            System.err.println("Error saving scores to CSV: " + e.getMessage());
        }

        // Find the best chicken in the final population
        Chicken bestChicken = population.get(0);
        for (Chicken chicken : population) {
            if (chicken.getScore() > bestChicken.getScore()) {
                bestChicken = chicken;
            }
        }

        // Create and save the best solution found
        Solution bestSolution = new Solution(bestChicken.getPieces());
        bestSolution.saveToFile(solutionPath);
        // System.out.println("Best solution: \n" + bestSolution.toString());
        System.out.println("Best score: " + bestChicken.getScore());
        
        return bestChicken.getScore();
    }

    public static void swapById(List<Piece> pieces, int id1, int id2) { 
        int pos1 = -1;
        int pos2 = -1;
        
        // Find positions of pieces with given ids
        for (int i = 0; i < pieces.size(); i++) {
            if (pieces.get(i).getId() == id1) {
                pos1 = i;
            }
            if (pieces.get(i).getId() == id2) {
                pos2 = i;
            }
            if (pos1 != -1 && pos2 != -1) {
                break;
            }
        }
        
        // Swap pieces at found positions
        if (pos1 != -1 && pos2 != -1) {
            Piece temp = pieces.get(pos1);
            pieces.set(pos1, pieces.get(pos2));
            pieces.set(pos2, temp);
        }
    }

    public static void crossOver(List<Piece> pieces1, List<Piece> pieces2, int start, int end) {
        // Check that start and end are not on borders
        // if (start % width == 0 || start % width == width - 1 || 
        //     start < width || start >= width * (height - 1) ||
        //     end % width == 0 || end % width == width - 1 ||
        //     end < width || end >= width * (height - 1)) {
        //     throw new IllegalArgumentException("Start and end positions must not be on borders");
        // }

        Map<Piece.PieceType, List<Piece>> replaced1 = new EnumMap<>(Piece.PieceType.class);
        replaced1.put(Piece.PieceType.CORNER, new ArrayList<>());
        replaced1.put(Piece.PieceType.BORDER, new ArrayList<>());
        replaced1.put(Piece.PieceType.CENTER, new ArrayList<>());
        
        Map<Piece.PieceType, List<Piece>> replaced2 = new EnumMap<>(Piece.PieceType.class);
        replaced2.put(Piece.PieceType.CORNER, new ArrayList<>());
        replaced2.put(Piece.PieceType.BORDER, new ArrayList<>());
        replaced2.put(Piece.PieceType.CENTER, new ArrayList<>());
        
        Map<Piece.PieceType, List<Integer>> idReplaced1 = new EnumMap<>(Piece.PieceType.class);
        idReplaced1.put(Piece.PieceType.CORNER, new ArrayList<>());
        idReplaced1.put(Piece.PieceType.BORDER, new ArrayList<>());
        idReplaced1.put(Piece.PieceType.CENTER, new ArrayList<>());

        Map<Piece.PieceType, List<Integer>> idReplaced2 = new EnumMap<>(Piece.PieceType.class);
        idReplaced2.put(Piece.PieceType.CORNER, new ArrayList<>());
        idReplaced2.put(Piece.PieceType.BORDER, new ArrayList<>());
        idReplaced2.put(Piece.PieceType.CENTER, new ArrayList<>());

        Map<Piece.PieceType, List<Integer>> different1 = new EnumMap<>(Piece.PieceType.class);
        different1.put(Piece.PieceType.CORNER, new ArrayList<>());
        different1.put(Piece.PieceType.BORDER, new ArrayList<>());
        different1.put(Piece.PieceType.CENTER, new ArrayList<>());

        Map<Piece.PieceType, List<Integer>> different2 = new EnumMap<>(Piece.PieceType.class);
        different2.put(Piece.PieceType.CORNER, new ArrayList<>());
        different2.put(Piece.PieceType.BORDER, new ArrayList<>());
        different2.put(Piece.PieceType.CENTER, new ArrayList<>());

        for (int i = start; i <= end; i++) {
            if (i%width >= start%width && i%width <= end%width) {
                replaced1.get(pieces1.get(i).getType()).add(pieces1.get(i));
                replaced2.get(pieces2.get(i).getType()).add(pieces2.get(i));
            }
        }
        for (Piece.PieceType type : Piece.PieceType.values()) {
            for (int i = 0; i < replaced1.get(type).size(); i++) {
                idReplaced1.get(type).add(replaced1.get(type).get(i).getId());
                idReplaced2.get(type).add(replaced2.get(type).get(i).getId());
            }
            for (int id : idReplaced1.get(type)) {
                if (!idReplaced2.get(type).contains(id)) {
                    different1.get(type).add(id);
                }
            }
            for (int id : idReplaced2.get(type)) {
                if (!idReplaced1.get(type).contains(id)) {
                    different2.get(type).add(id);
                }
            }
            for (int i = 0; i < different1.get(type).size(); i++) {
                swapById(pieces1, different1.get(type).get(i), different2.get(type).get(i));
            }
            int pos = 0;
            for (int i = start; i <= end; i++) {
                if (i%width >= start%width && i%width <= end%width) {
                    if (pieces1.get(i).getType() == type) {
                        pieces1.set(i, replaced2.get(type).get(pos));
                        pos++;
                    }
                }
            }
        }
        fixEdges(pieces1);
        fixEdges(pieces2);

    }

    public static void fixEdges(List<Piece> pieces){
        List<Piece> corners = new ArrayList<>();
        List<Piece> borders = new ArrayList<>();
        for (Piece piece : pieces){
            if (piece.getType() == Piece.PieceType.CORNER) {
                corners.add(piece);
            }
            else if (piece.getType() == Piece.PieceType.BORDER) {
                borders.add(piece);
            }
        }
        for (Piece piece : corners){
            piece.resetRotation();
        }
        for (Piece piece : borders){
            piece.resetRotation();
        }
        corners.get(0).rotate(1);
        corners.get(1).rotate(2);
        corners.get(3).rotate(3);

        int count = 0;
        for (Piece border: borders){
            if (count<width-2){
                border.rotate(2);
            }
            else if (count< width - 2 + 2*(height - 2)){
                if (count % 2 == width % 2){
                    border.rotate(1);
                }
                else{
                    border.rotate(3);
                }
            }
            count++;
        }
    }

}
