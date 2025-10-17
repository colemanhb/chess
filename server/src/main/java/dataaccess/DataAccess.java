package dataaccess;

import model.UserData;

public interface DataAccess {
    void saveUser(UserData userData);
    UserData getUser(String username);
    void clearData();
}
