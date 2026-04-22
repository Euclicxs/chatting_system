# Simple Chat System

This is a basic client-server chat application written in Java. The server listens on port 8080 and broadcasts messages to all connected clients.

## Files
- `Server.java`: The server application
- `Client.java`: The client application

## How to Run

1. **Compile the code:**
   ```
   javac Server.java Client.java
   ```

2. **Start the server:**
   ```
   java Server
   ```
   The server will start listening on port 12345.

3. **Start clients:**
   Open multiple terminal windows and run:
   ```
   java Client
   ```
   Each client will connect to the server.

4. **Chat:**
   - Enter your name when prompted.
   - Type messages and press Enter to send.
   - Type "exit" to disconnect.

## Features
- Multiple clients can connect simultaneously.
- Messages are broadcasted to all connected clients.
- Sessions remain open until "exit" is sent.
- Simple console-based interface.

## Notes
- Server and clients must be on the same network or localhost.
- Port 8080 is hardcoded; change in the code if needed.