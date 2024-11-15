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
                } else if (messageFromServer.equals("Type anything to receive the next question, or leave empty to quit.")) { 
                    while (true) { // 입력이 나올 때까지
                        System.out.print("Type anything to continue (or leave empty to quit): ");
                        String nextCommand = scan.nextLine().trim();

                        if (nextCommand.isEmpty()) { // 공백 입력 시 종료
                            System.out.println("Exiting quiz.");
                            return;
                        }

                        try {
                            serverOutput.write(nextCommand + "\n");
                            serverOutput.flush();
                            break; // 입력 완료 후 반복 종료
                        } catch (IOException e) {
                            System.out.println("Error sending input to server. Closing connection.");
                            return; // 연결 종료
                        }
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Error : " + e.getMessage());
        } finally {
            try {
                if (serverConnection != null) serverConnection.close(); // 서버 연결 닫기
                System.out.println("Disconnected");
            } catch (IOException e) {
                System.out.println("Error closing : " + e.getMessage());
            }
        }
    }
}
