package com.app.controllers.Ventas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import javafx.application.Platform;

public class bascula implements SerialPortEventListener {
    private static final int TIMEOUT = 2000; // Tiempo de espera en milisegundos
    private SerialPort serialPort;
    private OutputStream outputStream;
    private BufferedReader reader;
    private VentasController ventasController;

    public bascula(String portName) throws PortInUseException, NoSuchPortException,
            UnsupportedCommOperationException, IOException, TooManyListenersException {
        openSerialPort(portName);
    }

    private void openSerialPort(String portName) throws PortInUseException, NoSuchPortException,
            UnsupportedCommOperationException, IOException, TooManyListenersException {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if (portIdentifier.getPortType() == CommPortIdentifier.PORT_SERIAL) {
            serialPort = (SerialPort) portIdentifier.open("Bascula", TIMEOUT);
            serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
            outputStream = serialPort.getOutputStream();
            reader = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));

            System.out.println("Puerto serial abierto: " + portName);
        } else {
            throw new IllegalArgumentException("El puerto " + portName + " no es un puerto serial.");
        }
    }

    public void close() {
        if (serialPort != null) {
            serialPort.close();
            System.out.println("Puerto serial cerrado.");
        }
    }

    public void sendCommand(String command) {
        try {
            outputStream.write(command.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al enviar el comando a la báscula: " + e.getMessage());
        }
    }

    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                String inputLine = reader.readLine();
                System.out.println("Respuesta de la báscula: " + inputLine);

                // Eliminar la unidad "kg" de la respuesta de la báscula
                String peso = inputLine.replace("kg", "").trim();

                Platform.runLater(() -> {
                    if (ventasController != null) {
                        ventasController.actualizarPesoDesdeBascula(peso);
                    } else {
                        System.out.println("El controlador de ventas no está configurado.");
                    }
                });
            } catch (IOException e) {
                System.out.println("Error al leer la respuesta de la báscula: " + e.getMessage());
            }
        }
    }

    public void reopen(String portName) {
        close();
        try {
            openSerialPort(portName);
        } catch (PortInUseException | NoSuchPortException | UnsupportedCommOperationException | IOException
                | TooManyListenersException e) {
            System.out.println("Error al volver a abrir el puerto serial: " + e.getMessage());
        }
    }

    public void setVentasController(VentasController ventasController) {
        this.ventasController = ventasController;
    }
}