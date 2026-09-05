package com.smartbiz.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationDto {
    private Long id;
    private String type;
    private String message;
    private String lien;
    private boolean lue;
    private String dateCreation;
}
