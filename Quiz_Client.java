import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Quiz_Client {
    public static void main(String[] args) {
        BufferedReader serverInput = null;
        BufferedWriter serverOutput = null;
        Socket serverConnection = null;
        Scanner scan = new Scanner(System.in);
        
        // 기본 서버 주소와 포트 설정
        String address = "localhost";
        int port = 8888;

        // 파일에서 서버 정보 받기
        File File = new File("server_info.dat");
        if (File.exists()) {
            try (BufferedReader configReader = new BufferedReader(new FileReader(File))) {
                address = configReader.readLine();
                port = Integer.parseInt(configReader.readLine());
                System.out.println("Server information loaded."); 
            } catch (IOException | NumberFormatException e) {
                System.out.println("Error! Use default values."); // 오류 시 기본 값 사용
            }
        } else {
            System.out.println("File not found! Use default values."); // 파일이 없을 시 기본 값 사용
        }

        try {
            serverConnection = new Socket(address, port);
            System.out.println("Connected.");

            // 서버와 데이터 송수신을 위한 스트림
            serverInput = new BufferedReader(new InputStreamReader(serverConnection.getInputStream()));
            serverOutput = new BufferedWriter(new OutputStreamWriter(serverConnection.getOutputStream()));

            String messageFromServer;
            while ((messageFromServer = serverInput.readLine()) != null) {
                System.out.println("Server: " + messageFromServer);

                if (messageFromServer.startsWith("Quiz finished!")) { // 퀴즈 종료 메시지을 받았다면 종료
                    break;
                } else if (messageFromServer.startsWith("Question ( ")) {
                    System.out.print("Your answer (or leave empty to quit): ");
                    String userAnswer = scan.nextLine().trim();

                    if (userAnswer.isEmpty()) { // 비어있는 답변이면 퀴즈 종료
                        System.out.println("Exiting quiz.");
                        break;
                    }

                    // 서버로 답 전송
                    serverOutput.write(userAnswer + "\n");
                    serverOutput.flush();
                } else if (messageFromServer.equals("Type 'n' to receive the next question.")) { // 다음 문제를 원한다면 'n' 나가고 싶다면 공백
                    System.out.print("Type 'n' to continue (or leave empty to quit): ");
                    String nextCommand = scan.nextLine().trim();

                    if (nextCommand.isEmpty()) {
                        System.out.println("Exiting quiz.");
                        break;
                    }

                    // 잘못된 입력이면 다시 입력 요청
                    if (!(nextCommand.equalsIgnoreCase("n"))) {
                        System.out.println("Invalid input. Type 'n' to continue.");
                        continue;
                    }

                    // 서버로 'n' 전송
                    serverOutput.write(nextCommand + "\n");
                    serverOutput.flush();
                }
            }

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            try {
                if (serverConnection != null) serverConnection.close(); // 서버 연결 닫기
                System.out.println("Disconnected from the server.");
            } catch (IOException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
