package http.handlers;

import http.HttpTaskServerTest;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HistoryHandlerTest extends HttpTaskServerTest {

    @BeforeEach
    public void addTasks() {
        manager.addEpic(epic);
        manager.addTask(task);
        manager.addSubTask(subTask);
    }

    @Nested
    class HistoryHeadTest {

        private HttpRequest getRequest(String value) {
            return getMethod("HEAD", value);
        }

        @Test
        public void shouldReturn200AndRightHeadersWhenHeadWithEmptyTasks() throws IOException, InterruptedException {

            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/history"),
                    HttpResponse.BodyHandlers.ofString());

            System.out.println(response.headers().firstValue("Content-type"));
            assertEquals(200, response.statusCode());

            assertEquals("application/json;charset=utf-8",
                    response.headers().firstValue("Content-type").get());
            assertEquals("2",
                    response.headers().firstValue("Content-length").get());
        }

        @Test
        public void shouldReturn404AndRightHeadersWhenHeadWithInvalidId() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/history/1"),
                    HttpResponse.BodyHandlers.ofString());

            System.out.println(response.headers().firstValue("Content-type"));
            assertEquals(404, response.statusCode());

            assertEquals("application/json;charset=utf-8",
                    response.headers().firstValue("Content-type").get());
            assertEquals("73",
                    response.headers().firstValue("Content-length").get());
        }

        @Test
        public void shouldReturn404AndRightHeadersWhenHeadWithInvalidSubResource() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/history/1/sub"),
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
                    getRequest("http://localhost:8080/history/-1"),
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
    class HistoryGetTest {

        private HttpRequest getRequest(String value) {
            return getMethod("GET", value);
        }

        @Test
        public void shouldBeEqualAndCode200() throws IOException, InterruptedException {

            manager.getEpic(1);
            manager.getTask(2);
            manager.getSubTask(3);

            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/history"),
                    HttpResponse.BodyHandlers.ofString());


            List<Task> list = gson.fromJson(response.body(), new ListTaskTypeToken().getType());
            assertEquals(200, response.statusCode());
            assertEquals(3, list.size());
        }

        @Test
        public void shouldReturn404WithInvalidId() throws IOException, InterruptedException {

            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/history/1"),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(404, response.statusCode());
        }

        @Test
        public void shouldReturn404WithInvalidIdAndSub() throws IOException, InterruptedException {

            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/history/1/sub"),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(404, response.statusCode());
        }

        @Test
        public void shouldReturn404WithInvalidResource() throws IOException, InterruptedException {

            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/historyABC"),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(404, response.statusCode());
        }

    }

    @Nested
    class HistoryPostTest {

        @Test
        public void shouldReturn405() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getPost("http://localhost:8080/history", ""),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(405, response.statusCode());
        }
    }

    @Nested
    class HistoryDeleteTest {

        private HttpRequest getRequest(String value) {
            return getMethod("DELETE", value);
        }

        @Test
        public void shouldReturn405() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/history"),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(405, response.statusCode());
        }
    }

    @Nested
    class SubTaskOptionsTest {

        private HttpRequest getRequest(String value) {
            return getMethod("OPTIONS", value);
        }

        @Test
        public void shouldReturn204AndValidMethods() throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/history"),
                    HttpResponse.BodyHandlers.ofString()
            );
            List<String> expected = List.of("GET, OPTIONS, HEAD");

            List<String> methods = response.headers().allValues("Allow");

            assertEquals(204, response.statusCode());
            assertEquals(expected, methods);
        }
    }

}