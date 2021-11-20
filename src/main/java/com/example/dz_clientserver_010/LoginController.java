package com.example.dz_clientserver_010;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import java.io.IOException;
import java.util.Properties;

public class LoginController {
    @FXML
    public Button loginButton;
    @FXML
    public TextField email;
    @FXML
    public PasswordField password;
    @FXML
    public Label error;

    @FXML
    void initialize(){
        loginButton.setOnAction(actionEvent -> {
            //check
            String user = email.getText();
            String pass = password.getText();
            boolean check = false;
            try {
                final String host = "imap.gmail.com";

                // Создание свойств
                Properties props = new Properties();

                //Указываем протокол - IMAP с SSL
                props.put("mail.store.protocol", "imaps");
                Session session = Session.getInstance(props);
                Store store = session.getStore();

                //подключаемся к почтовому серверу
                store.connect(host, user, pass);
                store.close();

                check = true;
            } catch (MessagingException e) {
                //e.printStackTrace();
            }

            if (!check){
                error.setText("login error");
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("app.fxml"));

                Stage stage = new Stage();
                try {
                    stage.setScene(new Scene(loader.load()));
                    loader.<AppController>getController().initData(user, pass);
                    stage.show();
                    ((Stage) loginButton.getScene().getWindow()).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }
}