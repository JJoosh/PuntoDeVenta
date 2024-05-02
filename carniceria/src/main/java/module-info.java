module com.carniceria {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.hibernate.orm.core;
    requires java.persistence;
    requires java.sql;
    
    opens com.app.models to org.hibernate.orm.core, javafx.base;
    
    opens com.app.controllers to javafx.fxml;
    opens com.app.controllers.Inventario to javafx.fxml;
    
    exports com.app.controllers;
}