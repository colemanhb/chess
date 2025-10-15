import dataaccess.MemoryDataAccess;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import service.Service;

public class ServiceTest {
    @Test
    public void registerNormal() {
        var dataAccess = new MemoryDataAccess();
        var userService = new Service(dataAccess);

        var res = userService.register(new UserData("cow", "rat", "john"));
        Assertions.assertNotNull(res);
    }

}
