public class Piece {
    int id; // Unique ID for the piece
    int[] values; // Square pieces, four values
    int rotations = 0; // Number of rotations (0-3)
    PieceType type;
    public enum PieceType {
        CORNER, BORDER, CENTER
    }

    public Piece(int id, int[] values, PieceType type) {
        this.id = id;
        this.values = values;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public int[] getValues() {
        return values;
    }

    public PieceType getType() {
        return type;
    }

    public int getRotation() {
        return rotations;
    }

    public void rotate(int times) {
        for (int i = 0; i < times; i++) {
            int tempVal = values[3];
            for (int j = 3; j > 0; j--) {
                values[j] = values[j - 1];
            }
            values[0] = tempVal;
        }
        rotations = (rotations + times) % 4;
    }

    public void resetRotation() {
        this.rotate(4 - rotations);
    }

    
}

