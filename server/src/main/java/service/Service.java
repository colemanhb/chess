package service;

import dataaccess.DataAccess;
import model.*;

public class Service {
    private DataAccess dataAccess;
    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }
    public RegisterResult register(RegisterRequest registerRequest) {
        var existingUser = dataAccess.getUser(userData);
        if(existingUser == null) {
            dataAccess.saveUser(userData);
            return new RegisterResult(userData.username(), "zyyz");
        }
        else {
            throw AlreadyTakenException;
        }
    }
    public LoginResult login(LoginRequest loginRequest) {

    }
    public void logout(LogoutRequest logoutRequest) {

    }
    public ListGamesResult listGames(ListGamesRequest listGamesRequest) {

    }
    public CreateGameResult createGame(CreateGameRequest createGameRequest) {

    }
    public void joinGame(JoinGameRequest joinGameRequest) {

    }
    public void clear(ClearRequest clearRequest) {

    }
}
