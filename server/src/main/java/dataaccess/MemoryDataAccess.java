package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class MemoryDataAccess implements DataAccess{
    private HashMap<String, UserData> users = new HashMap<>();
    private HashMap<String, GameData> games = new HashMap<>();
    public HashMap<String, AuthData> auths = new HashMap<>();
    private int currentGameID = 0;
    @Override
    public void saveUser(UserData userData) {
        users.put(userData.username(), userData);
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    public boolean findAuth(String authKey) {
        return auths.containsKey(authKey);
    }

    public void deleteAuth(String authKey) {
        auths.remove(authKey);
    }

    @Override
    public void addAuth(AuthData authData) {
        auths.put(authData.authToken(), authData);
    }

    @Override
    public ArrayList<GameData> listGames() {
        var gamesCollection = games.values();
        return new ArrayList<>(gamesCollection);
    }

    @Override
    public void createGame(String gameName, int gameID) {
        games.put(gameName, new GameData(gameID,null,null, gameName, new ChessGame()));
        currentGameID ++;
    }

    @Override
    public boolean gameExists(String gameName) {
        return games.containsKey(gameName);
    }

    public void clearData() {
        users.clear();
        games.clear();
        auths.clear();
    }
}
