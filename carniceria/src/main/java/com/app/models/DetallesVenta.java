package com.app.models;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "Detalles_Venta")
public class DetallesVenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Detalle")
    private Long id;
    @ManyToOne
    @JoinColumn(name = "ID_Venta")
    private Ventas venta;
    @ManyToOne
    @JoinColumn(name = "ID_Producto")
    private Productos producto;
    @Column(name = "Cantidad")
    private BigDecimal cantidad;
    @Column(name = "Total")
    private BigDecimal total;

    @Column(name = "Forma_Pago")
    private String formaPago;

    public DetallesVenta() {
    }

    public DetallesVenta(Long id, Ventas venta, Productos producto, BigDecimal cantidad, BigDecimal total,
            String formaPago) {
        this.id = id;
        this.venta = venta;
        this.producto = producto;
        this.cantidad = cantidad;
        this.total = total;
        this.formaPago = formaPago;
    }
    public DetallesVenta(Ventas venta, Productos producto, BigDecimal cantidad, BigDecimal total, String formaPago) {
        this.venta = venta;
        this.producto = producto;
        this.cantidad = cantidad;
        this.total = total;
        this.formaPago = formaPago;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ventas getVenta() {
        return venta;
    }

    public void setVenta(Ventas venta) {
        this.venta = venta;
    }

    public Productos getProducto() {
        return producto;
    }

    public void setProducto(Productos producto) {
        this.producto = producto;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }


    public String getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(String formaPago) {
        this.formaPago = formaPago;
    }
}