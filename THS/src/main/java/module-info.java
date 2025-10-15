module com.mycompany.ths {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;   // for SwingFXUtils
    requires java.desktop;   // for ImageIO
    requires org.apache.pdfbox;

    exports com.mycompany.ths;
    opens com.mycompany.ths to javafx.fxml;
    opens com.mycompany.ths.controller to javafx.fxml;
}
