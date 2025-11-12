import com.google.gson.Gson;
import server.ServerFacade;
import service.ServiceException;

import java.util.Arrays;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class ChessClient {
    private final ServerFacade server;
    private State state = State.LOGGEDOUT;
    private String authToken;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
    }

    public void run() {
        if(state == State.LOGGEDOUT) {
            System.out.println("♕ Welcome to Chess. Sign in to start.");
            System.out.print(help());
        }
        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit") && state.equals(State.LOGGEDOUT)) {
            printPrompt();
            String line = scanner.nextLine();
            try {
                result = eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        while (!result.equals("quit") && state.equals(State.LOGGEDIN)) {
            printPrompt();
            String line = scanner.nextLine();
            try {
                result = eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.println(msg);
            }
        }
        System.out.println();
        if(!result.equals("quit")) {
            run();
        }
    }

    private void printPrompt() {
        System.out.println("\n" + ">>> " + SET_TEXT_COLOR_GREEN);
    }

    public String eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String cmd = (tokens.length >0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            if(state == State.LOGGEDOUT) {
                return switch (cmd) {
                    case "l", "login" -> login(params);
                    case "r", "register" -> register(params);
                    case "q", "quit" -> "quit";
                    default -> help();
                };
            } else if (state == State.LOGGEDIN) {
                return switch (cmd) {
                    case "l", "list" -> list();
                    case "c", "create" -> create(params);
                    case "logout" -> logout();
                    default -> help();
                };
            }
            return "";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public String register(String... params) throws ServiceException {
        if (params.length >= 3) {
            String username = params[0];
            String password = params[1];
            String email = params[2];
            var registerData = server.register(new model.RegisterRequest(username, password, email));
            authToken = registerData.authToken();
            state = State.LOGGEDIN;
            return String.format("You registered as %s.", username);
        }
        throw new ServiceException("Expected: <USERNAME> <PASSWORD> <EMAIL>", ServiceException.Code.BadRequestError);
    }

    public String login(String... params) throws ServiceException {
        if (params.length >= 2) {
            String username = params[0];
            String password = params[1];
            var loginData = server.login(new model.LoginRequest(username, password));
            authToken = loginData.authToken();
            state = State.LOGGEDIN;
            return String.format("You logged in as %s.", username);
        }
        throw new ServiceException("Expected: <USERNAME> <PASSWORD>", ServiceException.Code.BadRequestError);
    }

    public String list() throws ServiceException {
        var games = server.list();
        var result = new StringBuilder();
        var gson = new Gson();
        for(var game : games.games()) {
            result.append(gson.toJson(game)).append('\n');
        }
        return result.toString();
    }

    public String create(String... params) throws ServiceException {
        if (params.length >= 1) {
            String gameName = params[0];
            var gameData = server.create(new model.CreateGameRequest(authToken, gameName));
            return String.format("Game started with ID %s.", gameData.gameID());
        }
        throw new ServiceException("Expected: <GAMENAME>", ServiceException.Code.BadRequestError);
    }

    public String logout() throws ServiceException {
        server.logout(new model.AuthorizationRequest(authToken));
        state = State.LOGGEDOUT;
        return "Logout successful";
    }

    public String help() {
        if(state == State.LOGGEDOUT) {
            return """
                    Options:
                    Login as an existing user: “l”, “login” <USERNAME> <PASSWORD>
                    Register a new user: “r”, “register” <USERNAME> <PASSWORD> <EMAIL>
                    Exit the program: “q”, “quit”
                    Print this message: “h”, “help”
                    """;
        } else if (state == State.LOGGEDIN) {
            return """
                    Options:
                    List current games: “l”, “list”
                    Create a new game: “c”, “create” <GAME NAME>
                    Join a game: “j”, “join” <GAME ID> <COLOR>
                    Watch a game: “w”, “watch” <GAME ID>
                    Logout: “logout”
                    """;
        }
        return null;
    }
}
