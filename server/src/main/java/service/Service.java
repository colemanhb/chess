package service;

import dataaccess.DataAccess;
import model.*;

import javax.xml.crypto.Data;

public class Service {
    private DataAccess dataAccess;
    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws Exception {
        var existingUser = dataAccess.getUser(registerRequest.username());
        if(existingUser == null) {
            var userData = new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());
            dataAccess.saveUser(userData);
            return new RegisterResult(userData.username(), "zyyz");
        }
        else {
            throw new AlreadyTakenException("Username not available");
        }
    }
    /*
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
    */
    public void clear() {
        dataAccess.clearData();
    }
}
