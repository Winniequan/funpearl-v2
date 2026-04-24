package com.funpearl.funpearl.user.dto;

import com.funpearl.funpearl.common.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 40, message = "New password must be between 8 and 40 characters")
    @StrongPassword
    private String newPassword;
}
