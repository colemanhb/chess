import server.ServerFacade;
import service.ServiceException;

import java.util.Arrays;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class PreLoginClient {
    private final ServerFacade server;
    private State state = State.LOGGEDOUT;

    public PreLoginClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println("♕ Welcome to Chess. Sign in to start.");
        System.out.print(help());

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
        System.out.println();
    }

    private void printPrompt() {
        System.out.println("\n" + ">>> " + SET_TEXT_COLOR_GREEN);
    }

    public String eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String cmd = (tokens.length >0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "l", "login" -> login(params);
                case "r", "register" -> register(params);
                case "q", "quit" -> "quit";
                default -> help();
            };
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public String register(String... params) throws ServiceException {
        if (params.length >= 3) {
            state = State.LOGGEDIN;
            String username = params[0];
            String password = params[1];
            String email = params[2];
            server.register(new model.RegisterRequest(username, password, email));
            return String.format("You registered as %s.", username);
        }
        throw new ServiceException("Expected: <USERNAME> <PASSWORD> <EMAIL>", ServiceException.Code.BadRequestError);
    }

    public String login(String... params) throws ServiceException {
        if (params.length >= 2) {
            state = State.LOGGEDIN;
            String username = params[0];
            String password = params[1];
            server.login(new model.LoginRequest(username, password));
            return String.format("You logged in as %s.", username);
        }
        throw new ServiceException("Expected: <USERNAME> <PASSWORD>", ServiceException.Code.BadRequestError);
    }

    public String help() {
        return """
                Options:
                Login as an existing user: “l”, “login” <USERNAME> <PASSWORD>
                Register a new user: “r”, “register” <USERNAME> <PASSWORD> <EMAIL>
                Exit the program: “q”, “quit”
                Print this message: “h”, “help”
                """;
    }
}
