package com.inno.userservice.mapper;

import com.inno.userservice.dto.request.UserCreateRequestDTO;
import com.inno.userservice.dto.request.UserUpdateRequestDTO;
import com.inno.userservice.dto.response.UserResponseDTO;
import com.inno.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

  @Mapping(target = "email", source = "email", qualifiedByName = "cleanEmail")
  @Mapping(target = "name", source = "name", qualifiedByName = "stripString")
  @Mapping(target = "surname", source = "surname", qualifiedByName = "stripString")
  User toEntity(UserCreateRequestDTO dto);

  UserResponseDTO toDto(User entity);

  @Mapping(target = "name", source = "name", qualifiedByName = "stripString")
  @Mapping(target = "surname", source = "surname", qualifiedByName = "stripString")
  void updateEntity(UserUpdateRequestDTO dto, @MappingTarget User entity);

  @Named("cleanEmail")
  default String cleanEmail(String email) {
    return email == null ? null : email.trim().toLowerCase();
  }

  @Named("stripString")
  default String stripString(String value) {
    return value == null ? null : value.strip();
  }
}
