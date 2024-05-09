package com.app.controllers.Inventario;

import com.app.controllers.Ventas.VentasController;
import com.app.models.Categoria;
import com.app.models.Productos;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import javafx.scene.Parent;

public class FXMLInventarioController implements Initializable {

    @FXML
    private TableView<Productos> tableView;
    @FXML
    private TableColumn<Productos, Long> codigoColumn;
    @FXML
    private TableColumn<Productos, String> descripcionColumn;
    @FXML
    private TableColumn<Productos, BigDecimal> costoColumn;
    @FXML
    private TableColumn<Productos, BigDecimal> precioVentaColumn;
    @FXML
    private TableColumn<Productos, BigDecimal> existenciaColumn;
    @FXML
    private TableColumn<Productos, BigDecimal> inventarioMinimoColumn;
    @FXML
    private TextField idP;
    @FXML
    private ComboBox<String> categorias;
    @FXML private Pane rootPane;
    
    @FXML private Button btnAgregar;

    private ObservableList<Productos> productosData;
    private ObservableList<Productos> productosOriginalData;

    //LOGICA PARA MANEJAR FILTRADO POR ID
    public void buscarforID() {
        idP.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                buscar(newValue);
            }
        });
    }
  
    private void buscar(String consultaTexto) {
        // Crear una nueva lista para almacenar los elementos filtrados
        ObservableList<Productos> filteredItems = FXCollections.observableArrayList();
    
        // Si el texto de consulta está vacío, restaurar la lista original
        if (consultaTexto == null || consultaTexto.isEmpty()) {
            tableView.setItems(productosData);
        } else {
            String selectedCategory = categorias.getValue();
            
            // Iterar sobre la lista original y agregar los elementos que coincidan con el filtro y la categoría seleccionada
            for (Productos producto : productosOriginalData) {
                String idString = producto.getId().toString();
                String nombreProducto = producto.getNombre(); // Suponiendo que tengas un método `getNombre()` en tu clase `Productos`
    
                // Verificar si tanto el ID como el nombre del producto contienen el texto de consulta
                // y si la categoría seleccionada coincide con la categoría del producto
                if ((idString.contains(consultaTexto) || nombreProducto.contains(consultaTexto))
                    && (selectedCategory == null || selectedCategory.equals("Todos") || producto.getCategoria().getNombreCategoria().equals(selectedCategory))) {
                    filteredItems.add(producto);
                }
            }
    
            tableView.setItems(filteredItems);
            if (!filteredItems.isEmpty()) {
                categorias.setValue("Todos");
            }
        }
    }
    
    //LOGICA PARA MANEJAR LOS EVENTOS DE LOS BOTONES
    @FXML
    private void evento(ActionEvent e) { //TODAVIA ESTE EVENTO
        System.out.println("Seleccionaste ventas....");
    }


    @FXML
    private void ModProd() {
        Productos productoSeleccionado = tableView.getSelectionModel().getSelectedItem();
        if (productoSeleccionado != null) {
            
            long id = productoSeleccionado.getId();
            String nombre=productoSeleccionado.getNombre();
            BigDecimal precio=productoSeleccionado.getPrecio();
            BigDecimal Cantidad=productoSeleccionado.getCantidad();
            BigDecimal Costo=productoSeleccionado.getCosto();
            Categoria categoria=productoSeleccionado.getCategoria();
            
            
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FXMLModificarProducto.fxml"));
                Parent root = loader.load();
                
                FXML_ModProducto modProductoController = loader.getController();
             
                modProductoController.setInventarioController(this);
                modProductoController.setDatos(nombre, Costo, Cantidad, id, precio, categoria.getNombreCategoria());
                Scene scene = new Scene(root);
                
                Stage stage = new Stage();
                stage.setScene(scene);
                modProductoController.setStage(stage);
                
                // Mostrar el escenario
                stage.show();
    
                
            } catch (IOException ex) {
                ex.printStackTrace();
            }
    
        } else {
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("No se ha seleccionado ningún producto.");
            alert.showAndWait();
        }
    }


    
    @FXML
    private void addInventario() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FXMLProductoNew.fxml"));
            Parent root = loader.load();
            
            FXML_NewProducto newProductoController = loader.getController();
            newProductoController.setInventarioController(this);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void cargarCategorias(ComboBox<String> categorias, int opcion) {
        Configuration configuration = new Configuration().configure();
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.openSession();
    
        try {
            Query<String> query = session.createQuery("SELECT c.nombreCategoria FROM Categoria c", String.class);
            
            ObservableList<String> listaNombresCategorias = FXCollections.observableArrayList();
            categorias.setPromptText("Categorias");
           if (opcion==1)listaNombresCategorias.add("Todos");
            listaNombresCategorias.addAll(query.list());
            ObservableList<String> observableListaNombresCategorias = FXCollections.observableArrayList(listaNombresCategorias);
            categorias.setItems(observableListaNombresCategorias);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
            sessionFactory.close();
        }
    }
    
    //LOGICA PARA FILTRAR EN LA TABLA
    public  void filtarCategorias(){
        Categoria id=new Categoria();
        categorias.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String name) {
                if (name != null) {
                    long idCategoria=0;
                    if(name.equals("Todos")){
                        agregaraTabla();
                    } else {
                        idCategoria=id.getIDconName(name);
                        actualizarTablaF(obtenerProductosF(idCategoria));
                    }
                }
            }
        });
    }

    public void actualizarTablaF( List<Productos> productos) {
        productosData.clear();
        productosData.addAll(productos);
    }

    public List<Productos> obtenerProductosF(long id) {
        Configuration configuration = new Configuration().configure();
        configuration.addAnnotatedClass(Productos.class);
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory entityManagerFactory = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        // Realizar la consulta en la base de datos
        String query = "SELECT p FROM Productos p WHERE p.id LIKE :consultaTexto OR p.nombre LIKE :consultaTexto";
        TypedQuery<Productos> typedQuery = entityManager.createQuery(query, Productos.class);
        typedQuery.setParameter("consultaTexto", "%" + id + "%");
        List<Productos> productosEncontrados = typedQuery.getResultList();

        entityManager.close();
        entityManagerFactory.close();
    
        return productosEncontrados;
    }
    
    //LOGICA QUE SE CARGARA AL INICIAR LA PAGINA DE INVENTARIO 
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        agregaraTabla();
        cargarCategorias(this.categorias, 1);
        filtarCategorias();
        buscarforID();
        
        rootPane.setOnKeyPressed(this::handleKeyPressed);
    }
    
    @FXML
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.F5) {
            addInventario();
        
        }

        if(event.getCode()==KeyCode.F6){
            ModProd();
        }
        if(event.getCode()==KeyCode.F1){
            abrirVentas();
        }
    }
    public void agregaraTabla(){
       
        codigoColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        descripcionColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        costoColumn.setCellValueFactory(new PropertyValueFactory<>("costo"));
        precioVentaColumn.setCellValueFactory(new PropertyValueFactory<>("precio"));
        existenciaColumn.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        inventarioMinimoColumn.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        // Obtener los productos de la base de datos
        List<Productos> productos = obtenerProductos();

        // Crear una lista observable a partir de la lista de productos
        productosOriginalData = FXCollections.observableArrayList(productos);
        productosData = FXCollections.observableArrayList(productos);

        tableView.setItems(productosData);
    }

    private List<Productos> obtenerProductos() {
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");

        configuration.addAnnotatedClass(Productos.class);
      
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = emf.createEntityManager();

        TypedQuery<Productos> query = entityManager.createQuery("SELECT p FROM Productos p", Productos.class);
        List<Productos> productos = query.getResultList();

        entityManager.close();
        emf.close();

        return productos;
    }

    public void actualizarTabla() {
        List<Productos> productosActualizados = obtenerProductos();
        productosData.clear();
        productosData.addAll(productosActualizados);
    }

    //Codigo para exportar todo el inventario a excell
 
     
    @FXML
    public void Exportar() {
        // Obtén la lista observable de la TableView
        ObservableList<Productos> data = tableView.getItems();

        Date fecha = new Date();
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        String fechaString = formato.format(fecha);


        // Crea el libro de trabajo y la hoja de cálculo
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Datos");
    
        int rowNum = 0;
    
        // Agregar fila de encabezado con los nombres de las columnas
        Row headerRow = sheet.createRow(rowNum++);
        String[] columnNames = {"ID","Nombre", "Existencia", "Costo", "Precio Venta"}; // Agrega aquí los nombres de tus columnas
        int colNum = 0;
        for (String columnName : columnNames) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(colNum++);
            cell.setCellValue(columnName);
        }
    
        // Iterar sobre los datos y las filas
        for (Productos producto : data) {
            Row excelRow = sheet.createRow(rowNum++);
            colNum = 0;
            
            org.apache.poi.ss.usermodel.Cell cellID = excelRow.createCell(colNum++);
            cellID.setCellValue(producto.getId());

            org.apache.poi.ss.usermodel.Cell cellNombre = excelRow.createCell(colNum++);
            cellNombre.setCellValue(producto.getNombre());
    
            org.apache.poi.ss.usermodel.Cell cellCantidad = excelRow.createCell(colNum++);
            cellCantidad.setCellValue(producto.getCantidad().doubleValue()); 

            org.apache.poi.ss.usermodel.Cell cellCosto = excelRow.createCell(colNum++);
            cellCosto.setCellValue(producto.getCosto().doubleValue()); 
            
            org.apache.poi.ss.usermodel.Cell cellPrecio = excelRow.createCell(colNum++);
            cellPrecio.setCellValue(producto.getPrecio().doubleValue()); 
        }
        String fechaExcell=fechaString.replace("/", "-");
        try (FileOutputStream fileOut = new FileOutputStream("Inventario " + (categorias.getValue() != null && !categorias.getValue().equalsIgnoreCase("Todos") ? categorias.getValue() : "Total") +" "+fechaExcell+ ".xlsx")) {
            workbook.write(fileOut);
            System.out.println("¡Los datos se han exportado correctamente a Excel!");
             Alert alert=new Alert(AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("Se exporto correctamente el documento Excell");
            alert.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
    }


    //Movimientos

    @FXML
    public void movimientos(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Movimientos.fxml"));
            Parent root = loader.load();
            
            MovimientosController newMovimientos = loader.getController();

            
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


   @FXML
public void eliminarProducto() {
    // Obtener el producto seleccionado del TableView
    Productos productoSeleccionado = tableView.getSelectionModel().getSelectedItem();

    if (productoSeleccionado != null) {
        // Mostrar un diálogo de confirmación
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Estás seguro de que deseas eliminar este producto?");
        alert.setContentText("Esta acción no se puede deshacer.");

        // Esperar la respuesta del usuario
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Eliminar el producto de la base de datos
                Configuration configuration = new Configuration();
                configuration.configure("hibernate.cfg.xml");
                configuration.addAnnotatedClass(Productos.class);

                SessionFactory sessionFactory = configuration.buildSessionFactory();
                EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
                EntityManager entityManager = emf.createEntityManager();

                entityManager.getTransaction().begin();
                entityManager.remove(entityManager.contains(productoSeleccionado) ? productoSeleccionado : entityManager.merge(productoSeleccionado));
                entityManager.getTransaction().commit();

                entityManager.close();
                emf.close();

                
                productosData.remove(productoSeleccionado);
                Alert alert2 = new Alert(Alert.AlertType.CONFIRMATION);
                alert2.setHeaderText(null);
                alert2.setContentText("Se elimino el producto correctamente");
                alert2.showAndWait();
                
            }
        });
    } else {
        // Mostrar un mensaje de error si no se ha seleccionado ningún producto
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("No se ha seleccionado ningún producto");
        alert.setContentText("Por favor, selecciona un producto para eliminar.");
        alert.showAndWait();
    }
}


public void abrirVentas() {
    try {
        // Cargar el archivo FXML con el nuevo contenido
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Ventas.fxml"));
        Pane nuevoContenido = loader.load();
        
        // Obtener el controlador del nuevo contenido
        VentasController inventarioController = loader.getController();
        
        // Reemplazar el contenido del contenedor principal con el nuevo contenido
        rootPane.getChildren().setAll(nuevoContenido);
    } catch (IOException e) {
        e.printStackTrace();
    }
}



}