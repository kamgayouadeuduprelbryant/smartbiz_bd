package com.smartbiz.service;

import com.smartbiz.dto.CongeDto;
import com.smartbiz.exception.ConflitDonneesException;
import com.smartbiz.model.Conge;
import com.smartbiz.model.Employe;
import com.smartbiz.model.StatutConge;
import com.smartbiz.model.TypeConge;
import com.smartbiz.model.TypeNotification;
import com.smartbiz.notification.NotificationService;
import com.smartbiz.repository.CongeRepository;
import com.smartbiz.repository.EmployeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CongeServiceTest {

    @Mock
    private CongeRepository congeRepository;
    @Mock
    private EmployeRepository employeRepository;
    @Mock
    private AuditService auditService;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CongeService congeService;

    @Test
    void demander_rejetteUneDateDeFinAnterieureALaDateDeDebut() {
        CongeDto dto = new CongeDto();
        dto.setEmployeId(1L);
        dto.setType(TypeConge.CONGE_PAYE);
        dto.setDateDebut(LocalDate.of(2026, 6, 10));
        dto.setDateFin(LocalDate.of(2026, 6, 5));

        assertThatThrownBy(() -> congeService.demander(dto))
                .isInstanceOf(ConflitDonneesException.class)
                .hasMessageContaining("date de fin");

        verifyNoInteractions(employeRepository, congeRepository);
    }

    @Test
    void demander_creeLaDemandeEtNotifie() {
        Employe employe = Employe.builder().id(1L).matricule("EMP-0002").nom("Ateba").prenom("Marie").build();
        when(employeRepository.findById(1L)).thenReturn(Optional.of(employe));
        when(congeRepository.save(any(Conge.class))).thenAnswer(inv -> {
            Conge c = inv.getArgument(0);
            c.setId(5L);
            return c;
        });

        CongeDto dto = new CongeDto();
        dto.setEmployeId(1L);
        dto.setType(TypeConge.CONGE_PAYE);
        dto.setDateDebut(LocalDate.of(2026, 7, 1));
        dto.setDateFin(LocalDate.of(2026, 7, 10));

        Conge resultat = congeService.demander(dto);

        assertThat(resultat.getStatut()).isEqualTo(StatutConge.EN_ATTENTE);
        assertThat(resultat.getNombreJours()).isEqualTo(10);
        verify(notificationService).notifierTous(eq(TypeNotification.DEMANDE_CONGE), any(), any());
    }

    @Test
    void approuver_changeLeStatutEnApprouve() {
        Employe employe = Employe.builder().id(2L).matricule("EMP-0003").nom("Fotso").prenom("Paul").build();
        Conge conge = Conge.builder().id(8L).employe(employe).statut(StatutConge.EN_ATTENTE)
                .dateDebut(LocalDate.now()).dateFin(LocalDate.now().plusDays(2)).build();
        when(congeRepository.findById(8L)).thenReturn(Optional.of(conge));

        Conge resultat = congeService.approuver(8L, "Accorde");

        assertThat(resultat.getStatut()).isEqualTo(StatutConge.APPROUVE);
        assertThat(resultat.getCommentaireTraitement()).isEqualTo("Accorde");
    }
}
