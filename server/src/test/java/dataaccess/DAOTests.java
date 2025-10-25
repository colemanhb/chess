package dataaccess;

import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DAOTests {
    @BeforeEach
    public void setup() {

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

}
