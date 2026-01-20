package pupr.capstone.myapplication;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class NotificationHelper {

    private final Context context;

    public NotificationHelper(Context context) {
        this.context = context;
    }

    // Create notification channel (only once, usually in MainActivity)
    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "maintenance_channel",
                    "Mantenimientos Programados",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleMaintenanceEmailAt7am(Context context,
                                                     LocalDate date,       // nextMaintenanceDate
                                                     String recipients,    // "correo1@x.com,correo2@x.com"
                                                     String subject,       // opcional (ver comentario abajo)
                                                     String body) {        // opcional (ver comentario abajo)

        // 7:00 AM en la zona local del dispositivo
        ZonedDateTime zdt = ZonedDateTime.of(date, LocalTime.of(7, 0), ZoneId.systemDefault());
        long triggerAtMillis = zdt.toInstant().toEpochMilli();
        //long triggerAtMillis = System.currentTimeMillis() + 10_000; // 10 000 ms = 10 s
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(NotificationReceiver.ACTION_SEND_MAINTENANCE_EMAIL);
        intent.putExtra(NotificationReceiver.EXTRA_RECIPIENTS, recipients);
        intent.putExtra(NotificationReceiver.EXTRA_SUBJECT, subject);
        intent.putExtra(NotificationReceiver.EXTRA_BODY, body);

// ✅ Agregar título y mensaje para que la notificación no salga vacía
        intent.putExtra("title", "Alerta de Mantenimiento");
        intent.putExtra("message", "Para mas información favor verificar correo electronico!!!");


        int requestCode = (int) (triggerAtMillis % Integer.MAX_VALUE);

        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            // Programa alarma exacta (incluso en Doze)
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
        }
    }
    // Schedule notification
    @SuppressLint("ScheduleExactAlarm")
    public void scheduleNotification(long timeInMillis, String title, String message) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            Toast.makeText(context, "⏰ Notificación programada para: " + new Date(timeInMillis), Toast.LENGTH_LONG).show();
        }
    }
}