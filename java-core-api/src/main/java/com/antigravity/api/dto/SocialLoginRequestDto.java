package com.antigravity.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialLoginRequestDto {
    @NotBlank(message = "idToken boş olamaz")
    private String idToken;
    
    // Opsiyonel: Hangi platformdan gelindiğini tutabiliriz
    private String platform; 
}
