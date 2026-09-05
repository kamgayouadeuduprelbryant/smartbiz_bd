package com.smartbiz.service;

import com.smartbiz.dto.EmployeDto;
import com.smartbiz.exception.ConflitDonneesException;
import com.smartbiz.exception.ResourceNotFoundException;
import com.smartbiz.model.Employe;
import com.smartbiz.model.StatutEmploye;
import com.smartbiz.repository.DepartementRepository;
import com.smartbiz.repository.EmployeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeServiceTest {

    @Mock
    private EmployeRepository employeRepository;
    @Mock
    private DepartementRepository departementRepository;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private EmployeService employeService;

    private EmployeDto dto;

    @BeforeEach
    void setUp() {
        dto = new EmployeDto();
        dto.setMatricule("EMP-0099");
        dto.setNom("Ateba");
        dto.setPrenom("Marie");
        dto.setSalaire(new BigDecimal("250000"));
    }

    @Test
    void creer_enregistreUnNouvelEmployeQuandLeMatriculeEstLibre() {
        when(employeRepository.existsByMatricule(dto.getMatricule())).thenReturn(false);
        when(employeRepository.save(any(Employe.class))).thenAnswer(inv -> {
            Employe e = inv.getArgument(0);
            e.setId(42L);
            return e;
        });

        Employe resultat = employeService.creer(dto);

        assertThat(resultat.getId()).isEqualTo(42L);
        assertThat(resultat.getMatricule()).isEqualTo("EMP-0099");
        assertThat(resultat.getStatut()).isEqualTo(StatutEmploye.ACTIF);
        verify(auditService).enregistrer(eq("CREATION"), eq("Employe"), any(), any());
    }

    @Test
    void creer_rejetteUnMatriculeDejaUtilise() {
        when(employeRepository.existsByMatricule(dto.getMatricule())).thenReturn(true);

        assertThatThrownBy(() -> employeService.creer(dto))
                .isInstanceOf(ConflitDonneesException.class)
                .hasMessageContaining("matricule");

        verify(employeRepository, never()).save(any());
    }

    @Test
    void trouverParId_lanceUneExceptionQuandIntrouvable() {
        when(employeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeService.trouverParId(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void supprimer_appelleLeRepositoryEtJournaliseLaction() {
        Employe existant = Employe.builder().id(7L).matricule("EMP-0007").nom("Fotso").prenom("Paul").build();
        when(employeRepository.findById(7L)).thenReturn(Optional.of(existant));

        employeService.supprimer(7L);

        verify(employeRepository).delete(existant);
        verify(auditService).enregistrer(eq("SUPPRESSION"), eq("Employe"), eq("7"), any());
    }
}
