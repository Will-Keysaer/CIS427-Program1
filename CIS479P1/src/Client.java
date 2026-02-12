import java.io.*;
import java.net.*;

public class Client {

    public static final int SERVER_PORT = 4080;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Client <server_ip>");
            System.exit(1);
        }

        String host = args[0];

        try (
                Socket socket = new Socket(host, SERVER_PORT);
                BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String line;

            while ((line = userInput.readLine()) != null) {
                // Check for quit command before sending
                if (line.equalsIgnoreCase("QUIT")) {
                    out.println(line); // tell server we're quitting
                    System.out.println("You have disconnected from the server.");
                    break; // exit input loop
                }

                out.println(line); // send command to server

                // Read multi-line response from server
                String response;
                while ((response = in.readLine()) != null) {
                    System.out.println(response);

                    // If server closes socket (after SHUTDOWN), in.readLine() will return null
                    if (!in.ready()) break;
                }

                // Check if server closed the connection
                if (!socket.isConnected() || socket.isClosed()) {
                    System.out.println("Server has shut down. Connection closed.");
                    break;
                }
            }

        } catch (IOException e) {
            System.out.println("Connection to server lost.");
        }
    }
}
