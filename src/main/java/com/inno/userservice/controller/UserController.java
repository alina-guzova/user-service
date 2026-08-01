package com.inno.userservice.controller;

import com.inno.userservice.dto.request.UserCreateRequestDTO;
import com.inno.userservice.dto.request.UserFilterDTO;
import com.inno.userservice.dto.request.UserUpdateRequestDTO;
import com.inno.userservice.dto.response.UserResponseDTO;
import com.inno.userservice.service.UserService;
import jakarta.validation.Valid;
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
@RequestMapping("api/v1/users")
public class UserController {

  private final UserService userService;

  @PostMapping("/{userId}")
  public ResponseEntity<UserResponseDTO> create(
      @PathVariable Long userId, @Valid @RequestBody UserCreateRequestDTO userCreateRequest) {
    UserResponseDTO user = userService.create(userId, userCreateRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(user);
  }

  @PutMapping("/{userId}")
  @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal")
  public ResponseEntity<UserResponseDTO> update(
      @PathVariable Long userId, @Valid @RequestBody UserUpdateRequestDTO userUpdateRequest) {
    return ResponseEntity.ok(userService.update(userId, userUpdateRequest));
  }

  @GetMapping("/{userId}")
  @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal")
  public ResponseEntity<UserResponseDTO> getById(@PathVariable Long userId) {
    return ResponseEntity.ok(userService.getById(userId));
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<UserResponseDTO>> getAll(
      @Valid UserFilterDTO userFilter, @PageableDefault(size = 15) Pageable pageable) {
    return ResponseEntity.ok(userService.getAll(userFilter, pageable));
  }

  @PatchMapping("/{userId}/activate")
  @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal")
  public ResponseEntity<Void> activate(@PathVariable Long userId) {
    userService.activate(userId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{userId}/deactivate")
  @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal")
  public ResponseEntity<Void> deactivate(@PathVariable Long userId) {
    userService.deactivate(userId);
    return ResponseEntity.noContent().build();
  }
}
