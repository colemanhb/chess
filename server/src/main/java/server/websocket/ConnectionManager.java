package server.websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<Session, Session> connections = new ConcurrentHashMap<>();
    public final HashMap<Session, Integer> games = new HashMap<>();
    public void add(Session session, int game) {
        connections.put(session, session);
        games.put(session, game);
    }

    public void remove(Session session) {
        connections.remove(session);
    }

    public void broadcast(Session excludeSession, String msg, int game) throws IOException {
        for (Session c : connections.values()) {
            if (c.isOpen()) {
                if (!c.equals(excludeSession) && games.get(c) == game) {
                    c.getRemote().sendString(msg);
                }
            }
        }
    }
}