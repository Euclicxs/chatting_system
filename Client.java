import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void main(String[] args) {
        System.out.println("Chat Client starting...");
        System.out.println("Connecting to server at " + HOST + ":" + PORT);
        
        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connected to server successfully at " + LocalDateTime.now().format(formatter));
            System.out.println("Chat session started. Type 'exit' or 'quit' to end session.");

            // Read welcome message
            String welcome = in.readLine();
            if (welcome != null) {
                System.out.println(welcome);
            }

            // Read name prompt and send name
            String prompt = in.readLine();
            if (prompt != null) {
                System.out.print(prompt + " ");
            }
            String name = console.readLine();
            if (name == null || name.trim().isEmpty()) {
                name = "Anonymous";
            }
            out.println(name);
            System.out.println("Joined chat as: " + name);

            // Start a thread to read messages from server (maintains session)
            Thread readerThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    System.err.println("Connection lost: " + e.getMessage());
                }
            }, "MessageReader-" + name);
            readerThread.start();

            // Send messages (session data handling)
            String message;
            System.out.println("You can now send messages. Type 'exit' or 'quit' to leave:");
            while ((message = console.readLine()) != null) {
                message = message.trim();
                
                if (message.equalsIgnoreCase("exit") || message.equalsIgnoreCase("quit")) {
                    System.out.println("Ending chat session...");
                    out.println(message);
                    break;
                }
                
                if (!message.isEmpty()) {
                    out.println(message);
                }
            }
            
            System.out.println("Chat session ended at " + LocalDateTime.now().format(formatter));
            
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}