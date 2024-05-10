package com.app.controllers.devoluciones;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import javax.persistence.Column;
import javafx.scene.control.TableColumn;

public class tabledata {
    @Column(name = "cantidad")
    private BigDecimal cantidad;
    @Column(name = "total")
    private BigDecimal total;
    @Column(name = "fecha")
    private String fecha;
    @Column(name = "ticket1")
    private String ticket1;
    @Column(name = "nombre")
    private String nombre;
    @Column(name = "detalle")
    private Long detalle;

    public tabledata(String ticket12, String fechaFormateada, BigDecimal total, BigDecimal cantidad, String nombre, Long detalle) {
        this.ticket1 = ticket12;
        this.fecha = fechaFormateada;
        this.total = total;
        this.cantidad = cantidad;
        this.nombre = nombre;
        this.detalle = detalle;
    }

    public String getTicket1() {
        return ticket1;
    }

    public void setTicket1(String ticket1) {
        this.ticket1 = ticket1;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
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
    public Long getDetalle() {
        return detalle;
    }

    public void setdetalle(Long detalle) {
        this.detalle = detalle;
    }
}