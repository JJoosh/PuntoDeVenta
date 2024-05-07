module com.carniceria{
    requires javafx.controls;
    requires javafx.fxml;
    requires org.hibernate.orm.core;
    requires java.persistence;
    requires java.sql;
    requires java.naming;
    requires dom4j;
    requires org.apache.pdfbox;
    requires itextpdf;
    requires icu4j;
    requires nrjavaserial;

    opens com.app.models to org.hibernate.orm.core, javafx.base;
    opens com.app.controllers to javafx.fxml;
    opens com.app.controllers.Ventas;


    exports com.app.controllers;
    exports com.app.controllers.Ventas;
    
}