package com.app.controllers.devoluciones;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javafx.scene.control.TableColumn;

public class tabledata {
    @Column(name = "cantidad")
    private BigDecimal cantidad;
    @Column(name = "total")
    private float total;
    @Column(name = "fecha")
    private Date fecha;
    @Column(name = "ticket1")
    private String ticket1;
    @Column(name = "nombre")
    private String nombre;

    public tabledata(String ticket12, java.sql.Date fecha2, float total2, BigDecimal cantidad, String nombre2) {
        this.ticket1 = ticket12;
        this.fecha = fecha2;
        this.total = total2;
        this.cantidad = cantidad;
        this.nombre = nombre2;
    }

    public String getTicket1() {
        return ticket1;
    }

    public void setTicket1(String ticket1) {
        this.ticket1 = ticket1;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}