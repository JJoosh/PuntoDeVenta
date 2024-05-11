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
    requires java.desktop;
    requires org.apache.poi.poi;
    requires org.jpos.jpos;
    requires org.apache.poi.ooxml;
    requires javafx.graphics;

    opens com.app.models to org.hibernate.orm.core, javafx.base;
    opens com.app.controllers to javafx.fxml;
    opens com.app.controllers.Ventas;
    opens com.app.controllers.Login;
    opens com.app.controllers.Inventario;
    opens com.app.controllers.corte;
    opens com.app.controllers.devoluciones to javafx.fxml;
    exports com.app.controllers.Inventario;

    exports com.app.controllers;
    exports com.app.controllers.Ventas;
    exports com.app.controllers.Login;
    exports com.app.controllers.devoluciones;
    exports com.app.controllers.corte;

    
}