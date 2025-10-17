package pupr.capstone.myapplication;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {
    public static final String ACTION_SEND_MAINTENANCE_EMAIL = "pupr.capstone.myapplication.SEND_MAINTENANCE_EMAIL";
    public static final String EXTRA_RECIPIENTS = "recipients";
    public static final String EXTRA_SUBJECT    = "subject";
    public static final String EXTRA_BODY       = "body";


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        if (ACTION_SEND_MAINTENANCE_EMAIL.equals(intent.getAction())) {
            final String recipients = intent.getStringExtra(EXTRA_RECIPIENTS);
            final String subject    = intent.getStringExtra(EXTRA_SUBJECT);
            final String body       = intent.getStringExtra(EXTRA_BODY);

            new Thread(() -> {
                try {
                    JavaMailUtil.sendMail(recipients, subject, body);
                    Log.d("NotificationReceiver", "Correo enviado a: " + recipients);
                } catch (Exception e) {
                    Log.e("NotificationReceiver", "Error enviando correo: " + e.getMessage(), e);
                }
            }).start();
        }
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "maintenance_channel")
                .setSmallIcon(R.drawable.default_car_image)  // make sure this icon exists in res/drawable
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify((int) System.currentTimeMillis(), builder.build());
    }
}

