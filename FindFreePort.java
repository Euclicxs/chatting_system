import java.io.IOException;
import java.net.ServerSocket;

public class FindFreePort {
    public static void main(String[] args) {
        try (ServerSocket socket = new ServerSocket(0)) {
            int port = socket.getLocalPort();
            System.out.println("A free port is: " + port);
        } catch (IOException e) {
            System.err.println("Error finding free port: " + e.getMessage());
        }
    }
}