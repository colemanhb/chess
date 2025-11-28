import chess.*;
import model.*;
import server.ServerFacade;
import service.ServiceException;
import websocket.NotificationHandler;
import websocket.WebSocketFacade;
import websocket.messages.ServerMessage;

import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class ChessClient implements NotificationHandler {
    private final ServerFacade server;
    private State state = State.LOGGEDOUT;
    private String authToken;
    private final WebSocketFacade ws;
    private int currentGame = 0;
    private ChessGame.TeamColor teamColor = null;

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
                switch (cmd) {
                    case "r", "redraw" -> {
                        return redraw(false,(String) null);
                    }
                    case "l", "leave" -> leave();
                    case "m", "move" -> makeMove(params);
                    case "resign" -> resign();
                    case "highlight" -> {
                        return redraw(true, params);
                    }
                    default -> {
                        return help();
                    }
                }
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
            server.join(new JoinGameRequest(authToken, color, gameID));
            state = State.PLAYINGGAME;
            ws.join(authToken, gameID);
            currentGame = gameID;
            teamColor = color;
            return "";
        }
        throw new ServiceException("Expected: <GAME ID> <COLOR>", ServiceException.Code.BadRequestError);
    }

    public String watch(String... params) throws ServiceException {
        if(params.length >= 1) {
            int gameID = Integer.parseInt(params[0]);
            var gameList = server.watch(new JoinGameRequest(authToken, null, gameID));
            state = State.WATCHINGGAME;
            currentGame = gameID;
            teamColor = ChessGame.TeamColor.WHITE;
            return makeGrid(findGame(gameList, gameID), true, null);
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

    private String makeGrid(ChessBoard board, boolean whiteSide, ChessPosition highlightPosition) {
        var highlight = highlightPosition != null;
        ChessGame tempGame;
        Collection<ChessMove> validMoves = null;
        if(highlight) {
            tempGame = new ChessGame(board);
            validMoves = tempGame.validMoves(highlightPosition);
        }
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
                } else {
                    col = 9 - j;
                }
                if(j == 1) {
                    result.append(" ").append(row).append(" ");
                }
                var currentPosition = new ChessPosition(row,col);
                boolean highlightSquare = false;
                if(highlight) {
                    var onCurrentPosition = currentPosition.equals(highlightPosition);
                    var validMove = validMoves.contains(new ChessMove(highlightPosition, currentPosition, null));
                    if(onCurrentPosition || validMove) {
                        highlightSquare = true;
                    }
                }
                result.append(makeSquare(board, new ChessPosition(row,col), highlightSquare));
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

    private String makeSquare(ChessBoard board, ChessPosition position, boolean highlight) {
        var piece = board.getPiece(position);
        var row = position.getRow();
        var col = position.getColumn();
        StringBuilder result = new StringBuilder();
        if((row + col) % 2 == 1) {
            if(highlight) {
                result.append(SET_BG_COLOR_RED);
            }
            else {
                result.append(SET_BG_COLOR_LIGHT_GREY);
            }
        }
        else {
            if(highlight) {
                result.append(SET_BG_COLOR_DARK_GREEN);
            }
            else {
                result.append(SET_BG_COLOR_DARK_GREY);
            }
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
        return "Logout successful";
    }

    public void leave() throws ServiceException {
        state = State.LOGGEDIN;
        ws.leave(authToken, currentGame);
    }

    public void makeMove(String... params) throws ServiceException {
        if(params.length < 2) {
            throw new ServiceException("Expected: <STARTING LOCATION> <ENDING LOCATION>", ServiceException.Code.BadRequestError);
        }
        String start = params[0];
        String end = params[1];
        String promotion = null;
        if(params.length > 2) {
            promotion = params[2];
        }
        var startingLocation = stringToLocation(start);
        var endingLocation = stringToLocation(end);
        if(startingLocation == null || endingLocation == null) {
            throw new ServiceException("Please enter valid square (example: a1, h8", ServiceException.Code.BadRequestError);
        }
        ChessPiece.PieceType promotionPiece;
        try {
            promotionPiece = ChessPiece.PieceType.valueOf(promotion);
        } catch (Exception ex) {
            promotionPiece =  null;
        }
        var chessMove = new ChessMove(startingLocation, endingLocation, promotionPiece);
        ws.makeMove(authToken, currentGame, chessMove);
    }

    public void resign() throws ServiceException {
        ws.resign(authToken, currentGame);
    }

    private String redraw(boolean highlight, String... params) throws ServiceException {
        String highlightString = null;
        if(highlight && params.length == 0) {
            throw new ServiceException("Expected: <PIECE LOCATION>", ServiceException.Code.BadRequestError);
        }
        if(params.length > 0) {
            highlightString = params[0];
        }
        var games = server.list(new AuthorizationRequest(authToken));
        var game = findGame(games, currentGame);
        if(game == null){
            return "GAME BOARD IS NULL";
        }
        ChessPosition highlightPosition = null;
        if(highlight) {
            highlightPosition = stringToLocation(highlightString);
        }
        return makeGrid(game, teamColor == ChessGame.TeamColor.WHITE, highlightPosition);
    }

    private ChessPosition stringToLocation(String str) {
        str = str.toLowerCase();
        var letter = str.charAt(0);
        var number = str.charAt(1);
        if(!Character.isLetter(letter) || !Character.isDigit(number)) {
            return null;
        }
        var row = Integer.parseInt(String.valueOf(number));
        var col = (int) letter - 96;
        if(row >= 1 && col >= 1 && row <= 8 && col <= 8) {
            return new ChessPosition(row, col);
        }
        return null;
    }

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
                    Highlight legal moves: "highlight" <PIECE LOCATION>
                    Print help message: "h", "help"
                    """;
        }
        return SET_TEXT_COLOR_WHITE + result + SET_TEXT_COLOR_BLUE;
    }

    @Override
    public void notify(ServerMessage msg) {
        switch(msg.getServerMessageType()) {
            case LOAD_GAME -> {
                try {
                    System.out.println(redraw(false, (String) null));
                } catch (ServiceException e) {
                    System.out.println(e.getMessage());
                }

            }
            case ERROR -> System.out.println(SET_TEXT_COLOR_RED + msg.getErrorMessage());
            case NOTIFICATION -> System.out.println(SET_TEXT_COLOR_RED + msg.getMessage());
        }
        printPrompt();
    }
}
