package websocket;

import service.ServiceException;
import websocket.messages.ServerMessage;

public interface NotificationHandler {
     void notify(ServerMessage msg) throws ServiceException;
}
