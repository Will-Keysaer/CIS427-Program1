import java.io.*;
import java.net.*;

public class Client {

    public static final int SERVER_PORT = 5432;

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
                out.println(line); // send command to server

                // Read multi-line response from server
                String response;
                while ((response = in.readLine()) != null) {
                    System.out.println(response);
                    if (!in.ready()) break; // stop if no more lines
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
