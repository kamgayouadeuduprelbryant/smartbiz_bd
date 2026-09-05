package com.smartbiz.service;

import com.smartbiz.dto.FinanceStatsDto;
import com.smartbiz.dto.MontantMensuelDto;
import com.smartbiz.exception.ResourceNotFoundException;
import com.smartbiz.model.Depense;
import com.smartbiz.model.Revenu;
import com.smartbiz.repository.DepenseRepository;
import com.smartbiz.repository.RevenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final RevenuRepository revenuRepository;
    private final DepenseRepository depenseRepository;
    private final FactureService factureService;
    private final AuditService auditService;

    // ---------- Revenus ----------

    @Transactional(readOnly = true)
    public Page<Revenu> listerRevenus(Pageable pageable) {
        return revenuRepository.findAllByOrderByDateDesc(pageable);
    }

    @Transactional
    public Revenu creerRevenu(Revenu revenu) {
        Revenu enregistre = revenuRepository.save(revenu);
        auditService.enregistrer("CREATION", "Revenu", enregistre.getId().toString(),
                "Ajout du revenu " + enregistre.getLibelle() + " (" + enregistre.getMontant() + ")");
        return enregistre;
    }

    @Transactional
    public void supprimerRevenu(Long id) {
        Revenu revenu = revenuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Revenu introuvable : " + id));
        revenuRepository.delete(revenu);
        auditService.enregistrer("SUPPRESSION", "Revenu", id.toString(), "Suppression du revenu " + revenu.getLibelle());
    }

    // ---------- Depenses ----------

    @Transactional(readOnly = true)
    public Page<Depense> listerDepenses(Pageable pageable) {
        return depenseRepository.findAllByOrderByDateDesc(pageable);
    }

    @Transactional
    public Depense creerDepense(Depense depense) {
        Depense enregistree = depenseRepository.save(depense);
        auditService.enregistrer("CREATION", "Depense", enregistree.getId().toString(),
                "Ajout de la depense " + enregistree.getLibelle() + " (" + enregistree.getMontant() + ")");
        return enregistree;
    }

    @Transactional
    public void supprimerDepense(Long id) {
        Depense depense = depenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Depense introuvable : " + id));
        depenseRepository.delete(depense);
        auditService.enregistrer("SUPPRESSION", "Depense", id.toString(), "Suppression de la depense " + depense.getLibelle());
    }

    // ---------- Statistiques ----------

    /**
     * CA = total des factures emises/payees. Depenses = ventes de
     * marchandises + depenses libres saisies dans ce module. Benefice = CA - depenses.
     * (point 17 du cahier des charges)
     */
    @Transactional(readOnly = true)
    public FinanceStatsDto calculerStats() {
        BigDecimal ca = factureService.chiffreAffaires();

        LocalDate debutAnnee = LocalDate.now().withDayOfYear(1);
        LocalDate finAnnee = debutAnnee.plusYears(1).minusDays(1);
        BigDecimal totalDepenses = depenseRepository.sommeEntre(debutAnnee, finAnnee);

        BigDecimal benefice = ca.subtract(totalDepenses);

        LocalDate depuis = LocalDate.now().minusMonths(5).withDayOfMonth(1);

        return FinanceStatsDto.builder()
                .chiffreAffaires(ca)
                .totalDepenses(totalDepenses)
                .benefice(benefice)
                .revenusMensuels(convertirSeries(revenuRepository.sommesMensuellesDepuis(depuis)))
                .depensesMensuelles(convertirSeries(depenseRepository.sommesMensuellesDepuis(depuis)))
                .build();
    }

    private List<MontantMensuelDto> convertirSeries(List<Object[]> lignes) {
        return lignes.stream()
                .map(ligne -> {
                    LocalDate date = convertirEnLocalDate(ligne[0]);
                    String libelleMois = date.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH);
                    BigDecimal montant = (BigDecimal) ligne[1];
                    return new MontantMensuelDto(libelleMois, montant);
                })
                .collect(Collectors.toList());
    }

    /**
     * Le type Java renvoye pour function('date_trunc', ...) depend de la
     * version d'Hibernate (Timestamp, LocalDateTime ou directement LocalDate
     * selon les versions) : on gere ici tous les cas plutot que de forcer
     * un cast unique et fragile.
     */
    private LocalDate convertirEnLocalDate(Object valeur) {
        if (valeur instanceof LocalDate localDate) {
            return localDate;
        }
        if (valeur instanceof java.time.LocalDateTime localDateTime) {
            return localDateTime.toLocalDate();
        }
        if (valeur instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }
        if (valeur instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        throw new IllegalStateException("Type de date inattendu pour la statistique mensuelle : " +
                (valeur == null ? "null" : valeur.getClass()));
    }
}