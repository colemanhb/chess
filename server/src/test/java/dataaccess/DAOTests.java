package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DAOTests {
    @Test
    public void setup() throws Exception {
        //var dataAccess = new MySqlDataAccess();
    }

    @Test
    public void clearSuccess() throws Exception {
        var dataAccess = new MySqlDataAccess();
        dataAccess.clearData();
    }

    @Test
    public void saveUserSuccess() throws Exception {
        var dataAccess = new MySqlDataAccess();
        dataAccess.clearData();
        var writeUser = new UserData("coleman", "1234", "this@gmail.com");
        dataAccess.saveUser(writeUser);
        var readUser = dataAccess.getUser("coleman");
        Assertions.assertNotNull(readUser);
        Assertions.assertEquals("this@gmail.com",readUser.email());
    }

    @Test
    public void addAuthSuccess() throws Exception {
        var dataAccess = new MySqlDataAccess();
        dataAccess.clearData();
        var writeAuth = new AuthData("token", "coleman");
        dataAccess.addAuth(writeAuth);
        Assertions.assertTrue(dataAccess.findAuth("token"));
    }

    @Test
    public void deleteAuthSuccess() throws Exception {
        var dataAccess = new MySqlDataAccess();
        dataAccess.clearData();
        var writeAuth = new AuthData("token", "coleman");
        dataAccess.addAuth(writeAuth);
        dataAccess.deleteAuth("token");
        Assertions.assertFalse(dataAccess.findAuth("token"));
    }

    /*
    saveUserFail
    getUserSuccess
    getUserFail
    findAuth
    deleteAuthFailure
    addAuthFailure
    listGames
    createGame
    getGame
    addPlayerToGame
     */

}
