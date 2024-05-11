package com.app.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CortedeCaja")
public class CortedeCaja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CAJA")
    private Long id;

    @Column(name = "Fecha")
    private LocalDateTime fecha;

    @Column(name = "Total_Ventas")
    private BigDecimal totalVentas;

    @Column(name = "cantidad_Ventas")
    private BigDecimal cantidadVentas;

    public CortedeCaja() {
    }
    
    public CortedeCaja(Long id, LocalDateTime fecha, BigDecimal totalVentas, BigDecimal cantidadVentas)  {
        this.id = id;
        this.fecha = fecha;
        this.totalVentas = totalVentas;
        this.cantidadVentas = cantidadVentas;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getTotalVentas() {
        return totalVentas;
    }

    public void setTotalVentas(BigDecimal totalVentas) {
        this.totalVentas = totalVentas;
    }

    public BigDecimal getCantidadVentas() {
        return cantidadVentas;
    }

    public void setCantidadVentas(BigDecimal cantidadVentas) {
        this.cantidadVentas = cantidadVentas;
    }
}
