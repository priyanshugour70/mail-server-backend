package com.lssgoo.mail.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrganisationRequest {

    @NotBlank(message = "Organisation name is required")
    @Size(min = 2, max = 100, message = "Organisation name must be between 2 and 100 characters")
    private String name;

    @Size(max = 255, message = "Domain must not exceed 255 characters")
    private String domain;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}

