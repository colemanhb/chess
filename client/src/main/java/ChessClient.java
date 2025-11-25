import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import model.*;
import server.ServerFacade;
import service.ServiceException;
import websocket.NotificationHandler;
import websocket.WebSocketFacade;

import javax.management.Notification;
import java.util.Arrays;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class ChessClient implements NotificationHandler {
    private String username = null;
    private final ServerFacade server;
    private State state = State.LOGGEDOUT;
    private String authToken;
    private final WebSocketFacade ws;

    public ChessClient(String serverUrl) throws ServiceException {
        server = new ServerFacade(serverUrl);
        ws = new WebSocketFacade(serverUrl,this);
    }

    public void run() {
        if(state == State.LOGGEDOUT) {
            System.out.println("♕ Welcome to Chess. Sign in to start.");
            System.out.print(help());
        }
        run(false);
    }

    public void run(boolean printMessages) {
        if(printMessages) {
            run();
        }
        Scanner scanner = new Scanner(System.in);
        var result = "";
        result = printMessagesByState(result, State.LOGGEDOUT, scanner);
        result = printMessagesByState(result, State.LOGGEDIN, scanner);
        System.out.println();
        if(state == State.LOGGEDIN && !result.equals("quit")) {
            run(false);
        }
        if(state.equals(State.PLAYINGGAME) || state.equals(State.WATCHINGGAME)) {
            result = printMessagesByState(result, state, scanner);
        }
        if(!result.equals("quit")) {
            run(false);
        }
    }

    private String printMessagesByState(String result, State targetState, Scanner scanner) {
        while (!result.equals("quit") && state.equals(targetState)) {
            printPrompt();
            String line = scanner.nextLine();
            try {
                result = eval(line);
                System.out.print(result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        return result;
    }

    private void printPrompt() {
        System.out.println("\n" + SET_TEXT_COLOR_GREEN + ">>> " + SET_TEXT_COLOR_BLUE);
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
                    case "j", "join" -> join(params);
                    case "w", "watch" -> watch(params);
                    case "logout" -> logout();
                    default -> help();
                };
            } else if (state == State.PLAYINGGAME || state == State.WATCHINGGAME) {
                return switch (cmd) {
                    //case "r", "redraw" -> redraw();
                    case "l", "leave" -> leave();
                    case "m", "move" -> makeMove(params);
                    //case "resign" -> resign();
                    //case "h", "highlight" -> highlight(params);
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
            var registerData = server.register(new RegisterRequest(username, password, email));
            authToken = registerData.authToken();
            state = State.LOGGEDIN;
            this.username = username;
            return String.format("You registered as %s.", username);
        }
        throw new ServiceException("Expected: <USERNAME> <PASSWORD> <EMAIL>", ServiceException.Code.BadRequestError);
    }

    public String login(String... params) throws ServiceException {
        if (params.length >= 2) {
            String username = params[0];
            String password = params[1];
            var loginData = server.login(new LoginRequest(username, password));
            authToken = loginData.authToken();
            state = State.LOGGEDIN;
            this.username = username;
            return String.format("You logged in as %s.", username);
        }
        throw new ServiceException("Expected: <USERNAME> <PASSWORD>", ServiceException.Code.BadRequestError);
    }

    public String list() throws ServiceException {
        var games = server.list(new AuthorizationRequest(authToken));
        var result = new StringBuilder();
        for(var game : games.games()) {
            result.append(readableGameData(game)).append("\n\n");
        }
        return result.toString();
    }

    private String readableGameData(GameData gameData) {
        return "Game " + gameData.gameID() + ": " +
                gameData.gameName() + "\n" +
                "White player: " + gameData.whiteUsername() + ", black player: " + gameData.blackUsername();
    }

    public String create(String... params) throws ServiceException {
        if (params.length >= 1) {
            String gameName = params[0];
            var gameData = server.create(new CreateGameRequest(authToken, gameName));
            return String.format("Game started with ID %s.", gameData.gameID());
        }
        throw new ServiceException("Expected: <GAMENAME>", ServiceException.Code.BadRequestError);
    }

    public String join(String... params) throws ServiceException {
        if(params.length >= 2) {
            int gameID;
            try {
                gameID = Integer.parseInt(params[0]);
            } catch (Exception e) {
                throw new ServiceException("Not a number", ServiceException.Code.BadRequestError);
            }
            if(gameID <= 0 || !list().contains("Game " + gameID)) {
                throw new ServiceException("Invalid number", ServiceException.Code.BadRequestError);
            }
            ChessGame.TeamColor color;
            try {
                color = ChessGame.TeamColor.valueOf(params[1].toUpperCase());
            } catch (Exception e) {
                throw new ServiceException("Invalid color", ServiceException.Code.BadRequestError);
            }
            ListGamesResult gameList;
            gameList = server.join(new JoinGameRequest(authToken, color, gameID));
            state = State.PLAYINGGAME;
            ws.join(authToken, gameID);
            return makeGrid(findGame(gameList, gameID), color == ChessGame.TeamColor.WHITE);
        }
        throw new ServiceException("Expected: <GAME ID> <COLOR>", ServiceException.Code.BadRequestError);
    }

    public String watch(String... params) throws ServiceException {
        if(params.length >= 1) {
            int gameID = Integer.parseInt(params[0]);
            var gameList = server.watch(new JoinGameRequest(authToken, null, gameID));
            state = State.WATCHINGGAME;
            return makeGrid(findGame(gameList, gameID), true);
        }
        throw new ServiceException("Expected: <GAME ID>", ServiceException.Code.BadRequestError);
    }

    private ChessBoard findGame(ListGamesResult gameList, int gameID) {
        for(var i : gameList.games()) {
            if (i.gameID() == gameID) {
                return i.game().getBoard();
            }
        }
        return null;
    }


    private String makeGrid(ChessBoard board, boolean whiteSide) {
        StringBuilder result = new StringBuilder();
        result.append(makeLetterLabels(whiteSide)).append("\n");
        var row = 0;
        var col = 0;
        for(int i = 1; i <= 8; i ++) {
            for(int j = 1; j <= 8; j ++) {
                row = i;
                col = j;
                if(whiteSide) {
                    row = 9 - i;
                }
                if(j == 1) {
                    result.append(" ").append(row).append(" ");
                }
                result.append(makeSquare(board, new ChessPosition(row,col)));
            }
            result.append(RESET_BG_COLOR + SET_TEXT_COLOR_WHITE);
            result.append(" ").append(row).append(" ").append("\n");
        }
        result.append(makeLetterLabels(whiteSide));
        return result.toString();
    }

    private String makeLetterLabels(boolean forwards) {
        if(forwards) {
            return RESET_BG_COLOR + SET_TEXT_COLOR_WHITE + "    a  b  c  d  e  f  g  h   ";
        }
        else {
            return RESET_BG_COLOR + SET_TEXT_COLOR_WHITE + "    h  g  f  e  d  c  b  a   ";
        }
    }

    private String makeSquare(ChessBoard board, ChessPosition position) {
        var piece = board.getPiece(position);
        var row = position.getRow();
        var col = position.getColumn();
        StringBuilder result = new StringBuilder();
        if((row + col) % 2 == 1) {
            result.append(SET_BG_COLOR_LIGHT_GREY);
        }
        else {
            result.append(SET_BG_COLOR_DARK_GREY);
        }
        if(piece != null) {
            if(piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                result.append(SET_TEXT_COLOR_BLACK);
            }
            else {
                result.append(SET_TEXT_COLOR_WHITE);
            }
            result.append(visualizePiece(piece));
        }
        else {
            result.append("   ");
        }
        return result.toString();
    }

    private String visualizePiece(ChessPiece piece) {
        var type = piece.getPieceType();
        var color = piece.getTeamColor();
        if(color == ChessGame.TeamColor.WHITE)
        {
            return switch(type) {
                case ROOK -> WHITE_ROOK;
                case BISHOP -> WHITE_BISHOP;
                case KNIGHT -> WHITE_KNIGHT;
                case KING -> WHITE_KING;
                case QUEEN -> WHITE_QUEEN;
                case PAWN -> WHITE_PAWN;
            };
        }
        else {
            return switch(type) {
                case ROOK -> BLACK_ROOK;
                case BISHOP -> BLACK_BISHOP;
                case KNIGHT -> BLACK_KNIGHT;
                case KING -> BLACK_KING;
                case QUEEN -> BLACK_QUEEN;
                case PAWN -> BLACK_PAWN;
            };
        }
    }

    public String logout() throws ServiceException {
        server.logout(new AuthorizationRequest(authToken));
        state = State.LOGGEDOUT;
        username = null;
        return "Logout successful";
    }

    //public String redraw()

    public String leave() throws ServiceException {
        state = State.LOGGEDIN;
        ws.leave(authToken);
        return String.format("%s left the game", username);
    }

    public String makeMove(String... params) throws ServiceException {
        String start = params[0];
        String end = params[1];
        String promotion = params[2];
        ws.makeMove(authToken, start, end, promotion);
        return "";
    }

    //public String resign()

    //public String highlight()

    public String help() {
        var result = "";
        if(state == State.LOGGEDOUT) {
            result =  """
                    Options:
                    Login as an existing user: “l”, “login” <USERNAME> <PASSWORD>
                    Register a new user: “r”, “register” <USERNAME> <PASSWORD> <EMAIL>
                    Exit the program: “q”, “quit”
                    Print this message: “h”, “help”
                    """;
        } else if (state == State.LOGGEDIN) {
            result =  """
                    Options:
                    List current games: “l”, “list”
                    Create a new game: “c”, “create” <GAME NAME>
                    Join a game: “j”, “join” <GAME ID> <COLOR>
                    Watch a game: “w”, “watch” <GAME ID>
                    Logout: “logout”
                    Print help message: "h", "help"
                    """;
        } else if(state == State.PLAYINGGAME) {
            result = """
                    Options:
                    Redraw chess board: "r", "redraw"
                    Leave your game: "l", "leave"
                    Make a move: "m", "move" <STARTING LOCATION> <ENDING LOCATION> <PROMOTION PIECE>
                    Resign (forfeit): "resign"
                    Highlight legal moves: "h", "highlight" <PIECE LOCATION>
                    Print help message: "help"
                    """;
        } else if(state == State.WATCHINGGAME) {
            result = """
                    Options:
                    Redraw chess board: "r", "redraw"
                    Leave your game: "l", "leave"
                    Highlight legal moves: "h", "highlight" <PIECE LOCATION>
                    Print help message: "help"
                    """;
        }
        return SET_TEXT_COLOR_WHITE + result + SET_TEXT_COLOR_BLUE;
    }

    @Override
    public void notify(Notification notification) {

    }
}
