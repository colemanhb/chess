package dataaccess;

import chess.ChessGame;
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
        Assertions.assertNotNull(dataAccess.findAuth("token"));
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
    public void listGamesSuccess() throws Exception {
        var dataAccess = new MySqlDataAccess();
        dataAccess.clearData();
        dataAccess.listGames();
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
    public void getGameSuccess() throws Exception {
        var dataAccess = new MySqlDataAccess();
        dataAccess.clearData();
        int gameID = dataAccess.createGame("first game");
        var game = dataAccess.getGame(gameID);
        Assertions.assertNotNull(game);
        Assertions.assertEquals("first game", game.gameName());
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

    /*
    saveUserFail
    getUserSuccess
    getUserFail
    findAuth
    deleteAuthFailure
    addAuthFailure
    listGamesFailure
    createGameFailure
    getGameFailure
    addPlayerToGame
     */

}
