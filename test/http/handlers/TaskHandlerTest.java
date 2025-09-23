package http.handlers;

import http.HttpTaskServerTest;
import model.Task;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import util.enums.Status;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


class TaskHandlerTest extends HttpTaskServerTest {

    @Nested
    class TaskHeadTest {

        private HttpRequest getRequest(String value) {
            return getMethod("HEAD", value);
        }

        @Test
        public void shouldReturn200AndRightHeadersWhenHeadWithEmptyTasks() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/tasks"),
                    HttpResponse.BodyHandlers.ofString());

            System.out.println(response.headers().firstValue("Content-type"));
            assertEquals(200, response.statusCode());

            assertEquals("application/json;charset=utf-8",
                    response.headers().firstValue("Content-type").get());
            assertEquals("2",
                    response.headers().firstValue("Content-length").get());
        }

        @Test
        public void shouldReturn404AndRightHeadersWhenHeadWithInvalidTask() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/tasks/1"),
                    HttpResponse.BodyHandlers.ofString());

            System.out.println(response.headers().firstValue("Content-type"));
            assertEquals(404, response.statusCode());

            assertEquals("application/json;charset=utf-8",
                    response.headers().firstValue("Content-type").get());
            assertEquals("69",
                    response.headers().firstValue("Content-length").get());
        }

        @Test
        public void shouldReturn404AndRightHeadersWhenHeadWithInvalidSubResource() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/tasks/1/sub"),
                    HttpResponse.BodyHandlers.ofString());

            System.out.println(response.headers().firstValue("Content-type"));
            assertEquals(404, response.statusCode());

            assertEquals("application/json;charset=utf-8",
                    response.headers().firstValue("Content-type").get());
            assertEquals("76",
                    response.headers().firstValue("Content-length").get());
        }

        @Test
        public void shouldReturn400AndRightHeadersWhenHeadWithInvalidId() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/tasks/-1"),
                    HttpResponse.BodyHandlers.ofString());

            System.out.println(response.headers().firstValue("Content-type"));
            assertEquals(400, response.statusCode());

            assertEquals("application/json;charset=utf-8",
                    response.headers().firstValue("Content-type").get());
            assertEquals("65",
                    response.headers().firstValue("Content-length").get());
        }
    }

    @Nested
    class TaskGetTest {

        private HttpRequest getRequest(String value) {
            return getMethod("GET", value);
        }

        @Test
        public void shouldReturn404WhenInvalidResource() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/tasksABC"),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(404, response.statusCode());
        }

        @Test
        public void shouldBeEqualAndCode200() throws IOException, InterruptedException {
            manager.addTask(task);

            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/tasks/1"),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            assertEquals(task, gson.fromJson(response.body(), Task.class));
        }

        @Test
        public void shouldSameAmountAndCode200() throws IOException, InterruptedException {
            manager.addTask(task);
            manager.addTask(new Task("task2", "demo", Status.NEW));

            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/tasks"),
                    HttpResponse.BodyHandlers.ofString());

            List<Task> list = gson.fromJson(response.body(), new ListSubTaskTypeToken().getType());

            assertEquals(200, response.statusCode());
            assertEquals(2, list.size());
        }

        @Test
        public void shouldReturn404WhenNoId() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/tasks/1"),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(404, response.statusCode());
        }

        @Test
        public void shouldReturn404WhenSubResourcePresent() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/tasks/1/sub"),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(404, response.statusCode());
        }

        @Test
        public void shouldReturn404WhenIdInvalid() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/tasks/-2a"),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(400, response.statusCode());
        }

    }

    @Nested
    class TaskPostTest {

        @Test
        public void shouldBeEqualAndCode201() throws IOException, InterruptedException {
            String taskJson = gson.toJson(task);

            HttpResponse<String> response = client.send(
                    getPost("http://localhost:8080/tasks", taskJson),
                    HttpResponse.BodyHandlers.ofString());

            task.setTaskId(1);

            assertEquals(201, response.statusCode());
            assertEquals(task, manager.getTask(1));
        }

        @Test
        public void shouldBeUpdatedAndCode201() throws IOException, InterruptedException {
            manager.addTask(task);

            task.setTitle("new title");
            String taskJson = gson.toJson(task);

            HttpResponse<String> response = client.send(
                    getPost("http://localhost:8080/tasks", taskJson),
                    HttpResponse.BodyHandlers.ofString());


            assertEquals(201, response.statusCode());
            assertEquals(task, manager.getTask(1));
        }

        @Test
        public void shouldNotBeUpdatedAndCode404WhenNotFound() throws IOException, InterruptedException {

            task.setTaskId(1);
            String taskJson = gson.toJson(task);

            HttpResponse<String> response = client.send(
                    getPost("http://localhost:8080/tasks", taskJson),
                    HttpResponse.BodyHandlers.ofString());


            assertEquals(404, response.statusCode());
        }

        @Test
        public void shouldReturn405AndNotPostWhenIdIsPresent() throws IOException, InterruptedException {
            String taskJson = gson.toJson(task);

            HttpResponse<String> response = client.send(
                    getPost("http://localhost:8080/tasks/1", taskJson),
                    HttpResponse.BodyHandlers.ofString()
            );
            task.setTaskId(1);

            assertEquals(405, response.statusCode());
            assertNull(manager.getTask(1));
        }

        @Test
        public void shouldReturn400WhenNoBody() throws IOException, InterruptedException {

            HttpResponse<String> response = client.send(
                    getPost("http://localhost:8080/tasks", ""),
                    HttpResponse.BodyHandlers.ofString()
            );
            task.setTaskId(1);

            assertEquals(400, response.statusCode());
            assertNull(manager.getTask(1));
        }

        @Test
        public void shouldReturn400WhenInvalidBody() throws IOException, InterruptedException {
            String taskJson = "{blablabla:blabla}";

            HttpResponse<String> response = client.send(
                    getPost("http://localhost:8080/tasks", taskJson),
                    HttpResponse.BodyHandlers.ofString()
            );
            task.setTaskId(1);

            assertEquals(400, response.statusCode());
            assertNull(manager.getTask(1));
        }

        @Test
        public void shouldReturn406WhenTimeOverlap() throws IOException, InterruptedException {
            manager.addTask(new Task("task1", "demo", Status.NEW, 10, epochTime));

            String taskJson = gson.toJson(new Task("task2", "demo", Status.NEW, 5, epochTime));

            HttpResponse<String> response = client.send(
                    getPost("http://localhost:8080/tasks", taskJson),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(406, response.statusCode());
        }
    }

    @Nested
    class TaskDeleteTest {

        private HttpRequest getRequest(String value) {
            return getMethod("DELETE", value);
        }

        @Test
        public void shouldReturn200AndDeleteAllTasks() throws IOException, InterruptedException {
            manager.addTask(task);
            manager.addTask(new Task("task2", "demo", Status.NEW));

            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/tasks"),
                    HttpResponse.BodyHandlers.ofString()
            );

            assertEquals(200, response.statusCode());
            assertEquals(0, manager.getTasks().size());
        }

        @Test
        public void shouldReturn200AndDeleteTask() throws IOException, InterruptedException {
            manager.addTask(task);
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/tasks/1"),
                    HttpResponse.BodyHandlers.ofString()
            );

            assertEquals(200, response.statusCode());
            assertNull(manager.getTask(1));
        }
    }

    @Nested
    class TaskOptionsTest {

        private HttpRequest getRequest(String value) {
            return getMethod("OPTIONS", value);
        }

        @Test
        public void shouldReturn204AndValidMethods() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/tasks"),
                    HttpResponse.BodyHandlers.ofString()
            );
            List<String> expected = List.of("GET, POST, DELETE, OPTIONS, HEAD");

            List<String> methods = response.headers().allValues("Allow");

            assertEquals(204, response.statusCode());
            assertEquals(expected, methods);
        }

        @Test
        public void shouldReturn204AndValidMethodsWhenIdIsPresent() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/tasks/1"),
                    HttpResponse.BodyHandlers.ofString()
            );
            List<String> expected = List.of("GET, DELETE, OPTIONS, HEAD");

            List<String> methods = response.headers().allValues("Allow");

            assertEquals(204, response.statusCode());
            assertEquals(expected, methods);
        }

    }
}