package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Stack;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor currentTeam;
    private ChessBoard board;
    private Stack<ChessBoard> pastBoards = new Stack<>();

    public ChessGame() {

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTeam;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTeam = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        if(board.getPiece(startPosition) == null) {
            return null;
        }
        Collection<ChessMove> moves = board.getPiece(startPosition).pieceMoves(board,startPosition);
        TeamColor color = board.getPiece(startPosition).getTeamColor();
        HashSet<ChessMove> forcedMoves = new HashSet<>();
        for(var move : moves) {
            board.movePiece(move);
            if(!isInCheck(color)) {
                forcedMoves.add(move);
            }
            undoMove();
        }
        return forcedMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if(board.getPiece(move.getStartPosition()) == null || board.getPiece(move.getStartPosition()).getTeamColor() != currentTeam)
            throw new InvalidMoveException();
        ChessPosition startPosition = move.getStartPosition();
        Collection<ChessMove> validMoves = validMoves(startPosition);
        boolean valid = false;
        for(var validMove : validMoves) {
            if(move.equals(validMove))
                valid = true;
        }
        if(!valid)
            throw new InvalidMoveException();
        else {
            pastBoards.push(board.clone());
            board.movePiece(move);
        }
        switch(currentTeam) {
            case BLACK -> currentTeam = TeamColor.WHITE;
            case WHITE -> currentTeam = TeamColor.BLACK;
        }
    }
    
    public void undoMove() {
        if(!pastBoards.empty())
            board = pastBoards.pop();
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        var kingLocation = findKing(teamColor);
        if(board != null) {
            for (int i = 1; i <= 8; i++) {
                for (int j = 1; j <= 8; j++) {
                    var piece = board.getPiece(new ChessPosition(i, j));
                    if (piece != null && piece.getTeamColor() != teamColor) {
                        for (var move : piece.pieceMoves(board, new ChessPosition(i, j))) {
                            if (move.getEndPosition().equals(kingLocation)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private ChessPosition findKing(TeamColor teamColor) {
        if(board != null) {
            for(int i = 1; i <= 8; i ++) {
                for(int j = 1; j <= 8; j ++) {
                    var pos = new ChessPosition(i,j);
                    var piece = board.getPiece(pos);
                    if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && isInMate(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && isInMate(teamColor);
    }

    public boolean isInMate(TeamColor teamColor) {
        for(int i = 1; i <= 8; i ++) {
            for(int j = 1; j <= 8; j ++) {
                var pos = new ChessPosition(i,j);
                if(board!= null) {
                    var piece = board.getPiece(pos);
                    if (piece != null && piece.getTeamColor() == teamColor) {
                        for(var move : validMoves(pos)) {
                            board.movePiece(move);
                            if(!isInCheck(teamColor)) {
                                undoMove();
                                return false;
                            }
                            undoMove();
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
