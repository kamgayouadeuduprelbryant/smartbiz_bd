package com.smartbiz.service;

import com.smartbiz.dto.MouvementStockDto;
import com.smartbiz.exception.ConflitDonneesException;
import com.smartbiz.model.MouvementStock;
import com.smartbiz.model.Produit;
import com.smartbiz.model.TypeMouvementStock;
import com.smartbiz.model.TypeNotification;
import com.smartbiz.notification.NotificationService;
import com.smartbiz.repository.MouvementStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Toute variation du stock passe par ce service afin que la quantite du
 * produit et l'historique des mouvements restent toujours coherents
 * (point 14 du cahier des charges : le stock est mis a jour automatiquement
 * lors de toute operation d'entree/sortie).
 */
@Service
@RequiredArgsConstructor
public class StockService {

    private final MouvementStockRepository mouvementStockRepository;
    private final ProduitService produitService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    @Transactional
    public MouvementStock enregistrerMouvement(MouvementStockDto dto) {
        Produit produit = produitService.trouverParId(dto.getProduitId());

        if (dto.getType() == TypeMouvementStock.SORTIE && produit.getQuantiteStock() < dto.getQuantite()) {
            throw new ConflitDonneesException(
                    "Stock insuffisant pour " + produit.getNom() + " (disponible : " + produit.getQuantiteStock() + ").");
        }

        int nouvelleQuantite = dto.getType() == TypeMouvementStock.ENTREE
                ? produit.getQuantiteStock() + dto.getQuantite()
                : produit.getQuantiteStock() - dto.getQuantite();
        produit.setQuantiteStock(nouvelleQuantite);

        String email = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "SYSTEME";

        MouvementStock mouvement = MouvementStock.builder()
                .produit(produit)
                .type(dto.getType())
                .quantite(dto.getQuantite())
                .motif(dto.getMotif())
                .utilisateurEmail(email)
                .build();

        MouvementStock enregistre = mouvementStockRepository.save(mouvement);

        auditService.enregistrer(
                dto.getType() == TypeMouvementStock.ENTREE ? "ENTREE_STOCK" : "SORTIE_STOCK",
                "Produit", produit.getId().toString(),
                dto.getType() + " de " + dto.getQuantite() + " unite(s) sur " + produit.getNom()
                        + " (nouveau stock : " + nouvelleQuantite + ")");

        if (dto.getType() == TypeMouvementStock.SORTIE) {
            if (produit.isRupture()) {
                notificationService.notifierTous(TypeNotification.STOCK_FAIBLE,
                        produit.getNom() + " est en rupture de stock.", "/stock/alertes");
            } else if (produit.isStockFaible()) {
                notificationService.notifierTous(TypeNotification.STOCK_FAIBLE,
                        produit.getNom() + " passe en stock faible (" + nouvelleQuantite + " restants).", "/stock/alertes");
            }
        }

        return enregistre;
    }

    @Transactional(readOnly = true)
    public Page<MouvementStock> historique(Pageable pageable) {
        return mouvementStockRepository.findAllByOrderByDateMouvementDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Page<MouvementStock> historiqueParProduit(Long produitId, Pageable pageable) {
        Produit produit = produitService.trouverParId(produitId);
        return mouvementStockRepository.findByProduitOrderByDateMouvementDesc(produit, pageable);
    }
}
