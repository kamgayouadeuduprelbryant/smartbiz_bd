package com.smartbiz.service;

import com.smartbiz.dto.CommandeDto;
import com.smartbiz.dto.LigneCommandeDto;
import com.smartbiz.dto.MouvementStockDto;
import com.smartbiz.exception.ConflitDonneesException;
import com.smartbiz.exception.ResourceNotFoundException;
import com.smartbiz.model.*;
import com.smartbiz.notification.NotificationService;
import com.smartbiz.repository.ClientRepository;
import com.smartbiz.repository.CommandeRepository;
import com.smartbiz.util.NumeroGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final ClientRepository clientRepository;
    private final ProduitService produitService;
    private final StockService stockService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Page<Commande> rechercher(StatutCommande statut, Pageable pageable) {
        return statut != null
                ? commandeRepository.findByStatutOrderByDateCommandeDesc(statut, pageable)
                : commandeRepository.findAllByOrderByDateCommandeDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Commande trouverParId(Long id) {
        return commandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable : " + id));
    }

    @Transactional
    public Commande creer(CommandeDto dto) {
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable : " + dto.getClientId()));

        Commande commande = Commande.builder()
                .client(client)
                .dateCommande(dto.getDateCommande() != null ? dto.getDateCommande() : java.time.LocalDate.now())
                .notes(dto.getNotes())
                .statut(StatutCommande.EN_ATTENTE)
                .build();

        Commande enregistree = commandeRepository.save(commande);
        enregistree.setNumero(NumeroGenerator.generer("CMD", enregistree.getId()));

        auditService.enregistrer("CREATION", "Commande", enregistree.getId().toString(),
                "Creation de la commande " + enregistree.getNumero() + " pour " + client.getNom());
        notificationService.notifierTous(TypeNotification.NOUVELLE_COMMANDE,
                "Nouvelle commande " + enregistree.getNumero() + " pour " + client.getNom(),
                "/commandes/" + enregistree.getId());
        return enregistree;
    }

    @Transactional
    public Commande ajouterLigne(Long commandeId, LigneCommandeDto dto) {
        Commande commande = trouverParId(commandeId);
        Produit produit = produitService.trouverParId(dto.getProduitId());

        LigneCommande ligne = LigneCommande.builder()
                .commande(commande)
                .produit(produit)
                .quantite(dto.getQuantite())
                .prixUnitaire(produit.getPrixVente())
                .build();

        commande.getLignes().add(ligne);
        auditService.enregistrer("MODIFICATION", "Commande", commande.getId().toString(),
                "Ajout de " + dto.getQuantite() + " x " + produit.getNom() + " a la commande " + commande.getNumero());
        return commande;
    }

    @Transactional
    public void supprimerLigne(Long commandeId, Long ligneId) {
        Commande commande = trouverParId(commandeId);
        commande.getLignes().removeIf(l -> l.getId().equals(ligneId));
    }

    @Transactional
    public Commande changerStatut(Long commandeId, StatutCommande nouveauStatut) {
        Commande commande = trouverParId(commandeId);

        if (nouveauStatut == StatutCommande.LIVREE && !commande.isStockDeduit()) {
            if (commande.getLignes().isEmpty()) {
                throw new ConflitDonneesException("Impossible de livrer une commande sans lignes.");
            }
            for (LigneCommande ligne : commande.getLignes()) {
                MouvementStockDto mouvement = new MouvementStockDto();
                mouvement.setProduitId(ligne.getProduit().getId());
                mouvement.setType(TypeMouvementStock.SORTIE);
                mouvement.setQuantite(ligne.getQuantite());
                mouvement.setMotif("Livraison commande " + commande.getNumero());
                stockService.enregistrerMouvement(mouvement);
            }
            commande.setStockDeduit(true);
        }

        commande.setStatut(nouveauStatut);
        auditService.enregistrer("MODIFICATION", "Commande", commande.getId().toString(),
                "Commande " + commande.getNumero() + " passee au statut " + nouveauStatut);
        return commande;
    }

    @Transactional(readOnly = true)
    public long compterParStatut(StatutCommande statut) {
        return commandeRepository.countByStatut(statut);
    }
}
