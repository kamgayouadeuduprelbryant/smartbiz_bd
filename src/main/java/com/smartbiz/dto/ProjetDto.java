package com.smartbiz.dto;

import com.smartbiz.model.StatutProjet;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ProjetDto {

    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    private String description;

    private StatutProjet statut;

    private LocalDate dateDebut;

    private LocalDate dateEcheance;

    private List<Long> membreIds;
}
