package com.smartbiz.dto;

import com.smartbiz.model.StatutPresence;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class PresenceDto {

    @NotNull(message = "L'employe est obligatoire")
    private Long employeId;

    @NotNull(message = "La date est obligatoire")
    private LocalDate date;

    @NotNull(message = "Le statut est obligatoire")
    private StatutPresence statut;

    private LocalTime heureArrivee;

    private LocalTime heureDepart;

    private String notes;
}
