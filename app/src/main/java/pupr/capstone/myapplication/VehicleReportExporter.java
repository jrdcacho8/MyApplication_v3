package pupr.capstone.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Exporta un INFORME DE MANTENIMIENTO tal como el diseño del ejemplo:
 *  - Encabezado
 *  - "Marca Modelo Año"
 *  - "Tablilla 000-000"
 *  - Tabla: Fecha | Servicio | Costo
 *  - Total
 */
public final class VehicleReportExporter {

    private VehicleReportExporter() {}




    /**
     * Exporta leyendo los servicios de la BD por EMAIL + LICENSE_PLATE.
     * El PDF se guarda en Descargas vía MediaStore (Android 10+) o en /Downloads (<=9).
     */
    public static Uri export(Context ctx,
                             String userEmail,
                             String brand,
                             String model,
                             Integer year,
                             String plate,
                             Bitmap image,
                             Date startDate,   // <— NUEVO
                             Date endDate      // <— NUEVO
    ) throws IOException {

        // 1) Cargar datos de servicios desde la BD (FILTRADO)
        List<ServiceRow> rows = queryServices(userEmail, plate, startDate, endDate);

        // 2) Construir PDF
        PdfDocument pdf = buildPdf(userEmail, brand, model, year, plate, image, rows);
        try {
            String fname = "InformeMantenimiento_" + clean(brand) + "_" + clean(model)
                    + "_" + clean(plate) + "_" + System.currentTimeMillis() + ".pdf";
            return saveToDownloads(ctx, pdf, fname);
        } finally {
            pdf.close();
        }
    }


