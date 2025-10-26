package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import service.ServiceException;

import java.sql.*;
import java.util.ArrayList;

import static java.sql.Types.NULL;

public class MySqlDataAccess implements DataAccess{

    public MySqlDataAccess() throws Exception {
        deconstructDatabase();
        configureDatabase();
    }

    @Override
    public void saveUser(UserData userData) throws Exception {
        var statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        executeUpdate(statement, userData.username(), hashPassword(userData.password()), userData.email());
    }

    private void executeUpdate(String statement, Object... params) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
                    /*else if (param instanceof Integer p) ps.setInt(i + 1, p);
                    else if (param instanceof PetType p) ps.setString(i + 1, p.toString());*/
                    else if (param == null) ps.setNull(i + 1, NULL);
                }
                ps.executeUpdate();
            }
        } catch (Exception e) {
            //throw new DataBaseException(ResponseException.Code.ServerError, String.format("unable to update database: %s, %s", statement, e.getMessage()));
            throw new DataAccessException(String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    private int executeUpdateGetID(String statement, Object... params) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
                    /*else if (param instanceof Integer p) ps.setInt(i + 1, p);
                    else if (param instanceof PetType p) ps.setString(i + 1, p.toString());*/
                    else if (param == null) ps.setNull(i + 1, NULL);
                }
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if(rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (Exception e) {
            //throw new DataBaseException(ResponseException.Code.ServerError, String.format("unable to update database: %s, %s", statement, e.getMessage()));
            throw new DataAccessException(String.format("unable to update database: %s, %s", statement, e.getMessage()));
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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    private UserData readUser(ResultSet rs) throws SQLException {
        var username = rs.getString("username");
        var password = rs.getString("password");
        var email = rs.getString("email");
        return new UserData(username, password, email);
    }

    @Override
    public void clearData() throws Exception {
        var statements = new String[]{"TRUNCATE user", "TRUNCATE game", "TRUNCATE auth"};
        for(var statement : statements) {
            executeUpdate(statement);
        }
    }

    @Override
    public boolean findAuth(String authKey) {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken, username FROM auth WHERE authToken=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1,authKey);
                try (ResultSet rs = ps.executeQuery()) {
                    if(rs.next()) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    @Override
    public void deleteAuth(String authKey) {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "DELETE from auth WHERE authToken=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1,authKey);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addAuth(AuthData authData) throws Exception {
        var statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        executeUpdate(statement, authData.authToken(), authData.username());
    }

    @Override
    public ArrayList<GameData> listGames() {
        var res = new ArrayList<GameData>();
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameJson FROM game";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while(rs.next()) {
                        String gameJson = rs.getString("gameJson");
                        GameData game = new Gson().fromJson(gameJson, GameData.class);
                        res.add(game);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    @Override
    public int createGame(String gameName) throws Exception {
        var statement = "INSERT INTO game (whiteUsername, blackUsername, gameName, gameJson) VALUES (?, ?, ?, ?)";
        var game = new ChessGame();
        String gameJson = new Gson().toJson(game);
        return executeUpdateGetID(statement, null, null, gameName, gameJson);
    }

    @Override
    public GameData getGame(int gameID){
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameName, gameJson FROM game WHERE gameID=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String gameName = rs.getString("gameName");
                        String gameJson = rs.getString("gameJson");
                        var game = new Gson().fromJson(gameJson, ChessGame.class);
                        return new GameData(gameID, null, null, gameName, game);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void addPlayerToGame(String authToken, ChessGame.TeamColor playerColor, int gameID) {

    }

    String hashPassword(String clearTextPassword) {
        return BCrypt.hashpw(clearTextPassword, BCrypt.gensalt());
    }

    /*void storeUserPassword(String username, String clearTextPassword) {
        String hashedPassword = BCrypt.hashpw(clearTextPassword, BCrypt.gensalt());

        // write the hashed password in database along with the user's other information
        writeHashedPasswordToDatabase(username, hashedPassword);
    }*/

    /*boolean verifyUser(String username, String providedClearTextPassword) {
        // read the previously hashed password from the database
        var hashedPassword = readHashedPasswordFromDatabase(username);

        return BCrypt.checkpw(providedClearTextPassword, hashedPassword);
    }*/

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
            authToken varchar(100) NOT NULL,
            username varchar(100) NOT NULL,
            PRIMARY KEY(username)
          ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
          """
    };

    private void deconstructDatabase() throws Exception {
        var statements = new String[]{"DROP TABLE IF EXISTS user", "DROP TABLE IF EXISTS game", "DROP TABLE IF EXISTS auth"};
        for(var statement : statements) {
            executeUpdate(statement);
        }
    }

    private void configureDatabase() throws Exception {
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
        } catch (SQLException ex) {
            throw new ServiceException(String.format("Unable to configure database: %s", ex.getMessage()), ServiceException.Code.ServerError);
        }
    }
}
