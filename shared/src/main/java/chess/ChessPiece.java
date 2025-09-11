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
        if(type == ChessPiece.PieceType.KING) {
            return kingMoves(board,myPosition);
        }
        if(type == ChessPiece.PieceType.KNIGHT) {
            return knightMoves(board,myPosition);
        }
        if(type == PieceType.PAWN) {
            return pawnMoves(board,myPosition);
        }
        if(type == PieceType.QUEEN) {
            return queenMoves(board,myPosition);
        }
        if(type == PieceType.ROOK) {
            return rookMoves(board,myPosition);
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
                if(!validSquare(row,col)) {
                    break;
                }
                ChessPosition endPosition = new ChessPosition(row,col);
                if(board.getPiece(endPosition) != null) {
                    if(board.getPiece(endPosition).getTeamColor() != pieceColor) {
                        moves.add(new ChessMove(myPosition,endPosition,null));
                    }
                    break;
                }
                moves.add(new ChessMove(myPosition,endPosition,null));
                //break if enemy piece found
            }
            row = myPosition.getRow();
            col = myPosition.getCol();
        }

        return moves;
    }
    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> moves = new HashSet();
        var row = myPosition.getRow();
        var col = myPosition.getCol();
        for(int i = -1; i <= 1; i ++) {
            for(int j = -1; j <= 1; j ++) {
                if(i == 0 && j == 0) {
                    continue;
                }
                int endRow = row + i;
                int endCol = col + j;
                if(validSquare(endRow,endCol)) {
                    ChessPosition endPosition = new ChessPosition(endRow,endCol);
                    if(board.getPiece(endPosition) == null) {
                        moves.add(new ChessMove(myPosition,endPosition,null));
                    }
                    else if(board.getPiece(endPosition).getTeamColor() != pieceColor) {
                        moves.add(new ChessMove(myPosition,endPosition,null));
                    }
                }
            }
        }
        return moves;
    }
    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> moves = new HashSet<ChessMove>();
        int row = myPosition.getRow();
        int col = myPosition.getCol();
        for(int i = -2; i <= 2; i ++) {
            for(int j = -2; j <= 2; j ++) {
                if(i * j != -2 && i * j != 2)
                    continue;
                int endRow = row + i;
                int endCol = col + j;
                if(validSquare(endRow,endCol)) {
                    ChessPosition endPosition = new ChessPosition(endRow,endCol);
                    if(board.getPiece(endPosition) == null) {
                        moves.add(new ChessMove(myPosition,endPosition,null));
                    }
                    else if(board.getPiece(endPosition).getTeamColor() != pieceColor) {
                        moves.add(new ChessMove(myPosition,endPosition,null));
                    }
                }
            }
        }
        return moves;
    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> moves = new HashSet<>();
        int row = myPosition.getRow();
        int col = myPosition.getCol();
        boolean blocked = true;
        if(pieceColor == ChessGame.TeamColor.WHITE) {
            ChessMove move = pawnHelper(board, myPosition,row + 1, col, false);
            if(move != null) {
                blocked = false;
                moves.add(move);
                if(move.getPromotionPiece() == PieceType.QUEEN) {
                    addOptions(moves,move);
                }
            }
            if(row == 2) {
                if(!blocked) {
                    move = pawnHelper(board, myPosition, row + 2, col, false);
                    if(move != null) {
                        moves.add(move);
                    }
                }
            }
            move = pawnHelper(board, myPosition, row + 1, col + 1, true);
            if(move != null) {
                moves.add(move);
                if(move.getPromotionPiece() == PieceType.QUEEN) {
                    addOptions(moves,move);
                }
            }
            move = pawnHelper(board, myPosition, row + 1, col - 1, true);
            if(move != null) {
                moves.add(move);
                if(move.getPromotionPiece() == PieceType.QUEEN) {
                    addOptions(moves,move);
                }
            }
        }
        if(pieceColor == ChessGame.TeamColor.BLACK) {
            ChessMove move = pawnHelper(board, myPosition,row - 1, col, false);
            if(move != null) {
                blocked = false;
                moves.add(move);
                if(move.getPromotionPiece() == PieceType.QUEEN) {
                    addOptions(moves,move);
                }
            }
            if(!blocked) {
                if(row == 7) {
                    move = pawnHelper(board, myPosition, row - 2, col, false);
                    if(move != null) {
                        moves.add(move);
                    }
                }
            }
            move = pawnHelper(board, myPosition, row - 1, col + 1, true);
            if(move != null) {
                moves.add(move);
                if(move.getPromotionPiece() == PieceType.QUEEN) {
                    addOptions(moves,move);
                }
            }
            move = pawnHelper(board, myPosition, row - 1, col - 1, true);
            if(move != null) {
                moves.add(move);
                if(move.getPromotionPiece() == PieceType.QUEEN) {
                    addOptions(moves,move);
                }
            }
        }
        return moves;
    }

    private ChessMove pawnHelper(ChessBoard board, ChessPosition startPosition, int row, int col, boolean capture) {
        if(validSquare(row,col)) {
            PieceType promotionPiece = null;
            if((row == 1 && pieceColor == ChessGame.TeamColor.BLACK) || (row == 8 && pieceColor == ChessGame.TeamColor.WHITE)) {
                promotionPiece = PieceType.QUEEN;
            }
            ChessPosition endPosition = new ChessPosition(row,col);
            if(capture) {
                if(board.getPiece(endPosition) != null && board.getPiece(endPosition).getTeamColor() != pieceColor) {
                    return new ChessMove(startPosition, endPosition, promotionPiece);
                }
            }
            else {
                if(board.getPiece(endPosition) == null) {
                    return new ChessMove(startPosition, endPosition, promotionPiece);
                }
            }
        }
        return null;
    }

    private boolean validSquare(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition) {
        var moves = new HashSet<ChessMove>();
        int row = myPosition.getRow();
        int col = myPosition.getCol();
        for(int i = -1; i <= 1; i ++) {
            for(int j = -1; j <= 1; j ++) {
                if(i == 0 && j == 0) {
                    continue;
                }
                while(validSquare(row + i,col + j)) {
                    row += i;
                    col += j;
                    var endPosition = new ChessPosition(row,col);
                    if(board.getPiece(endPosition) == null) {
                        moves.add(new ChessMove(myPosition,endPosition,null));
                    }
                    else {
                        if(board.getPiece(endPosition).getTeamColor() == pieceColor) {
                            break;
                        }
                        else {
                            moves.add(new ChessMove(myPosition,endPosition,null));
                            break;
                        }
                    }
                }
                row = myPosition.getRow();
                col = myPosition.getCol();
            }
        }
        return moves;
    }

    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition) {
        var moves = new HashSet<ChessMove>();
        int row = myPosition.getRow();
        int col = myPosition.getCol();
        for(int i = -1; i <= 1; i ++) {
            for(int j = -1; j <= 1; j ++) {
                if(i * j != 0 || i + j == 0) {
                    continue;
                }
                while(validSquare(row + i, col + j)) {
                    row += i;
                    col += j;
                    var endPosition = new ChessPosition(row,col);
                    if(board.getPiece(endPosition) == null) {
                        moves.add(new ChessMove(myPosition,endPosition,null));
                    }
                    else {
                        if(board.getPiece(endPosition).getTeamColor() == pieceColor) {
                            break;
                        }
                        else {
                            moves.add(new ChessMove(myPosition,endPosition,null));
                            break;
                        }
                    }
                }
                row = myPosition.getRow();
                col = myPosition.getCol();
            }
        }
        return moves;
    }

    private void addOptions(HashSet<ChessMove> moves, ChessMove move) {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        moves.add(new ChessMove(start,end, PieceType.ROOK));
        moves.add(new ChessMove(start,end, PieceType.BISHOP));
        moves.add(new ChessMove(start,end, PieceType.KNIGHT));
    }
}
