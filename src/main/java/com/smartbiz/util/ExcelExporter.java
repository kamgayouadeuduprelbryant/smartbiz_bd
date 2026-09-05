package com.smartbiz.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * Genere un classeur Excel (.xlsx) simple a partir d'une liste d'en-tetes
 * et de lignes de donnees. Utilise pour tous les exports du module Rapports
 * (point 27 du cahier des charges).
 */
public final class ExcelExporter {

    private ExcelExporter() {
    }

    public static byte[] exporter(String titreFeuille, List<String> entetes, List<List<Object>> lignes) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(titreFeuille);

            CellStyle styleEntete = workbook.createCellStyle();
            Font policeEntete = workbook.createFont();
            policeEntete.setBold(true);
            policeEntete.setColor(IndexedColors.WHITE.getIndex());
            styleEntete.setFont(policeEntete);
            styleEntete.setFillForegroundColor(IndexedColors.INDIGO.getIndex());
            styleEntete.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row ligneEntete = sheet.createRow(0);
            for (int i = 0; i < entetes.size(); i++) {
                Cell cellule = ligneEntete.createCell(i);
                cellule.setCellValue(entetes.get(i));
                cellule.setCellStyle(styleEntete);
            }

            int numeroLigne = 1;
            for (List<Object> ligne : lignes) {
                Row row = sheet.createRow(numeroLigne++);
                for (int i = 0; i < ligne.size(); i++) {
                    Cell cellule = row.createCell(i);
                    Object valeur = ligne.get(i);
                    ecrireValeur(cellule, valeur);
                }
            }

            for (int i = 0; i < entetes.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Erreur lors de la generation du fichier Excel", e);
        }
    }

    private static void ecrireValeur(Cell cellule, Object valeur) {
        if (valeur == null) {
            cellule.setCellValue("");
        } else if (valeur instanceof Number nombre) {
            cellule.setCellValue(nombre.doubleValue());
        } else if (valeur instanceof Boolean booleen) {
            cellule.setCellValue(booleen);
        } else {
            cellule.setCellValue(valeur.toString());
        }
    }
}
