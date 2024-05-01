module com.carniceria {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.app.controllers to javafx.fxml;
    exports com.app.controllers;
}
