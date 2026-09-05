package com.smartbiz.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CommandeDto {

    @NotNull(message = "Le client est obligatoire")
    private Long clientId;

    private LocalDate dateCommande;

    private String notes;
}
