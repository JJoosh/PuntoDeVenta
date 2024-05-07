package com.app.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.GenerationType;

@Entity
@Table(name = "Movimientos")
public class Movimientos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Long id_movimiento;

    @ManyToOne
    @JoinColumn(name = "id_producto")
    private Productos id_producto;

    @Column(name = "tipo_movimiento")
    private String tipo_movimiento;

    @Column(name = "cantidad")
    private BigDecimal cantidad;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "hora")
    private LocalTime hora;

    // Getters and Setters
    public Long getId_movimiento() {
        return id_movimiento;
    }

    public void setId_movimiento(Long id_movimiento) {
        this.id_movimiento = id_movimiento;
    }

    public Productos getIdProducto() {
        return id_producto;
    }

    public void setIdProducto(Productos idProducto) {
        this.id_producto = idProducto;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal bigDecimal) {
        this.cantidad = bigDecimal;
    }

    public String getTipoMovimiento() {
        return tipo_movimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipo_movimiento = tipoMovimiento;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fechaHora) {
        if (fechaHora != null) {
            this.fecha = fechaHora.toLocalDate();
            this.hora = fechaHora.toLocalTime();
        }
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getNombreProducto() {
        if (id_producto != null) {
            return id_producto.getNombre();
        }
        return "";
    }
}