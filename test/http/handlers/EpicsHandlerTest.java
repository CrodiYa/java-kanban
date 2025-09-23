package http.handlers;

import http.HttpTaskServerTest;
import model.Epic;
import model.SubTask;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import util.enums.Status;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EpicsHandlerTest extends HttpTaskServerTest {

    @Nested
    class EpicHeadTest {
        private HttpRequest getRequest(String value) {
            URI url = URI.create(value);

            return HttpRequest
                    .newBuilder()
                    .HEAD()
                    .uri(url)
                    .build();
        }

        @Test
        public void shouldReturn200AndRightHeadersWhenHeadWithEmptyEpics() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/epics"),
                    HttpResponse.BodyHandlers.ofString());

            System.out.println(response.headers().firstValue("Content-type"));
            assertEquals(200, response.statusCode());

            assertEquals("application/json;charset=utf-8",
                    response.headers().firstValue("Content-type").get());
            assertEquals("2",
                    response.headers().firstValue("Content-length").get());
        }

        @Test
        public void shouldReturn404AndRightHeadersWhenHeadWithInvalidEpic() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/epics/1"),
                    HttpResponse.BodyHandlers.ofString());

            System.out.println(response.headers().firstValue("Content-type"));
            assertEquals(404, response.statusCode());

            assertEquals("application/json;charset=utf-8",
                    response.headers().firstValue("Content-type").get());
            assertEquals("69",
                    response.headers().firstValue("Content-length").get());
        }

        @Test
        public void shouldReturn200AndRightHeadersWhenHeadWithSubResource() throws IOException, InterruptedException {
            manager.addEpic(epic);
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/epics/1/subtasks"),
                    HttpResponse.BodyHandlers.ofString());

            System.out.println(response.headers().firstValue("Content-type"));
            assertEquals(200, response.statusCode());

            assertEquals("application/json;charset=utf-8",
                    response.headers().firstValue("Content-type").get());
            assertEquals("2",
                    response.headers().firstValue("Content-length").get());
        }

        @Test
        public void shouldReturn400AndRightHeadersWhenHeadWithInvalidId() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/epics/-1"),
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
    class EpicGetTest {

        private HttpRequest getRequest(String value) {
            return getMethod("GET", value);
        }

        @Test
        public void shouldReturn404WhenInvalidResource() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/epicsABC"),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(404, response.statusCode());
        }

        @Test
        public void shouldBeEqualAndCode200() throws IOException, InterruptedException {
            manager.addEpic(epic);

            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/epics/1"),
                    HttpResponse.BodyHandlers.ofString());

            System.out.println(response.body());
            assertEquals(200, response.statusCode());
            assertEquals(epic, gson.fromJson(response.body(), Epic.class));
        }

        @Test
        public void shouldSameAmountAndCode200() throws IOException, InterruptedException {
            manager.addEpic(epic);
            manager.addEpic(new Epic("epic2", "demo", Status.NEW));

            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/epics"),
                    HttpResponse.BodyHandlers.ofString());

            List<Epic> list = gson.fromJson(response.body(), new ListEpicTypeToken().getType());

            assertEquals(200, response.statusCode());
            assertEquals(2, list.size());
        }

        @Test
        public void shouldReturn404WhenNoId() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/epics/1"),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(404, response.statusCode());
        }

        @Test
        public void shouldReturn404WhenSubResourcePresent() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/epics/1/sub"),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(404, response.statusCode());
        }

        @Test
        public void shouldReturn404WhenIdInvalid() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/epics/-2a"),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(400, response.statusCode());
        }

        @Test
        public void shouldReturn200AndSubtasks() throws IOException, InterruptedException {
            manager.addEpic(epic);
            manager.addSubTask(subTask);

            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/epics/1/subtasks"),
                    HttpResponse.BodyHandlers.ofString());

            List<SubTask> list = gson.fromJson(response.body(), new ListSubTaskTypeToken().getType());

            assertEquals(200, response.statusCode());
            assertEquals(1, list.size());
            assertEquals(subTask, list.getFirst());

        }

        @Test
        public void shouldReturn404WhenNoIdAndSubTasks() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/epics/1/subtasks"),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(404, response.statusCode());
        }
    }

    @Nested
    class EpicPostTest {

        @Test
        public void shouldBeEqualAndCode201() throws IOException, InterruptedException {
            String taskJson = gson.toJson(epic);

            HttpResponse<String> response = client.send(
                    getPost("http://localhost:8080/epics", taskJson),
                    HttpResponse.BodyHandlers.ofString());

            epic.setTaskId(1);

            assertEquals(201, response.statusCode());
            assertEquals(epic, manager.getEpic(1));
        }

        @Test
        public void shouldBeUpdatedAndCode201() throws IOException, InterruptedException {
            manager.addEpic(epic);

            epic.setTitle("new title");
            String taskJson = gson.toJson(epic);

            HttpResponse<String> response = client.send(
                    getPost("http://localhost:8080/epics", taskJson),
                    HttpResponse.BodyHandlers.ofString());


            assertEquals(201, response.statusCode());
            assertEquals(epic, manager.getEpic(1));
        }

        @Test
        public void shouldNotBeUpdatedAndCode404WhenNotFound() throws IOException, InterruptedException {

            epic.setTaskId(1);
            String taskJson = gson.toJson(epic);

            HttpResponse<String> response = client.send(
                    getPost("http://localhost:8080/epics", taskJson),
                    HttpResponse.BodyHandlers.ofString());

            System.out.println(response.body());
            assertEquals(404, response.statusCode());
        }

        @Test
        public void shouldReturn405AndNotPostWhenIdAndSubIsPresent() throws IOException, InterruptedException {
            String taskJson = gson.toJson(epic);

            HttpResponse<String> response = client.send(
                    getPost("http://localhost:8080/epics/1/subtasks", taskJson),
                    HttpResponse.BodyHandlers.ofString()
            );
            epic.setTaskId(1);

            assertEquals(405, response.statusCode());
            assertNull(manager.getEpic(1));
        }

        @Test
        public void shouldReturn400WhenNoBody() throws IOException, InterruptedException {

            HttpResponse<String> response = client.send(
                    getPost("http://localhost:8080/epics", ""),
                    HttpResponse.BodyHandlers.ofString()
            );
            epic.setTaskId(1);

            assertEquals(400, response.statusCode());
            assertNull(manager.getEpic(1));
        }

        @Test
        public void shouldReturn400WhenInvalidBody() throws IOException, InterruptedException {
            String taskJson = "{blablabla:blabla}";

            HttpResponse<String> response = client.send(
                    getPost("http://localhost:8080/epics", taskJson),
                    HttpResponse.BodyHandlers.ofString()
            );
            epic.setTaskId(1);

            assertEquals(400, response.statusCode());
            assertNull(manager.getEpic(1));
        }

    }

    @Nested
    class EpicDeleteTest {
        private HttpRequest getRequest(String value) {
            return getMethod("DELETE", value);
        }

        @Test
        public void shouldReturn404WhenIdIsNotPresentAndDeletingSubtasks() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/epics/1/subtasks"),
                    HttpResponse.BodyHandlers.ofString()
            );

            assertEquals(404, response.statusCode());
        }

        @Test
        public void shouldReturn200AndDeleteAllEpics() throws IOException, InterruptedException {
            manager.addEpic(epic);
            manager.addEpic(new Epic("epic2", "demo", Status.NEW));

            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/epics"),
                    HttpResponse.BodyHandlers.ofString()
            );

            assertEquals(200, response.statusCode());
            assertEquals(0, manager.getEpics().size());
        }

        @Test
        public void shouldReturn200AndDeleteEpic() throws IOException, InterruptedException {
            manager.addEpic(epic);
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/epics/1"),
                    HttpResponse.BodyHandlers.ofString()
            );

            assertEquals(200, response.statusCode());
            assertNull(manager.getEpic(1));
        }

        @Test
        public void shouldReturn200AndDeleteSubtasksFromEpic() throws IOException, InterruptedException {
            manager.addEpic(epic);
            manager.addSubTask(subTask);

            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/epics/1/subtasks"),
                    HttpResponse.BodyHandlers.ofString()
            );

            assertEquals(200, response.statusCode());
            assertEquals(0, manager.getSubTasksFromEpic(1).size());
        }


    }

    @Nested
    class EpicOptionsTest {

        private HttpRequest getRequest(String value) {
            return getMethod("OPTIONS", value);
        }

        @Test
        public void shouldReturn204AndValidMethods() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/epics"),
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


        @Test
        public void shouldReturn204AndValidMethodsWhenIdAndSubTasksIsPresent() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/epics/1/subtasks"),
                    HttpResponse.BodyHandlers.ofString()
            );
            List<String> expected = List.of("GET, DELETE, OPTIONS, HEAD");

            List<String> methods = response.headers().allValues("Allow");

            assertEquals(204, response.statusCode());
            assertEquals(expected, methods);
        }
    }
}