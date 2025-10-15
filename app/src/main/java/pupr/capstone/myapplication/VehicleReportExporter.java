package pupr.capstone.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Genera y guarda un PDF con la información de un vehículo.
 * Guarda en /Downloads mediante MediaStore (Android 10+ sin permisos).
 *
 * Uso típico:
 *   Uri uri = VehicleReportExporter.export(
 *       context, userEmail, brand, model, year, plate, imageBitmap
 *   );
 *   VehicleReportExporter.openPdf(context, uri); // opcional
 */
public final class VehicleReportExporter {

    private VehicleReportExporter() { }

    // =============== APIS PÚBLICAS =================

    /** Exporta usando un objeto Vehicle (usará brand, model, plate, year si lo tienes y la imagen si viene). */
    public static Uri export(Context ctx, String userEmail, Vehicle v) throws IOException {
        String brand = safe(v.getBrand());
        String model = safe(v.getModel());        // puede ser null en tu constructor
        String plate = safe(v.getLicense_plate());
        int year = v.getYear();                   // si no lo tienes, quedará 0
        Bitmap image = v.getImageBitmap();        // puede ser null

        return export(ctx, userEmail, brand, model, year, plate, image);
    }

    /** Exporta con parámetros sueltos (útil si no tienes un Vehicle completo). */
    public static Uri export(Context ctx,
                             String userEmail,
                             String brand,
                             String model,
                             Integer year, // admite null
                             String plate,
                             Bitmap image) throws IOException {

        PdfDocument pdf = buildPdf(userEmail, brand, model, year, plate, image);
        try {
            return saveToDownloads(ctx, pdf,
                    "InformeVehiculo_" + clean(brand) + "_" + clean(model) + "_" + System.currentTimeMillis() + ".pdf");
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

    // =============== IMPLEMENTACIÓN =================

    private static PdfDocument buildPdf(String userEmail,
                                        String brand,
                                        String model,
                                        Integer year,
                                        String plate,
                                        Bitmap image) {

        // Tamaño A4 en puntos (72dpi): 595x842
        PdfDocument pdf = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdf.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint title = new Paint();
        title.setColor(Color.BLACK);
        title.setTextSize(20f);
        title.setFakeBoldText(true);

        Paint label = new Paint();
        label.setColor(Color.DKGRAY);
        label.setTextSize(12f);

        Paint value = new Paint();
        value.setColor(Color.BLACK);
        value.setTextSize(14f);

        Paint line = new Paint();
        line.setColor(Color.LTGRAY);
        line.setStrokeWidth(2f);

        int margin = 40;
        int y = 60;

        // Encabezado
        canvas.drawText("Informe de Vehículo", margin, y, title);
        y += 24;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        canvas.drawText("Generado: " + sdf.format(new Date()), margin, y, label);
        y += 18;
        canvas.drawText("Usuario: " + safe(userEmail), margin, y, label);

        canvas.drawLine(margin, y + 14, 595 - margin, y + 14, line);
        y += 40;

        // Datos
        canvas.drawText("Marca:", margin, y, label);
        canvas.drawText(safe(brand), margin + 120, y, value);

        canvas.drawText("Modelo:", margin, y += 20, label);
        canvas.drawText(safe(model), margin + 120, y, value);

        if (year != null && year > 0) {
            canvas.drawText("Año:", margin, y += 20, label);
            canvas.drawText(String.valueOf(year), margin + 120, y, value);
        }

        canvas.drawText("Tablilla:", margin, y += 20, label);
        canvas.drawText(safe(plate), margin + 120, y, value);

        // Imagen (opcional)
        if (image != null) {
            int imgTop = y + 30;
            int imgLeft = margin;
            int maxW = 220;
            int maxH = 140;

            Bitmap scaled = scaleToBox(image, maxW, maxH);
            canvas.drawBitmap(scaled, imgLeft, imgTop, null);
        }

        pdf.finishPage(page);
        return pdf;
    }

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
            // Android 9 o menor: guardar como archivo físico
            File dir = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS);
            if (!dir.exists()) dir.mkdirs();
            File out = new File(dir, fileName);
            try (OutputStream os = new FileOutputStream(out)) {
                pdf.writeTo(os);
            }
            // Recomendado usar FileProvider al abrir:
            return FileProvider.getUriForFile(ctx,
                    ctx.getPackageName() + ".fileprovider", out);
        }
    }

    private static Bitmap scaleToBox(Bitmap src, int maxW, int maxH) {
        int w = src.getWidth();
        int h = src.getHeight();
        float ratio = Math.min(maxW / (float) w, maxH / (float) h);
        if (ratio >= 1f) return src;
        int nw = Math.round(w * ratio);
        int nh = Math.round(h * ratio);
        return Bitmap.createScaledBitmap(src, nw, nh, true);
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String clean(String s) {
        s = safe(s);
        return s.replaceAll("[^\\w\\-]+", "_");
    }
}
