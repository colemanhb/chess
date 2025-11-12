package client;

import model.RegisterRequest;
import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade;
import service.ServiceException;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void registerSuccess() throws ServiceException {
        var authData = facade.register(new RegisterRequest("username", "password", "email"));
        assertTrue(authData.authToken().length() > 10);
    }

    @Test
    public void registerFailure() {
        try {
            facade.register(new RegisterRequest("username", "other password", "other email"));
            fail("Expected error to be thrown");
        }
        catch(ServiceException e) {
            Assertions.assertTrue(true);
        }
    }

}
