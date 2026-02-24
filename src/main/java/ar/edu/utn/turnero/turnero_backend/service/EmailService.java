package ar.edu.utn.turnero.turnero_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Servicio para envío de emails (recuperación de contraseña, notificaciones de reserva).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@turnero.com}")
    private String fromEmail;

    @Value("${app.mail.from-name:Turnero Canchas}")
    private String fromName;

    /**
     * Envía email con token de recuperación de contraseña.
     */
    public void enviarEmailRecuperacion(String destino, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromName + " <" + fromEmail + ">");
            helper.setTo(destino);
            helper.setSubject("🔒 Recuperación de contraseña - Turnero Canchas");
            helper.setText(buildHtmlRecuperacion(token), true);
            mailSender.send(message);
            log.info("Email de recuperación enviado a: {}", destino);
        } catch (MessagingException e) {
            log.error("Error al enviar email de recuperación a {}: {}", destino, e.getMessage());
            throw new RuntimeException("Error al enviar el email de recuperación");
        }
    }

    /**
     * Envía confirmación de reserva al cliente.
     */
    public void enviarConfirmacionReserva(String destino, String nombreCliente,
                                           String cancha, String fecha,
                                           String horaInicio, String horaFin,
                                           String predio) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromName + " <" + fromEmail + ">");
            helper.setTo(destino);
            helper.setSubject("⚽ Reserva confirmada - " + cancha);
            helper.setText(buildHtmlConfirmacion(nombreCliente, cancha, fecha, horaInicio, horaFin, predio), true);
            mailSender.send(message);
            log.info("Confirmación de reserva enviada a: {}", destino);
        } catch (MessagingException e) {
            log.error("Error al enviar confirmación de reserva: {}", e.getMessage());
            // No lanzar excepción - la reserva igual se creó
        }
    }

    /**
     * Envía notificación de cancelación de reserva.
     */
    public void enviarCancelacionReserva(String destino, String nombreCliente,
                                          String cancha, String fecha,
                                          String horaInicio, String horaFin) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromName + " <" + fromEmail + ">");
            helper.setTo(destino);
            helper.setSubject("❌ Reserva cancelada - " + cancha);
            helper.setText(buildHtmlCancelacion(nombreCliente, cancha, fecha, horaInicio, horaFin), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Error al enviar email de cancelación: {}", e.getMessage());
        }
    }

    private String buildHtmlRecuperacion(String token) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head><meta charset="UTF-8"><title>Recuperación de contraseña</title></head>
            <body style="margin:0;padding:0;font-family:'Segoe UI',sans-serif;background:#f5f5f5;">
              <div style="max-width:600px;margin:30px auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 4px 12px rgba(0,0,0,0.1);">
                <div style="background:#2e7d32;padding:24px;text-align:center;">
                  <h1 style="color:#fff;margin:0;font-size:22px;">⚽ Turnero Canchas</h1>
                </div>
                <div style="padding:32px;color:#333;">
                  <h2 style="color:#2e7d32;">Recuperación de contraseña</h2>
                  <p>Recibimos una solicitud para restablecer tu contraseña.</p>
                  <p>Tu código de verificación es:</p>
                  <div style="background:#e8f5e9;border-radius:8px;padding:20px;text-align:center;margin:24px 0;">
                    <span style="font-size:36px;font-weight:bold;color:#2e7d32;letter-spacing:10px;font-family:monospace;">%s</span>
                  </div>
                  <p style="color:#666;font-size:14px;">Este código expira en <strong>15 minutos</strong>. No lo compartas con nadie.</p>
                  <p style="color:#666;font-size:14px;">Si no solicitaste este cambio, podés ignorar este email.</p>
                </div>
                <div style="background:#f5f5f5;padding:16px;text-align:center;font-size:12px;color:#999;">
                  © 2024 Turnero Canchas de Fútbol
                </div>
              </div>
            </body>
            </html>
            """.formatted(token);
    }

    private String buildHtmlConfirmacion(String nombre, String cancha, String fecha,
                                          String horaInicio, String horaFin, String predio) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head><meta charset="UTF-8"></head>
            <body style="font-family:'Segoe UI',sans-serif;background:#f5f5f5;">
              <div style="max-width:600px;margin:30px auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 4px 12px rgba(0,0,0,0.1);">
                <div style="background:#2e7d32;padding:24px;text-align:center;">
                  <h1 style="color:#fff;margin:0;">✅ Reserva Confirmada</h1>
                </div>
                <div style="padding:32px;">
                  <p>Hola <strong>%s</strong>, tu reserva fue confirmada exitosamente.</p>
                  <table style="width:100%%;border-collapse:collapse;margin-top:16px;">
                    <tr><td style="padding:8px;border-bottom:1px solid #eee;color:#666;">Predio</td><td style="padding:8px;border-bottom:1px solid #eee;font-weight:bold;">%s</td></tr>
                    <tr><td style="padding:8px;border-bottom:1px solid #eee;color:#666;">Cancha</td><td style="padding:8px;border-bottom:1px solid #eee;font-weight:bold;">%s</td></tr>
                    <tr><td style="padding:8px;border-bottom:1px solid #eee;color:#666;">Fecha</td><td style="padding:8px;border-bottom:1px solid #eee;font-weight:bold;">%s</td></tr>
                    <tr><td style="padding:8px;color:#666;">Horario</td><td style="padding:8px;font-weight:bold;">%s - %s</td></tr>
                  </table>
                  <p style="color:#666;font-size:14px;margin-top:24px;">El pago se realiza de forma presencial en el predio.</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(nombre, predio, cancha, fecha, horaInicio, horaFin);
    }

    private String buildHtmlCancelacion(String nombre, String cancha, String fecha,
                                         String horaInicio, String horaFin) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head><meta charset="UTF-8"></head>
            <body style="font-family:'Segoe UI',sans-serif;background:#f5f5f5;">
              <div style="max-width:600px;margin:30px auto;background:#fff;border-radius:12px;overflow:hidden;">
                <div style="background:#c62828;padding:24px;text-align:center;">
                  <h1 style="color:#fff;margin:0;">❌ Reserva Cancelada</h1>
                </div>
                <div style="padding:32px;">
                  <p>Hola <strong>%s</strong>, tu reserva fue cancelada.</p>
                  <p><strong>Cancha:</strong> %s | <strong>Fecha:</strong> %s | <strong>Horario:</strong> %s - %s</p>
                  <p style="color:#666;font-size:14px;">Podés hacer una nueva reserva cuando quieras.</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(nombre, cancha, fecha, horaInicio, horaFin);
    }
}
