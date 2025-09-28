package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Stack;

import static java.lang.Math.abs;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private static final ChessPosition WHITE_KING_POSITION = new ChessPosition(1, 5);
    private static final ChessPosition WHITE_QUEENSIDE_ROOK_POSITION = new ChessPosition(1, 1);
    private static final ChessPosition WHITE_KINGSIDE_ROOK_POSITION = new ChessPosition(1, 8);
    private static final ChessMove WHITE_QUEENSIDE_CASTLE = new ChessMove(WHITE_KING_POSITION, new ChessPosition(1, 3), null, true);
    private static final ChessMove WHITE_KINGSIDE_CASTLE = new ChessMove(WHITE_KING_POSITION, new ChessPosition(1, 7), null, true);
    private static final ChessMove WHITE_QUEENSIDE_ROOK_CASTLE = new ChessMove(WHITE_QUEENSIDE_ROOK_POSITION, new ChessPosition(1, 4), null, true);
    private static final ChessMove WHITE_KINGSIDE_ROOK_CASTLE = new ChessMove(WHITE_KINGSIDE_ROOK_POSITION, new ChessPosition(1, 6), null, true);

    private static final ChessPosition BLACK_KING_POSITION = new ChessPosition(8, 5);
    private static final ChessPosition BLACK_QUEENSIDE_ROOK_POSITION = new ChessPosition(8, 1);
    private static final ChessPosition BLACK_KINGSIDE_ROOK_POSITION = new ChessPosition(8, 8);
    private static final ChessMove BLACK_QUEENSIDE_CASTLE = new ChessMove(BLACK_KING_POSITION, new ChessPosition(8, 3), null, true);
    private static final ChessMove BLACK_KINGSIDE_CASTLE = new ChessMove(BLACK_KING_POSITION, new ChessPosition(8, 7), null, true);
    private static final ChessMove BLACK_QUEENSIDE_ROOK_CASTLE = new ChessMove(BLACK_QUEENSIDE_ROOK_POSITION, new ChessPosition(8, 4), null, true);
    private static final ChessMove BLACK_KINGSIDE_ROOK_CASTLE = new ChessMove(BLACK_KINGSIDE_ROOK_POSITION, new ChessPosition(8, 6), null, true);

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
            if(queenSideCastleMove != null) {
                forcedMoves.add(queenSideCastleMove);
            }
            var kingSideCastleMove = castleMove(piece, startPosition, false);
            if(kingSideCastleMove != null) {
                forcedMoves.add(kingSideCastleMove);
            }
        }
        else {
            var castleMove = castleMove(piece, startPosition, true);
            if(castleMove != null) {
                forcedMoves.add(castleMove);
            }
        }
        if(piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            var enPassantMove = enPassantMove(startPosition, true);
            if(enPassantMove != null)
                forcedMoves.add(enPassantMove);
            enPassantMove = enPassantMove(startPosition, false);
            if(enPassantMove != null)
                forcedMoves.add(enPassantMove);
        }
        return forcedMoves;
    }

    private ChessMove enPassantMove(ChessPosition startPosition, boolean right) {
        var piece = board.getPiece(startPosition);
        var color = piece.getTeamColor();
        var row = startPosition.getRow();
        var col = startPosition.getColumn();
        int rowIndex = (color == TeamColor.BLACK ? -1 : 1);
        int colIndex = (right ? 1 : -1);
        if((color == TeamColor.BLACK && row == 4) || (color == TeamColor.WHITE && row == 5)) {
            if(nextToEnemy(startPosition, right)) {
                if(justMoved(new ChessPosition(row, col + colIndex))) {
                    var endPosition = new ChessPosition(row + rowIndex, col + colIndex);
                    return new ChessMove(startPosition,endPosition,null);
                }
            }
        }
        return null;
    }

    private boolean justMoved(ChessPosition pos) {
        var currentRow = pos.getRow();
        var currentCol = pos.getColumn();
        var piece = board.getPiece(pos);
        int oldRow = 0;
        if(currentRow == 5)
            oldRow = 7;
        else if(currentRow == 4)
            oldRow = 2;
        var oldPosition = new ChessPosition(oldRow,currentCol);
        var oldBoard = pastBoards.pop();
        var oldPiece = oldBoard.getPiece(oldPosition);
        pastBoards.push(oldBoard);
        return oldPiece != null && oldPiece.equals(piece);
    }

    private boolean nextToEnemy(ChessPosition pos, boolean right) {
        var col = pos.getColumn();
        var row = pos.getRow();
        var piece = board.getPiece(pos);
        var color = piece.getTeamColor();
        if(right) {
            if(col == 8)
                return false;
            var rightSide = board.getPiece(new ChessPosition(row,col + 1));
            return rightSide != null && rightSide.getPieceType() == ChessPiece.PieceType.PAWN && rightSide.getTeamColor() != color;
        }
        else {
            if(col == 1)
                return false;
            var leftSide = board.getPiece(new ChessPosition(row,col - 1));
            return leftSide != null && leftSide.getPieceType() == ChessPiece.PieceType.PAWN && leftSide.getTeamColor() != color;
        }
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
                if(pathIsClear(identifyCastle(startPosition,queenSide)) && !isInCheck(piece.getTeamColor())) {
                    return identifyCastle(startPosition,queenSide);
                }
            }
        }
        return null;
    }

    private boolean stagnant(ChessPosition pos) {
        if(pos == null)
            return false;
        var currentPiece = board.getPiece(pos);
        for(var pastBoard : pastBoards) {
            var pastPiece = pastBoard.getPiece(pos);
            if(pastPiece == null || !pastBoard.getPiece(pos).equals(currentPiece)) {
                return false;
            }
        }
        return true;
    }

    private boolean pathIsClear(ChessMove move) {
        var startPosition = move.getStartPosition();
        var endPosition = move.getEndPosition();
        var startRow = startPosition.getRow();
        var startCol = startPosition.getColumn();
        var endRow = endPosition.getRow();
        var endCol = endPosition.getColumn();
        if(startRow > endRow) {
            var temp = startRow;
            startRow = endRow;
            endRow = temp;
        }
        if(startCol > endCol) {
            var temp = startCol;
            startCol = endCol;
            endCol = temp;
        }
        var color = board.getPiece(startPosition).getTeamColor();
        for(int i = startRow; i <= endRow; i ++) {
            for(int j = startCol; j <= endCol; j ++) {
                if(j == 5)
                    continue;
                var thisSpot = new ChessPosition(i,j);
                if(board.getPiece(thisSpot) != null)
                    return false;
                ChessBoard otherBoard = board.clone();
                var thisMove = new ChessMove(startPosition, thisSpot,null);
                otherBoard.movePiece(thisMove);
                if(isInCheck(color,otherBoard)) {
                    return false;
                }
            }
        }
        return true;
    }

    private ChessMove identifyCastle(ChessPosition startPosition, boolean queenSide) {
        if(startPosition.equals(WHITE_KINGSIDE_ROOK_POSITION))
            return WHITE_KINGSIDE_ROOK_CASTLE;
        if(startPosition.equals(WHITE_QUEENSIDE_ROOK_POSITION))
            return WHITE_QUEENSIDE_ROOK_CASTLE;
        if(startPosition.equals(WHITE_KING_POSITION)) {
            if(queenSide)
                return WHITE_QUEENSIDE_CASTLE;
            else
                return WHITE_KINGSIDE_CASTLE;
        }
        if(startPosition.equals(BLACK_KINGSIDE_ROOK_POSITION))
            return BLACK_KINGSIDE_ROOK_CASTLE;
        if(startPosition.equals(BLACK_QUEENSIDE_ROOK_POSITION))
            return BLACK_QUEENSIDE_ROOK_CASTLE;
        if(startPosition.equals(BLACK_KING_POSITION)) {
            if(queenSide)
                return BLACK_QUEENSIDE_CASTLE;
            else
                return BLACK_KINGSIDE_CASTLE;
        }
        return null;
    }

    private ChessMove identifyOtherCastle(ChessMove move) {
        if(move.equals(WHITE_KINGSIDE_CASTLE))
            return WHITE_KINGSIDE_ROOK_CASTLE;
        if(move.equals(WHITE_KINGSIDE_ROOK_CASTLE))
            return WHITE_KINGSIDE_CASTLE;
        if(move.equals(WHITE_QUEENSIDE_CASTLE))
            return WHITE_QUEENSIDE_ROOK_CASTLE;
        if(move.equals(WHITE_QUEENSIDE_ROOK_CASTLE))
            return WHITE_QUEENSIDE_CASTLE;
        if(move.equals(BLACK_KINGSIDE_CASTLE))
            return BLACK_KINGSIDE_ROOK_CASTLE;
        if(move.equals(BLACK_KINGSIDE_ROOK_CASTLE))
            return BLACK_KINGSIDE_CASTLE;
        if(move.equals(BLACK_QUEENSIDE_CASTLE))
            return BLACK_QUEENSIDE_ROOK_CASTLE;
        if(move.equals(BLACK_QUEENSIDE_ROOK_CASTLE))
            return BLACK_QUEENSIDE_CASTLE;
        return null;
    }

    private ChessPosition castlePairPosition(ChessPosition startPosition, boolean queenSide) {
        if(startPosition.equals(WHITE_KINGSIDE_ROOK_POSITION) || startPosition.equals(WHITE_QUEENSIDE_ROOK_POSITION))
            return WHITE_KING_POSITION;
        if(startPosition.equals(WHITE_KING_POSITION)) {
            if(queenSide)
                return WHITE_QUEENSIDE_ROOK_POSITION;
            else
                return WHITE_KINGSIDE_ROOK_POSITION;
        }
        if(startPosition.equals(BLACK_KINGSIDE_ROOK_POSITION) || startPosition.equals(BLACK_QUEENSIDE_ROOK_POSITION))
            return BLACK_KING_POSITION;
        if(startPosition.equals(BLACK_KING_POSITION)) {
            if(queenSide)
                return BLACK_QUEENSIDE_ROOK_POSITION;
            else
                return BLACK_KINGSIDE_ROOK_POSITION;
        }
        return null;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if(move == null)
            throw new InvalidMoveException();
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
            var piece = board.getPiece(startPosition);
            board.movePiece(move);
            var distanceTraveled = abs(move.getStartPosition().getColumn() - move.getEndPosition().getColumn());
            var unlabeledCastle = piece.getPieceType() == ChessPiece.PieceType.KING && distanceTraveled == 2;
            if(move.isCastle() || unlabeledCastle) {
                board.movePiece(identifyOtherCastle(move));
            }
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
