package com.app.models;



import javax.persistence.*;


@Entity
@Table(name = "Usuarios")
public class Usuarios {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   
    @Column(name = "Fecha_Reporte")
    private String nombre_usuario;
    
    @Column(name = "Contraseña")
    private String contraseña;

    @Column(name =  "Rol")
    private String Rol;


    public Usuarios() {
    }

    public Usuarios(long id, String nombre_usser, String password, String Rol) {
       this.id=id;
       this.nombre_usuario=nombre_usser;
       this.contraseña=password;
       this.Rol=Rol;
    } 

    // Getters y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre(){
        return nombre_usuario;
    }

    public void setNombre(String nombre_usuario){
        this.nombre_usuario=nombre_usuario;
    }

    public String getPassword(){
        return contraseña;
    }

    public void setPassword(String password){
        this.contraseña=password;
    }

    public String getRol(){
        return Rol;
    }

    public void setRol(String Rol){
        this.Rol=Rol;
    }
}