package dataaccess;

import model.UserData;

public interface DataAccess {
    void saveUser(UserData userData);
    void getUser(String username);
}
