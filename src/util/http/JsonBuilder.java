package util.http;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class JsonBuilder {
    private final Gson gson;
    private RequestSegments segments;

    public JsonBuilder(Gson gson) {
        this.gson = gson;
    }

    public void setSegments(RequestSegments segments) {
        this.segments = segments;
    }

    public String message(String message) {
        Map<String, String> map = new HashMap<>();
        map.put("message", message);
        return gson.toJson(map);
    }

    public String tooMuchSubResources() {
        return notFound("Subresources do not exist");
    }

    public String badRequest(String message) {
        return ErrorResponse.ErrorToJson(
                gson,
                "Bad Request",
                message
        );
    }

    public String notFound(String message) {
        return ErrorResponse.ErrorToJson(
                gson,
                "Not Found",
                message
        );
    }

    public String resourceNotFound() {
        return notFound("Resource '" + segments.resource() + "' does not exist");
    }

    public String subresourceNotFound() {
        return notFound("Subresource '" + segments.subResource().orElse("") + "' does not exist");
    }

    public String hasOverlaps() {
        return ErrorResponse.ErrorToJson(gson, "Task time is overlapping",
                "This task cannot be added due to overlap"
        );
    }

    public String invalidId() {
        return ErrorResponse.ErrorToJson(
                gson,
                "Bad Request",
                "Id must be a positive integer"
        );
    }
}
