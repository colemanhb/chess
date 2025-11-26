package websocket;

import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import jakarta.websocket.*;
import service.ServiceException;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

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
                    ServerMessage msg = new Gson().fromJson(message, ServerMessage.class);
                    try {
                        notificationHandler.notify(msg);
                    } catch (ServiceException e) {
                        throw new RuntimeException(e);
                    }
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

    public void leave(String authToken, int gameID) throws ServiceException {
        try {
            var cmd = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(cmd));
        } catch (IOException ex) {
            throw new ServiceException(ex.getMessage(), ServiceException.Code.ServerError);
        }
    }

    public void makeMove(String authToken, int currentGame, ChessMove chessMove) throws ServiceException {
        try {
            var cmd = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, currentGame, chessMove);
            this.session.getBasicRemote().sendText(new Gson().toJson(cmd));
        } catch (IOException ex) {
            throw new ServiceException(ex.getMessage(), ServiceException.Code.ServerError);
        }

    }

    public void resign(String authToken, int currentGame) throws ServiceException {
        try {
            var cmd = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, currentGame);
            this.session.getBasicRemote().sendText(new Gson().toJson(cmd));
        } catch (IOException ex) {
            throw new ServiceException(ex.getMessage(), ServiceException.Code.ServerError);
        }
    }
}
