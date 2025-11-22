package server.websocket;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import io.javalin.websocket.*;
import org.jetbrains.annotations.NotNull;
import websocket.commands.UserGameCommand;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import javax.management.Notification;
import java.io.IOException;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private DataAccess dataAccess;

    public WebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }


    @Override
    public void handleConnect(@NotNull WsConnectContext ctx) throws Exception {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) throws Exception {
        System.out.println("Websocket closed");
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) throws Exception {
        try {
            UserGameCommand cmd = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            switch(cmd.getCommandType()) {
                case CONNECT -> connect(cmd.getAuthToken(), cmd.getGameID(), ctx.session);
                //case MAKE_MOVE -> makeMove(cmd.getAuthToken(), ctx.session);
                //case LEAVE -> leave(cmd.getAuthToken(), cmd.getGameID(), ctx.session);
                //case RESIGN -> resign(cmd.getAuthToken(), cmd.getGameID(), ctx.session);
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    private void connect(String authToken, int gameID, Session session) throws IOException {
        connections.add(session);
        dataAccess.
        var username = "name";
        var message = String.format("%s joined game %d", username, gameID);
        var msg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(session, msg);
    }
}
