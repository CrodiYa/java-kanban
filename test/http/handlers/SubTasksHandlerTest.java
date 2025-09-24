package http.handlers;

import http.HttpTaskServerTest;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import util.enums.Status;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SubTasksHandlerTest extends HttpTaskServerTest {

    @BeforeEach
    public void addEpic() {
        manager.addEpic(epic);
    }

    @Nested
    class SubTaskGetTest {

        private HttpRequest getRequest(String value) {
            return getMethod("GET", value);
        }

        @Test
        public void shouldReturn404WhenInvalidResource() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/subtasksABC"),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(404, response.statusCode());
        }

        @Test
        public void shouldBeEqualAndCode200() throws IOException, InterruptedException {
            manager.addSubTask(subTask);

            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/subtasks/2"),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            assertEquals(subTask, gson.fromJson(response.body(), SubTask.class));
        }

        @Test
        public void shouldSameAmountAndCode200() throws IOException, InterruptedException {
            manager.addSubTask(subTask);
            manager.addSubTask(new SubTask("subtask2", "demo", Status.NEW, 1));

            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/subtasks"),
                    HttpResponse.BodyHandlers.ofString());

            List<Task> list = gson.fromJson(response.body(), new ListSubTaskTypeToken().getType());

            assertEquals(200, response.statusCode());
            assertEquals(2, list.size());
        }

        @Test
        public void shouldReturn404WhenNoId() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/subtasks/2"),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(404, response.statusCode());
        }

        @Test
        public void shouldReturn404WhenSubResourcePresent() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/subtasks/2/sub"),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(404, response.statusCode());
        }

        @Test
        public void shouldReturn404WhenIdInvalid() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/subtasks/-2a"),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(400, response.statusCode());
        }

    }

    @Nested
    class SubTaskPostTest {

        @Test
        public void shouldBeEqualAndCode201() throws IOException, InterruptedException {
            String taskJson = gson.toJson(subTask);

            HttpResponse<String> response = client.send(
                    getPost("http://localhost:8080/subtasks", taskJson),
                    HttpResponse.BodyHandlers.ofString());

            subTask.setTaskId(2);

            assertEquals(201, response.statusCode());
            assertEquals(subTask, manager.getSubTask(2));
        }

        @Test
        public void shouldBeUpdatedAndCode201() throws IOException, InterruptedException {
            manager.addSubTask(subTask);

            subTask.setTitle("new title");
            String taskJson = gson.toJson(subTask);

            HttpResponse<String> response = client.send(
                    getPost("http://localhost:8080/subtasks", taskJson),
                    HttpResponse.BodyHandlers.ofString());


            assertEquals(201, response.statusCode());
            assertEquals(subTask, manager.getSubTask(2));
        }

        @Test
        public void shouldNotBeUpdatedAndCode404WhenNotFound() throws IOException, InterruptedException {

            subTask.setTaskId(2);
            String taskJson = gson.toJson(subTask);

            HttpResponse<String> response = client.send(
                    getPost("http://localhost:8080/subtasks", taskJson),
                    HttpResponse.BodyHandlers.ofString());


            assertEquals(404, response.statusCode());
        }

        @Test
        public void shouldReturn405AndNotPostWhenIdIsPresent() throws IOException, InterruptedException {
            String taskJson = gson.toJson(subTask);

            HttpResponse<String> response = client.send(
                    getPost("http://localhost:8080/subtasks/2", taskJson),
                    HttpResponse.BodyHandlers.ofString()
            );
            subTask.setTaskId(2);

            assertEquals(405, response.statusCode());
            assertNull(manager.getSubTask(2));
        }

        @Test
        public void shouldReturn400WhenNoBody() throws IOException, InterruptedException {

            HttpResponse<String> response = client.send(
                    getPost("http://localhost:8080/subtasks", ""),
                    HttpResponse.BodyHandlers.ofString()
            );
            subTask.setTaskId(2);

            assertEquals(400, response.statusCode());
            assertNull(manager.getSubTask(2));
        }

        @Test
        public void shouldReturn400WhenInvalidBody() throws IOException, InterruptedException {
            String taskJson = "{blablabla:blabla}";

            HttpResponse<String> response = client.send(
                    getPost("http://localhost:8080/subtasks", taskJson),
                    HttpResponse.BodyHandlers.ofString()
            );
            subTask.setTaskId(2);

            assertEquals(400, response.statusCode());
            assertNull(manager.getSubTask(2));
        }

        @Test
        public void shouldReturn406WhenTimeOverlap() throws IOException, InterruptedException {

            manager.addSubTask(new SubTask("subtask1",
                    "demo",
                    Status.NEW,
                    1,
                    10,
                    epochTime));

            String taskJson = gson.toJson(new SubTask("subtask2",
                    "demo",
                    Status.NEW,
                    1,
                    5,
                    epochTime));

            HttpResponse<String> response = client.send(
                    getPost("http://localhost:8080/subtasks", taskJson),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(406, response.statusCode());
        }
    }

    @Nested
    class SubTaskDeleteTest {

        private HttpRequest getRequest(String value) {
            return getMethod("DELETE", value);
        }

        @Test
        public void shouldReturn200AndDeleteAllTasks() throws IOException, InterruptedException {
            manager.addSubTask(subTask);
            manager.addSubTask(new SubTask("subtask2", "demo", Status.NEW, 1));

            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/subtasks"),
                    HttpResponse.BodyHandlers.ofString()
            );

            assertEquals(200, response.statusCode());
            assertEquals(0, manager.getSubTasks().size());
        }

        @Test
        public void shouldReturn200AndDeleteTask() throws IOException, InterruptedException {
            manager.addSubTask(subTask);
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/subtasks/2"),
                    HttpResponse.BodyHandlers.ofString()
            );

            assertEquals(200, response.statusCode());
            assertNull(manager.getSubTask(2));
        }
    }

}