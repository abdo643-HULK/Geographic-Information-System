module at.shehata.ex1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires java.desktop;
    requires javafx.swing;


    opens at.shehata.ex1 to javafx.fxml;
    exports at.shehata.ex1;
}