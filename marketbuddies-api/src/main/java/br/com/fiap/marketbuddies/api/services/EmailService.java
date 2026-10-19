package br.com.fiap.marketbuddies.api.services;

import br.com.fiap.marketbuddies.api.configs.StoreMailProperties;
import br.com.fiap.marketbuddies.api.dtos.EmailDto;
import br.com.fiap.marketbuddies.api.entities.EmailEntity;
import br.com.fiap.marketbuddies.api.entities.OrderEntity;
import br.com.fiap.marketbuddies.api.entities.OrderItemEntity;
import br.com.fiap.marketbuddies.api.enums.StatusEmail;
import br.com.fiap.marketbuddies.api.exceptions.NotFoundException;
import br.com.fiap.marketbuddies.api.repositories.EmailRepository;
import br.com.fiap.marketbuddies.api.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

// Como o EmailService do professor (RabbitmqSpringEmail): SimpleMailMessage, SENT ou ERROR,
// e o registro em TB_EMAILS é salvo sempre. Diferenças:
//  - o envio não roda dentro de transação: o SMTP é lento e não deve segurar conexão do banco
//    (o pedido é lido já com itens e cliente, e o save do registro abre a própria transação);
//  - a falha vai para errorMessage e nunca é relançada, para a mensagem da fila não voltar.
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    // Tamanho da coluna error_message (EmailEntity).
    private static final int ERROR_MAX = 500;

    private final EmailRepository emailRepository;
    private final OrderRepository orderRepository;
    private final JavaMailSender mailSender;
    private final StoreMailProperties mailProperties;

    public StatusEmail sendConfirmation(Long orderId) {
        OrderEntity order = orderRepository.findDetailedById(orderId)
                .orElseThrow(() -> new NotFoundException("Pedido " + orderId + " não encontrado"));

        EmailEntity email = new EmailEntity();
        email.setOrder(order);
        email.setEmailFrom(mailProperties.from());
        email.setEmailTo(order.getCustomer().getEmail());
        email.setSubject("MarketBuddies · Pagamento confirmado do pedido #" + order.getId());
        email.setText(body(order));
        email.setSendDateEmail(LocalDateTime.now());

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(email.getEmailFrom());
            message.setTo(email.getEmailTo());
            message.setSubject(email.getSubject());
            message.setText(email.getText());
            mailSender.send(message);
            email.setStatusEmail(StatusEmail.SENT);
            log.info("E-mail do pedido {} enviado para {}", orderId, email.getEmailTo());
        } catch (MailException ex) {
            email.setStatusEmail(StatusEmail.ERROR);
            email.setErrorMessage(truncate(ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage()));
            log.warn("E-mail do pedido {} não enviado: {}", orderId, ex.getMessage(), ex);
        }

        emailRepository.save(email);
        return email.getStatusEmail();
    }

    // Controle de envio (GET /api/emails): mais recentes primeiro, até 50, com o destinatário mascarado.
    @Transactional(readOnly = true)
    public List<EmailDto> findRecent() {
        return emailRepository.findTop50ByOrderByIdDesc().stream().map(EmailDto::from).toList();
    }

    private static String body(OrderEntity order) {
        StringBuilder text = new StringBuilder()
                .append("Olá, ").append(order.getCustomer().getFullName()).append("!\n\n")
                .append("O Tranquilo Bank confirmou o pagamento do seu pedido #").append(order.getId()).append(".\n\n")
                .append("Itens:\n");
        for (OrderItemEntity item : order.getItems()) {
            BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            text.append("  ").append(item.getQuantity()).append(" × ").append(item.getProductName())
                    .append(" (").append(money(item.getUnitPrice())).append(" cada) = ").append(money(subtotal))
                    .append('\n');
        }
        return text.append("\nTotal: ").append(money(order.getTotalAmount())).append('\n')
                .append("Código de autorização: ").append(order.getAuthorizationCode()).append("\n\n")
                .append("Obrigado por comprar no MarketBuddies!\n")
                .toString();
    }

    private static String money(BigDecimal value) {
        return NumberFormat.getCurrencyInstance(Locale.of("pt", "BR")).format(value);
    }

    private static String truncate(String message) {
        return message.length() <= ERROR_MAX ? message : message.substring(0, ERROR_MAX - 1) + "…";
    }
}
