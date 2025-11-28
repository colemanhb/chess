package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import java.sql.*;
import java.util.ArrayList;

import static java.sql.Types.NULL;

public class MySqlDataAccess implements DataAccess{

    public MySqlDataAccess() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public void saveUser(UserData userData) throws DataAccessException {
        var statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        executeUpdate(statement, userData.username(), userData.password(), userData.email());
    }

    private void executeUpdate(String statement, Object... params) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    switch (param) {
                        case String p -> ps.setString(i + 1, p);
                        case Integer p -> ps.setInt(i + 1, p);

                        //else if (param instanceof PetType p) ps.setString(i + 1, p.toString());
                        case null -> ps.setNull(i + 1, NULL);
                        default -> throw new IllegalStateException("Unexpected value: " + param);
                    }
                }
                ps.executeUpdate();
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Error: unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    private int executeUpdateGetID(String statement, Object... params) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param instanceof String p) {
                        ps.setString(i + 1, p);
                    }
                    else if (param == null) {
                        ps.setNull(i + 1, NULL);
                    }
                }
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if(rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Error: unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, password, email FROM user WHERE username=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1,username);
                try (ResultSet rs = ps.executeQuery()) {
                    if(rs.next()) {
                        return readUser(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Error: unable to update database: %s", e.getMessage()));
        }
        return null;
    }

    private UserData readUser(ResultSet rs) throws DataAccessException {
        try {
            var username = rs.getString("username");
            var password = rs.getString("password");
            var email = rs.getString("email");
            return new UserData(username, password, email);
        }
        catch(SQLException e) {
            throw new DataAccessException(String.format("Error: unable to update database: %s", e.getMessage()));
        }
    }

    @Override
    public void clearData() throws DataAccessException {
        var statements = new String[]{"TRUNCATE user", "TRUNCATE game", "TRUNCATE auth"};
        for(var statement : statements) {
            executeUpdate(statement);
        }
    }

    @Override
    public String findAuth(String authKey) throws DataAccessException{
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken, username FROM auth WHERE authToken=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, authKey);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("username");
                    }
                }
            }
        }
        catch(Exception e) {
            throw new DataAccessException(String.format("Error: unable to update database: %s", e.getMessage()));
        }
        return null;
    }

    @Override
    public void deleteAuth(String authKey) throws DataAccessException{
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "DELETE from auth WHERE authToken=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1,authKey);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Error: unable to update database: %s", e.getMessage()));
        }
    }

    @Override
    public void addAuth(AuthData authData) throws DataAccessException {
        var statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        executeUpdate(statement, authData.authToken(), authData.username());

    }

    @Override
    public ArrayList<GameData> listGames() throws DataAccessException{
        var res = new ArrayList<GameData>();
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, gameJson FROM game";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while(rs.next()) {
                        int gameID = rs.getInt("gameID");
                        String whiteUsername = rs.getString("whiteUsername");
                        String blackUsername = rs.getString("blackUsername");
                        String gameName = rs.getString("gameName");
                        String gameJson = rs.getString("gameJson");
                        var game = new Gson().fromJson(gameJson, ChessGame.class);
                        res.add(new GameData(gameID, whiteUsername, blackUsername, gameName, game));
                    }
                }
            }
        } catch(Exception e) {
            throw new DataAccessException(String.format("Error: unable to update database: %s", e.getMessage()));
        }
        return res;
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        var statement = "INSERT INTO game (whiteUsername, blackUsername, gameName, gameJson) VALUES (?, ?, ?, ?)";
        var gameJson = "";
        var gameID =  executeUpdateGetID(statement, null, null, gameName, gameJson);
        var game = new ChessGame();
        var gameData = new GameData(gameID,null,null,gameName,game);
        gameJson = new Gson().toJson(gameData);
        statement = "UPDATE game SET gameJson=? WHERE gameID=?";
        executeUpdate(statement, gameJson, gameID);
        return gameID;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException{
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT whiteUsername, blackUsername, gameName, gameJson FROM game WHERE gameID=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String whiteUsername = rs.getString("whiteUsername");
                        String blackUsername = rs.getString("blackUsername");
                        String gameName = rs.getString("gameName");
                        String gameJson = rs.getString("gameJson");
                        var game = new Gson().fromJson(gameJson, ChessGame.class);
                        return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Error: unable to update database: %s", e.getMessage()));
        }
        return null;
    }

    @Override
    public void addPlayerToGame(String authToken, ChessGame.TeamColor playerColor, int gameID) throws DataAccessException {
        var username = findAuth(authToken);
        var statement = "";
        var gameData = getGame(gameID);
        if(playerColor == ChessGame.TeamColor.BLACK) {
            statement = "UPDATE game SET blackUsername=?, gameJson=? WHERE gameID=?";
            gameData = new GameData(gameID,gameData.whiteUsername(),username,gameData.gameName(),gameData.game());
        } else if (playerColor == ChessGame.TeamColor.WHITE) {
            statement = "UPDATE game SET whiteUsername=?, gameJson=? WHERE gameID=?";
            gameData = new GameData(gameID,username,gameData.blackUsername(),gameData.gameName(),gameData.game());
        }
        var gameJson = new Gson().toJson(gameData);
        executeUpdate(statement, username, gameJson, gameID);
    }

    private final String[] createUserTable = {
            """
            CREATE TABLE IF NOT EXISTS user (
            username varchar(100) NOT NULL,
            password varchar (100) NOT NULL,
            email varchar (100) NOT NULL,
            PRIMARY KEY(username),
            INDEX(password),
            INDEX(username)
          ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
          """
    };

    private final String[] createGameTable = {
            """
            CREATE TABLE IF NOT EXISTS game (
            gameID int NOT NULL AUTO_INCREMENT,
            whiteUsername varchar(100),
            blackUsername varchar(100),
            gameName varchar(100) NOT NULL,
            gameJson TEXT DEFAULT NULL,
            PRIMARY KEY(gameID)
          ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
          """
    };

    private final String[] createAuthTable = {
            """
            CREATE TABLE IF NOT EXISTS auth (
            authID int NOT NULL AUTO_INCREMENT,
            authToken varchar(100) NOT NULL,
            username varchar(100) NOT NULL,
            PRIMARY KEY(authID)
          ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
          """
    };

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            var tableStatements = new String[][]{createUserTable, createGameTable, createAuthTable};
            for (String[] table : tableStatements) {
                for (String statement : table) {
                    try (var preparedStatement = conn.prepareStatement(statement)) {
                        preparedStatement.executeUpdate();
                    }
                }
            }
        } catch (Exception ex) {
            throw new DataAccessException(String.format("Error: Unable to configure database: %s", ex.getMessage()));
        }
    }

    public void deconstructDatabase() throws DataAccessException {
        var statements = new String[]{"DROP TABLE IF EXISTS user", "DROP TABLE IF EXISTS game", "DROP TABLE IF EXISTS auth"};
        for(var statement : statements) {
            executeUpdate(statement);
        }
    }

    public void removeFromGame(int gameID, ChessGame.TeamColor color) throws DataAccessException {
        var statement = "";
        var gameData = getGame(gameID);
        if(color == ChessGame.TeamColor.BLACK) {
            statement = "UPDATE game SET blackUsername=?, gameJson=? WHERE gameID=?";
            gameData = new GameData(gameID,gameData.whiteUsername(),null,gameData.gameName(),gameData.game());
        } else if (color == ChessGame.TeamColor.WHITE) {
            statement = "UPDATE game SET whiteUsername=?, gameJson=? WHERE gameID=?";
            gameData = new GameData(gameID,null,gameData.blackUsername(),gameData.gameName(),gameData.game());
        }
        var gameJson = new Gson().toJson(gameData);
        executeUpdate(statement, null, gameJson, gameID);
    }

    @Override
    public void updateGame(GameData gameData) throws DataAccessException {
        var statement = "UPDATE game SET gameJSON=? WHERE gameID=?";
        var gameJson = new Gson().toJson(gameData.game());
        executeUpdate(statement, gameJson, gameData.gameID());
    }

}
