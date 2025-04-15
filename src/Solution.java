import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Solution {
    List<Piece> pieces;

    Solution(List<Piece> pieces) {
        this.pieces = pieces;
    }

    public void saveToFile(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Piece piece : pieces) {
                writer.write(piece.getId() + " " + piece.getRotation());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    public List<Pair<Integer, Integer>> getSolution(){
        List<Pair<Integer, Integer>> solution = new ArrayList<>();
        for (Piece piece : pieces) {
            solution.add(new Pair<>(piece.getId(), piece.getRotation()));
        }
        return solution;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        for (Piece piece : pieces) {
            sb.append(piece.values[0]).append(" ")
            .append(piece.values[1]).append(" ")
            .append(piece.values[2]).append(" ")
            .append(piece.values[3]).append("    ")
            .append(piece.getRotation()).append("\n");
        }
        return sb.toString();
    }

}