package com.inno.userservice.service.impl;

import static com.inno.userservice.cache.CacheConstants.USERS_CACHE;
import static com.inno.userservice.exception.BaseAppExceptionBuilder.buildConflictException;
import static com.inno.userservice.exception.BaseAppExceptionBuilder.buildNotFoundException;
import static com.inno.userservice.exception.ExceptionMessages.USER_ALREADY_EXISTS_WITH_EMAIL;
import static com.inno.userservice.exception.ExceptionMessages.USER_ALREADY_EXISTS_WITH_ID;
import static com.inno.userservice.exception.ExceptionMessages.USER_NOT_FOUND;

import com.inno.userservice.dto.request.UserCreateRequestDTO;
import com.inno.userservice.dto.request.UserFilterDTO;
import com.inno.userservice.dto.request.UserUpdateRequestDTO;
import com.inno.userservice.dto.response.UserResponseDTO;
import com.inno.userservice.entity.User;
import com.inno.userservice.mapper.UserMapper;
import com.inno.userservice.repository.UserRepository;
import com.inno.userservice.repository.specification.UserSpecifications;
import com.inno.userservice.service.UserService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final EntityManager entityManager;

  @Override
  @Transactional
  public UserResponseDTO create(Long id, UserCreateRequestDTO dto) {

    if (userRepository.existsById(id)) {
      log.warn("User creation failed: ID already exists | userId={}", id);
      throw buildConflictException(USER_ALREADY_EXISTS_WITH_ID, id);
    }

    if (userRepository.existsByEmail(dto.email())) {
      log.warn("User creation failed: email already exists | email={}", dto.email());
      throw buildConflictException(USER_ALREADY_EXISTS_WITH_EMAIL, dto.email());
    }

    User user = userMapper.toEntity(dto);
    user.setId(id);

    entityManager.persist(user);

    log.info("User created successfully | userId={}", user.getId());
    return userMapper.toDto(user);
  }

  @Override
  @Transactional
  @CacheEvict(value = USERS_CACHE, key = "#userId", beforeInvocation = false)
  public UserResponseDTO update(Long userId, UserUpdateRequestDTO dto) {

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> {
                  log.warn("User update failed: user not found | userId={}", userId);
                  return buildNotFoundException(USER_NOT_FOUND, userId);
                });

    userMapper.updateEntity(dto, user);

    User updatedUser = userRepository.save(user);
    log.info("User updated successfully | userId={}", userId);
    return userMapper.toDto(updatedUser);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = USERS_CACHE, key = "#userId")
  public UserResponseDTO getById(Long userId) {

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> {
                  log.warn("User fetch failed: user not found | userId={}", userId);
                  return buildNotFoundException(USER_NOT_FOUND, userId);
                });

    return userMapper.toDto(user);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<UserResponseDTO> getAll(UserFilterDTO filter, Pageable pageable) {

    Specification<User> spec =
        Specification.where(UserSpecifications.hasName(filter.name()))
            .and(UserSpecifications.hasSurname(filter.surname()));

    return userRepository.findAll(spec, pageable).map((userMapper::toDto));
  }

  @Override
  @Transactional
  @CacheEvict(value = USERS_CACHE, key = "#userId", beforeInvocation = false)
  public void activate(Long userId) {
    int updatedRows = userRepository.activateUser(userId);

    if (updatedRows == 0) {
      log.warn("User activate failed: user not found | userId={}", userId);
      throw buildNotFoundException(USER_NOT_FOUND, userId);
    }
    log.info("User activation successfully | userId={}", userId);
  }

  @Override
  @Transactional
  @CacheEvict(value = USERS_CACHE, key = "#userId", beforeInvocation = false)
  public void deactivate(Long userId) {
    int updatedRows = userRepository.deactivateUser(userId);

    if (updatedRows == 0) {
      log.warn("User deactivation failed: user not found | userId={}", userId);
      throw buildNotFoundException(USER_NOT_FOUND, userId);
    }
    log.info("User deactivated successfully | userId={}", userId);
  }
}
