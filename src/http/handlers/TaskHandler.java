package http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import managers.TaskManager;
import model.Task;
import util.exceptions.TaskNotFound;
import util.exceptions.TaskTimeOverlapException;
import util.http.JsonBuilder;
import util.http.RequestSegments;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TaskHandler extends BaseHttpHandler {

    public TaskHandler(TaskManager manager, Gson gson, JsonBuilder jsonBuilder) {
        super(manager, gson, jsonBuilder);
    }

    @Override
    protected void handleOptions(HttpExchange exchange, RequestSegments segments) throws IOException {
        if (segments.id() == 0) {
            exchange.getResponseHeaders().add("Allow", "GET, POST, DELETE, OPTIONS, HEAD");
        } else {
            exchange.getResponseHeaders().add("Allow", "GET, DELETE, OPTIONS, HEAD");
        }
        exchange.sendResponseHeaders(204, -1);
    }


    @Override
    protected void handleGet(HttpExchange exchange, RequestSegments segments) throws IOException {

        if (segments.id() == 0) {
            sendText(exchange, gson.toJson(manager.getTasks()), 200);
        } else {
            Task task = manager.getTask(segments.id());
            if (task != null) {
                sendText(exchange, gson.toJson(task), 200);
            } else {
                sendNotFound(exchange, jsonBuilder.notFound("Task with id '" + segments.id() + "'not found"));
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
            Task task = gson.fromJson(body, Task.class);
            isTaskValid(task);

            if (task.getTaskId() == 0) {
                manager.addTask(task);
            } else {
                manager.updateTask(task);
            }
            sendText(exchange, gson.toJson(manager.getTaskWithoutHistory(task.getTaskId())), 201);
        } catch (JsonSyntaxException e) {
            System.out.println(e);
            sendText(exchange,
                    jsonBuilder.badRequest(
                            "Json must contain: taskId, title, description and status[NEW, IN_PROGRESS, DONE]."),
                    400);
        } catch (TaskNotFound e) {
            sendNotFound(exchange, jsonBuilder.notFound(e.getMessage()));
        } catch (TaskTimeOverlapException e) {
            sendHasOverlaps(exchange, jsonBuilder.hasOverlaps());
        }
    }

    @Override
    protected void handleDelete(HttpExchange exchange, RequestSegments segments) throws IOException {
        if (segments.id() == 0) {
            manager.clearTasks();
            sendText(exchange, jsonBuilder.message("Tasks were deleted"), 200);
        } else {
            manager.deleteTask(segments.id());
            sendText(exchange, jsonBuilder.message("Task: '" + segments.id() + "' was deleted"), 200);
        }
    }

    @Override
    protected boolean validateResources(HttpExchange exchange, RequestSegments segments) throws IOException {
        if (!"tasks".equals(segments.resource())) {
            sendNotFound(exchange, jsonBuilder.resourceNotFound());
            return true;
        }
        return false;
    }

    protected void isTaskValid(Task task) {
        if (task.getTitle() == null || task.getDescription() == null || task.getStatus() == null) {
            throw new JsonSyntaxException("Task has invalid fields");
        }
    }
}
