package com.funpearl.funpearl.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "username should not be empty")
    private String username;

    @NotBlank(message = "password should not be empty")
    private String password;
}