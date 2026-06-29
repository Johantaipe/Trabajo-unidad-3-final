package com.vetexpert.sistema_veterinaria.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppOtpService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppOtpService.class);

    /**
     * Envía un código OTP por WhatsApp/SMS.
     * Esta arquitectura está preparada para que se integre con APIs de pasarelas como Twilio, Meta Cloud API, etc.
     */
    public void enviarOtpWhatsApp(String telefono, String otp) {
        logger.info("\n" +
                "================================================================================\n" +
                "               [VET EXPERTS - WHATSAPP MOCK OTP SEND]\n" +
                "================================================================================\n" +
                "CELULAR DESTINO: +51 " + telefono + "\n" +
                "MENSAJE OTP: Hola, tu código de verificación Vet Experts es: " + otp + "\n" +
                "ARQUITECTURA PREPARADA: Para integrar envíos de WhatsApp reales, configure su API token en esta sección.\n" +
                "================================================================================");
    }
}
