package http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import managers.TaskManager;
import util.http.JsonBuilder;
import util.http.RequestSegments;

import java.io.IOException;

public class PrioritizedHandler extends HistoryHandler {

    public PrioritizedHandler(TaskManager manager, Gson gson, JsonBuilder jsonBuilder) {
        super(manager, gson, jsonBuilder);
    }

    @Override
    protected void handleGet(HttpExchange exchange, RequestSegments segments) throws IOException {
        sendText(exchange, gson.toJson(manager.getPrioritizedTasks()), 200);
    }

    @Override
    protected boolean validateResources(HttpExchange exchange, RequestSegments segments) throws IOException {
        if (!"prioritized".equals(segments.resource())) {
            sendNotFound(exchange, jsonBuilder.resourceNotFound());
            return true;
        } else if (segments.id() != 0) {
            sendNotFound(exchange, jsonBuilder.subresourceNotFound());
            return true;
        }
        return false;
    }
}
