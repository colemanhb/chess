package service;

import dataaccess.DataAccess;
import model.RegistrationResult;
import model.UserData;

public class UserService {
    private DataAccess dataAccess;
    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }
    public RegistrationResult register(UserData userData) {
        dataAccess.saveUser(userData);
        return new RegistrationResult(userData.username(), "zyyz");
    }
}
