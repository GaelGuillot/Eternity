import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.ArrayList;

public class ChickenSorter {
    
    /**
     * Sorts a list of chickens in descending order by their scores
     * @param chickens List of chickens to sort
     */
    public static void sortByScore(List<Chicken> chickens) {
        Collections.sort(chickens, new Comparator<Chicken>() {
            @Override
            public int compare(Chicken c1, Chicken c2) {
                return Integer.compare(c2.getScore(), c1.getScore()); // Descending order
            }
        });
    }

    /**
     * Returns the top N chickens with highest scores
     * @param chickens List of chickens to filter
     * @param n Number of top chickens to keep
     * @return List containing the top N chickens
     */
    public static List<Chicken> getTopNChickens(List<Chicken> chickens, int n) {
        sortByScore(chickens);
        return chickens.subList(0, Math.min(n, chickens.size()));
    }

    /**
     * Creates a new population through weighted random selection
     * @param chickens List of chickens to select from
     * @return New list of chickens selected based on their scores as weights
     */
    public static List<Chicken> getWeightedPopulation(List<Chicken> chickens) {
        List<Chicken> newPopulation = new ArrayList<>();
        Random random = new Random();
        
        // Calculate total score
        int totalScore = 0;
        for (Chicken chicken : chickens) {
            totalScore += chicken.getScore();
        }
        
        // If all scores are 0, use uniform selection
        if (totalScore == 0) {
            for (int i = 0; i < chickens.size(); i++) {
                newPopulation.add(new Chicken(chickens.get(random.nextInt(chickens.size()))));
            }
            return newPopulation;
        }
        
        // Perform weighted selection
        for (int i = 0; i < chickens.size(); i++) {
            int randomValue = random.nextInt(totalScore);
            int cumulativeScore = 0;
            
            for (Chicken chicken : chickens) {
                cumulativeScore += chicken.getScore();
                if (randomValue < cumulativeScore) {
                    newPopulation.add(new Chicken(chicken));
                    break;
                }
            }
        }
        
        return newPopulation;
    }

    /**
     * Performs crossover operations between pairs of chickens
     * @param chickens List of chickens to perform crossover on
     * @param width Width of the board
     * @param height Height of the board
     * @return New list of chickens after crossover
     */
    public static List<Chicken> crossover(List<Chicken> chickens, int width, int height) {
        List<Chicken> newPopulation = new ArrayList<>();
        Random random = new Random();
        
        // Shuffle the population to randomize pairs
        List<Chicken> shuffledChickens = new ArrayList<>(chickens);
        Collections.shuffle(shuffledChickens);
        
        // Process chickens in pairs
        for (int i = 0; i < shuffledChickens.size(); i += 2) {
            if (i + 1 >= shuffledChickens.size()) {
                // If odd number of chickens, add the last one without crossover
                newPopulation.add(new Chicken(shuffledChickens.get(i)));
                break;
            }
            
            Chicken parent1 = shuffledChickens.get(i);
            Chicken parent2 = shuffledChickens.get(i + 1);
            
            // Create deep copies for crossover
            Chicken child1 = new Chicken(parent1);
            Chicken child2 = new Chicken(parent2);
            
            // Generate random start and end positions that don't include borders
            int start, end;
            // do {
                start = random.nextInt(width * height);
                end = random.nextInt(width * height);
                if (start > end) {
                    int temp = start;
                    start = end;
                    end = temp;
                }
            // } while (start % width == 0 || start % width == width - 1 || 
            //         start < width || start >= width * (height - 1) ||
            //         end % width == 0 || end % width == width - 1 ||
            //         end < width || end >= width * (height - 1));
            
            // Create deep copies of the pieces lists
            List<Piece> pieces1 = new ArrayList<>();
            List<Piece> pieces2 = new ArrayList<>();
            for (Piece piece : child1.getPieces()) {
                pieces1.add(new Piece(piece.getId(), piece.getValues().clone(), piece.type));
            }
            for (Piece piece : child2.getPieces()) {
                pieces2.add(new Piece(piece.getId(), piece.getValues().clone(), piece.type));
            }
            
            // Perform crossover on the copies
            Main.crossOver(pieces1, pieces2, start, end);
            Main.fixEdges(pieces1);
            Main.fixEdges(pieces2);
            
            // Create new chickens with the crossed-over pieces
            child1.setPieces(pieces1);
            child2.setPieces(pieces2);
            
            // Add both children to the new population
            newPopulation.add(child1);
            newPopulation.add(child2);
        }
        
        return newPopulation;
    }

    /**
     * Recalculates the score for each chicken in the list
     * @param eval The evaluator used to calculate scores
     * @param chickens List of chickens to recalculate scores for
     */
    public static boolean recalculateScores(Eval eval, List<Chicken> chickens, int maxScore) {
        boolean bestSolutionFound = false;
        for (Chicken chicken : chickens) {
            Solution solution = new Solution(chicken.getPieces());
            int newScore = eval.evaluateSolution(solution.getSolution());
            if (newScore == maxScore){
                System.out.println("Best solution found");
                bestSolutionFound = true;
            }
            chicken.setScore(newScore);
        }
        return bestSolutionFound;
    }

    /**
     * Applies mutation to a list of chickens
     * @param chickens List of chickens to mutate
     * @param width Width of the board
     * @param height Height of the board
     * @param mutationRate Probability of mutation for each chicken
     */
    public static void mutate(List<Chicken> chickens, int width, int height, double mutationRate) {
        Random random = new Random();
        
        for (Chicken chicken : chickens) {
            if (random.nextDouble() > mutationRate) {
                continue; // Skip mutation based on mutation rate
            }
            
            List<Piece> pieces = chicken.getPieces();
            
            // Randomly swap two pieces
            int pos1 = random.nextInt(pieces.size());
            int pos2 = random.nextInt(pieces.size());
            
            // Ensure we're not swapping pieces of different types
            if (pieces.get(pos1).getType() == pieces.get(pos2).getType()) {
                Collections.swap(pieces, pos1, pos2);
            }
            
            // Randomly rotate some pieces
            for (Piece piece : pieces) {
                if (random.nextDouble() < 0.1) { // 10% chance to rotate each piece
                    int rotations = random.nextInt(4); // Random number of rotations (0-3)
                    piece.rotate(rotations);
                }
            }
            
            // Fix edges to ensure border and corner pieces are valid
            Main.fixEdges(pieces);
        }
    }
} 