package com.smartbiz.dto;

import com.smartbiz.model.PrioriteTache;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TacheDto {

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    private String description;

    private Long responsableId;

    private PrioriteTache priorite;

    private LocalDate dateLimite;
}
