package com.smartbiz.service;

import com.smartbiz.dto.FactureDto;
import com.smartbiz.dto.LigneFactureDto;
import com.smartbiz.dto.PaiementDto;
import com.smartbiz.exception.ConflitDonneesException;
import com.smartbiz.exception.ResourceNotFoundException;
import com.smartbiz.model.*;
import com.smartbiz.notification.NotificationService;
import com.smartbiz.repository.ClientRepository;
import com.smartbiz.repository.FactureRepository;
import com.smartbiz.repository.PaiementRepository;
import com.smartbiz.util.NumeroGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FactureService {

    private final FactureRepository factureRepository;
    private final ClientRepository clientRepository;
    private final ProduitService produitService;
    private final PaiementRepository paiementRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public Page<Facture> rechercher(StatutFacture statut, Pageable pageable) {
        return statut != null
                ? factureRepository.findByStatutOrderByDateEmissionDesc(statut, pageable)
                : factureRepository.findAllByOrderByDateEmissionDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Facture trouverParId(Long id) {
        return factureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable : " + id));
    }

    @Transactional
    public Facture creer(FactureDto dto) {
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable : " + dto.getClientId()));

        Facture facture = Facture.builder()
                .client(client)
                .dateEmission(dto.getDateEmission() != null ? dto.getDateEmission() : LocalDate.now())
                .dateEcheance(dto.getDateEcheance())
                .tauxTaxe(dto.getTauxTaxe() != null ? dto.getTauxTaxe() : BigDecimal.ZERO)
                .remiseMontant(dto.getRemiseMontant() != null ? dto.getRemiseMontant() : BigDecimal.ZERO)
                .statut(StatutFacture.BROUILLON)
                .build();

        Facture enregistree = factureRepository.save(facture);
        enregistree.setNumero(NumeroGenerator.generer("FAC", enregistree.getId()));

        auditService.enregistrer("CREATION", "Facture", enregistree.getId().toString(),
                "Creation de la facture " + enregistree.getNumero() + " pour " + client.getNom());
        return enregistree;
    }

    @Transactional
    public Facture ajouterLigne(Long factureId, LigneFactureDto dto) {
        Facture facture = trouverParId(factureId);

        LigneFacture.LigneFactureBuilder ligneBuilder = LigneFacture.builder()
                .facture(facture)
                .libelle(dto.getLibelle())
                .quantite(dto.getQuantite())
                .prixUnitaire(dto.getPrixUnitaire());

        if (dto.getProduitId() != null) {
            Produit produit = produitService.trouverParId(dto.getProduitId());
            ligneBuilder.produit(produit);
        }

        facture.getLignes().add(ligneBuilder.build());

        auditService.enregistrer("MODIFICATION", "Facture", facture.getId().toString(),
                "Ajout d'une ligne a la facture " + facture.getNumero());
        return facture;
    }

    @Transactional
    public void supprimerLigne(Long factureId, Long ligneId) {
        Facture facture = trouverParId(factureId);
        facture.getLignes().removeIf(l -> l.getId().equals(ligneId));
    }

    @Transactional
    public Facture emettre(Long factureId) {
        Facture facture = trouverParId(factureId);
        if (facture.getLignes().isEmpty()) {
            throw new ConflitDonneesException("Impossible d'emettre une facture sans lignes.");
        }
        facture.setStatut(StatutFacture.EMISE);
        auditService.enregistrer("MODIFICATION", "Facture", facture.getId().toString(),
                "Facture " + facture.getNumero() + " emise");
        return facture;
    }

    @Transactional
    public Facture annuler(Long factureId) {
        Facture facture = trouverParId(factureId);
        facture.setStatut(StatutFacture.ANNULEE);
        auditService.enregistrer("MODIFICATION", "Facture", facture.getId().toString(),
                "Facture " + facture.getNumero() + " annulee");
        return facture;
    }

    /**
     * Enregistre un paiement et recalcule automatiquement le statut de la
     * facture (PARTIELLEMENT_PAYEE ou PAYEE) - point 16 du cahier des charges.
     */
    @Transactional
    public Facture enregistrerPaiement(Long factureId, PaiementDto dto) {
        Facture facture = trouverParId(factureId);

        if (facture.getStatut() == StatutFacture.BROUILLON) {
            throw new ConflitDonneesException("Emettez d'abord la facture avant d'enregistrer un paiement.");
        }
        if (facture.getStatut() == StatutFacture.ANNULEE) {
            throw new ConflitDonneesException("Impossible d'encaisser une facture annulee.");
        }
        if (dto.getMontant().compareTo(facture.getResteAPayer()) > 0) {
            throw new ConflitDonneesException(
                    "Le montant depasse le reste a payer (" + facture.getResteAPayer() + ").");
        }

        Paiement paiement = Paiement.builder()
                .facture(facture)
                .montant(dto.getMontant())
                .modePaiement(dto.getModePaiement())
                .reference(dto.getReference())
                .build();
        paiementRepository.save(paiement);
        facture.getPaiements().add(paiement);

        facture.setMontantPaye(facture.getMontantPaye().add(dto.getMontant()));

        if (facture.getMontantPaye().compareTo(facture.getTotal()) >= 0) {
            facture.setStatut(StatutFacture.PAYEE);
        } else {
            facture.setStatut(StatutFacture.PARTIELLEMENT_PAYEE);
        }

        auditService.enregistrer("PAIEMENT", "Facture", facture.getId().toString(),
                "Paiement de " + dto.getMontant() + " enregistre sur la facture " + facture.getNumero());
        notificationService.notifierTous(TypeNotification.NOUVEAU_PAIEMENT,
                "Paiement de " + dto.getMontant() + " recu sur la facture " + facture.getNumero(),
                "/factures/" + facture.getId());
        emailService.envoyerConfirmationPaiement(facture, dto.getMontant().toString());
        return facture;
    }

    @Transactional(readOnly = true)
    public long compterImpayees() {
        return factureRepository.countByStatutIn(List.of(StatutFacture.EMISE, StatutFacture.PARTIELLEMENT_PAYEE, StatutFacture.IMPAYEE));
    }

    /**
     * Chiffre d'affaires = somme des totaux des factures emises/payees
     * (hors brouillons et factures annulees). Calcule cote application via
     * les lignes chargees en une seule requete (voir FactureRepository).
     */
    @Transactional(readOnly = true)
    public BigDecimal chiffreAffaires() {
        return factureRepository.trouverAvecLignesParStatuts(
                        List.of(StatutFacture.EMISE, StatutFacture.PAYEE, StatutFacture.PARTIELLEMENT_PAYEE))
                .stream()
                .map(Facture::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
