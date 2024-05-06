package com.app.models;

import java.math.BigDecimal;
import java.util.Date;

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
    @Column(name="id_movimiento")
    private Long id_movimiento;

    @ManyToOne
    @JoinColumn(name="id_producto")
    private Productos id_producto;

    @Column(name = "tipo_movimiento")
    private String tipo_movmiento;

    @Column(name = "cantidad")
    private BigDecimal cantidad;

    @Column(name ="fecha")
    private Date fecha;



    //Getters and Setters

    public Long getID(){
        return id_movimiento;
    }

    public void setID(long id_movimiento){
        this.id_movimiento=id_movimiento;
    }
    public Productos getIDProducto(){
        return id_producto;
    }

    public void setIDProducto(Productos idProducto){
        this.id_producto=idProducto;
    }

    public BigDecimal getCantidad(){
        return cantidad;
    }
    public void setCantidad(BigDecimal bigDecimal){
        this.cantidad=bigDecimal;
    }
    public String getTipoMovimiento(){
        return tipo_movmiento;
    }

    public void setTipoMovimiento(String tipoMovimiento){
        this.tipo_movmiento=tipoMovimiento;
    }

    public Date getFecha(){
        return fecha;
    }

    public void setFecha(java.util.Date date){
        this.fecha=date;

    }
    }


