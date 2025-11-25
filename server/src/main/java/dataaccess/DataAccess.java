package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.ArrayList;

public interface DataAccess {
    void saveUser(UserData userData) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    void clearData() throws DataAccessException;
    String findAuth(String authKey) throws DataAccessException;
    void deleteAuth(String authKey) throws DataAccessException;
    void addAuth(AuthData authData) throws DataAccessException;
    ArrayList<GameData> listGames() throws DataAccessException;
    int createGame(String gameName) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    void addPlayerToGame(String authToken, ChessGame.TeamColor playerColor, int gameID) throws DataAccessException;
    void removeFromGame(int gameID, ChessGame.TeamColor teamColor) throws DataAccessException;
    void updateGame(GameData gameData) throws DataAccessException;
}
