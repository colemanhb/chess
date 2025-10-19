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
    public HashMap<Integer, GameData> games = new HashMap<>();
    public HashMap<String, AuthData> auths = new HashMap<>();
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
        games.put(gameID, new GameData(gameID,null,null, gameName, new ChessGame()));
    }

    @Override
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    @Override
    public void addPlayerToGame(String authToken, ChessGame.TeamColor playerColor, int gameID) {
        var username = auths.get(authToken).username();
        var existingGame = games.get(gameID);
        if(playerColor == ChessGame.TeamColor.BLACK) {
            games.put(gameID, new GameData(gameID, existingGame.whiteUsername(), username, existingGame.gameName(), existingGame.game()));
        } else if (playerColor == ChessGame.TeamColor.WHITE) {
            games.put(gameID, new GameData(gameID, username, existingGame.blackUsername(), existingGame.gameName(), existingGame.game()));
        }
    }

    public void clearData() {
        users.clear();
        games.clear();
        auths.clear();
    }
}
