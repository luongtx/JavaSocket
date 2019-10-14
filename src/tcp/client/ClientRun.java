/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tcp.client;

/**
 *
 * @author luongtx
 */
public class ClientRun {
    public static void main(String[] args) {
        ClientLoginFrm loginView = new ClientLoginFrm();
        ClientSignUpFrm signUpView = new ClientSignUpFrm();
        loginView.setVisible(true);
        signUpView.setVisible(false);
        Client control = new Client(loginView, signUpView);
    }
}
