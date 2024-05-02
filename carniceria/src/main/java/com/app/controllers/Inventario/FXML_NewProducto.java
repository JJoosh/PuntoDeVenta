package com.app.controllers.Inventario;



import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;

public class FXML_NewProducto {
    @FXML
    private TextField layout1;

    @FXML
    private TextField layout4;

    @FXML
    private TextField layout2;

    @FXML
    private Spinner<Double> spinner1;

    @FXML
    private Spinner<Double>spinner2;

    @FXML
    private Spinner<Double> spinner3;

    @FXML
    public void agregar() {
        



        if(layout1.getText().isEmpty()==false && layout2.getText().isEmpty()==false && layout4.getText().isEmpty()==false && spinner1.getValue()!=4.9E-324 && spinner2.getValue()!=4.9E-324 && spinner3.getValue()!=4.9E-324 && 
spinner1.getValue()>0 && spinner2.getValue()>0 && spinner3.getValue()>0) {

        try{
            int codigoBarras = Integer.parseInt(layout1.getText());
            String descripcion = layout2.getText();
            double precioCosto = spinner1.getValue();
            double precioVenta = spinner2.getValue();
            double precioMayoreo = spinner3.getValue();
            double cantidadKg=Double.parseDouble(layout4.getText());

            
            
            System.err.println("Correcto");
        } catch(Exception e){
            System.out.println("Por favor escriba los valores correctamente");
        }
            

        }
        else{
            System.out.println("Algo falta");
        }
       



        

    }

    
    public void initialize() {
       
        SpinnerValueFactory<Double> valueFactory1 = new SpinnerValueFactory.DoubleSpinnerValueFactory(
            Double.MIN_VALUE, Double.MAX_VALUE, 0.0, 0.1); // Valores mínimos, máximos, valor inicial y paso
        spinner1.setValueFactory(valueFactory1);
               
        SpinnerValueFactory<Double> valueFactory2 = new SpinnerValueFactory.DoubleSpinnerValueFactory(
            Double.MIN_VALUE, Double.MAX_VALUE, 0.0, 0.1); // Valores mínimos, máximos, valor inicial y paso
        spinner2.setValueFactory(valueFactory2);

        SpinnerValueFactory<Double> valueFactory3 = new SpinnerValueFactory.DoubleSpinnerValueFactory(
            Double.MIN_VALUE, Double.MAX_VALUE, 0.0, 0.1); // Valores mínimos, máximos, valor inicial y paso
        spinner3.setValueFactory(valueFactory3);




    }
}
    
