package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import model.*;
import io.javalin.*;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import service.Service;
import service.ServiceException;

public class Server {

    private final Javalin httpHandler;
    private final Service service;

    public Server() {
        DataAccess dataAccess = new MemoryDataAccess();
        service = new Service(dataAccess);
        httpHandler = Javalin.create(config -> config.staticFiles.add("web"))
        // Register your endpoints and exception handlers here.
                .post("/user", this::register)
                .post("/session", this::login)
                .delete("/session", this::logout)
                .get("/game", this::listGames)
                .post("/game", this::createGame)
                .put("/game", this::joinGame)
                .delete("/db", this::clear)
                .exception(ServiceException.class, this::exceptionHandler);
    }

    private void joinGame(@NotNull Context ctx) throws Exception {
        var serializer = new Gson();
        String jsonRequest = ctx.body();
        var request = serializer.fromJson(jsonRequest, JoinGameRequest.class);
        //call to the service
        service.joinGame(request);

    }

    private void createGame(@NotNull Context ctx) throws Exception {
        var serializer = new Gson();
        String jsonRequest = ctx.body();
        var request = serializer.fromJson(jsonRequest, CreateGameRequest.class);
        //call to the service
        var res = service.createGame(request);
        ctx.result(serializer.toJson(res));

    }

    private void listGames(@NotNull Context ctx) throws Exception {
        var serializer = new Gson();
        String jsonRequest = ctx.body();
        var request = serializer.fromJson(jsonRequest, AuthorizationRequest.class);
        //call to the service
        var res = service.listGames(request);
        ctx.result(serializer.toJson(res));

    }

    private void logout(@NotNull Context ctx) throws Exception {
        var serializer = new Gson();
        String jsonRequest = ctx.body();
        var request = serializer.fromJson(jsonRequest, AuthorizationRequest.class);
        //call to the service
        service.logout(request);

    }

    private void register(Context ctx) throws Exception{
        var serializer = new Gson();
        String jsonRequest = ctx.body();
        var request = serializer.fromJson(jsonRequest, RegisterRequest.class);
        //call to the service and register
        var res = service.register(request);
        ctx.result(serializer.toJson(res));
    }

    private void login(Context ctx) throws Exception {
        var serializer = new Gson();
        String jsonRequest = ctx.body();
        var request = serializer.fromJson(jsonRequest, LoginRequest.class);
        //call to the service
        var res = service.login(request);
        ctx.result(serializer.toJson(res));
    }

    private void clear(Context ctx) {
        if(service != null) {
            service.clear();
        }
    }

    public int run(int desiredPort) {
        httpHandler.start(desiredPort);
        return httpHandler.port();
    }
    public int port() {
        return httpHandler.port();
    }
    public void stop() {
        httpHandler.stop();
    }
    private void exceptionHandler(ServiceException e, Context ctx) {
        ctx.status(e.toHttpStatusCode());
        ctx.json(e.toJson());
    }
}
