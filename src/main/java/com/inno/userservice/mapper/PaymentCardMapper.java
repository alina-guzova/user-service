package com.inno.userservice.mapper;

import com.inno.userservice.dto.request.PaymentCardCreateRequestDTO;
import com.inno.userservice.dto.request.PaymentCardUpdateRequestDTO;
import com.inno.userservice.dto.response.PaymentCardResponseDTO;
import com.inno.userservice.entity.PaymentCard;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentCardMapper {

  @Mapping(target = "number", source = "number", qualifiedByName = "maskCardNumber")
  @Mapping(
      target = "expirationDate",
      source = "expirationDate",
      qualifiedByName = "formatExpirationDate")
  PaymentCardResponseDTO toDto(PaymentCard entity);

  @Mapping(target = "number", source = "number", qualifiedByName = "stripString")
  @Mapping(target = "holder", source = "holder", qualifiedByName = "stripString")
  @Mapping(
      target = "expirationDate",
      source = "expirationDate",
      qualifiedByName = "parseExpirationDate")
  PaymentCard toEntity(PaymentCardCreateRequestDTO dto);

  @Mapping(target = "holder", source = "holder", qualifiedByName = "stripString")
  @Mapping(
      target = "expirationDate",
      source = "expirationDate",
      qualifiedByName = "parseExpirationDate")
  void updateEntity(PaymentCardUpdateRequestDTO dto, @MappingTarget PaymentCard entity);

  @Named("stripString")
  default String stripString(String value) {
    return value == null ? null : value.strip();
  }

  @Named("maskCardNumber")
  default String maskCardNumber(String number) {
    if (number == null || number.length() < 4) {
      return "****";
    }
    String digitsOnly = number.replaceAll("\\D", "");
    String lastFour = digitsOnly.substring(digitsOnly.length() - 4);
    return "**** **** **** " + lastFour;
  }

  @Named("formatExpirationDate")
  default String formatExpirationDate(LocalDate date) {
    if (date == null) {
      return null;
    }
    return date.format(DateTimeFormatter.ofPattern("MM/yy"));
  }

  @Named("parseExpirationDate")
  default LocalDate parseExpirationDate(String dateString) {
    if (dateString == null || dateString.isBlank()) {
      return null;
    }

    DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("dd/MM/yy");
    return LocalDate.parse("01/" + dateString.strip(), fullFormatter);
  }
}
