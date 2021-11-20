package com.example.dz_clientserver_010;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class AppController {
    @FXML
    public Label email;
    @FXML
    public Button logoutButton;
    @FXML
    public ListView listLetters;
    @FXML
    public TextArea textArea;

    private String user;
    private String pass;

    @FXML
    void initialize(){
        logoutButton.setOnAction(actionEvent -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Stage stage = new Stage();
            try{
                stage.setScene(new Scene(loader.load()));
                stage.show();
                ((Stage) logoutButton.getScene().getWindow()).close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }

    public void initData(String user, String pass){
        this.user = user;
        this.pass = pass;

        email.setText(user);

        listLetters.setItems(getObservableListLetter());
        listLetters.setCellFactory(param -> new ListCell<Letter>() {
            @Override
            protected void updateItem(Letter item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.getFrom() == null) {
                    setText(null);
                } else {
                    setText("From: "+item.getFrom()+"\n Subject: "+item.getSubject());
                }
            }
        });
        listLetters.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                textArea.setText(((Letter)listLetters.getSelectionModel().getSelectedItem()).getText());
            }
        });

    }

    private ObservableList<Letter> getObservableListLetter(){
        ObservableList<Letter> observableList = FXCollections.observableArrayList();

        try {
            final String host = "imap.gmail.com";
            // FIXME: 20.11.2021 Обязательно использовать именно POP3? У меня тот же код изначально на нем и работал, а потом просто перестал.

            // Создание свойств
            Properties props = new Properties();

            //Указываем протокол - IMAP с SSL
            props.put("mail.store.protocol", "imaps");
            Session session = Session.getInstance(props);
            Store store = session.getStore();

            //подключаемся к почтовому серверу
            store.connect(host, user, pass);

            //получаем папку с входящими сообщениями
            Folder inbox = store.getFolder("INBOX");

            //открываем её только для чтения
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();

            List<Letter> letters = new ArrayList<>();
            for (Message message : messages) {
                letters.add(new Letter(message));
            }

            observableList.addAll(letters);

            store.close();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return observableList;
    }

    class Letter{
        private String from;
        private String subject;
        private String text;

        public Letter(Message message) {
            try {
                this.from = Arrays.toString(message.getFrom());
                this.subject = message.getSubject();
                this.text = getTextFromMessage(message);

            } catch (MessagingException | IOException e) {
                e.printStackTrace();
            }
        }

        public String getFrom() {
            return from;
        }

        public String getSubject() {
            return subject;
        }

        public String getText() {
            return text;
        }
    }

    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    private String getTextFromMimeMultipart(
            MimeMultipart mimeMultipart)  throws MessagingException, IOException{
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
            } else if (bodyPart.getContent() instanceof MimeMultipart){
                result = result + getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
            }
        }
        return result;
    }
}