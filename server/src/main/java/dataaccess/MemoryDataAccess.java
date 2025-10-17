package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.HashMap;

public class MemoryDataAccess implements DataAccess{
    private HashMap<String, UserData> users = new HashMap<>();
    private HashMap<String, GameData> games = new HashMap<>();
    private HashMap<String, AuthData> auths = new HashMap<>();
    @Override
    public void saveUser(UserData userData) {
        users.put(userData.username(), userData);
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    public void clearData() {
        users.clear();
        games.clear();
        auths.clear();
    }
}
