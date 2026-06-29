package com.vetexpert.sistema_veterinaria.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    @org.springframework.beans.factory.annotation.Value("${mail.sender}")
    private String emisor;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSender = mailSenderProvider.getIfAvailable();
    }

    public void enviarOtp(String destinatario, String otp) {
        String asunto = "Código de Verificación - VET EXPERTS";
        String contenido = "Hola,\n\nTu código de verificación de 6 dígitos para Vet Experts es: " + otp + 
                           "\n\nIntroduce este código en la página de registro para activar tu cuenta.\n\nSaludos,\nEl equipo de Vet Experts.";
        
        enviar(destinatario, asunto, contenido);
    }

    public void enviarRecuperacion(String destinatario, String enlace) {
        String asunto = "Recuperación de Contraseña - VET EXPERTS";
        String contenido = "Hola,\n\nHemos recibido una solicitud para restablecer tu contraseña. " +
                           "Haz clic en el siguiente enlace para definir tu nueva contraseña:\n\n" + enlace + 
                           "\n\nEste enlace es de uso único.\n\nSi no solicitaste este cambio, puedes ignorar este correo.\n\nSaludos,\nEl equipo de Vet Experts.";
        
        enviar(destinatario, asunto, contenido);
    }

    /**
     * Envía un correo electrónico en formato HTML.
     */
    public void enviarHtml(String para, String asunto, String contenidoHtml) {
        if (mailSender == null) {
            throw new RuntimeException("El servicio de correo (SMTP) no está configurado. Ingrese las credenciales correctas en application-local.properties.");
        }
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(emisor);
            helper.setTo(para);
            helper.setSubject(asunto);
            helper.setText(contenidoHtml, true);
            mailSender.send(mimeMessage);
            logger.info(">>> Correo HTML enviado exitosamente vía SMTP real a: {}", para);
        } catch (Exception e) {
            logger.error(">>> Error al enviar correo HTML vía SMTP real a: {}, error: {}", para, e.getMessage());
            throw new RuntimeException("Error al enviar correo HTML vía SMTP: " + e.getMessage(), e);
        }
    }

    private void enviar(String para, String asunto, String contenido) {
        if (mailSender == null) {
            throw new RuntimeException("El servicio de correo (SMTP) no está configurado. Ingrese las credenciales correctas en application-local.properties.");
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emisor);
            message.setTo(para);
            message.setSubject(asunto);
            message.setText(contenido);
            mailSender.send(message);
            logger.info(">>> Correo enviado exitosamente vía SMTP real a: {}", para);
        } catch (Exception e) {
            logger.error(">>> Error al enviar correo vía SMTP real a: {}, error: {}", para, e.getMessage());
            throw new RuntimeException("Error al enviar correo vía SMTP: " + e.getMessage(), e);
        }
    }
}
