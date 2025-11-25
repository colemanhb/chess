package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
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
                case MAKE_MOVE -> makeMove(cmd.getAuthToken(), cmd.getMove(), ctx.session);
                case LEAVE -> leave(cmd.getAuthToken(), ctx.session);
                //case RESIGN -> resign(cmd.getAuthToken(), cmd.getGameID(), ctx.session);
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    private void makeMove(String authToken, ChessMove move, Session session) throws IOException, DataAccessException {
        if(checkAuth(authToken, session)) {
            return;
        }
        var username = dataAccess.findAuth(authToken);
        var games = dataAccess.listGames();
        GameData gameData = null;
        ChessGame.TeamColor color = null;
        for(var game : games) {
            if(game.blackUsername() != null && game.blackUsername().equals(username)) {
                gameData = game;
                color = BLACK;
            }
            if(game.whiteUsername() != null && game.whiteUsername().equals(username)) {
                gameData = game;
                color = WHITE;
            }
        }
        if(gameData == null) {
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
    }

    private void leave(String authToken, Session session) throws DataAccessException, IOException {
        connections.remove(session);
        if(checkAuth(authToken, session)) {
            return;
        }
        var username = dataAccess.findAuth(authToken);
        var games = dataAccess.listGames();
        var gameID = 0;
        for(var game : games) {
            if(game.blackUsername() != null && game.blackUsername().equals(username)) {
                dataAccess.removeFromGame(game.gameID(), BLACK);
                gameID = game.gameID();
            }
            if(game.whiteUsername() != null && game.whiteUsername().equals(username)) {
                dataAccess.removeFromGame(game.gameID(), WHITE);
                gameID = game.gameID();
            }
        }
        if(gameID == 0) {
            var notifString = String.format("%s left game %d", username, gameID);
            var notifMsg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notifString);
            connections.broadcast(session, new Gson().toJson(notifMsg));
        } else {
            var notifString = String.format("%s stopped watching game", username);
            var notifMsg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notifString);
            connections.broadcast(session, new Gson().toJson(notifMsg));
        }
    }

    private void connect(String authToken, int gameID, Session session) throws DataAccessException, IOException {
        connections.add(session);
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
        var notifString = String.format("%s joined game %d", username, gameID);
        var notifMsg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notifString);
        connections.broadcast(session, new Gson().toJson(notifMsg));
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
