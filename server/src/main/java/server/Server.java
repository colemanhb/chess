package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import model.RegisterRequest;
import model.UserData;
import io.javalin.*;
import io.javalin.http.Context;
import service.Service;
import service.ServiceException;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Server {

    private final Javalin httpHandler;
    private Service service;
    private DataAccess dataAccess;

    public Server() {
        dataAccess = new MemoryDataAccess();
        service = new Service(dataAccess);
        httpHandler = Javalin.create(config -> config.staticFiles.add("web"))
        // Register your endpoints and exception handlers here.
                .post("/user", this::register)
                /*.post("/session/{username,password}", this::login)
                .delete("/session/{authToken}", this::logout)
                .get("/game/{authToken", this::listGames)
                .post("/game/authToken/{gameName}", this::createGame)
                .put("/game/authToken/{playerColor,gameID}", this::joinGame)*/
                .delete("/db", this::clear)
                .exception(ServiceException.class, this::exceptionHandler);
    }

    private void register(Context ctx) throws Exception{
        var serializer = new Gson();
        String jsonRequest = ctx.body();
        var request = serializer.fromJson(jsonRequest, RegisterRequest.class);
        //call to the service and register
        var res = service.register(request);
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
