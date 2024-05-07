package com.app.controllers.Ventas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class bascula implements SerialPortEventListener {

    public static HashMap ListPorts = new HashMap();
    public CommPortIdentifier portId;
    public SerialPort serialPort;
    public OutputStream output;
    public BufferedReader reader;

    private static final int SCALE_READY = 0;
    private static final int SCALE_READING = 1;
    private static final int SCALE_READINGDECIMALS = 3;

    private double weightBuffer;
    private double weightDecimals;
    private int scaleStatus;
    private int sign = 1;

    public static HashMap getPorts() {
        CommPortIdentifier port;
        Enumeration<?> puertos = CommPortIdentifier.getPortIdentifiers();
        while (puertos.hasMoreElements()) {
            port = (CommPortIdentifier) puertos.nextElement();
            if (port.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                System.out.println("Lista de puertos: " + port.getName());
                ListPorts.put(port.getName(), port);
            }
        }
        return ListPorts;
    }

  public double obtenerPeso() {
    try {
      System.out.println("Intentando abrir el puerto serial...");
      portId = (CommPortIdentifier) ListPorts.get("COM3");
      serialPort = (SerialPort) portId.open("Báscula Torrey", 2000);
      System.out.println("Puerto serial abierto.");

      serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);
      output = serialPort.getOutputStream();
      reader = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));

      // Aquí es donde debes agregar estas líneas
      

      System.out.println("JALANDO");

    //   scaleStatus = SCALE_READY;
    //   weightBuffer = 0.0;
    //   weightDecimals = 1.0;
    //   sign = 1;

      write(new byte[] { 0x50 }); // 'P'
      write(new byte[] { 0x000D });

      Thread.sleep(1000);

      if (scaleStatus == SCALE_READY) {
        double weight = (weightBuffer / weightDecimals) * sign;
        System.out.println("Peso obtenido: " + weight);
        resetValues();
        return weight;
      } else {
        System.out.println("No se pudo obtener el peso.");
        resetValues();
        return 404.0;
      }
    } catch (Exception e) {
      System.out.println("Error al obtener el peso: " + e.getMessage());
      e.printStackTrace();
      return 404.0;
    } finally {
      try {
        System.out.println("Cerrando el puerto serial...");
        serialPort.close();
        System.out.println("Puerto serial cerrado.");
      } catch (Exception e) {
        System.out.println("Error al cerrar el puerto serial: " + e.getMessage());
      }
    }
  }

    private void write(byte[] data) {
        System.err.println("Aca empieza el metodo write");
        try {
            output.write(data);
            output.flush();
        } catch (IOException e) {
            System.out.println("Error al escribir en el puerto serial");
        }
    }

    private void resetValues() {
        scaleStatus = SCALE_READY;
        weightBuffer = 0.0;
        weightDecimals = 1.0;
        sign = 1;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        System.out.println("Ejecutando serialEvent()");
        switch (event.getEventType()) {
            case SerialPortEvent.DATA_AVAILABLE:
                try {
                    while (reader.ready()) {
                        int b = reader.read();
                        System.out.println("Byte leído: 0x" + Integer.toHexString(b));
                        if (b == 0x000D) { // CR ASCII
                            System.out.println("Recibido carácter CR");
                            synchronized (this) {
                                scaleStatus = SCALE_READY;
                                System.out.println("scaleStatus = SCALE_READY");
                                notifyAll();
                            }
                        } else if ((b > 0x002F && b < 0x003A) || b == 0x002E || b == 0x002D) {
                            synchronized (this) {
                                if (scaleStatus == SCALE_READY) {
                                    scaleStatus = SCALE_READING;
                                    System.out.println("scaleStatus = SCALE_READING");
                                }
                                if (b == 0x002D) { // Signo negativo
                                    System.out.println("Signo negativo detectado");
                                    sign = -1;
                                } else if (b == 0x002E) { // Punto decimal
                                    System.out.println("Punto decimal detectado");
                                    scaleStatus = SCALE_READINGDECIMALS;
                                    weightDecimals = 1.0;
                                } else {
                                    if (scaleStatus == SCALE_READINGDECIMALS) {
                                        System.out.println("Procesando decimal, weightDecimals = " + weightDecimals);
                                        weightDecimals *= 10.0;
                                    }
                                    System.out.println("Procesando dígito: " + (char) b);
                                    weightBuffer = (weightBuffer * 10.0) + (b - 0x0030);
                                }
                            }
                        } else {
                            System.out.println("Byte no reconocido: 0x" + Integer.toHexString(b));
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Error al leer del puerto serial");
                }
                break;
            default:
                break;
        }
    }
}