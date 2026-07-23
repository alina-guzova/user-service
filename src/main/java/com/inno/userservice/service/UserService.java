package com.inno.userservice.service;

import com.inno.userservice.dto.request.UserCreateRequestDTO;
import com.inno.userservice.dto.request.UserFilterDTO;
import com.inno.userservice.dto.request.UserUpdateRequestDTO;
import com.inno.userservice.dto.response.UserResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

  UserResponseDTO create(UserCreateRequestDTO dto);

  UserResponseDTO update(Long userId, UserUpdateRequestDTO dto);

  UserResponseDTO getById(Long userId);

  Page<UserResponseDTO> getAll(UserFilterDTO filter, Pageable pageable);

  void activate(Long userId);

  void deactivate(Long userId);
}
