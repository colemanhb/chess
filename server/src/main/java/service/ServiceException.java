package service;

import com.google.gson.Gson;

import java.util.Map;

public class ServiceException extends Exception {

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
