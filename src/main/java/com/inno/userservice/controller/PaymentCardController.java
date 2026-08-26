package com.inno.userservice.controller;

import com.inno.userservice.dto.request.PaymentCardCreateRequestDTO;
import com.inno.userservice.dto.request.PaymentCardUpdateRequestDTO;
import com.inno.userservice.dto.response.PaymentCardResponseDTO;
import com.inno.userservice.service.PaymentCardService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/users/payment-cards")
public class PaymentCardController {

  private final PaymentCardService paymentCardService;

  @PostMapping
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<PaymentCardResponseDTO> create(
      @Valid @RequestBody PaymentCardCreateRequestDTO cardCreateRequest) {
    PaymentCardResponseDTO card = paymentCardService.create(cardCreateRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(card);
  }

  @GetMapping("/user/{userId}")
  @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal")
  public ResponseEntity<List<PaymentCardResponseDTO>> getAllCardsByUser(@PathVariable Long userId) {
    return ResponseEntity.ok(paymentCardService.getAllCardsByUser(userId));
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<PaymentCardResponseDTO>> getAll(
      @PageableDefault(size = 15) Pageable pageable) {
    return ResponseEntity.ok(paymentCardService.getAll(pageable));
  }

  @GetMapping("/{cardId}")
  @PreAuthorize(
      "hasRole('ADMIN') or @paymentCardSecurity.isCardOwner(#cardId, authentication.principal)")
  public ResponseEntity<PaymentCardResponseDTO> getById(@PathVariable Long cardId) {
    return ResponseEntity.ok(paymentCardService.getById(cardId));
  }

  @PutMapping("/{cardId}")
  @PreAuthorize(
      "hasRole('ADMIN') or @paymentCardSecurity.isCardOwner(#cardId, authentication.principal)")
  public ResponseEntity<PaymentCardResponseDTO> update(
      @PathVariable Long cardId,
      @Valid @RequestBody PaymentCardUpdateRequestDTO cardUpdateRequest) {
    return ResponseEntity.ok(paymentCardService.update(cardId, cardUpdateRequest));
  }

  @PatchMapping("/{cardId}/activate")
  @PreAuthorize(
      "hasRole('ADMIN') or @paymentCardSecurity.isCardOwner(#cardId, authentication.principal)")
  public ResponseEntity<Void> activate(@PathVariable Long cardId) {
    paymentCardService.activate(cardId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{cardId}/deactivate")
  @PreAuthorize(
      "hasRole('ADMIN') or @paymentCardSecurity.isCardOwner(#cardId, authentication.principal)")
  public ResponseEntity<Void> deactivate(@PathVariable Long cardId) {
    paymentCardService.deactivate(cardId);
    return ResponseEntity.noContent().build();
  }
}
