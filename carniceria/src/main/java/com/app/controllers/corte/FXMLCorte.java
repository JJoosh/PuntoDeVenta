package com.app.controllers.corte;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.app.models.Ventas;
import com.app.models.CortedeCaja;
import com.app.models.Devoluciones;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class FXMLCorte { 

    @FXML
    private TableView<CortedeCaja> Corte;
    @FXML
    private TableColumn<CortedeCaja, Long> IDcorte;
    @FXML
    private TableColumn<CortedeCaja, LocalDate> Fecha;
    @FXML
    private TableColumn<CortedeCaja, BigDecimal> CantidadC;
    @FXML
    private TableColumn<CortedeCaja, Long> CantidadV;
    @FXML
    private TextField search;

    private ObservableList<CortedeCaja> datosTabla = FXCollections.observableArrayList();
    private List<Ventas> listaVentas;
    private List<Ventas> ventasfiltrado;

    public void initialize() {
        configurarTabla();
        listaVentas = obtenerListaDeVentas();
        mostrarTodosLosCortesEnTabla();
    
        search.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                mostrarTodosLosCortesEnTabla();
            } else {
                filtrarCortePorFecha(newValue);
                System.out.println(newValue+"vamos");
            }
        });
    }
    private void configurarTabla() {
        IDcorte.setCellValueFactory(new PropertyValueFactory<>("id"));
        Fecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        CantidadC.setCellValueFactory(new PropertyValueFactory<>("totalVentas"));
        CantidadV.setCellValueFactory(new PropertyValueFactory<>("cantidadVentas"));
        datosTabla = FXCollections.observableArrayList();
    }

    public List<Ventas> obtenerListaDeVentas() {
        Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = emf.createEntityManager();
        TypedQuery<Ventas> query = entityManager.createQuery("SELECT v FROM Ventas v", Ventas.class);
        List<Ventas> ventas = query.getResultList();
        entityManager.close();
        emf.close();
        return ventas;
    }

    private void mostrarTodosLosCortesEnTabla() {
        List<CortedeCaja> cortesDeCaja = obtenerCorteDeCaja(null);
        datosTabla.clear();
        datosTabla.addAll(cortesDeCaja);
        Corte.setItems(datosTabla);
    }

    private void filtrarCortePorFecha(String fechaTexto) {
        ventasfiltrado = new ArrayList<>();
        try {
            for (Ventas venta :listaVentas ) {
                LocalDateTime fechaVenta = venta.getFecha();
                
                
            LocalDate fecha = LocalDate.parse(fechaTexto, DateTimeFormatter.ISO_DATE);
            List<CortedeCaja> cortesFiltrados = obtenerCorteDeCaja(fecha);
            System.out.println(fecha);
            datosTabla.clear();
            datosTabla.addAll(cortesFiltrados);
            Corte.setItems(datosTabla);
        }
     } catch (Exception e) {
            // Manejar el error de formato de fecha
            System.out.println("El formato de fecha ingresado no es vÃ¡lido. Debe ser 'yyyy-MM-dd'.");
        }
    }

    public List<CortedeCaja> obtenerCorteDeCaja(LocalDate fecha) {
        List<Ventas> ventasFiltradas = new ArrayList<>();
        BigDecimal totalVentas = BigDecimal.ZERO;

        for (Ventas venta : listaVentas) {
            if (fecha == null || venta.getFecha().toLocalDate().isEqual(fecha)) {
                ventasFiltradas.add(venta);
                totalVentas = totalVentas.add(venta.getTotal());
            }
        }

        List<CortedeCaja> cortesDeCaja = new ArrayList<>();
        if (!ventasFiltradas.isEmpty()) {
            LocalDate fechaCorte = ventasFiltradas.get(0).getFecha().toLocalDate();
            CortedeCaja corte = new CortedeCaja(null, fechaCorte.atStartOfDay(), totalVentas, new BigDecimal(ventasFiltradas.size()));
            cortesDeCaja.add(corte);
        }

        return cortesDeCaja;
    }
}