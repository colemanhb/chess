package client;

import chess.ChessGame;
import model.*;
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

    @BeforeEach
    public void setup() throws Exception {
        facade.clear();
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
            facade.register(new RegisterRequest("username", "password", "email"));
            facade.register(new RegisterRequest("username", "other password", "other email"));
            fail("Expected error to be thrown");
        }
        catch(ServiceException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void loginSuccess() throws ServiceException {
        facade.register(new RegisterRequest("username", "password", "email"));
        var authData = facade.login(new LoginRequest("username", "password"));
        assertTrue(authData.authToken().length() > 10);
    }

    @Test
    public void loginFailure() {
        try {
            facade.login(new LoginRequest("wrong username", "wrong password"));
            fail("Expected error to be thrown");
        }
        catch(ServiceException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void listSuccess() throws ServiceException {
        var authData = facade.register(new RegisterRequest("username", "password", "email"));
        facade.create(new CreateGameRequest(authData.authToken(), "new game"));
        var gamesList = facade.list(new AuthorizationRequest(authData.authToken()));
        assertNotNull(gamesList);
    }

    @Test
    public void listFailure() {
        try {
            facade.list(new AuthorizationRequest("wrong authtoken"));
            fail("Expected error to be thrown");
        }
        catch(ServiceException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void createSuccess() throws ServiceException {
        var authData = facade.register(new RegisterRequest("username", "password", "email"));
        var gameResult = facade.create(new CreateGameRequest(authData.authToken(), "new game"));
        assertNotNull(gameResult);
    }

    @Test
    public void createFailure() {
        try {
            facade.create(new CreateGameRequest("wrong auth token", "new game"));
            fail("Expected error to be thrown");
        }
        catch(ServiceException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void joinSuccess() throws ServiceException {
        var authData = facade.register(new RegisterRequest("username", "password", "email"));
        var gameResult = facade.create(new CreateGameRequest(authData.authToken(), "new game"));
        facade.join(new JoinGameRequest(authData.authToken(), ChessGame.TeamColor.BLACK, gameResult.gameID()));
        var username = facade.list(new AuthorizationRequest(authData.authToken())).games().getFirst().blackUsername();
        assertEquals("username", username);
    }

    @Test
    public void joinFailure() {
        try {
            facade.join(new JoinGameRequest("wrong token", ChessGame.TeamColor.BLACK, 1));
            fail("Expected error to be thrown");
        }
        catch(ServiceException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void watchSuccess() throws ServiceException {
        var authData = facade.register(new RegisterRequest("username", "password", "email"));
        var gameResult = facade.create(new CreateGameRequest(authData.authToken(), "new game"));
        var games = facade.watch(new JoinGameRequest(authData.authToken(), ChessGame.TeamColor.BLACK, gameResult.gameID()));
        assertNotNull(games);
    }

    @Test
    public void watchFailure() {
        try {
            facade.watch(new JoinGameRequest("wrong token", ChessGame.TeamColor.BLACK, 1));
            fail("Expected error to be thrown");
        }
        catch(ServiceException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void logoutSuccess() throws ServiceException {
        var authData = facade.register(new RegisterRequest("username", "password", "email"));
        facade.logout(new AuthorizationRequest(authData.authToken()));
        try {
            facade.list(new AuthorizationRequest(authData.authToken()));
            fail("Expected error to be thrown");
        }
        catch(ServiceException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void logoutFailure() {
        try {
            facade.logout(new AuthorizationRequest("wrong token"));
            fail("Expected error to be thrown");
        }
        catch(ServiceException e) {
            Assertions.assertTrue(true);
        }
    }

}
