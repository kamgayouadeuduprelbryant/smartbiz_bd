package com.smartbiz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RechercheResultatDto {
    private String type;
    private String libelle;
    private String url;
}
