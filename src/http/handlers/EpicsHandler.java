package http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import managers.TaskManager;
import model.Epic;
import model.Task;
import util.enums.Endpoint;
import util.exceptions.TaskNotFound;
import util.http.JsonBuilder;
import util.http.RequestSegments;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EpicsHandler extends TaskHandler {

    public EpicsHandler(TaskManager manager, Gson gson, JsonBuilder jsonBuilder) {
        super(manager, gson, jsonBuilder);
    }

    @Override
    protected void handleOptions(HttpExchange exchange, RequestSegments segments) throws IOException {
        if (segments.id() == 0 && segments.subResource().isEmpty()) {
            exchange.getResponseHeaders().add("Allow", "GET, POST, DELETE, OPTIONS, HEAD");
        } else {
            exchange.getResponseHeaders().add("Allow", "GET, DELETE, OPTIONS, HEAD");
        }
        exchange.sendResponseHeaders(204, -1);
    }

    @Override
    protected void handleGet(HttpExchange exchange, RequestSegments segments) throws IOException {
        int id = segments.id();

        if (segments.subResource().isPresent()) {
            if (manager.getEpicWithoutHistory(id) != null) {
                sendText(exchange, gson.toJson(manager.getSubTasksFromEpic(id)), 200);
            } else {
                sendNotFound(exchange, jsonBuilder.notFound("Epic with id '" + id + "'not found"));
            }
        } else if (id == 0) {
            sendText(exchange, gson.toJson(manager.getEpics()), 200);
        } else {
            Epic epic = manager.getEpic(id);
            if (epic != null) {
                sendText(exchange, gson.toJson(epic), 200);
            } else {
                sendNotFound(exchange, jsonBuilder.notFound("Epic with id '" + id + "'not found"));
            }
        }
    }

    @Override
    protected void handlePost(HttpExchange exchange, RequestSegments segments) throws IOException {
        if (isPostForbidden(exchange, segments)) {
            return;
        }
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        if (body.isBlank()) {
            sendText(exchange, jsonBuilder.badRequest("Body is required"), 400);
        }
        try {
            Epic epic = gson.fromJson(body, Epic.class);
            isTaskValid(epic);

            if (epic.getTaskId() == 0) {
                //новый эпик для безопасного создания
                epic = new Epic(epic.getTitle(), epic.getDescription(), epic.getStatus());
                manager.addEpic(epic);
            } else {
                manager.updateEpic(epic);
            }
            sendText(exchange, gson.toJson(manager.getEpicWithoutHistory(epic.getTaskId())), 201);
        } catch (JsonSyntaxException e) {
            sendText(exchange,
                    jsonBuilder.badRequest(
                            "Json must contain: taskId, title, description and status[NEW, IN_PROGRESS, DONE]."),
                    400);
        } catch (TaskNotFound e) {
            sendNotFound(exchange, jsonBuilder.notFound(e.getMessage()));
        }
    }

    @Override
    protected void handleDelete(HttpExchange exchange, RequestSegments segments) throws IOException {

        if (segments.subResource().isPresent()) {
            try {
                manager.clearSubTasksFromEpic(segments.id());
                sendText(exchange,
                        jsonBuilder.message("Subtasks from epic: '" + segments.id() + "' were deleted"),
                        200);
            } catch (TaskNotFound e) {
                sendNotFound(exchange, jsonBuilder.notFound(e.getMessage()));
            }
        } else if (segments.id() == 0) {
            manager.clearEpics();
            sendText(exchange, jsonBuilder.message("Epics were deleted"), 200);
        } else {
            manager.deleteEpic(segments.id());
            sendText(exchange, jsonBuilder.message("Epic: '" + segments.id() + "' was deleted"), 200);
        }
    }

    @Override
    protected boolean validateResources(HttpExchange exchange, RequestSegments segments) throws IOException {
        if (!"epics".equals(segments.resource())) {
            sendNotFound(exchange, jsonBuilder.resourceNotFound());
            return true;
        }

        if (segments.subResource().isPresent()) {
            if (!segments.subResource().get().equals("subtasks")) {
                sendNotFound(exchange, jsonBuilder.subresourceNotFound());
                return true;
            }
            if (segments.endpoint() == Endpoint.POST) {
                exchange.getResponseHeaders().add("Allow", "GET, DELETE, HEAD, OPTIONS");
                sendMethodNotAllowed(exchange);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void isTaskValid(Task task) {
        if (task.getTitle() == null || task.getDescription() == null) {
            throw new JsonSyntaxException("Epic has invalid fields");
        }
    }
}
