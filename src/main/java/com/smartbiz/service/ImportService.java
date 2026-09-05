package com.smartbiz.service;

import com.smartbiz.dto.ImportResultatDto;
import com.smartbiz.model.Client;
import com.smartbiz.model.Employe;
import com.smartbiz.model.Produit;
import com.smartbiz.model.StatutEmploye;
import com.smartbiz.repository.ClientRepository;
import com.smartbiz.repository.EmployeRepository;
import com.smartbiz.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Import Excel pour les employes, produits et clients (point 28 du cahier
 * des charges). Chaque ligne est validee independamment : une ligne
 * invalide est ignoree et signalee, sans bloquer l'import des lignes valides.
 */
@Service
@RequiredArgsConstructor
public class ImportService {

    private final EmployeRepository employeRepository;
    private final ProduitRepository produitRepository;
    private final ClientRepository clientRepository;
    private final AuditService auditService;

    /** Format : Matricule | Nom | Prenom | Email | Telephone | Poste */
    @Transactional
    public ImportResultatDto importerEmployes(MultipartFile fichier) {
        List<String> erreurs = new ArrayList<>();
        int importes = 0;
        int ignores = 0;

        try (Workbook workbook = WorkbookFactory.create(fichier.getInputStream())) {
            Sheet feuille = workbook.getSheetAt(0);
            DataFormatter formateur = new DataFormatter();

            for (int i = 1; i <= feuille.getLastRowNum(); i++) {
                Row ligne = feuille.getRow(i);
                if (ligne == null || estLigneVide(ligne, formateur, 6)) {
                    continue;
                }

                String matricule = valeur(ligne, 0, formateur);
                String nom = valeur(ligne, 1, formateur);
                String prenom = valeur(ligne, 2, formateur);
                String email = valeur(ligne, 3, formateur);
                String telephone = valeur(ligne, 4, formateur);
                String poste = valeur(ligne, 5, formateur);

                if (matricule.isBlank() || nom.isBlank() || prenom.isBlank()) {
                    erreurs.add("Ligne " + (i + 1) + " : matricule, nom et prenom sont obligatoires.");
                    ignores++;
                    continue;
                }
                if (employeRepository.existsByMatricule(matricule)) {
                    erreurs.add("Ligne " + (i + 1) + " : le matricule " + matricule + " existe deja, ligne ignoree.");
                    ignores++;
                    continue;
                }

                employeRepository.save(Employe.builder()
                        .matricule(matricule).nom(nom).prenom(prenom)
                        .email(email.isBlank() ? null : email)
                        .telephone(telephone.isBlank() ? null : telephone)
                        .poste(poste.isBlank() ? null : poste)
                        .statut(StatutEmploye.ACTIF)
                        .build());
                importes++;
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible de lire le fichier Excel fourni", e);
        }

        auditService.enregistrer("IMPORT", "Employe", "-",
                importes + " employe(s) importe(s), " + ignores + " ligne(s) ignoree(s).");
        return ImportResultatDto.builder().nombreImportes(importes).nombreIgnores(ignores).erreurs(erreurs).build();
    }

    /** Format : Reference | Nom | Prix achat | Prix vente | Stock initial */
    @Transactional
    public ImportResultatDto importerProduits(MultipartFile fichier) {
        List<String> erreurs = new ArrayList<>();
        int importes = 0;
        int ignores = 0;

        try (Workbook workbook = WorkbookFactory.create(fichier.getInputStream())) {
            Sheet feuille = workbook.getSheetAt(0);
            DataFormatter formateur = new DataFormatter();

            for (int i = 1; i <= feuille.getLastRowNum(); i++) {
                Row ligne = feuille.getRow(i);
                if (ligne == null || estLigneVide(ligne, formateur, 5)) {
                    continue;
                }

                String reference = valeur(ligne, 0, formateur);
                String nom = valeur(ligne, 1, formateur);
                String prixAchatTxte = valeur(ligne, 2, formateur);
                String prixVenteTxte = valeur(ligne, 3, formateur);
                String stockTxte = valeur(ligne, 4, formateur);

                if (reference.isBlank() || nom.isBlank() || prixAchatTxte.isBlank() || prixVenteTxte.isBlank()) {
                    erreurs.add("Ligne " + (i + 1) + " : reference, nom, prix d'achat et prix de vente sont obligatoires.");
                    ignores++;
                    continue;
                }
                if (produitRepository.existsByReference(reference)) {
                    erreurs.add("Ligne " + (i + 1) + " : la reference " + reference + " existe deja, ligne ignoree.");
                    ignores++;
                    continue;
                }

                try {
                    BigDecimal prixAchat = new BigDecimal(prixAchatTxte.replace(",", "."));
                    BigDecimal prixVente = new BigDecimal(prixVenteTxte.replace(",", "."));
                    int stock = stockTxte.isBlank() ? 0 : (int) Double.parseDouble(stockTxte.replace(",", "."));

                    produitRepository.save(Produit.builder()
                            .reference(reference).nom(nom)
                            .prixAchat(prixAchat).prixVente(prixVente)
                            .quantiteStock(stock).seuilMinimum(5).actif(true)
                            .build());
                    importes++;
                } catch (NumberFormatException ex) {
                    erreurs.add("Ligne " + (i + 1) + " : prix ou stock invalide.");
                    ignores++;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible de lire le fichier Excel fourni", e);
        }

        auditService.enregistrer("IMPORT", "Produit", "-",
                importes + " produit(s) importe(s), " + ignores + " ligne(s) ignoree(s).");
        return ImportResultatDto.builder().nombreImportes(importes).nombreIgnores(ignores).erreurs(erreurs).build();
    }

    /** Format : Nom | Email | Telephone | Ville */
    @Transactional
    public ImportResultatDto importerClients(MultipartFile fichier) {
        List<String> erreurs = new ArrayList<>();
        int importes = 0;
        int ignores = 0;

        try (Workbook workbook = WorkbookFactory.create(fichier.getInputStream())) {
            Sheet feuille = workbook.getSheetAt(0);
            DataFormatter formateur = new DataFormatter();

            for (int i = 1; i <= feuille.getLastRowNum(); i++) {
                Row ligne = feuille.getRow(i);
                if (ligne == null || estLigneVide(ligne, formateur, 4)) {
                    continue;
                }

                String nom = valeur(ligne, 0, formateur);
                String email = valeur(ligne, 1, formateur);
                String telephone = valeur(ligne, 2, formateur);
                String ville = valeur(ligne, 3, formateur);

                if (nom.isBlank()) {
                    erreurs.add("Ligne " + (i + 1) + " : le nom est obligatoire.");
                    ignores++;
                    continue;
                }

                clientRepository.save(Client.builder()
                        .nom(nom)
                        .email(email.isBlank() ? null : email)
                        .telephone(telephone.isBlank() ? null : telephone)
                        .ville(ville.isBlank() ? null : ville)
                        .actif(true)
                        .build());
                importes++;
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible de lire le fichier Excel fourni", e);
        }

        auditService.enregistrer("IMPORT", "Client", "-",
                importes + " client(s) importe(s), " + ignores + " ligne(s) ignoree(s).");
        return ImportResultatDto.builder().nombreImportes(importes).nombreIgnores(ignores).erreurs(erreurs).build();
    }

    private String valeur(Row ligne, int colonne, DataFormatter formateur) {
        Cell cellule = ligne.getCell(colonne);
        return cellule == null ? "" : formateur.formatCellValue(cellule).trim();
    }

    private boolean estLigneVide(Row ligne, DataFormatter formateur, int nombreColonnes) {
        for (int c = 0; c < nombreColonnes; c++) {
            if (!valeur(ligne, c, formateur).isBlank()) {
                return false;
            }
        }
        return true;
    }
}
