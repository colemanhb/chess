public class Main {
    public static void main(String[] args) {
        String serverUrl = "http://localhost:8080";
        if(args.length == 1) {
            serverUrl = args[0];
        }

        try {
            var chessClient = new ChessClient(serverUrl);
            chessClient.run();
        } catch (Throwable ex) {
            System.out.printf("Unable to start server: %s%n", ex.getMessage());
        }
    }
}