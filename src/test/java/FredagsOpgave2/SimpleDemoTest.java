package FredagsOpgave2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleDemoTest {
    SimpleDemo server = new SimpleDemo();

    @BeforeEach
    void setUp() {
        new Thread(()->server.run(9999)).start();
    }

    @AfterEach
    void tearDown() {

    }

    @Test
    @DisplayName("Testing multi client server")
    public void testServer(){
        SimpleClient client = new SimpleClient();
        client.startConnection("localhost",9999);
        String actual = client.sendMessage("Hello from client 1");
        String expected = "We received this message: Hello from client 1";
        assertEquals(expected,actual);

        client.sendMessage("Over and Out");
        client.stopConnection();
    }
}