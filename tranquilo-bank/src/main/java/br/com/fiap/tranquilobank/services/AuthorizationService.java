package br.com.fiap.tranquilobank.services;

import br.com.fiap.tranquilobank.dtos.AuthorizationRequestDto;
import br.com.fiap.tranquilobank.dtos.AuthorizationResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
public class AuthorizationService {

	private static final Locale PT_BR = Locale.of("pt", "BR");

	private final BigDecimal approvalLimit;

	/** Construtor explícito: {@code @RequiredArgsConstructor} não copia o {@code @Value} para o parâmetro. */
	public AuthorizationService(@Value("${tranquilo-bank.approval-limit}") BigDecimal approvalLimit) {
		this.approvalLimit = approvalLimit;
	}

	public AuthorizationResponseDto authorize(AuthorizationRequestDto request) {
		String amountText = format(request.amount());
		LocalDateTime now = LocalDateTime.now();

		if (request.amount().compareTo(approvalLimit) > 0) {
			String reason = "Valor acima do limite de R$ " + format(approvalLimit);
			log.info("Pedido {} · R$ {} · RECUSADO ({})", request.orderId(), amountText, reason);
			return new AuthorizationResponseDto(request.orderId(), false, null, reason, now);
		}

		String code = "TB-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
		log.info("Pedido {} · R$ {} · APROVADO {}", request.orderId(), amountText, code);
		return new AuthorizationResponseDto(request.orderId(), true, code, null, now);
	}

	private String format(BigDecimal value) {
		NumberFormat nf = NumberFormat.getNumberInstance(PT_BR);
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		return nf.format(value);
	}
}
