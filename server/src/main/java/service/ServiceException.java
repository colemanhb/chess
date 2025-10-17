package service;

import com.google.gson.Gson;

import java.util.Map;

public class ServiceException extends Exception {

    public enum Code {
        AlreadyTakenError,
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
            case AlreadyTakenError -> 403;
        };
    }
}
