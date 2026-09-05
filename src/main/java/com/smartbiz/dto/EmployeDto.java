package com.smartbiz.dto;

import com.smartbiz.model.StatutEmploye;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class EmployeDto {

    private Long id;

    @NotBlank(message = "Le matricule est obligatoire")
    private String matricule;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prenom est obligatoire")
    private String prenom;

    @Email(message = "Format d'email invalide")
    private String email;

    private String telephone;

    private String poste;

    private Long departementId;

    @Positive(message = "Le salaire doit etre positif")
    private BigDecimal salaire;

    private LocalDate dateEmbauche;

    private StatutEmploye statut;
}
