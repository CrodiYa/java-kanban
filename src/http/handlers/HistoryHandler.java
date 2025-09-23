package http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import managers.TaskManager;
import util.http.JsonBuilder;
import util.http.RequestSegments;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {

    public HistoryHandler(TaskManager manager, Gson gson, JsonBuilder jsonBuilder) {
        super(manager, gson, jsonBuilder);
    }

    @Override
    protected void handleOptions(HttpExchange exchange, RequestSegments segments) throws IOException {
        exchange.getResponseHeaders().add("Allow", "GET, OPTIONS, HEAD");
        exchange.sendResponseHeaders(204, -1);
    }

    @Override
    protected void handleGet(HttpExchange exchange, RequestSegments segments) throws IOException {
        sendText(exchange, gson.toJson(manager.getHistory()), 200);
    }

    @Override
    protected void handlePost(HttpExchange exchange, RequestSegments segments) throws IOException {
        exchange.getResponseHeaders().add("Allow", "GET, HEAD, OPTIONS");
        sendMethodNotAllowed(exchange);
    }

    @Override
    protected void handleDelete(HttpExchange exchange, RequestSegments segments) throws IOException {
        exchange.getResponseHeaders().add("Allow", "GET, HEAD, OPTIONS");
        sendMethodNotAllowed(exchange);
    }

    @Override
    protected boolean validateResources(HttpExchange exchange, RequestSegments segments) throws IOException {
        if (!"history".equals(segments.resource())) {
            sendNotFound(exchange, jsonBuilder.resourceNotFound());
            return true;
        } else if (segments.id() != 0) {
            sendNotFound(exchange, jsonBuilder.subresourceNotFound());
            return true;
        }
        return false;
    }
}
