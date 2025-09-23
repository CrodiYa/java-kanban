package http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import managers.TaskManager;
import model.SubTask;
import model.Task;
import util.exceptions.TaskNotFound;
import util.exceptions.TaskTimeOverlapException;
import util.http.JsonBuilder;
import util.http.RequestSegments;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SubTasksHandler extends TaskHandler {

    public SubTasksHandler(TaskManager manager, Gson gson, JsonBuilder jsonBuilder) {
        super(manager, gson, jsonBuilder);
    }

    @Override
    protected void handleGet(HttpExchange exchange, RequestSegments segments) throws IOException {
        if (segments.id() == 0) {
            sendText(exchange, gson.toJson(manager.getSubTasks()), 200);
        } else {
            SubTask subtask = manager.getSubTask(segments.id());
            if (subtask != null) {
                sendText(exchange, gson.toJson(subtask), 200);
            } else {
                sendNotFound(exchange, jsonBuilder.notFound("SubTask with id '" + segments.id() + "'not found"));
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
            SubTask subTask = gson.fromJson(body, SubTask.class);
            isTaskValid(subTask);

            if (subTask.getTaskId() == 0) {
                manager.addSubTask(subTask);
            } else {
                manager.updateSubTask(subTask);
            }
            sendText(exchange, gson.toJson(manager.getSubTaskWithoutHistory(subTask.getTaskId())), 201);
        } catch (JsonSyntaxException e) {
            sendText(exchange,
                    jsonBuilder.badRequest(
                            "Json must contain: taskId, title, description, status," +
                                    " status[NEW, IN_PROGRESS, DONE], epicId."),
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
            manager.clearSubTasks();
            sendText(exchange, jsonBuilder.message("Subtasks were deleted"), 200);
        } else {
            manager.deleteSubTask(segments.id());
            sendText(exchange, jsonBuilder.message("Subtask: '" + segments.id() + "' was deleted"), 200);
        }
    }

    @Override
    protected boolean validateResources(HttpExchange exchange, RequestSegments segments) throws IOException {
        if (!"subtasks".equals(segments.resource())) {
            sendNotFound(exchange, jsonBuilder.resourceNotFound());
            return true;
        }
        return false;
    }

    @Override
    protected void isTaskValid(Task task) {
        SubTask subTask = (SubTask) task;
        if (subTask.getTitle() == null ||
                subTask.getDescription() == null ||
                subTask.getStatus() == null ||
                subTask.getEpicId() == 0) {
            throw new JsonSyntaxException("Subtask has invalid fields");
        }
    }

}
