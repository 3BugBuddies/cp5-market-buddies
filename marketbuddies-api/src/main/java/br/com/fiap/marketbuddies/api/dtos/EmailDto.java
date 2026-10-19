package br.com.fiap.marketbuddies.api.dtos;

import br.com.fiap.marketbuddies.api.entities.EmailEntity;
import br.com.fiap.marketbuddies.api.enums.StatusEmail;

import java.time.LocalDateTime;

// Linha do controle de envio (GET /api/emails). O destinatário sai mascarado e o corpo do e-mail não é exposto.
public record EmailDto(
        Long id,
        Long orderId,
        String emailTo,
        String subject,
        StatusEmail statusEmail,
        LocalDateTime sendDateEmail,
        String errorMessage) {

    // Chamar dentro da transação: lê o id do pedido.
    public static EmailDto from(EmailEntity email) {
        return new EmailDto(
                email.getId(),
                email.getOrder() == null ? null : email.getOrder().getId(),
                mask(email.getEmailTo()),
                email.getSubject(),
                email.getStatusEmail(),
                email.getSendDateEmail(),
                email.getErrorMessage());
    }

    // cliente@marketbuddies.com → cl***@marketbuddies.com (local com até 2 letras mostra só a primeira)
    static String mask(String email) {
        if (email == null) {
            return null;
        }
        int at = email.indexOf('@');
        if (at < 1) {
            return "***";
        }
        int visible = at > 2 ? 2 : 1;
        return email.substring(0, visible) + "***" + email.substring(at);
    }
}
