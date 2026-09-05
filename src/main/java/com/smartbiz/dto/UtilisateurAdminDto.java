package com.smartbiz.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UtilisateurAdminDto {

    private Long id;

    private String nom;

    private String prenom;

    private String email;

    private boolean actif;

    private List<Long> roleIds;
}
