import dataaccess.MemoryDataAccess;
import model.LoginRequest;
import model.RegisterRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import service.Service;
import service.ServiceException;

import static org.junit.jupiter.api.Assertions.fail;

public class ServiceTest {
    @Test
    public void registerNormal() throws Exception {
        var dataAccess = new MemoryDataAccess();
        var userService = new Service(dataAccess);

        var res = userService.register(new RegisterRequest("cow", "rat", "john"));
        Assertions.assertNotNull(res);
        var res2 = userService.register(new RegisterRequest("name", "word", "mail"));
        Assertions.assertNotNull(res2);
    }

    @Test
    public void registerFailure() throws Exception {
        var dataAccess = new MemoryDataAccess();
        var userService = new Service(dataAccess);
        userService.register(new RegisterRequest("cow", "rat", "john"));
        try {
            var res = userService.register(new RegisterRequest("cow", "rat1", "john1"));
            fail("Expected exception to be thrown");
        }
        catch (ServiceException e) {
            //Test passed, exception thrown as expected
        }
    }

    @Test
    public void clearSuccess() throws Exception {
        var dataAccess = new MemoryDataAccess();
        var userService = new Service(dataAccess);
        userService.clear();
        userService.register(new RegisterRequest("cow", "rat", "john"));
        userService.clear();
        Assertions.assertNull(dataAccess.getUser("cow"));
    }

    @Test
    public void registerAfterClear() throws Exception{
        var dataAccess = new MemoryDataAccess();
        var userService = new Service(dataAccess);
        userService.register(new RegisterRequest("cow", "rat", "john"));
        userService.clear();
        userService.register(new RegisterRequest("cow", "rat", "john"));
    }

    @Test
    public void loginNormal() throws Exception {
        var dataAccess = new MemoryDataAccess();
        var userService = new Service(dataAccess);
        userService.register(new RegisterRequest("cow","rat", "john"));
        var res = userService.login(new LoginRequest("cow", "rat"));
        Assertions.assertNotNull(res);
    }

    @Test
    public void loginWrongUsername() throws Exception {
        var dataAccess = new MemoryDataAccess();
        var userService = new Service(dataAccess);
        userService.register(new RegisterRequest("cow","rat","john"));
        try {
            var res = userService.login(new LoginRequest("col", "rat"));
            fail("Expected exception to be thrown");
        }
        catch (ServiceException e) {
            //Test passed, exception thrown as expected
        }
    }

    @Test
    public void loginWrongPassword() throws Exception {
        var dataAccess = new MemoryDataAccess();
        var userService = new Service(dataAccess);
        userService.register(new RegisterRequest("cow","rat","john"));
        try {
            var res = userService.login(new LoginRequest("cow", "ray"));
            fail("Expected exception to be thrown");
        }
        catch (ServiceException e) {
            //Test passed, exception thrown as expected
        }
    }

}
