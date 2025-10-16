import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
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

            var service = new Service(dataAccess);
            var server = new Server().run(port);
            port = server;
            System.out.printf("♕ 240 Chess Server: started on part %d with %s%n", port, dataAccess.getClass());
            return;
        } catch (Throwable ex) {
            System.out.printf("Unable to start server: %s%n", ex.getMessage());
        }
        //var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);

    }
}