    /** Abre el PDF en un visor. */
    public static void openPdf(Context ctx, Uri uri) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, "application/pdf")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            ctx.startActivity(Intent.createChooser(i, "Abrir PDF con…"));
        } catch (Exception e) {
            Toast.makeText(ctx, "No se pudo abrir el PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ================== BD: CONSULTA SERVICIOS ==================

    /** Consulta SERVICIO por EMAIL y LICENSE_PLATE. Ajusta nombres si difieren en tu DB. */
    private static List<ServiceRow> queryServices(String email, String plate, Date start, Date end) {
        List<ServiceRow> out = new ArrayList<>();

        // Si alguna de las fechas es null, no usamos filtro por rango
        final boolean useRange = (start != null && end != null);

        String base = "SELECT DATE_SERVICE, SERVICE_TYPE, COMPANY_SERVICE, COST_SERVICE " +
                "FROM SERVICE WHERE EMAIL = ? AND LICENSE_PLATE = ? ";
        String order = " ORDER BY DATE_SERVICE ASC";

        String sql = useRange
                ? base + " AND DATE_SERVICE BETWEEN ? AND ? " + order
                : base + order;

        try (Connection c = new MyJDBC().obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {

            int idx = 1;
            ps.setString(idx++, email);
            ps.setString(idx++, plate);

            if (useRange) {
                // Suponiendo que DATE_SERVICE es tipo DATE (sin hora) en MySQL:
                // java.sql.Date ignora horas; BETWEEN será inclusivo (yyyy-MM-dd)
                ps.setDate(idx++, new java.sql.Date(start.getTime()));
                ps.setDate(idx++, new java.sql.Date(end.getTime()));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date d = rs.getDate("DATE_SERVICE");
                    String type = rs.getString("SERVICE_TYPE");
                    String company = rs.getString("COMPANY_SERVICE");
                    BigDecimal cost = rs.getBigDecimal("COST_SERVICE");
                    out.add(new ServiceRow(d != null ? new Date(d.getTime()) : null,
                            safe(company), safe(type),
                            cost != null ? cost : BigDecimal.ZERO));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }


    // ================== PDF RENDER ==================

    private static PdfDocument buildPdf(String userEmail,
                                        String brand,
                                        String model,
                                        Integer year,
                                        String plate,
                                        Bitmap image,
                                        List<ServiceRow> rows) {

        // A4 a 72dpi: 595 x 842
        int pageW = 595, pageH = 842, margin = 40;
        PdfDocument pdf = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageW, pageH, 1).create();
        PdfDocument.Page page = pdf.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Paints
        Paint title = makePaint(Color.BLACK, 18f, true);
        Paint subtitle = makePaint(Color.BLACK, 16f, true);
        Paint label = makePaint(Color.DKGRAY, 12f, false);
        Paint normal = makePaint(Color.BLACK, 12f, false);
        Paint bold = makePaint(Color.BLACK, 12f, true);
        Paint line = new Paint(); line.setColor(0xFFBDBDBD); line.setStrokeWidth(2f);

        // Encabezado
        int y = margin + 6;

        // Título centrado con línea
        String header = "INFORME DE MANTENIMIENTO";
        drawCenteredText(canvas, header, pageW/2, y, title);
        y += 14;
        canvas.drawLine(margin, y, pageW - margin, y, line);
        y += 18;

        // Nombre del vehículo: "Toyota Tacoma 2015"
        String vehTitle = buildVehicleTitle(brand, model, year);
        drawCenteredText(canvas, vehTitle, pageW/2, y, subtitle);
        y += 18;

        // Tablilla centrada
        String plateText = "Tablilla " + safe(plate);
        drawCenteredText(canvas, plateText, pageW/2, y, label);
        y += 18;

        // header-info extra (opcional: usuario/fecha)
        // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        // drawCenteredText(canvas, "Generado: " + sdf.format(new Date()), pageW/2, y, label);
        // y += 12;

        // Separador
        canvas.drawLine(margin, y, pageW - margin, y, line);
        y += 16;

        // Tabla
        // Columnas: Fecha | Servicio | Costo (alinear costo a la derecha)
        int colFechaX = margin;
        int colServicioX1 = margin + 150;     // espacio para fecha
        int colServicioX2 = margin + 300;
        int colCostoRight = pageW - margin;  // dibujamos costo alineado a la derecha

        // Encabezados de tabla
        canvas.drawText("Fecha", colFechaX, y, bold);
        canvas.drawText("Servicio", colServicioX1, y, bold);
        canvas.drawText("Compañía", colServicioX2, y, bold);
        drawRightText(canvas, "Costo", colCostoRight, y, bold);
        y += 10;
        canvas.drawLine(margin, y, pageW - margin, y, line);
        y += 12;

        // Filas
        NumberFormat money = NumberFormat.getCurrencyInstance(Locale.US); // $xxx.yy
        money.setMinimumFractionDigits(2);
        money.setMaximumFractionDigits(2);

        SimpleDateFormat sdfEs = new SimpleDateFormat("MMMM d, yyyy", new Locale("es", "ES"));

        BigDecimal total = BigDecimal.ZERO;
        int rowGap = 8;
        int maxServicioWidth = colCostoRight - colServicioX1 - 8;

        if (rows == null || rows.isEmpty()) {
            canvas.drawText("No hay servicios registrados", colServicioX1, y, normal);
            y += 18;
        } else {
            for (ServiceRow r : rows) {
                // Fecha formateada
                String fechaTxt = r.date != null ? capitalizeFirst(sdfEs.format(r.date)) : "";

                // Servicio (posible multilínea)
                List<String> servicioLines = breakTextLines(r.service, bold, maxServicioWidth);

                // Compañía (posible multilínea) 🌟 NEW: Calculate lines for Company
                List<String> companyLines = breakTextLines(r.company, bold, colCostoRight - colServicioX2 - 8);
                // Note: The max width for Company is the space up to Cost's right edge (colCostoRight)

                // Costo
                String costTxt = money.format(r.cost != null ? r.cost : BigDecimal.ZERO);

                // Alturas usando FontMetrics (más preciso que textSize)
                Paint.FontMetricsInt fmNormal = normal.getFontMetricsInt();
                Paint.FontMetricsInt fmBold   = bold.getFontMetricsInt();
                int lineHNormal = fmNormal.descent - fmNormal.ascent;
                int lineHBold   = fmBold.descent   - fmBold.ascent;

                // altura de la fila = max(fecha, servicio multi-línea, compañía multi-línea) 🌟 UPDATED: Max with Company
                int maxLines = Math.max(servicioLines.size(), companyLines.size());
                int rowHeight = Math.max(lineHNormal, maxLines * lineHBold);

                // Dibujar: Fecha (alineada arriba de la fila)
                int baseFecha = y - fmNormal.ascent;
                canvas.drawText(fechaTxt, colFechaX, baseFecha, normal);

                // Servicio multilínea
                int sy = y - fmBold.ascent;  // línea base para el texto del servicio/compañía
                int syService = sy;
                for (String part : servicioLines) {
                    canvas.drawText(part, colServicioX1, syService, bold);
                    syService += lineHBold;
                }

                // Compañía multilínea 🌟 NEW: Draw Company Text
                int syCompany = sy;
                for (String part : companyLines) {
                    canvas.drawText(part, colServicioX2, syCompany, bold);
                    syCompany += lineHBold;
                }
                total = total.add(r.cost != null ? r.cost : BigDecimal.ZERO);
                // Costo (alineado a la derecha, altura de la primera línea)
                drawRightText(canvas, costTxt, colCostoRight, baseFecha, normal);

                // --- calcular parte baja real de la fila ---
                int bottomFecha   = baseFecha + fmNormal.descent;             // borde inferior visual del texto de fecha
                int bottomServicio = (sy - lineHBold) + fmBold.descent;       // borde inferior visual de la última línea del servicio
                int rowBottom     = Math.max(bottomFecha, bottomServicio);

                // --- dibujar separador bien por debajo del texto ---
                int separatorPadding = 6;   // espacio bajo la fila
                int sepY = rowBottom + separatorPadding;
                canvas.drawLine(margin, sepY, pageW - margin, sepY, line);

                // próxima fila: deja un gap adicional para respirar
                int extraGap = 6;
                y = sepY + extraGap;
            }

        }

        // Total
        y += 6;
        Paint totalPaint = makePaint(Color.BLACK, 13f, true);
        canvas.drawText("Total", colServicioX1, y, totalPaint);
        drawRightText(canvas, money.format(total), colCostoRight, y, totalPaint);

        pdf.finishPage(page);
        return pdf;
    }

    // ================== Helpers de dibujo ==================

    private static Paint makePaint(int color, float size, boolean bold) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setTextSize(size);
        p.setTypeface(bold ? Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                : Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        return p;
    }

    private static void drawCenteredText(Canvas c, String text, int centerX, int y, Paint p) {
        if (TextUtils.isEmpty(text)) return;
        float w = p.measureText(text);
        c.drawText(text, centerX - w / 2f, y, p);
    }

    private static void drawRightText(Canvas c, String text, int rightX, int y, Paint p) {
        if (TextUtils.isEmpty(text)) return;
        float w = p.measureText(text);
        c.drawText(text, rightX - w, y, p);
    }

    private static List<String> breakTextLines(String text, Paint p, int maxWidthPx) {
        List<String> lines = new ArrayList<>();
        if (text == null) return lines;
        int start = 0;
        int len = text.length();
        while (start < len) {
            int count = p.breakText(text, start, len, true, maxWidthPx, null);
            if (count <= 0) break;
            lines.add(text.substring(start, start + count));
            start += count;
        }
        if (lines.isEmpty()) lines.add("");
        return lines;
    }

    private static String buildVehicleTitle(String brand, String model, Integer year) {
        StringBuilder sb = new StringBuilder();
        if (!safe(brand).isEmpty()) sb.append(brand);
        if (!safe(model).isEmpty()) sb.append(sb.length() > 0 ? " " : "").append(model);
        if (year != null && year > 0) sb.append(" ").append(year);
        return sb.length() == 0 ? "Vehículo" : sb.toString();
    }

    private static String capitalizeFirst(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0,1).toUpperCase(new Locale("es","ES")) + s.substring(1);
        // (SimpleDateFormat en español devuelve "enero"; esto lo deja "Enero")
    }

    // ================== Guardado archivo ==================

    private static Uri saveToDownloads(Context ctx, PdfDocument pdf, String fileName) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
            values.put(MediaStore.Downloads.IS_PENDING, 1);

            Uri uri = ctx.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) throw new IOException("No se pudo crear el archivo en Descargas.");

            try (OutputStream os = ctx.getContentResolver().openOutputStream(uri)) {
                pdf.writeTo(os);
            }

            values.clear();
            values.put(MediaStore.Downloads.IS_PENDING, 0);
            ctx.getContentResolver().update(uri, values, null, null);
            return uri;
        } else {
            File dir = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS);
            if (!dir.exists()) dir.mkdirs();
            File out = new File(dir, fileName);
            try (OutputStream os = new FileOutputStream(out)) {
                pdf.writeTo(os);
            }
            return FileProvider.getUriForFile(ctx,
                    ctx.getPackageName() + ".fileprovider", out);
        }
    }

    private static String safe(String s) { return s == null ? "" : s; }

    private static String clean(String s) {
        s = safe(s);
        return s.replaceAll("[^\\w\\-]+", "_");
    }

    // ================== Modelo interno ==================

    private static class ServiceRow {
        final Date date;
        final String service, company;
        final BigDecimal cost;
        ServiceRow(Date d, String b,String s, BigDecimal c) {
            this.date = d;this.company=b; this.service = s; this.cost = c;
        }
    }
}
