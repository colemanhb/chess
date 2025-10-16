package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import model.RegisterRequest;
import model.UserData;
import io.javalin.*;
import io.javalin.http.Context;
import service.Service;

public class Server {

    private final Javalin server;
    private Service service;
    private DataAccess dataAccess;

    public Server() {
        dataAccess = new MemoryDataAccess();
        server = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.

    }

    private void register(Context ctx) throws Exception{
        var serializer = new Gson();
        String reqJson = ctx.body();
        var req = serializer.fromJson(reqJson, RegisterRequest.class);
        //call to the service and register
        var res = service.register(req);
        ctx.result(serializer.toJson(res));
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
