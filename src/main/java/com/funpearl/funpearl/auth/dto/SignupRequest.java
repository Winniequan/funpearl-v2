package com.funpearl.funpearl.auth.dto;

import com.funpearl.funpearl.common.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {

    @NotBlank(message = "username should not be empty")
    @Size(min = 3, max = 20, message = "username must be between 3 and 20 characters")
    private String username;

    @NotBlank(message = "email should not be empty")
    @Email(message = "email should be valid")
    private String email;

    @NotBlank(message = "password should not be empty")
    @Size(min = 8, max = 40, message = "password must be between 8 and 40 characters")
    @StrongPassword
    private String password;
}