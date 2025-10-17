package service;

import dataaccess.DataAccess;
import model.*;

import javax.xml.crypto.Data;
import java.util.UUID;

public class Service {
    private DataAccess dataAccess;
    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public LoginResult register(RegisterRequest registerRequest) throws Exception {
        if(registerRequest.username() == null || registerRequest.password() == null || registerRequest.email() == null) {
            throw new ServiceException("Error: Bad request", ServiceException.Code.BadRequestError);
        }
        var existingUser = dataAccess.getUser(registerRequest.username());
        if(existingUser == null) {
            var userData = new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());
            dataAccess.saveUser(userData);
            return new LoginResult(userData.username(), generateToken());
        }
        else {
            throw new ServiceException("Error: Username not available", ServiceException.Code.AlreadyTakenError);
        }
    }

    public LoginResult login(LoginRequest loginRequest) throws Exception{
        if(loginRequest.username() == null || loginRequest.password() == null) {
            throw new ServiceException("Error: Bad request", ServiceException.Code.BadRequestError);
        }
        var existingUser = dataAccess.getUser(loginRequest.username());
        if(existingUser == null) {
            throw new ServiceException("Error: Username not found", ServiceException.Code.NotFoundError);
        }
        if(existingUser.password() != loginRequest.password()) {
            throw new ServiceException("Error: Password incorrect", ServiceException.Code.IncorrectPasswordError);
        }
        return new LoginResult(existingUser.username(), generateToken());
    }

/*
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

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
