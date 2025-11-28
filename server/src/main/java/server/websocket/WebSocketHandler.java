package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import io.javalin.websocket.*;
import model.GameData;
import org.jetbrains.annotations.NotNull;
import websocket.commands.UserGameCommand;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Objects;

import static chess.ChessGame.TeamColor.BLACK;
import static chess.ChessGame.TeamColor.WHITE;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final DataAccess dataAccess;

    public WebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }


    @Override
    public void handleConnect(@NotNull WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) throws Exception {
        try {
            UserGameCommand cmd = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            switch(cmd.getCommandType()) {
                case CONNECT -> connect(cmd.getAuthToken(), cmd.getGameID(), ctx.session);
                case MAKE_MOVE -> makeMove(cmd.getAuthToken(), cmd.getGameID(), cmd.getMove(), ctx.session);
                case LEAVE -> leave(cmd.getAuthToken(), cmd.getGameID(), ctx.session);
                case RESIGN -> resign(cmd.getAuthToken(), cmd.getGameID(), ctx.session);
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    private void resign(String authToken, Integer gameID, Session session) throws IOException, DataAccessException {
        if(checkAuth(authToken, session)) {
            return;
        }
        var username = dataAccess.findAuth(authToken);
        GameData gameData = dataAccess.getGame(gameID);
        if(!Objects.equals(gameData.blackUsername(), username) && !Objects.equals(gameData.whiteUsername(), username)) {
            var errorString = "Trying to resign as an observer";
            var errorMsg = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorString);
            session.getRemote().sendString(new Gson().toJson(errorMsg));
            return;
        }
        if(gameData.game().gameOver()) {
            var errorString = "Game is already over";
            var errorMsg = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorString);
            session.getRemote().sendString(new Gson().toJson(errorMsg));
            return;
        }
        var game = gameData.game();
        game.setGameOver(true);
        var newGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game);
        dataAccess.updateGame(newGameData);

        var notifString = String.format("%s resigned from game %d", username, gameData.gameID());
        var notifMsg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notifString);
        connections.broadcast(null, new Gson().toJson(notifMsg), gameData.gameID());
    }

    private void makeMove(String authToken, Integer gameID, ChessMove move, Session session) throws IOException, DataAccessException, InvalidMoveException {
        if(checkAuth(authToken, session)) {
            return;
        }
        var username = dataAccess.findAuth(authToken);
        GameData gameData = dataAccess.getGame(gameID);
        if(gameData == null) {
            var errorString = "Invalid game";
            var errorMsg = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorString);
            session.getRemote().sendString(new Gson().toJson(errorMsg));
            return;
        }
        ChessGame.TeamColor color = null;
        if(Objects.equals(gameData.whiteUsername(), username)) {
            color = WHITE;
        } else if(Objects.equals(gameData.blackUsername(), username)) {
            color = BLACK;
        }
        if(color == null) {
            var errorString = "Trying to make a move as an observer";
            var errorMsg = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorString);
            session.getRemote().sendString(new Gson().toJson(errorMsg));
            return;
        }
        var game = gameData.game();
        if(game.getTeamTurn() != color) {
            var errorString = "Trying to move out of turn";
            var errorMsg = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorString);
            session.getRemote().sendString(new Gson().toJson(errorMsg));
            return;
        }
        if(game.gameOver()) {
            var errorString = "Trying to move when game is over";
            var errorMsg = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorString);
            session.getRemote().sendString(new Gson().toJson(errorMsg));
            return;
        }
        var currentBoard = game.getBoard();
        if(currentBoard.getPiece(move.getStartPosition()) == null) {
            var errorString = "Trying to move a nonexistent piece";
            var errorMsg = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorString);
            session.getRemote().sendString(new Gson().toJson(errorMsg));
            return;
        } else if (currentBoard.getPiece(move.getStartPosition()).getTeamColor() != color) {
            var errorString = "Trying to move the other team's piece";
            var errorMsg = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorString);
            session.getRemote().sendString(new Gson().toJson(errorMsg));
            return;
        }
        var validMoves = game.validMoves(move.getStartPosition());
        if(!validMoves.contains(move)) {
            var errorString = "Invalid move";
            var errorMsg = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorString);
            session.getRemote().sendString(new Gson().toJson(errorMsg));
            return;
        }
        game.makeMove(move);
        var newGameData = new GameData(gameID, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game);
        dataAccess.updateGame(newGameData);

        var loadMsg = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, Integer.toString(gameID));
        connections.broadcast(null, new Gson().toJson(loadMsg), gameData.gameID());

        var notifString = String.format("%s moved from %s to %s", username, move.getStartPosition().toString(), move.getEndPosition().toString());
        var notifMsg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notifString);
        connections.broadcast(session, new Gson().toJson(notifMsg), gameData.gameID());

        var checkString = "";

        if(game.isInCheckmate(BLACK)) {
            checkString = String.format("%s (black team) is in checkmate", gameData.blackUsername());
        } else if(game.isInCheckmate(WHITE)) {
            checkString = String.format("%s (white team) is in checkmate", gameData.whiteUsername());
        } else if(game.isInCheck(BLACK)) {
            checkString = String.format("%s (black team) is in check", gameData.blackUsername());
        } else if(game.isInCheck(WHITE)) {
            checkString = String.format("%s (white team) is in check", gameData.whiteUsername());
        } else if(game.isInStalemate(BLACK)) {
            checkString = String.format("%s (black team) is in stalemate", gameData.blackUsername());
        } else if(game.isInStalemate(WHITE)) {
            checkString = String.format("%s (white team) is in stalemate", gameData.whiteUsername());
        }
        if(!checkString.isEmpty()) {
            var checkMsg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, checkString);
            connections.broadcast(session, new Gson().toJson(checkMsg), gameData.gameID());
        }

    }

    private void leave(String authToken, Integer gameID, Session session) throws DataAccessException, IOException {
        connections.remove(session);
        if(checkAuth(authToken, session)) {
            return;
        }
        var username = dataAccess.findAuth(authToken);
        var game = dataAccess.getGame(gameID);
        var player = true;
        if(game.blackUsername() != null && game.blackUsername().equals(username)) {
            dataAccess.removeFromGame(game.gameID(), BLACK);
        }
        else if(game.whiteUsername() != null && game.whiteUsername().equals(username)) {
            dataAccess.removeFromGame(game.gameID(), WHITE);
        }
        else {
            player = false;
        }
        if(player) {
            var notifString = String.format("%s left game %d", username, gameID);
            var notifMsg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notifString);
            connections.broadcast(session, new Gson().toJson(notifMsg), gameID);
        } else {
            var notifString = String.format("%s stopped watching game %d", username, gameID);
            var notifMsg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notifString);
            connections.broadcast(session, new Gson().toJson(notifMsg), gameID);
        }
    }

    private void connect(String authToken, int gameID, Session session) throws DataAccessException, IOException {
        connections.add(session, gameID);
        var username = dataAccess.findAuth(authToken);
        if(dataAccess.listGames().size() < gameID) {
            var errorString = "Invalid game ID";
            var errorMsg = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorString);
            session.getRemote().sendString(new Gson().toJson(errorMsg));
            return;
        }
        if(checkAuth(authToken, session)) {
            return;
        }
        var gameData = dataAccess.getGame(gameID);
        String notifString;
        if(Objects.equals(gameData.blackUsername(), username)) {
            notifString = String.format("%s joined game %d as black player", username, gameID);
        } else if(Objects.equals(gameData.whiteUsername(), username)) {
            notifString = String.format("%s joined game %d as white player", username, gameID);
        } else {
            notifString = String.format("%s started watching game %d", username, gameID);
        }
        var notifMsg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notifString);
        connections.broadcast(session, new Gson().toJson(notifMsg), gameID);
        var loadMsg = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, Integer.toString(gameID));
        session.getRemote().sendString(new Gson().toJson(loadMsg));
    }

    private boolean checkAuth(String authToken, Session session) throws DataAccessException, IOException {
        if(dataAccess.findAuth(authToken) == null) {
            var errorString = "Invalid auth token";
            var errorMsg = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorString);
            session.getRemote().sendString(new Gson().toJson(errorMsg));
            return true;
        }
        return false;
    }
}
