package com.inno.userservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
public class User {

  @Id private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "surname", nullable = false)
  private String surname;

  @Column(name = "birth_date", nullable = false)
  private LocalDate birthDate;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL)
  List<PaymentCard> paymentCards = new ArrayList<>();

  @Builder.Default
  @Column(name = "active", nullable = false)
  private Boolean active = true;

  @Column(name = "created_at", updatable = false, nullable = false)
  @CreatedDate
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  @LastModifiedDate
  private OffsetDateTime updatedAt;
}
