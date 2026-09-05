package com.smartbiz.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ImportResultatDto {
    private int nombreImportes;
    private int nombreIgnores;
    private List<String> erreurs;
}
