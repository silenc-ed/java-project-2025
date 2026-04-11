module com.mycompany.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;

    opens com.mycompany.demo to javafx.fxml;
    exports com.mycompany.demo;
}
