import java.io.*;
import java.net.*;

public class Quiz_Server {
    private static final String[] QUESTIONS = {
        "What is the capital of France?",
        "What is 5 + 7?",
        "Who wrote 'Hamlet'?"
    };
    private static final String[] ANSWERS = {
        "Paris",
        "12",
        "Shakespeare"
    };

    public static void main(String[] args) {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(8888);
            System.out.println("Quiz Server started on port 8888...");
            System.out.println("Waiting...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connection successful");

                new Thread(new ClientHandler(clientSocket)).start();
            }

        } catch (IOException e) {
            System.out.println("Server Error: " + e.getMessage());
        } finally {
            try {
                if (serverSocket != null) serverSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing server: " + e.getMessage());
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader clientInput;
        private BufferedWriter clientOutput;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                clientOutput = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

                int clientScore = 0;
                for (int questionIndex = 0; questionIndex < QUESTIONS.length; questionIndex++) {
                    clientOutput.write("Question ( " + QUESTIONS[questionIndex] + " )\n");
                    clientOutput.flush();

                    String clientAnswer = clientInput.readLine();

                    if (clientAnswer == null || clientAnswer.trim().isEmpty()) {
                        System.out.println("Client has chosen to quit or disconnected.");
                        break;
                    }

                    if (clientAnswer.equalsIgnoreCase(ANSWERS[questionIndex])) {
                        clientOutput.write("Correct!\n");
                        clientScore++;
                    } else {
                        clientOutput.write("Incorrect!\n");
                    }
                    clientOutput.flush();

                    if (questionIndex < QUESTIONS.length - 1) {
                        clientOutput.write("Type 'n' to receive the next question.\n");
                        clientOutput.flush();

                        String nextCommand = clientInput.readLine();
                        if (nextCommand == null || nextCommand.trim().isEmpty() || !nextCommand.equalsIgnoreCase("n")) {
                            System.out.println("Client ended the quiz or did not respond with 'n'.");
                            break;
                        }
                    }
                }
                clientOutput.write("Quiz finished! Your total score : " + clientScore + "/" + QUESTIONS.length + "\n");
                clientOutput.flush();

            } catch (IOException e) {
                System.out.println("Client Error: " + e.getMessage());
            } finally {
                try {
                    if (clientSocket != null) clientSocket.close();
                } catch (IOException e) {
                    System.out.println("Error closing client connection: " + e.getMessage());
                }
            }
        }
    }
}
