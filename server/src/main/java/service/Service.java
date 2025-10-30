package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.*;
import org.mindrot.jbcrypt.BCrypt;
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
            var userData = new UserData(registerRequest.username(), hashPassword(registerRequest.password()), registerRequest.email());
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
        if(!verifyUser(loginRequest.username(), loginRequest.password())) {
            throw new ServiceException("Error: Password incorrect", ServiceException.Code.IncorrectPasswordError);
        }
        var authToken = "";
        var username = existingUser.username();
        authToken = generateToken();
        dataAccess.addAuth(new AuthData(authToken, username));
        return new LoginResult(username, authToken);
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
        if(createGameRequest.gameName() == null) {
            throw new ServiceException("Error: Bad request", ServiceException.Code.BadRequestError);
        }
        checkAuthorization(new AuthorizationRequest(createGameRequest.authToken()));
        var gameID = dataAccess.createGame(createGameRequest.gameName());
        //System.out.println(currentGameID - 1);
        return new CreateGameResult(gameID);
    }

    public void joinGame(JoinGameRequest joinGameRequest) throws Exception{
        if(joinGameRequest.playerColor() == null || joinGameRequest.gameID() == 0) {
            throw new ServiceException("Error: Bad request", ServiceException.Code.BadRequestError);
        }
        checkAuthorization(new AuthorizationRequest(joinGameRequest.authToken()));
        var desiredGame = dataAccess.getGame(joinGameRequest.gameID());
        if(desiredGame == null) {
            throw new ServiceException("Error: unauthorized", ServiceException.Code.GameNotFoundError);
        }
        var existingPlayer = switch(joinGameRequest.playerColor())
        {case BLACK -> desiredGame.blackUsername(); case WHITE -> desiredGame.whiteUsername();};
        if(existingPlayer != null) {
            throw new ServiceException("Error: already taken", ServiceException.Code.ColorNotAvailableError);
        }
        dataAccess.addPlayerToGame(joinGameRequest.authToken(), joinGameRequest.playerColor(), joinGameRequest.gameID());
    }

    public void clear() throws Exception {
        dataAccess.clearData();
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    public void checkAuthorization(AuthorizationRequest authorizationRequest) throws Exception {
        if(authorizationRequest.authToken() == null) {
            throw new ServiceException("Error: Bad request", ServiceException.Code.BadRequestError);
        }
        //System.out.println("Looking for authKey" + authorizationRequest.authToken());
        if(dataAccess.findAuth(authorizationRequest.authToken()) == null) {
            //System.out.println("Didn't find it");
            throw new ServiceException("Error: AuthToken not found", ServiceException.Code.NotLoggedInError);
        }
        //System.out.println("Found it!");
    }

    String hashPassword(String clearTextPassword) {
        return BCrypt.hashpw(clearTextPassword, BCrypt.gensalt());
    }

    boolean verifyUser(String username, String providedClearTextPassword) throws DataAccessException {
        // read the previously hashed password from the database
        var hashedPassword = dataAccess.getUser(username).password();
        return BCrypt.checkpw(providedClearTextPassword, hashedPassword);
    }
}
