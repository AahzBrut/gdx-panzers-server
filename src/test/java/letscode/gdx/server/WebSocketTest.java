package letscode.gdx.server;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.util.stream.IntStream;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@Slf4j
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class WebSocketTest {

    @LocalServerPort
    private int port;

    @Test
    @SneakyThrows
    void test() {
        IntStream
            .range(0, 100)
            .mapToObj(i -> new Thread(this::runWebSocketClient))
            .forEach(Thread::start);

        Thread.sleep(1000);
    }

    @SneakyThrows
    private void runWebSocketClient(){
        final var webSocketClient = new StandardWebSocketClient();
        final var baseUrl = "ws://localhost:" + port + "/ws";
        webSocketClient.doHandshake(new TestSessionHandler(), baseUrl);
    }

    static class TestSessionHandler implements WebSocketHandler {

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            log.info("Connected");
        }

        @Override
        public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
            log.info("Received message: {}", message.getPayload());

            session.close();
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
            log.error("Error", exception);
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
            log.info("Disconnected");
        }

        @Override
        public boolean supportsPartialMessages() {
            return false;
        }
    }
}
