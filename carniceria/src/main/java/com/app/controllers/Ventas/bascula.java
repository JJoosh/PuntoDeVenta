package com.app.controllers.Ventas;

import com.ibm.icu.math.BigDecimal;
import com.fazecast.jSerialComm.*;

public class bascula {
    private static SerialPort serialPort;

    public static BigDecimal obtenerPeso() {
        try {
            // Obtener una lista de puertos seriales disponibles
            SerialPort[] puertosDisponibles = SerialPort.getCommPorts();

            // Seleccionar el puerto serial de la báscula (en este ejemplo, asumimos que es el primer puerto disponible)
            serialPort = puertosDisponibles[2];

            // Abrir el puerto serial
            serialPort.openPort();

            // Configurar la tasa de baudios y otros parámetros según la báscula Torrey OPCR40
            serialPort.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);

            // Leer los datos del puerto serial
            byte[] bufferDatos = new byte[serialPort.bytesAvailable()];
            int bytesLeidos = serialPort.readBytes(bufferDatos, bufferDatos.length);

            // Convertir los datos a una cadena de texto
            String datosRecibidos = new String(bufferDatos, 0, bytesLeidos);

            // Procesar los datos recibidos para obtener el peso
            BigDecimal peso = procesarDatos(datosRecibidos);

            // Cerrar el puerto serial
            serialPort.closePort();

            return peso;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static BigDecimal procesarDatos(String datosRecibidos) {
        // La báscula Torrey OPCR40 envía los datos en el formato: "ST,GS, 12.345,kg"
        String[] partes = datosRecibidos.split(",");
        if (partes.length == 4 && partes[0].equals("ST") && partes[1].equals("GS")) {
            String pesoStr = partes[2];
            try {
                return new BigDecimal(pesoStr);
            } catch (NumberFormatException e) {
                // Manejar el caso en el que el valor del peso no sea un número válido
                e.printStackTrace();
            }
        }

        // Si los datos no tienen el formato esperado, devolver un valor predeterminado
        return new BigDecimal("0.0");
    }
}