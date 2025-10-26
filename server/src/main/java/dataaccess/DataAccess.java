package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.ArrayList;

public interface DataAccess {
    void saveUser(UserData userData) throws Exception;
    UserData getUser(String username) throws DataAccessException;
    void clearData() throws Exception;
    boolean findAuth(String authKey);
    void deleteAuth(String authKey);
    void addAuth(AuthData authData) throws Exception;
    ArrayList<GameData> listGames();
    int createGame(String gameName) throws Exception;
    GameData getGame(int gameID) throws Exception;
    void addPlayerToGame(String authToken, ChessGame.TeamColor playerColor, int gameID);
}
