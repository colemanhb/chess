package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.ArrayList;

public interface DataAccess {
    void saveUser(UserData userData) throws Exception;
    boolean userLoggedIn(String username) throws DataAccessException;
    String authFromUsername(String username) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    void clearData() throws Exception;
    String findAuth(String authKey) throws Exception;
    void deleteAuth(String authKey) throws Exception;
    void addAuth(AuthData authData) throws Exception;
    ArrayList<GameData> listGames() throws DataAccessException;
    int createGame(String gameName) throws Exception;
    GameData getGame(int gameID) throws Exception;
    void addPlayerToGame(String authToken, ChessGame.TeamColor playerColor, int gameID) throws Exception;
}
