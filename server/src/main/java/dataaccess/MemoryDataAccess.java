package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.HashMap;
import java.util.Vector;

public class MemoryDataAccess implements DataAccess{
    private HashMap<String, UserData> users = new HashMap<>();
    private HashMap<String, GameData> games = new HashMap<>();
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
    public Vector<GameData> listGames() {
        return (Vector<GameData>)games.values();
    }

    public void clearData() {
        users.clear();
        games.clear();
        auths.clear();
    }
}
