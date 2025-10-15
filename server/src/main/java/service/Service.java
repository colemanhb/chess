package service;

import dataaccess.DataAccess;
import model.RegisterResult;

public class Service {
    private DataAccess dataAccess;
    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }
    public RegisterResult register(RegisterRequest registerRequest) {
        dataAccess.saveUser(userData);
        return new RegisterResult(userData.username(), "zyyz");
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
