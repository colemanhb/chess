package websocket;

import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import jakarta.websocket.*;
import service.ServiceException;
import websocket.commands.UserGameCommand;

import javax.management.Notification;
import javax.swing.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint{
    Session session;
    NotificationHandler notificationHandler;

    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws ServiceException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    Notification notification = new Gson().fromJson(message, Notification.class);
                    notificationHandler.notify(notification);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ServiceException(ex.getMessage(), ServiceException.Code.ServerError);
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {

    }

    public void join(String authToken, int gameID) throws ServiceException {
        try {
            var cmd = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(cmd));
        } catch (IOException ex) {
            throw new ServiceException(ex.getMessage(), ServiceException.Code.ServerError);
        }
    }

    public void leave(String authToken) throws ServiceException {
        try {
            var cmd = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken);
            this.session.getBasicRemote().sendText(new Gson().toJson(cmd));
        } catch (IOException ex) {
            throw new ServiceException(ex.getMessage(), ServiceException.Code.ServerError);
        }
    }

    public void makeMove(String authToken, String start, String end, String promotion) throws ServiceException {
        var startingLocation = stringToLocation(start);
        var endingLocation = stringToLocation(end);
        ChessPiece.PieceType promotionPiece = null;
        try {
            promotionPiece = ChessPiece.PieceType.valueOf(promotion);
        } catch(Exception _) {
        }
        var chessMove = new ChessMove(startingLocation, endingLocation, promotionPiece);
        try {
            var cmd = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, chessMove);
            this.session.getBasicRemote().sendText(new Gson().toJson(cmd));
        } catch (IOException ex) {
            throw new ServiceException(ex.getMessage(), ServiceException.Code.ServerError);
        }

    }

    private ChessPosition stringToLocation(String str) {
        str = str.toLowerCase();
        var letter = str.charAt(0);
        var number = str.charAt(1);
        if(!Character.isLetter(letter) || !Character.isDigit(number)) {
            return null;
        }
        var row = Integer.parseInt(String.valueOf(number));
        var col = (int) letter - 96;
        if(row >= 1 && col >= 1 && row <= 8 && col <= 8) {
            return new ChessPosition(row, col);
        }
        return null;
    }

    public void resign(String authToken) throws ServiceException {
        try {
            var cmd = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken);
            this.session.getBasicRemote().sendText(new Gson().toJson(cmd));
        } catch (IOException ex) {
            throw new ServiceException(ex.getMessage(), ServiceException.Code.ServerError);
        }
    }
}
