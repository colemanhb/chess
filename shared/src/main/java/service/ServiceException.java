package service;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class ServiceException extends Exception {

    public static ServiceException fromJson(String json) {
        var map = new Gson().fromJson(json, HashMap.class);
        var status = Code.valueOf(map.get("status").toString());
        String message = map.get("message").toString();
        return new ServiceException(message, status);
    }

    public static Code fromHttpStatusCode(int httpStatusCode) {
        return switch (httpStatusCode) {
            case 500, 402 -> Code.ServerError;
            case 400 -> Code.BadRequestError;
            case 403 -> Code.AlreadyTakenError;
            case 401 -> Code.NotFoundError;
            default -> throw new IllegalStateException("Unexpected value: " + httpStatusCode);
        };
    }

    public enum Code {
        AlreadyTakenError,
        NotFoundError,
        IncorrectPasswordError,
        NotLoggedInError,
        GameNotFoundError,
        BadRequestError,
        ServerError,
        ColorNotAvailableError,
    }

    final private Code code;

    public ServiceException(String message, Code code) {
        super(message);
        this.code = code;
    }

    public String toJson() {
        return new Gson().toJson(Map.of("message", getMessage(), "status", code));
    }
    public int toHttpStatusCode() {
        return switch (code) {
            case AlreadyTakenError, ColorNotAvailableError -> 403;
            case BadRequestError -> 400;
            case NotFoundError, IncorrectPasswordError, GameNotFoundError, NotLoggedInError -> 401;
            case ServerError -> 402;
        };
    }
}
