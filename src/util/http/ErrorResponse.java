package util.http;

import com.google.gson.Gson;

public class ErrorResponse {
    private final String error;
    private final String message;

    private ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
    }

    public static String ErrorToJson(Gson gson, String error, String message) {
        return gson.toJson(new ErrorResponse(error, message));
    }
}