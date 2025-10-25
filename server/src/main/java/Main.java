import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import dataaccess.MySqlDataAccess;
import server.Server;
import service.Service;

public class Main {
    public static void main(String[] args) {
        try {
            var port = 8080;
            if (args.length >= 1) {
                port = Integer.parseInt(args[0]);
            }
            DataAccess dataAccess = new MemoryDataAccess();
            if (args.length >= 2 && args[1].equals("sql")) {
                dataAccess = new MySqlDataAccess();
            }

            //var service = new Service(dataAccess);
            port = new Server().run(port);
            System.out.printf("♕ 240 Chess Server: started on part %d with %s%n", port, dataAccess.getClass());
            //return;
        } catch (Throwable ex) {
            System.out.printf("Unable to start server: %s%n", ex.getMessage());
        }
        //var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);

    }
}