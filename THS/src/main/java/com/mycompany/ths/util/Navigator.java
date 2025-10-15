package com.mycompany.ths.util;

import com.mycompany.ths.App;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class Navigator {
    private Navigator(){}

    public static void to(String fxmlName) {
        try {
            var root = FXMLLoader.load(App.class.getResource(fxmlName));
            Stage stage = StageHolder.get();
            stage.setScene(new Scene((Parent) root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // StageHolder stores the primary Stage once
    public static class StageHolder {
        private static Stage stage;
        public static void set(Stage s){ stage = s; }
        public static Stage get(){ return stage; }
    }
}
