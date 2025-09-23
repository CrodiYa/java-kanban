package http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.TaskManager;
import util.http.JsonBuilder;
import util.http.RequestSegments;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static util.enums.Endpoint.*;
import static util.http.RequestSegments.getRequestSegments;

public abstract class BaseHttpHandler implements HttpHandler {
    private final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    protected final TaskManager manager;

    protected Gson gson;
    protected JsonBuilder jsonBuilder;


    public BaseHttpHandler(TaskManager manager, Gson gson, JsonBuilder jsonBuilder) {
        this.manager = manager;
        this.gson = gson;

        this.jsonBuilder = jsonBuilder;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            RequestSegments segments = getPreparedSegments(exchange);

            if (segments == null || validateResources(exchange, segments)) {
                return;
            }

            mapEndpoints(exchange, segments);

        } catch (JsonSyntaxException e) {
            sendText(exchange, jsonBuilder.badRequest(
                    "Json must contain: taskId, title, description and status[NEW, IN_PROGRESS, DONE]. " +
                            "If subtasks is resource: epicId. " +
                            "If being added taskId must be 0."), 400);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            try {
                sendServerError(exchange);
                e.printStackTrace();
            } catch (IOException ex) {
                System.out.println("Failed to send response");
            }
        }
    }

    private RequestSegments getPreparedSegments(HttpExchange exchange) throws IOException {
        RequestSegments segments = getRequestSegments(exchange);

        jsonBuilder.setSegments(segments);

        if (!isSegmentsValid(exchange, segments)) {
            return null;
        }

        return segments;
    }

    private void mapEndpoints(HttpExchange exchange, RequestSegments segments) throws IOException {
        switch (segments.endpoint()) {
            case OPTIONS -> handleOptions(exchange, segments);
            case GET -> handleGet(exchange, segments);
            case POST -> handlePost(exchange, segments);
            case DELETE -> handleDelete(exchange, segments);
            default -> sendText(exchange, jsonBuilder.badRequest("No such endpoint"), 400);
        }
    }

    private boolean isSegmentsValid(HttpExchange exchange, RequestSegments segments) throws IOException {
        System.out.printf("Validating segments: %s Method: %s\n", segments, exchange.getRequestMethod());

        if (segments.endpoint() == INVALID_METHOD) {
            exchange.getResponseHeaders().add("Allow", "GET, POST, DELETE, HEAD, OPTIONS");
            sendMethodNotAllowed(exchange);
            return false;
        } else if (segments.endpoint() == INVALID) {
            sendText(exchange, jsonBuilder.badRequest("No such endpoint"), 400);
            return false;
        } else if (segments.endpoint() == INVALID_SUBRESOURCE) {
            sendNotFound(exchange, jsonBuilder.tooMuchSubResources());
            return false;
        } else if (segments.id() == -1) {
            sendText(exchange, jsonBuilder.invalidId(), 400);
            return false;
        } else if (segments.subResource().isPresent() && !segments.resource().equals("epics")) {
            sendNotFound(exchange, jsonBuilder.subresourceNotFound());
            return false;
        }

        return true;
    }


    protected void sendText(HttpExchange exchange, String responseString, int responseCode) throws IOException {
        byte[] resp = responseString.getBytes(DEFAULT_CHARSET);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.getResponseHeaders().add("Content-Length", String.valueOf(resp.length));

        if (!"HEAD".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(responseCode, resp.length);
            exchange.getResponseBody().write(resp);
        } else {
            exchange.sendResponseHeaders(responseCode, -1);
        }

        exchange.close();
    }

    protected void sendNotFound(HttpExchange exchange, String responseString) throws IOException {
        sendText(exchange, responseString, 404);
    }

    protected void sendHasOverlaps(HttpExchange exchange, String responseString) throws IOException {
        sendText(exchange, responseString, 406);
    }

    protected void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(405, -1);
        exchange.close();
    }

    protected void sendServerError(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(500, -1);
        exchange.close();
    }

    protected boolean isPostForbidden(HttpExchange exchange, RequestSegments segments) throws IOException {
        if (segments.id() != 0) {
            exchange.getResponseHeaders().add("Allow", "GET, DELETE, OPTIONS, HEAD");
            sendMethodNotAllowed(exchange);
            return true;
        }
        return false;
    }

    protected abstract void handleOptions(HttpExchange exchange, RequestSegments segments) throws IOException;

    protected abstract void handleGet(HttpExchange exchange, RequestSegments segments) throws IOException;

    protected abstract void handlePost(HttpExchange exchange, RequestSegments segments) throws IOException;

    protected abstract void handleDelete(HttpExchange exchange, RequestSegments segments) throws IOException;

    protected abstract boolean validateResources(HttpExchange exchange, RequestSegments segments) throws IOException;
}
