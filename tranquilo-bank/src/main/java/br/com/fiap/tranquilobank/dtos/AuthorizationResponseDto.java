package br.com.fiap.tranquilobank.dtos;

import java.time.LocalDateTime;

public record AuthorizationResponseDto(
		Long orderId,
		Boolean approved,
		String authorizationCode,
		String reason,
		LocalDateTime processedAt) {
}
