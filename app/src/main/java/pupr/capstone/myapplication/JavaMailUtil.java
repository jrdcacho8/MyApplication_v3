// JavaMailUtil.java
package pupr.capstone.myapplication;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class JavaMailUtil {

    public static void sendMail(String recipient) throws Exception {
        // Compatibilidad con tu método actual
        sendMail(recipient, "Recordatorio de mantenimiento",
                "Saludos,\nEl vehículo necesita mantenimiento. ¡No olvides revisarlo!");
    }

    // NUEVO: método con asunto y cuerpo personalizados
    public static void sendMail(String recipients, String subject, String body) throws Exception {
        System.out.println("Preparing to send email...");
        Properties properties = new Properties();

        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        String myAccountEmail = "aldia.app02@gmail.com";
        String password = "Pelusa123"; // App password de Gmail (no publiques esto en producción)

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(myAccountEmail, password);
            }
        });

        Message message = prepareMessage(session, myAccountEmail, recipients, subject, body);
        Transport.send(message);
        System.out.println("✅ Correo enviado exitosamente!");
    }

    private static Message prepareMessage(Session session, String from, String recipients,
                                          String subject, String body) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
            message.setSubject(subject);
            message.setText(body);
            return message;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
