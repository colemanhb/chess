package server;

import com.google.gson.Gson;
import model.*;
import service.ServiceException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public ServerFacade(int port) {
        serverUrl = String.format("http://localhost:%s", port);
    }

    public LoginResult register(RegisterRequest registerRequest) throws ServiceException{
        var request = buildRequest("POST", "/user", registerRequest, null);
        var response = sendRequest(request);
        return handleResponse(response, LoginResult.class);
    }

    public LoginResult login(LoginRequest loginRequest) throws ServiceException {
        var request = buildRequest("POST", "/session", loginRequest, null);
        var response = sendRequest(request);
        return handleResponse(response, LoginResult.class);
    }

    public ListGamesResult list(AuthorizationRequest authorizationRequest) throws ServiceException {
        var request = buildRequest("GET", "/game", null, authorizationRequest.authToken());
        var response = sendRequest(request);
        return handleResponse(response, ListGamesResult.class);
    }

    public CreateGameResult create(CreateGameRequest createGameRequest) throws ServiceException {
        var request = buildRequest("POST", "/game", createGameRequest, createGameRequest.authToken());
        var response = sendRequest(request);
        return handleResponse(response, CreateGameResult.class);
    }

    public ListGamesResult join(JoinGameRequest joinGameRequest) throws ServiceException {
        return list(new AuthorizationRequest(joinGameRequest.authToken()));
    }

    public ListGamesResult watch(JoinGameRequest watchGameRequest) throws ServiceException {
        return list(new AuthorizationRequest(watchGameRequest.authToken()));
    }

    public void logout(AuthorizationRequest logoutRequest) throws ServiceException {
        var request = buildRequest("DELETE", "/session", logoutRequest, logoutRequest.authToken());
        sendRequest(request);
    }

    public void clear() throws ServiceException {
        var request = buildRequest("DELETE", "/db", null, null);
        sendRequest(request);
    }

    private HttpRequest buildRequest(String method, String path, Object body, String authToken) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }
        if(authToken != null) {
            request.setHeader("authorization", authToken);
        }
        return request.build();
    }

    private HttpRequest.BodyPublisher makeRequestBody(Object request) {
        if(request != null) {
            return HttpRequest.BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return HttpRequest.BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ServiceException {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ServiceException(ex.getMessage(), ServiceException.Code.ServerError);
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ServiceException {
        var status = response.statusCode();
        if (!isSuccessful(status)) {
            var body = response.body();
            if(body != null) {
                throw ServiceException.fromJson(body);
            }

            throw new ServiceException("other failure" + status, ServiceException.fromHttpStatusCode(status));
        }
        if(responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }

        return null;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
