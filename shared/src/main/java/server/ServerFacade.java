package server;

import com.google.gson.Gson;
import model.LoginRequest;
import model.LoginResult;
import model.RegisterRequest;
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

    public LoginResult register(RegisterRequest registerRequest) throws ServiceException{
        var request = buildRequest("POST", "/user", registerRequest);
        var response = sendRequest(request);
        return handleResponse(response, LoginResult.class);
    }

    public LoginResult login(LoginRequest loginRequest) throws ServiceException {
        var request = buildRequest("POST", "/session", loginRequest);
        var response = sendRequest(request);
        return handleResponse(response, LoginResult.class);
    }

    private HttpRequest buildRequest(String method, String path, Object body) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (body != null) {
            request.setHeader("Content-Type", "application/json");
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
