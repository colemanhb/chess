package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.fail;

public class DAOTests {

    @Test
    public void getUserSuccess() throws Exception {
        var dataAccess = new MySqlDataAccess();
        dataAccess.clearData();
        dataAccess.saveUser(new UserData("un","pw","em"));
        Assertions.assertNotNull(dataAccess.getUser("un"));
    }

    @Test
    public void getUserFailure() throws Exception {
        var dataAccess = new MySqlDataAccess();
        dataAccess.clearData();
        Assertions.assertNull(dataAccess.getUser("nonexistent user"));
    }

    @Test
    public void clearSuccess() throws Exception {
        var dataAccess = new MySqlDataAccess();
        dataAccess.clearData();
        dataAccess.addAuth(new AuthData("token","username"));
        var gameID = dataAccess.createGame("name");
        dataAccess.saveUser(new UserData("username","password","email"));
        dataAccess.clearData();
        Assertions.assertNull(dataAccess.findAuth("token"));
        Assertions.assertNull(dataAccess.getGame(gameID));
        Assertions.assertNull(dataAccess.getUser("username"));
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
    public void saveUserFailure() throws Exception {
        var dataAccess = new MySqlDataAccess();
        dataAccess.clearData();
        try {
            dataAccess.saveUser(new UserData("a" + "a".repeat(200), "password", "email"));
            fail("Expected error to be thrown");
        }
        catch(DataAccessException e) {
            Assertions.assertTrue(true);
        }

    }

    @Test
    public void addAuthSuccess() throws Exception {
        var dataAccess = new MySqlDataAccess();
        dataAccess.clearData();
        var writeAuth = new AuthData("token", "coleman");
        dataAccess.addAuth(writeAuth);
        dataAccess.addAuth(new AuthData("token1", "coleman"));
        Assertions.assertNotNull(dataAccess.findAuth("token"));
    }

    @Test
    public void addAuthFailure() throws Exception {
        var dataAccess = new MySqlDataAccess();
        dataAccess.clearData();
        try {
            dataAccess.addAuth(new AuthData("a".repeat(200), "username"));
            fail("Expected error to be thrown");
        }
        catch(DataAccessException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void findAuthSuccess() throws Exception {
        var dataAccess = new MySqlDataAccess();
        dataAccess.clearData();
        var writeAuth = new AuthData("token", "coleman");
        dataAccess.addAuth(writeAuth);
        Assertions.assertNotNull(dataAccess.findAuth("token"));
    }

    @Test
    public void findAuthFailure() throws Exception {
        var dataAccess = new MySqlDataAccess();
        dataAccess.clearData();
        Assertions.assertNull(dataAccess.findAuth("token"));
    }

    @Test
    public void deleteAuthSuccess() throws Exception {
        var dataAccess = new MySqlDataAccess();
        dataAccess.clearData();
        var writeAuth = new AuthData("token", "coleman");
        dataAccess.addAuth(writeAuth);
        dataAccess.deleteAuth("token");
        Assertions.assertNull(dataAccess.findAuth("token"));
    }

    @Test
    public void deleteAuthFailure() throws Exception {
        var dataAccess = new MySqlDataAccess();
        dataAccess.clearData();
        var starting_length = dataAccess.listGames().size();
        dataAccess.deleteAuth("nonexistent");
        var ending_length = dataAccess.listGames().size();
        Assertions.assertEquals(starting_length, ending_length);
    }

    @Test
    public void listGamesSuccess() throws Exception {
        var dataAccess = new MySqlDataAccess();
        dataAccess.clearData();
        var games = dataAccess.listGames();
        Assertions.assertTrue(games.isEmpty());
        dataAccess.createGame("first game");
        games = dataAccess.listGames();
        Assertions.assertEquals(1, games.size());
    }

    @Test
    public void listGamesFailure() throws Exception {
        var dataAccess = new MySqlDataAccess();
        dataAccess.clearData();
        Assertions.assertEquals(0, dataAccess.listGames().size());
    }

    @Test
    public void createGameSuccess() throws Exception {
        var dataAccess = new MySqlDataAccess();
        dataAccess.clearData();
        dataAccess.createGame("first game");
        var games = dataAccess.listGames();
        Assertions.assertNotNull(games);
    }

    @Test
    public void createGameFailure() throws Exception {
        var dataAccess = new MySqlDataAccess();
        dataAccess.clearData();
        try {
            dataAccess.createGame("a".repeat(200));
            fail("Expected error to be thrown");
        }
        catch(DataAccessException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void getGameSuccess() throws Exception {
        var dataAccess = new MySqlDataAccess();
        dataAccess.clearData();
        int gameID = dataAccess.createGame("first game");
        var game = dataAccess.getGame(gameID);
        Assertions.assertNotNull(game);
        Assertions.assertEquals("first game", game.gameName());
    }

    @Test
    public void getGameFailure() throws Exception {
        var dataAccess = new MySqlDataAccess();
        dataAccess.clearData();
        var game = dataAccess.getGame(0);
        Assertions.assertNull(game);
    }

    @Test
    public void addPlayerToGameSuccess() throws Exception {
        var dataAccess = new MySqlDataAccess();
        dataAccess.clearData();
        int gameID = dataAccess.createGame("first game");
        dataAccess.addAuth(new AuthData("token", "username"));
        dataAccess.addPlayerToGame("token", ChessGame.TeamColor.BLACK, gameID);
        var game = dataAccess.getGame(gameID);
        Assertions.assertEquals("username", game.blackUsername());
    }

    @Test
    public void AddPlayerToGameFailure() throws Exception {
        var dataAccess = new MySqlDataAccess();
        dataAccess.clearData();
        int gameID = dataAccess.createGame("first game");
        dataAccess.addPlayerToGame("nonexistent token", ChessGame.TeamColor.BLACK, gameID);
        var game = dataAccess.getGame(gameID);
        Assertions.assertNull(game.blackUsername());
    }

    @Test
    public void Debugging() throws Exception {
        var dataAccess = new MySqlDataAccess();
        dataAccess.clearData();

    }
}
