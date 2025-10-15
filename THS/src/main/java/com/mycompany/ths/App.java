package com.mycompany.ths;

import com.mycompany.ths.util.Navigator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
    Navigator.StageHolder.set(stage);              
    var root = FXMLLoader.load(App.class.getResource("login.fxml"));
    stage.setTitle("THS - Telehealth System");
    stage.setScene(new Scene((Parent) root));
    stage.show();
}
    public static void main(String[] args) { launch(args); }
}
