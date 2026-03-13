package com.antigravity.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * REST API'den gelen kayıt paketini temsil eden Data Transfer Object (DTO).
 * Entity'nin doğrudan API dışına açılmasını önler.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDto {

    @NotBlank(message = "Firebase UID boş olamaz")
    private String firebaseUid;

    @NotBlank(message = "E-posta boş olamaz")
    @Email(message = "Geçerli bir e-posta adresi giriniz")
    private String email;

    @NotBlank(message = "Ad soyad boş olamaz")
    private String fullName;

    private String profilePictureUrl;
}
