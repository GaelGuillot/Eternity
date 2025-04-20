import java.util.List;
import java.util.ArrayList;

public class Chicken {
    private List<Piece> pieces;
    private int score;

    public Chicken(List<Piece> pieces, int score) {
        this.pieces = pieces;
        this.score = score;
    }

    // Copy constructor
    public Chicken(Chicken other) {
        this.score = other.score;
        this.pieces = new ArrayList<>();
        // Create deep copies of each piece
        for (Piece piece : other.pieces) {
            this.pieces.add(new Piece(piece.getId(), piece.getValues().clone(), piece.type));
        }
    }

    public List<Piece> getPieces() {
        return pieces;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setPieces(List<Piece> pieces) {
        this.pieces = pieces;
    }
}

    
