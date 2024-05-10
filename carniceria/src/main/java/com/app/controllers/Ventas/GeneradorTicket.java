
package com.app.controllers.Ventas;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import com.app.models.DetallesVenta;
import com.app.models.Ventas;

public class GeneradorTicket {
    private static final String LOGO_IMAGE = "ticket/logo.jpg";
    private static final String TICKET_FILE = "ticket/printticket.pdf";
    private static final Font FUENTE = new Font(Font.FontFamily.HELVETICA, 9);
    private static final Font FUENTE_DOS = new Font(Font.FontFamily.HELVETICA, 11);
    private static final Font NEGRITA = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
    private static final Font NEGRITA_DOS = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public static void generarTicketVenta(Ventas venta) {
        try {
            
            Document document = new Document(new Rectangle(200f, 500f));
            document.setMargins(3, 2, 10, 3);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(TICKET_FILE));
            document.open();

            // Agregar logo
            Image logo = Image.getInstance(LOGO_IMAGE);
            logo.scalePercent(10, 10);
            logo.setAlignment(Element.ALIGN_CENTER);
            document.add(logo);

            // Agregar encabezado
            Paragraph encabezado = new Paragraph("Domicilio aqui", FUENTE);
            encabezado.setAlignment(Element.ALIGN_CENTER);
            encabezado.setLeading(1f, 1f);
            document.add(encabezado);

            // Agregar información de la venta
            Paragraph textoIzquierda = new Paragraph("No. venta: " + venta.getTicket(), FUENTE);
            textoIzquierda.setLeading(1f, 1f);
            document.add(textoIzquierda);

            textoIzquierda = new Paragraph("Fecha: " + DATE_FORMAT.format(venta.getFecha()), FUENTE);
            textoIzquierda.setLeading(1f, 1f);
            document.add(textoIzquierda);

            // Agregar línea separadora
            Paragraph lineaSeparadora = new Paragraph(
                    "________________________________________________________________________________",
                    new Font(Font.FontFamily.COURIER, 4, Font.BOLD));
            lineaSeparadora.setAlignment(Element.ALIGN_CENTER);
            lineaSeparadora.setLeading(1f, 1f);
            document.add(lineaSeparadora);

            // Agregar tabla de detalles de venta
            PdfPTable tablaDetalles = new PdfPTable(3);
            tablaDetalles.setHorizontalAlignment(0);
            tablaDetalles.getDefaultCell().setBorder(0);
            tablaDetalles.setWidthPercentage(new float[] { 30, 110, 50 }, new Rectangle(190f, 20f));

            PdfPCell cellDerecha = new PdfPCell();
            cellDerecha.setHorizontalAlignment(2);
            cellDerecha.setBorder(0);
            cellDerecha.setLeading(0.5f, 0.8f);
            cellDerecha.setPaddingRight(4f);

            PdfPCell cellIzquierda = new PdfPCell();
            cellIzquierda.setLeading(0.5f, 0.8f);
            cellIzquierda.setBorder(0);

            tablaDetalles.addCell(new Phrase("Cant:", FUENTE));
            tablaDetalles.addCell(new Phrase("Descripción", FUENTE));
            tablaDetalles.addCell(new Phrase("Importe $", FUENTE));

            for (DetallesVenta detalle : venta.getDetalles()) {
                cellDerecha.setPhrase(new Phrase(String.valueOf(detalle.getCantidad()), FUENTE));
                tablaDetalles.addCell(cellDerecha);

                String descripcionProducto = detalle.getProducto().getNombre();
                cellIzquierda.setPhrase(new Phrase(descripcionProducto, FUENTE));
                tablaDetalles.addCell(cellIzquierda);

                cellDerecha
                        .setPhrase(new Phrase(DecimalFormat.getCurrencyInstance().format(detalle.getTotal()), FUENTE));
                tablaDetalles.addCell(cellDerecha);
            }

            document.add(tablaDetalles);
            document.add(lineaSeparadora);

            // Agregar total de la venta
            PdfPTable tablaTotal = new PdfPTable(3);
            tablaTotal.setWidthPercentage(new float[] { 70, 10, 50 }, new Rectangle(130f, 50f));
            tablaTotal.setHorizontalAlignment(2);
            tablaTotal.getDefaultCell().setBorder(0);

            cellIzquierda.setPhrase(new Phrase("TOTAL:", FUENTE));
            tablaTotal.addCell(cellIzquierda);
            tablaTotal.addCell("");
            cellDerecha.setPhrase(new Phrase(DecimalFormat.getCurrencyInstance().format(venta.getTotal()), FUENTE));
            tablaTotal.addCell(cellDerecha);

            document.add(tablaTotal);

            // Agregar mensaje de agradecimiento
            Paragraph mensajeAgradecimiento = new Paragraph("\n¡GRACIAS, VUELVA PRONTO!", FUENTE);
            mensajeAgradecimiento.setAlignment(Element.ALIGN_CENTER);
            mensajeAgradecimiento.setLeading(1f, 1f);
            document.add(mensajeAgradecimiento);

            document.close();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
