package com.app.controllers.corte;

import java.math.BigDecimal;

import javax.persistence.Column;
import javafx.scene.control.TableColumn;

public class tablecorte {
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
    @Column(name = "producto")
    private Long detalle;
    @Column(name = "Forma_Pago")
    private String pago;

    public tablecorte(String ticket1 ,Long detalle,String nombre, BigDecimal cantidad, String fechaFormateada, BigDecimal total, String pago) {
        this.ticket1 = ticket1;
        this.fecha = fechaFormateada;
        this.total = total;
        this.cantidad = cantidad;
        this.nombre = nombre;
        this.detalle = detalle;
        this.pago = pago;
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
    public String getPago() {
        return pago;
    }

    public void setpago(String pago){
        this.pago = pago;
    }
}