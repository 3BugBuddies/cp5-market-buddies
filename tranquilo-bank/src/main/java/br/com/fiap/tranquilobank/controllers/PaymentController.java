package br.com.fiap.tranquilobank.controllers;

import br.com.fiap.tranquilobank.dtos.AuthorizationRequestDto;
import br.com.fiap.tranquilobank.dtos.AuthorizationResponseDto;
import br.com.fiap.tranquilobank.services.AuthorizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final AuthorizationService authorizationService;

	/** A recusa também é 200: é decisão de negócio, não erro de requisição. */
	@PostMapping("/authorize")
	public ResponseEntity<AuthorizationResponseDto> authorize(@Valid @RequestBody AuthorizationRequestDto request) {
		return ResponseEntity.ok(authorizationService.authorize(request));
	}
}
