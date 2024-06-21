package com.app.controllers.Ventas;

import com.app.models.Productos;

import javafx.collections.ObservableList;

public class TicketData {
    private String tabName;
    private ObservableList<Productos> productos;

    public TicketData(String tabName, ObservableList<Productos> productos) {
        this.tabName = tabName;
        this.productos = productos;
    }

    public String getTabName() {
        return tabName;
    }

    public ObservableList<Productos> getProductos() {
        return productos;
    }
}