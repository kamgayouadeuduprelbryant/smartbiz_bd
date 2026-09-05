package com.smartbiz.util;

import com.smartbiz.model.Facture;
import com.smartbiz.model.LigneFacture;
import com.smartbiz.model.Paiement;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Genere un PDF de facture simple et lisible (point 16 du cahier des
 * charges). Mise en page volontairement sobre : en-tete SmartBiz, informations
 * client, tableau des lignes, totaux, historique des paiements.
 */
public final class FacturePdfGenerator {

    private static final float MARGE = 50;
    private static final PDType1Font POLICE = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font POLICE_GRAS = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private FacturePdfGenerator() {
    }

    public static byte[] generer(Facture facture) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                float y = page.getMediaBox().getHeight() - MARGE;
                float largeurUtile = page.getMediaBox().getWidth() - 2 * MARGE;

                // En-tete
                ecrireTexte(cs, POLICE_GRAS, 20, MARGE, y, "SmartBiz");
                ecrireTexte(cs, POLICE, 9, MARGE, y - 15, "Pilotez votre entreprise. Simplement. Intelligemment.");

                ecrireTexteDroite(cs, POLICE_GRAS, 16, MARGE + largeurUtile, y, "FACTURE " + facture.getNumero());
                ecrireTexteDroite(cs, POLICE, 10, MARGE + largeurUtile, y - 18, "Emise le " + facture.getDateEmission());
                if (facture.getDateEcheance() != null) {
                    ecrireTexteDroite(cs, POLICE, 10, MARGE + largeurUtile, y - 32, "Echeance le " + facture.getDateEcheance());
                }

                y -= 70;
                ligneHorizontale(cs, MARGE, y, MARGE + largeurUtile);
                y -= 25;

                // Client
                ecrireTexte(cs, POLICE_GRAS, 11, MARGE, y, "Facture a :");
                y -= 15;
                ecrireTexte(cs, POLICE, 11, MARGE, y, facture.getClient().getNom());
                if (facture.getClient().getEmail() != null) {
                    y -= 14;
                    ecrireTexte(cs, POLICE, 10, MARGE, y, facture.getClient().getEmail());
                }
                if (facture.getClient().getVille() != null) {
                    y -= 14;
                    ecrireTexte(cs, POLICE, 10, MARGE, y, facture.getClient().getVille());
                }

                y -= 30;

                // En-tete du tableau des lignes
                float xLibelle = MARGE;
                float xQuantite = MARGE + 260;
                float xPrixUnitaire = MARGE + 340;
                float xSousTotal = MARGE + 440;

                ecrireTexte(cs, POLICE_GRAS, 10, xLibelle, y, "Libelle");
                ecrireTexte(cs, POLICE_GRAS, 10, xQuantite, y, "Qte");
                ecrireTexte(cs, POLICE_GRAS, 10, xPrixUnitaire, y, "P.U.");
                ecrireTexte(cs, POLICE_GRAS, 10, xSousTotal, y, "Sous-total");
                y -= 8;
                ligneHorizontale(cs, MARGE, y, MARGE + largeurUtile);
                y -= 16;

                for (LigneFacture ligne : facture.getLignes()) {
                    ecrireTexte(cs, POLICE, 10, xLibelle, y, tronquer(ligne.getLibelle(), 45));
                    ecrireTexte(cs, POLICE, 10, xQuantite, y, String.valueOf(ligne.getQuantite()));
                    ecrireTexte(cs, POLICE, 10, xPrixUnitaire, y, ligne.getPrixUnitaire().toString());
                    ecrireTexte(cs, POLICE, 10, xSousTotal, y, ligne.getSousTotal().toString());
                    y -= 16;

                    if (y < 150) {
                        break; // Securite : pas de gestion multi-pages dans cette version
                    }
                }

                y -= 10;
                ligneHorizontale(cs, MARGE, y, MARGE + largeurUtile);
                y -= 20;

                // Totaux
                ecrireTexteDroite(cs, POLICE, 10, MARGE + largeurUtile, y, "Sous-total : " + facture.getSousTotal());
                y -= 14;
                ecrireTexteDroite(cs, POLICE, 10, MARGE + largeurUtile, y,
                        "Taxe (" + facture.getTauxTaxe() + "%) : " + facture.getMontantTaxe());
                y -= 14;
                ecrireTexteDroite(cs, POLICE, 10, MARGE + largeurUtile, y, "Remise : -" + facture.getRemiseMontant());
                y -= 16;
                ecrireTexteDroite(cs, POLICE_GRAS, 13, MARGE + largeurUtile, y, "TOTAL : " + facture.getTotal());
                y -= 20;
                ecrireTexteDroite(cs, POLICE, 10, MARGE + largeurUtile, y, "Paye : " + facture.getMontantPaye());
                y -= 14;
                ecrireTexteDroite(cs, POLICE, 10, MARGE + largeurUtile, y, "Reste a payer : " + facture.getResteAPayer());

                // Paiements
                if (!facture.getPaiements().isEmpty()) {
                    y -= 35;
                    ecrireTexte(cs, POLICE_GRAS, 11, MARGE, y, "Paiements recus");
                    y -= 16;
                    for (Paiement p : facture.getPaiements()) {
                        ecrireTexte(cs, POLICE, 9, MARGE, y,
                                p.getDatePaiement() + " - " + p.getMontant() + " (" + p.getModePaiement() + ")");
                        y -= 13;
                    }
                }

                // Pied de page
                ecrireTexte(cs, POLICE, 8, MARGE, 40, "Document genere automatiquement par SmartBiz.");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Erreur lors de la generation du PDF de la facture", e);
        }
    }

    private static void ecrireTexte(PDPageContentStream cs, PDType1Font police, float taille,
                                     float x, float y, String texte) throws IOException {
        cs.beginText();
        cs.setFont(police, taille);
        cs.newLineAtOffset(x, y);
        cs.showText(texte != null ? texte : "");
        cs.endText();
    }

    private static void ecrireTexteDroite(PDPageContentStream cs, PDType1Font police, float taille,
                                           float xDroite, float y, String texte) throws IOException {
        float largeur = police.getStringWidth(texte) / 1000 * taille;
        ecrireTexte(cs, police, taille, xDroite - largeur, y, texte);
    }

    private static void ligneHorizontale(PDPageContentStream cs, float x1, float y, float x2) throws IOException {
        cs.setLineWidth(0.5f);
        cs.moveTo(x1, y);
        cs.lineTo(x2, y);
        cs.stroke();
    }

    private static String tronquer(String texte, int longueurMax) {
        if (texte == null) return "";
        return texte.length() > longueurMax ? texte.substring(0, longueurMax - 1) + "…" : texte;
    }
}
