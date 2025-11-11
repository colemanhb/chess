import server.ServerFacade;

public class PostLoginClient {
    private final ServerFacade server;
    private State state = State.LOGGEDIN;

    public PostLoginClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
    }

    public State getState() {
        return state;
    }

}
