package com.smartbiz.dto;

import com.smartbiz.model.TypeConge;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CongeDto {

    @NotNull(message = "L'employe est obligatoire")
    private Long employeId;

    @NotNull(message = "Le type de conge est obligatoire")
    private TypeConge type;

    @NotNull(message = "La date de debut est obligatoire")
    private LocalDate dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate dateFin;

    private String motif;
}
