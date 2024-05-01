module com.carniceria {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.hibernate.orm.core;
    requires java.persistence;
    requires java.sql;

    opens com.app.models to org.hibernate.orm.core;
    opens com.app.controllers to javafx.fxml;
    exports com.app.controllers;
}





