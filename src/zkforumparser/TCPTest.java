/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zkforumparser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 *
 * @author User
 */
public class TCPTest {

    public static void main(String argv[]) throws Exception {
        String base64 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789+/";
        String sentence = "";
        String modifiedSentence;
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        Socket clientSocket = new Socket("zero-k.info", 8200);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        //outToServer.writeBytes(sentence + '\n');
        modifiedSentence = inFromServer.readLine();
        String psw = "LPF1E2eYlCsqq13xhuygPw==";
        //System.out.println("FROM SERVER: " + modifiedSentence);
        for (int i = 0; i < 2; i++) {
            switch (i) {
                case 0:
                    psw = "alkjsdf";
                    break;
                case 1:
                    psw = "LPF1E2eYlCsqq13xhuygPwww";
                    break;
                default:
                    psw = "LPF1E2eYlCsqq13xhuygPw==";
                    break;
            }
            long time = System.currentTimeMillis();
            for (int blub = 0; blub < 10; blub++) {
                outToServer.writeBytes("Login {\"Name\":\"DeinFreund\",\"PasswordHash\":\"" + psw + "\",\"UserID\":1658962033,\"LobbyVersion\":\"Spring Web Lobby react dev\",\"ClientType\":2}\n");
                modifiedSentence = inFromServer.readLine();
            }
            time = System.currentTimeMillis() - time;
            System.out.println(time);
            if (modifiedSentence.startsWith("User")) {
                System.out.println("Password: " + psw);
            }
        }
        clientSocket.close();
    }
}
