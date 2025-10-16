import chess.InvalidMoveException;
import dataaccess.MemoryDataAccess;
import model.RegisterRequest;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import service.AlreadyTakenException;
import service.Service;

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
        catch (AlreadyTakenException e) {
            //Test passed, exception thrown as expected
        }
    }

}
