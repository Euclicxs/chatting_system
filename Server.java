import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Server {
    private static final int PORT = 8080;
    private static Set<ClientHandler> clientHandlers = new HashSet<>();
    private static List<String> messageHistory = new ArrayList<>();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void main(String[] args) {
        System.out.println("Chat Server starting on port " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started successfully at " + LocalDateTime.now().format(formatter));
            System.out.println("Waiting for client connections...");
            
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connection from: " + socket.getInetAddress() + " at " + LocalDateTime.now().format(formatter));
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String name;
        private String sessionId;
        private LocalDateTime sessionStart;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.sessionId = "SESSION-" + System.currentTimeMillis();
            this.sessionStart = LocalDateTime.now();
        }

        @Override
        public void run() {
            System.out.println("Session " + sessionId + " started for client: " + socket.getInetAddress());
            
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("Welcome to the Chat Server!");
                out.println("Enter your name:");
                name = in.readLine();
                
                if (name == null || name.trim().isEmpty()) {
                    name = "Anonymous";
                }
                
                System.out.println("Client '" + name + "' joined (Session: " + sessionId + ")");
                
                // Send message history to new client
                if (!messageHistory.isEmpty()) {
                    out.println("--- Previous Messages ---");
                    for (String historyMessage : messageHistory) {
                        out.println(historyMessage);
                    }
                    out.println("--- End of History ---");
                }
                
                String joinMessage = "[" + LocalDateTime.now().format(formatter) + "] " + name + " joined the chat";
                messageHistory.add(joinMessage);
                broadcast(joinMessage, this);

                String message;
                while ((message = in.readLine()) != null) {
                    message = message.trim();
                    
                    if (message.equalsIgnoreCase("exit") || message.equalsIgnoreCase("quit")) {
                        System.out.println("Client '" + name + "' requested to exit (Session: " + sessionId + ")");
                        break;
                    }
                    
                    if (message.isEmpty()) continue;
                    
                    String fullMessage = "[" + LocalDateTime.now().format(formatter) + "] " + name + ": " + message;
                    messageHistory.add(fullMessage);
                    broadcast(fullMessage, this);
                    
                    System.out.println("Message from '" + name + "': " + message);
                }
            } catch (IOException e) {
                System.err.println("Session " + sessionId + " error for client '" + name + "': " + e.getMessage());
            } finally {
                // Session cleanup
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing socket for session " + sessionId + ": " + e.getMessage());
                }
                
                clientHandlers.remove(this);
                String leaveMessage = "[" + LocalDateTime.now().format(formatter) + "] " + name + " left the chat";
                messageHistory.add(leaveMessage);
                broadcast(leaveMessage, this);
                
                LocalDateTime sessionEnd = LocalDateTime.now();
                System.out.println("Session " + sessionId + " ended for client '" + name + "' (Duration: " + 
                    java.time.Duration.between(sessionStart, sessionEnd).getSeconds() + " seconds)");
                
                if (clientHandlers.isEmpty()) {
                    messageHistory.clear();
                    System.out.println("All clients disconnected. Message history cleared.");
                }
            }
        }

        private void broadcast(String message, ClientHandler excludeClient) {
            for (ClientHandler handler : clientHandlers) {
                if (handler != excludeClient) {
                    handler.out.println(message);
                }
            }
        }
        
        private void broadcast(String message) {
            broadcast(message, null);
        }
    }
}