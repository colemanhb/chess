package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
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
    private Stack<ChessBoard> pastBoards;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        currentTeam = TeamColor.WHITE;
        pastBoards = new Stack<>();
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
        var piece = board.getPiece(startPosition);
        if(piece == null) {
            return null;
        }
        Collection<ChessMove> moves = piece.pieceMoves(board,startPosition);
        TeamColor color = piece.getTeamColor();
        HashSet<ChessMove> forcedMoves = new HashSet<>();
        for(var move : moves) {
            ChessBoard otherBoard = board.clone();
            otherBoard.movePiece(move);
            if(!isInCheck(color,otherBoard)) {
                forcedMoves.add(move);
            }
        }
        if(piece.getPieceType() == ChessPiece.PieceType.KING) {
            var queenSideCastleMove = castleMove(piece, startPosition, true);
            if(queenSideCastleMove != null)
                forcedMoves.add(queenSideCastleMove);
            var kingSideCastleMove = castleMove(piece, startPosition, false);
            if(kingSideCastleMove != null)
                forcedMoves.add(queenSideCastleMove);
        }
        else {
            var castleMove = castleMove(piece, startPosition, true);
            if(castleMove != null)
                forcedMoves.add(castleMove);
        }
        return forcedMoves;
    }

    private ChessMove castleMove(ChessPiece piece, ChessPosition startPosition, boolean queenSide) {
        var type = piece.getPieceType();
        if(type == ChessPiece.PieceType.KING || type == ChessPiece.PieceType.ROOK) {
            ChessPosition kingPosition;
            ChessPosition rookPosition;
            if(type == ChessPiece.PieceType.KING) {
                kingPosition = startPosition;
                rookPosition = castlePairPosition(startPosition,queenSide);
            }
            else {
                kingPosition = castlePairPosition(startPosition,queenSide);
                rookPosition = startPosition;
            }
            if(stagnant(kingPosition) && stagnant(rookPosition)) {
                if(noChecks(kingPosition, castleEndPosition(kingPosition))) {
                    return new ChessMove(startPosition, castleEndPosition(startPosition), null);
                }
            }
        }
        return null;
    }

    private boolean stagnant(ChessPosition pos) {
        var currentPiece = board.getPiece(pos);
        Stack<ChessBoard> pastBoardsCopy = pastBoards.clone();
        while(!pastBoards.empty()) {
            var pastBoard = pastBoards.pop();
            if(pastBoard.getPiece(pos) != currentPiece) {
                return false;
            }
        }
        pastBoards = pastBoardsCopy;
        return true;
    }

    private boolean noChecks(ChessPosition startPosition, ChessPosition endPosition) {

    }

    private ChessPosition castleEndPosition(ChessPosition startPosition) {
        var row = startPosition.getRow();
        var col = startPosition.getColumn();
        if(col == 1)
            return new ChessPosition(row,4);
        if(col == 5)
            return new ChessPosition(row,3);
        if(col == 8)
            return new ChessPosition(row,6);
        return null;
    }

    private ChessPosition castlePairPosition(ChessPosition startPosition, boolean queenSide) {
        var row = startPosition.getRow();
        var col = startPosition.getColumn();
        if(col == 1 || col == 8)
            return new ChessPosition(row,5);
        if(col == 5)
            if(queenSide)
                return new ChessPosition(row,8);
            else
                return new ChessPosition(row,1);
        return null;
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
            if(move.equals(validMove)) {
                valid = true;
                break;
            }
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
        var kingLocation = board.findKing(teamColor);
        if(board != null) {
            for (int i = 1; i <= 8; i++) {
                for (int j = 1; j <= 8; j++) {
                    var piece = board.getPiece(new ChessPosition(i, j));
                    if (piece != null && piece.getTeamColor() != teamColor) {
                        for (var move : piece.pieceMoves(board, new ChessPosition(i, j))) {
                            //System.out.println(move);
                            if (move.getEndPosition().equals(kingLocation)) {
                                //System.out.println("CHECK");
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean isInCheck(TeamColor teamColor, ChessBoard otherBoard) {
        var kingLocation = otherBoard.findKing(teamColor);
        if(otherBoard != null) {
            for (int i = 1; i <= 8; i++) {
                for (int j = 1; j <= 8; j++) {
                    var piece = otherBoard.getPiece(new ChessPosition(i, j));
                    if (piece != null && piece.getTeamColor() != teamColor) {
                        for (var move : piece.pieceMoves(otherBoard, new ChessPosition(i, j))) {
                            if(i == 6 && j == 5) {
                            }
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
        if(board == null)
            return true;
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                var pos = new ChessPosition(i, j);
                var piece = board.getPiece(pos);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    for (var move : piece.pieceMoves(board,pos)) {
                        ChessBoard otherBoard = board.clone();
                        otherBoard.movePiece(move);
                        if (!isInCheck(teamColor,otherBoard)) {
                            return false;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return currentTeam == chessGame.currentTeam && Objects.equals(board, chessGame.board) && Objects.equals(pastBoards, chessGame.pastBoards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentTeam, board, pastBoards);
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "currentTeam=" + currentTeam +
                ", board=" + board +
                '}';
    }
}
