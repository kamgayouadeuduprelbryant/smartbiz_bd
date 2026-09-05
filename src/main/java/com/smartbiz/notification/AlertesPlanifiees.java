package com.smartbiz.notification;

import com.smartbiz.model.StatutFacture;
import com.smartbiz.model.TypeNotification;
import com.smartbiz.repository.FactureRepository;
import com.smartbiz.service.TacheService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Genere automatiquement des notifications pour les evenements qui ne
 * dependent pas d'une action utilisateur immediate : taches proches de leur
 * echeance, factures en retard de paiement (points 22 et 16 du cahier des
 * charges). S'execute une fois par jour a 7h.
 */
@Component
@RequiredArgsConstructor
public class AlertesPlanifiees {

    private static final Logger log = LoggerFactory.getLogger(AlertesPlanifiees.class);

    private final TacheService tacheService;
    private final FactureRepository factureRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 7 * * *")
    public void verifierTachesProchesEcheance() {
        var taches = tacheService.tachesProchesEcheance();
        for (var tache : taches) {
            notificationService.notifierTous(TypeNotification.TACHE_ECHEANCE_PROCHE,
                    "La tache \"" + tache.getTitre() + "\" approche de son echeance (" + tache.getDateLimite() + ").",
                    "/projets/" + tache.getProjet().getId());
        }
        log.info("Alertes planifiees : {} tache(s) proche(s) de l'echeance notifiee(s).", taches.size());
    }

    @Scheduled(cron = "0 30 7 * * *")
    public void verifierFacturesImpayees() {
        List<StatutFacture> statutsSuivis = List.of(StatutFacture.EMISE, StatutFacture.PARTIELLEMENT_PAYEE);
        var factures = factureRepository.trouverAvecLignesParStatuts(statutsSuivis);

        for (var facture : factures) {
            if (facture.getDateEcheance() != null && facture.getDateEcheance().isBefore(LocalDate.now())) {
                notificationService.notifierTous(TypeNotification.FACTURE_IMPAYEE,
                        "La facture " + facture.getNumero() + " est en retard de paiement.",
                        "/factures/" + facture.getId());
            }
        }
    }
}
