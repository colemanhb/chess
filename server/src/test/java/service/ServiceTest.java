package service;

import chess.ChessGame;
import dataaccess.MemoryDataAccess;
import model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
            userService.register(new RegisterRequest("cow", "rat1", "john1"));
            fail("Expected exception to be thrown");
        }
        catch (ServiceException e) {
            Assertions.assertTrue(true);
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
        var res = userService.register(new RegisterRequest("cow", "rat", "john"));
        Assertions.assertNotNull(res);
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
            userService.login(new LoginRequest("col", "rat"));
            fail("Expected exception to be thrown");
        }
        catch (ServiceException e) {
            Assertions.assertTrue(true);
            //Test passed, exception thrown as expected
        }
    }

    @Test
    public void loginWrongPassword() throws Exception {
        var dataAccess = new MemoryDataAccess();
        var userService = new Service(dataAccess);
        userService.register(new RegisterRequest("cow","rat","john"));
        try {
            userService.login(new LoginRequest("cow", "ray"));
            fail("Expected exception to be thrown");
        }
        catch (ServiceException e) {
            Assertions.assertTrue(true);
            //Test passed, exception thrown as expected
        }
    }

    @Test
    public void logoutSuccess() throws Exception {
        var dataAccess = new MemoryDataAccess();
        var userService = new Service(dataAccess);
        var regResult = userService.register(new RegisterRequest("cow","rat","john"));
        var authToken = regResult.authToken();
        userService.logout(new AuthorizationRequest(authToken));
        Assertions.assertNull(dataAccess.findAuth(authToken));
    }

    @Test
    public void logoutFailure() throws Exception {
        var dataAccess = new MemoryDataAccess();
        var userService = new Service(dataAccess);
        userService.register(new RegisterRequest("cow","rat", "john"));
        try {
            userService.logout(new AuthorizationRequest("wrong"));
            fail("Expected exception to be thrown");
        }
        catch (ServiceException e) {
            Assertions.assertTrue(true);
            //Test passed, exception thrown as expected
        }
    }

    @Test
    public void listGamesSuccess() throws Exception {
        var dataAccess = new MemoryDataAccess();
        var userService = new Service(dataAccess);
        var regResult = userService.register(new RegisterRequest("cow","rat","john"));
        var authToken = regResult.authToken();
        userService.createGame(new CreateGameRequest(authToken, "FirstGame"));
        userService.createGame(new CreateGameRequest(authToken, "SecondGame"));
        var games = userService.listGames(new AuthorizationRequest(authToken)).games();
        var firstName = games.get(0).gameName();
        var secondName = games.get(1).gameName();
        if(firstName.equals("FirstGame")) {
            Assertions.assertEquals("SecondGame", secondName);
        } else if (firstName.equals("SecondGame")) {
            Assertions.assertEquals("SecondGame", secondName);
        }
        else {
            fail();
        }
    }

    @Test
    public void listGamesFailure() throws Exception {
        var dataAccess = new MemoryDataAccess();
        var userService = new Service(dataAccess);
        userService.register(new RegisterRequest("cow","rat", "john"));
        try {
            userService.listGames(new AuthorizationRequest("wrong"));
            fail("Expected exception to be thrown");
        }
        catch (ServiceException e) {
            Assertions.assertTrue(true);
            //Test passed, exception thrown as expected
        }
    }

    @Test
    public void createGameSuccess() throws Exception {
        var dataAccess = new MemoryDataAccess();
        var userService = new Service(dataAccess);
        var regResult = userService.register(new RegisterRequest("cow","rat","john"));
        var authToken = regResult.authToken();
        userService.createGame(new CreateGameRequest(authToken,"NEW GAME"));
        var games = userService.listGames(new AuthorizationRequest(authToken)).games();
        Assertions.assertEquals("NEW GAME", games.getFirst().gameName());
    }

    @Test
    public void createGameFailure() throws Exception {
        var dataAccess = new MemoryDataAccess();
        var userService = new Service(dataAccess);
        userService.register(new RegisterRequest("cow","rat", "john"));
        try {
            userService.createGame(new CreateGameRequest("wrong", "NEW GAME"));
            fail("Expected exception to be thrown");
        }
        catch (ServiceException e) {
            Assertions.assertTrue(true);
            //Test passed, exception thrown as expected
        }
    }

    @Test
    public void joinGameSuccess() throws Exception{
        var dataAccess = new MemoryDataAccess();
        var userService = new Service(dataAccess);
        var regResult = userService.register(new RegisterRequest("cow","rat","john"));
        var authToken = regResult.authToken();
        userService.createGame(new CreateGameRequest(authToken,"NEW GAME"));
        var newGameData = userService.createGame(new CreateGameRequest(authToken,"NEW GAME"));
        var gameID = newGameData.gameID();
        userService.joinGame(new JoinGameRequest(authToken, ChessGame.TeamColor.BLACK, gameID));
        var game = dataAccess.getGame(gameID);
        Assertions.assertEquals("cow", game.blackUsername());
    }

    @Test
    public void joinNonexistentGame() throws Exception{
        var dataAccess = new MemoryDataAccess();
        var userService = new Service(dataAccess);
        var regResult = userService.register(new RegisterRequest("cow","rat","john"));
        var authToken = regResult.authToken();
        userService.createGame(new CreateGameRequest(authToken,"NEW GAME"));
        try {
            userService.joinGame(new JoinGameRequest(authToken, ChessGame.TeamColor.BLACK, 9));
            fail("Expected exception to be thrown");
        }
        catch (ServiceException e) {
            Assertions.assertTrue(true);
            //Test passed, exception thrown as expected
        }
    }

    @Test
    public void joinGameWrongColor() throws Exception{
        var dataAccess = new MemoryDataAccess();
        var userService = new Service(dataAccess);
        var regResult = userService.register(new RegisterRequest("cow","rat","john"));
        var authToken = regResult.authToken();
        var newGameData = userService.createGame(new CreateGameRequest(authToken,"NEW GAME"));
        var gameID = newGameData.gameID();
        userService.joinGame(new JoinGameRequest(authToken, ChessGame.TeamColor.BLACK, gameID));
        try {
            userService.joinGame(new JoinGameRequest(authToken, ChessGame.TeamColor.BLACK, gameID));
            fail("Expected exception to be thrown");
        }
        catch (ServiceException e) {
            Assertions.assertTrue(true);
            //Test passed, exception thrown as expected
        }
    }

}

