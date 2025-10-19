package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public interface DataAccess {
    void saveUser(UserData userData);
    UserData getUser(String username);
    void clearData();
    boolean findAuth(String authKey);
    void deleteAuth(String authKey);
    void addAuth(AuthData authData);
    ArrayList<GameData> listGames();
    void createGame(String gameName, int gameID);
    GameData getGame(int gameID);
    void addPlayerToGame(String authToken, ChessGame.TeamColor playerColor, int gameID);
}
