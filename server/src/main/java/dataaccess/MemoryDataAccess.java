package dataaccess;

import model.UserData;

import java.util.HashMap;

public class MemoryDataAccess implements DataAccess{
    private HashMap<String, UserData> users = new HashMap<>();
    @Override
    public void saveUser(UserData userData) {
        users.put(userData.username(), userData);
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }
}
