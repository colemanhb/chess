package service;

import dataaccess.DataAccess;
import model.*;

import java.util.Objects;
import java.util.UUID;

public class Service {
    private final DataAccess dataAccess;
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
            String authToken = generateToken();
            dataAccess.addAuth(new AuthData(authToken, registerRequest.username()));
            return new LoginResult(userData.username(), authToken);
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
        if(!Objects.equals(existingUser.password(), loginRequest.password())) {
            throw new ServiceException("Error: Password incorrect", ServiceException.Code.IncorrectPasswordError);
        }
        String authToken = generateToken();
        dataAccess.addAuth(new AuthData(authToken, existingUser.username()));
        return new LoginResult(existingUser.username(), generateToken());
    }

    public void logout(AuthorizationRequest logoutRequest) throws Exception{
        if(!dataAccess.findAuth(logoutRequest.authToken())) {
            throw new ServiceException("Error: AuthToken not found", ServiceException.Code.NotLoggedInError);
        }
        dataAccess.deleteAuth(logoutRequest.authToken());
    }

    public ListGamesResult listGames(AuthorizationRequest listGamesRequest) throws Exception{
        if(!dataAccess.findAuth(listGamesRequest.authToken())) {
            throw new ServiceException("Error: AuthToken not found", ServiceException.Code.NotLoggedInError);
        }
        var games = dataAccess.listGames();
        return new ListGamesResult(games);
    }

    /*
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
