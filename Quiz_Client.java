import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Quiz_Client {
    public static void main(String[] args) {
        BufferedReader serverInput = null;
        BufferedWriter serverOutput = null;
        Socket serverConnection = null;
        Scanner userInputScanner = new Scanner(System.in);
        
        String serverAddress = "localhost";
        int serverPort = 8888;

        File configFile = new File("server_info.dat");
        if (configFile.exists()) {
            try (BufferedReader configReader = new BufferedReader(new FileReader(configFile))) {
                serverAddress = configReader.readLine();
                serverPort = Integer.parseInt(configReader.readLine());
                System.out.println("Server information loaded.");
            } catch (IOException | NumberFormatException e) {
                System.out.println("Error! Use default values.");
            }
        } else {
            System.out.println("File not found! Use default values.");
        }

        try {
            serverConnection = new Socket(serverAddress, serverPort);
            System.out.println("Connected.");

            serverInput = new BufferedReader(new InputStreamReader(serverConnection.getInputStream()));
            serverOutput = new BufferedWriter(new OutputStreamWriter(serverConnection.getOutputStream()));

            String messageFromServer;
            while ((messageFromServer = serverInput.readLine()) != null) {
                System.out.println("Server: " + messageFromServer);

                if (messageFromServer.startsWith("Quiz finished!")) {
                    break;
                } else if (messageFromServer.startsWith("Question ( ")) {
                    System.out.print("Your answer (or leave empty to quit): ");
                    String userAnswer = userInputScanner.nextLine().trim();

                    if (userAnswer.isEmpty()) {
                        System.out.println("Exiting quiz.");
                        break;
                    }

                    serverOutput.write(userAnswer + "\n");
                    serverOutput.flush();
                } else if (messageFromServer.equals("Type 'n' to receive the next question.")) {
                    System.out.print("Type 'n' to continue (or leave empty to quit): ");
                    String nextCommand = userInputScanner.nextLine().trim();

                    if (nextCommand.isEmpty()) {
                        System.out.println("Exiting quiz.");
                        break;
                    }

                    if (!(nextCommand.equalsIgnoreCase("n"))) {
                        System.out.println("Invalid input. Type 'n' to continue.");
                        continue;
                    }

                    serverOutput.write(nextCommand + "\n");
                    serverOutput.flush();
                }
            }

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            try {
                if (serverConnection != null) serverConnection.close();
                System.out.println("Disconnected from the server.");
            } catch (IOException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
