import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    static int width;
    static int height;
    static String filePath = "EternitySolver/lib/benchs/pieces_set/pieces_16x16.txt";
    static List<Piece> pieces = new ArrayList<>(); 

    public static void main(String[] args) throws Exception {
        // Parse the initial board:
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
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
                pieces.add(new Piece(id, values, type));
                id++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Eval eval = new Eval("EternitySolver/lib/benchs/pieces_set/pieces_" + String.format("%02d", width) + "x" + String.format("%02d", height) + ".txt");
        Eval eval = new Eval("EternitySolver/lib/benchs/benchEternity2WithoutHint.txt");
        // Eval eval = new Eval(filePath);

        pieces = new Init(pieces, width, height).shuffle(); // Shuffle the pieces
        Solution bestSolution = new Solution(pieces);
        bestSolution.saveToFile("EternitySolver/lib/solutions/solution_" + String.format("%02d", width) + "x" + String.format("%02d", height) + ".txt");

        int bestScore = eval.evaluateSolution(bestSolution.getSolution());


        int totalIterations = 1000000;
        int progressBarWidth = 50;
        while (eval.count < totalIterations) {
            pieces = new Init(pieces, width, height).shuffle(); // Shuffle the pieces
            Solution solution = new Solution(pieces);
            if (eval.evaluateSolution(solution.getSolution()) > bestScore) {
                bestScore = eval.evaluateSolution(solution.getSolution());
                bestSolution = solution;
            }
            
            // Update progress bar
            int progress = (int)((eval.count * progressBarWidth) / totalIterations);
            System.out.print("\r[");
            for (int i = 0; i < progressBarWidth; i++) {
                if (i < progress) System.out.print("=");
                else if (i == progress) System.out.print(">");
                else System.out.print(" ");
            }
            System.out.print("] " + (eval.count * 100 / totalIterations) + "%");
        }
        System.out.println(); // New line after progress bar completes
        bestSolution.saveToFile("EternitySolver/lib/solutions/solution_" + String.format("%02d", width) + "x" + String.format("%02d", height) + ".txt");
        System.out.println("Best score: " + bestScore);

        // List<Piece> pieces1 = new Init(pieces, width, height).shuffle();
        // List<Piece> pieces2 = new Init(pieces, width, height).shuffle();

        // Solution solution1 = new Solution(pieces1);
        // Solution solution2 = new Solution(pieces2);

        // System.out.println(solution1.toString());
        // System.out.println("Score: " + eval.evaluateSolution(solution1.getSolution()));
        // System.out.println();
        // System.out.println(solution2.toString());
        // System.out.println("Score: " + eval.evaluateSolution(solution2.getSolution()));
        // System.out.println();

        // crossOver(pieces1, pieces2, 12, 18);
        // Solution crossOverSolution1 = new Solution(pieces1);
        // Solution crossOverSolution2 = new Solution(pieces2);
        // System.out.println(crossOverSolution1.toString());
        // System.out.println("Score: " + eval.evaluateSolution(crossOverSolution1.getSolution()));
        // System.out.println();
        // System.out.println(crossOverSolution2.toString());
        // System.out.println("Score: " + eval.evaluateSolution(crossOverSolution2.getSolution()));
        // System.out.println();

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
        if (start % width == 0 || start % width == width - 1 || 
            start < width || start >= width * (height - 1) ||
            end % width == 0 || end % width == width - 1 ||
            end < width || end >= width * (height - 1)) {
            throw new IllegalArgumentException("Start and end positions must not be on borders");
        }

        List<Piece> replaced1 = new ArrayList<>();
        List<Piece> replaced2 = new ArrayList<>();
        List<Integer> idReplaced1 = new ArrayList<>();
        List<Integer> idReplaced2 = new ArrayList<>();
        List<Integer> different1 = new ArrayList<>();
        List<Integer> different2 = new ArrayList<>();

        for (int i = start; i <= end; i++) {
            if (i%width >= start%width && i%width <= end%width) {
                replaced1.add(pieces1.get(i));
                replaced2.add(pieces2.get(i));
            }
        }
        for (int i = 0; i < replaced1.size(); i++) {
            idReplaced1.add(replaced1.get(i).getId());
            idReplaced2.add(replaced2.get(i).getId());
        }
        for (int id : idReplaced1) {
            if (!idReplaced2.contains(id)) {
                different1.add(id);
            }
        }
        for (int id : idReplaced2) {
            if (!idReplaced1.contains(id)) {
                different2.add(id);
            }
        }
        for (int i = 0; i < different1.size(); i++) {
            swapById(pieces1, different1.get(i), different2.get(i));
        }

        int pos = 0;
        for (int i = start; i <= end; i++) {
            if (i%width >= start%width && i%width <= end%width) {
                pieces1.set(i, replaced2.get(pos));
                pos++;
            }
        }
    }
}
