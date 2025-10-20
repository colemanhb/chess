package service;

import dataaccess.DataAccess;
import model.*;

import java.util.Objects;
import java.util.UUID;

public class Service {
    private final DataAccess dataAccess;
    private int currentGameID;
    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
        currentGameID = 0;
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
        checkAuthorization(logoutRequest);
        //System.out.println("Authorization found!" + logoutRequest.authToken());
        dataAccess.deleteAuth(logoutRequest.authToken());
        //System.out.println("Authorization deleted!" + logoutRequest.authToken());
    }

    public ListGamesResult listGames(AuthorizationRequest listGamesRequest) throws Exception{
        checkAuthorization(listGamesRequest);
        return new ListGamesResult(dataAccess.listGames());
    }

    public CreateGameResult createGame(CreateGameRequest createGameRequest) throws Exception{
        checkAuthorization(new AuthorizationRequest(createGameRequest.authToken()));
        dataAccess.createGame(createGameRequest.gameName(), currentGameID);
        currentGameID ++;
        //System.out.println(currentGameID - 1);
        return new CreateGameResult(currentGameID - 1);
    }

    public void joinGame(JoinGameRequest joinGameRequest) throws Exception{
        checkAuthorization(new AuthorizationRequest(joinGameRequest.authToken()));
        var desiredGame = dataAccess.getGame(joinGameRequest.gameID());
        if(desiredGame == null) {
            throw new ServiceException("Error: unauthorized", ServiceException.Code.GameNotFoundError);
        }
        var existingPlayer = switch(joinGameRequest.playerColor()) {case BLACK -> desiredGame.blackUsername(); case WHITE -> desiredGame.whiteUsername();};
        if(existingPlayer != null) {
            throw new ServiceException("Error: already taken", ServiceException.Code.ColorNotAvailableError);
        }
        dataAccess.addPlayerToGame(joinGameRequest.authToken(), joinGameRequest.playerColor(), joinGameRequest.gameID());
    }

    public void clear() {
        dataAccess.clearData();
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    public void checkAuthorization(AuthorizationRequest authorizationRequest) throws Exception {
        if(authorizationRequest.authToken() == null) {
            throw new ServiceException("Error: Bad request", ServiceException.Code.BadRequestError);
        }
        if(!dataAccess.findAuth(authorizationRequest.authToken())) {
            throw new ServiceException("Error: AuthToken not found", ServiceException.Code.NotLoggedInError);
        }
    }
}
