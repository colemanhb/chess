import server.Server;

public class Main {
    public static void main(String[] args) {
        var port = 8080;
        port = new Server().run(port);
        System.out.printf("♕ 240 Chess Server: started on part %d%n", port);
    }
}