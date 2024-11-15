import java.io.*;
import java.net.*;

public class Quiz_Server { // 퀴즈 질문과 정답
    private static final String[] QUESTIONS = {
        "What is the method name for creating a socket for the specified specific destination?",
        "What is the port number of SMTP?",
        "What address identifies a computer or system on the network/internet?"
    };
    private static final String[] ANSWERS = {
        "Socket()",
        "25",
        "IP"
    };

    public static void main(String[] args) {
        ServerSocket socket = null;

        try { // 8888에서 서버 소켓을 생성 후 클라이언트 연결 기다림
            socket = new ServerSocket(8888);
            System.out.println("Waiting...");

            while (true) {
                Socket clientSocket = socket.accept();
                System.out.println("A new client has connected!");

                new Thread(new QuizSession(clientSocket)).start();  // 각 클라이언트를 위한 새로운 스레드 생성
            }

        } catch (IOException e) {
            System.out.println("Server Error : " + e.getMessage());
        } finally {
            try {
                if (socket != null) socket.close(); // 서버 소켓 닫기
            } catch (IOException e) {
                System.out.println("Error closing server : " + e.getMessage());
            }
        }
    }

     // 클라이언트와의 통신을 처리
    private static class QuizSession implements Runnable {
        private Socket clientSocket;
        private BufferedReader clientInput;
        private BufferedWriter clientOutput;

        public QuizSession(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {  // 클라이언트와 데이터 송수신을 위한 스트림
                clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                clientOutput = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

                int clientScore = 0; // 클라이언트 점수 초기화
                for (int questionIndex = 0; questionIndex < QUESTIONS.length; questionIndex++) {
                    clientOutput.write("Question ( " + QUESTIONS[questionIndex] + "\n");
                    clientOutput.flush();

                    String clientAnswer = clientInput.readLine(); // 클라이언트 답변 받기

                    if (clientAnswer == null || clientAnswer.trim().isEmpty()) {   // 답변이 비어있거나 연결이 끊어진 경우 종료
                        System.out.println("Client quit.");
                        break;
                    }

                    // 정답 체크 후 피드백
                    if (clientAnswer.equalsIgnoreCase(ANSWERS[questionIndex])) {
                        clientOutput.write("Correct!\n");
                        clientScore++;
                    } else {
                        clientOutput.write("Incorrect!\n");
                    }
                    clientOutput.flush();

                    // 마지막 질문이 아니라면 다음 질문을 받기 위한 요청
                    if (questionIndex < QUESTIONS.length - 1) { // 공백 제외 어떤 것을 입력해도 다음 질문
                        clientOutput.write("Type anything to receive the next question, or leave empty to quit.\n");
                        clientOutput.flush();

                        while (true) {
                            String nextCommand = clientInput.readLine();

                            if (nextCommand == null || nextCommand.trim().isEmpty()) { // 클라이언트가 종료하거나 답이 비어있다면 종료
                                System.out.println("Client quit."); 
                                return; // 세션 종료
                            } else { 
                                break;
                            }
                        }
                    }
                }
                clientOutput.write("Quiz finished! Your total score : " + clientScore + "/" + QUESTIONS.length + "\n"); // 퀴즈 종료 후 점수 전송
                clientOutput.flush();

            } catch (IOException e) {
                System.out.println("Client Error : " + e.getMessage());
            } finally {
                try {
                    if (clientSocket != null) clientSocket.close(); // 클라이언트 소켓 닫기
                } catch (IOException e) {
                    System.out.println("Error closing client : " + e.getMessage());
                }
            }
        }
    }
}
