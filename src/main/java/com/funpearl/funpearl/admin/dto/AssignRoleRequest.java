package com.funpearl.funpearl.admin.dto;

import com.funpearl.funpearl.user.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignRoleRequest {
    @NotNull(message = "Role is required")
    private Role role;
}
