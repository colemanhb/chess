package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.HashMap;
import java.util.Vector;

public interface DataAccess {
    void saveUser(UserData userData);
    UserData getUser(String username);
    void clearData();
    boolean findAuth(String authKey);
    void deleteAuth(String authKey);
    void addAuth(AuthData authData);
    Vector<GameData> listGames();
}
