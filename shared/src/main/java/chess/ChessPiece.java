package chess;

import java.util.Collection;
import java.util.HashSet;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private ChessGame.TeamColor pieceColor;
    private ChessPiece.PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        if(type == ChessPiece.PieceType.BISHOP) {
            return bishopMoves(board, myPosition);
        }
        return new HashSet<ChessMove>();
    }

    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> moves = new HashSet<ChessMove>();
        var row = myPosition.getRow();
        var col = myPosition.getCol();
        //lower right
        for(int i = 0; i < 4; i ++) {
            while(true) {
                if(i == 0)
                {
                    row -= 1;
                    col += 1;
                }
                if(i == 1) {
                    row -= 1;
                    col -= 1;
                }
                if(i == 2) {
                    row += 1;
                    col -= 1;
                }
                if(i == 3) {
                    row += 1;
                    col += 1;
                }
                if(row * col == 0 || row == 9 || col == 9) {
                    break;
                }
                ChessPosition endPosition = new ChessPosition(row,col);
                moves.add(new ChessMove(myPosition,endPosition,null));
                //break if enemy piece found
                if(board.getPiece(endPosition) != null) {
                    break;
                }
            }
            row = myPosition.getRow();
            col = myPosition.getCol();
        }

        return moves;
    }
}
