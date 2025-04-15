import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class Init {
    List<Piece> pieces;
    int width;
    int height;

    public Init(List<Piece> pieces, int width, int height) {
        this.width = width;
        this.height = height;
        this.pieces = pieces;
    }

    public List<Piece> shuffle(){
        List<Piece> corners = new ArrayList<>();
        List<Piece> borders = new ArrayList<>();
        List<Piece> centers = new ArrayList<>();
        List<Piece> shuffledPieces = new ArrayList<>();

        // Create deep copies of pieces
        for (Piece piece : pieces) {
            Piece copy = new Piece(piece.getId(), piece.getValues().clone(), piece.type);
            if (piece.type == Piece.PieceType.CORNER) {
                corners.add(copy);
            } else if (piece.type == Piece.PieceType.BORDER) {
                borders.add(copy);
            } else {
                centers.add(copy);
            }
        }

        Collections.shuffle(corners);
        corners.get(0).rotate(1);
        corners.get(1).rotate(2);
        corners.get(3).rotate(3);

        Collections.shuffle(borders);
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

        Collections.shuffle(centers);
        for (Piece center: centers){
            int rotation = (int) (Math.random() * 4);
            center.rotate(rotation);
        }

        for (int i=0; i<width*height; i++){
            if (i == 0 || i == width-1 || i == (width*(height-1)) || i == (width*height)-1){
                shuffledPieces.add(corners.remove(0));
            }
            else if (i < width || i % width == 0 || i % width == width-1 || i >= (width*(height-1))){
                shuffledPieces.add(borders.remove(0));
            }
            else {
                shuffledPieces.add(centers.remove(0));
            }
        }
        return shuffledPieces;
    }
}


