package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.ArrayList;
import java.util.HashMap;

public class MemoryDataAccess implements DataAccess{
    private final HashMap<String, UserData> users = new HashMap<>();
    private final HashMap<Integer, GameData> games = new HashMap<>();
    private final HashMap<String, AuthData> auths = new HashMap<>();
    private int currentGameID = 0;
    @Override
    public void saveUser(UserData userData) {
        users.put(userData.username(), userData);
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    public String findAuth(String authKey) {
        if(auths.get(authKey) == null) {
            return null;
        }
        else {
            return auths.get(authKey).username();
        }
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
    public int createGame(String gameName) {
        currentGameID ++;
        games.put(currentGameID, new GameData(currentGameID,null,null, gameName, new ChessGame()));
        return currentGameID;
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
