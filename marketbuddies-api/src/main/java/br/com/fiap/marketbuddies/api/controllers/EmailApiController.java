package br.com.fiap.marketbuddies.api.controllers;

import br.com.fiap.marketbuddies.api.dtos.EmailDto;
import br.com.fiap.marketbuddies.api.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Controle de envio de e-mails pedido no enunciado. Só ADMIN (SecurityConfig: /api/emails/**).
// Os 50 mais recentes primeiro, com o destinatário mascarado e sem o corpo.
@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
public class EmailApiController {

    private final EmailService emailService;

    @GetMapping
    public List<EmailDto> list() {
        return emailService.findRecent();
    }
}
