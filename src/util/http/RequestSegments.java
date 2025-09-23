package util.http;

import com.sun.net.httpserver.HttpExchange;
import util.enums.Endpoint;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public record RequestSegments(
        Endpoint endpoint,
        String resource,
        int id,
        Optional<String> subResource) {

    private final static HashSet<String> validMethods = new HashSet<>(List.of(
            "GET",
            "POST",
            "DELETE",
            "HEAD",
            "OPTIONS")
    );

    public static RequestSegments getRequestSegments(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        return RequestSegments.parse(exchange.getRequestMethod(), path);
    }

    private static RequestSegments parse(String method, String path) {
        String[] parts = path.split("/");

        return switch (parts.length) {
            case 2 -> new RequestSegments(
                    parseEndpoint(method),
                    parts[1],
                    0,
                    Optional.empty());
            case 3 -> new RequestSegments(
                    parseEndpoint(method),
                    parts[1],
                    parseInt(parts[2]),
                    Optional.empty());
            case 4 -> new RequestSegments(
                    parseEndpoint(method),
                    parts[1],
                    parseInt(parts[2]),
                    Optional.of(parts[3]));
            default -> new RequestSegments(
                    Endpoint.INVALID_SUBRESOURCE,
                    parts[1],
                    0,
                    Optional.empty());
        };
    }

    private static int parseInt(String idString) {
        try {
            int id = Integer.parseInt(idString);
            if (id < 0) {
                throw new NumberFormatException();
            } else {
                return id;
            }
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static Endpoint parseEndpoint(String method) {
        if (!validMethods.contains(method)) {
            return Endpoint.INVALID_METHOD;
        }

        Endpoint endpoint;

        if (method.equals("HEAD")) {
            method = "GET";
        }

        try {
            endpoint = Endpoint.valueOf(method);
        } catch (IllegalArgumentException e) {
            endpoint = Endpoint.INVALID;
        }

        return endpoint;
    }


}
