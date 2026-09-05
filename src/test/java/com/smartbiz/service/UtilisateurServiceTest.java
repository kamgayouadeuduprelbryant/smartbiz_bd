package com.smartbiz.service;

import com.smartbiz.dto.RegisterRequest;
import com.smartbiz.exception.ConflitDonneesException;
import com.smartbiz.model.Role;
import com.smartbiz.model.RoleType;
import com.smartbiz.model.Utilisateur;
import com.smartbiz.repository.RoleRepository;
import com.smartbiz.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuditService auditService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private UtilisateurService utilisateurService;

    private RegisterRequest requete;

    @BeforeEach
    void setUp() {
        requete = new RegisterRequest();
        requete.setNom("Mballa");
        requete.setPrenom("Jean");
        requete.setEmail("jean.mballa@smartbiz.local");
        requete.setMotDePasse("MotDePasse123");
    }

    @Test
    void inscrire_creeUnCompteAvecLeRoleEmploye() {
        when(utilisateurRepository.existsByEmail(requete.getEmail())).thenReturn(false);
        when(roleRepository.findByNom(RoleType.EMPLOYE))
                .thenReturn(Optional.of(Role.builder().id(1L).nom(RoleType.EMPLOYE).build()));
        when(passwordEncoder.encode(requete.getMotDePasse())).thenReturn("hash-encode");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(inv -> {
            Utilisateur u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        Utilisateur resultat = utilisateurService.inscrire(requete);

        assertThat(resultat.getEmail()).isEqualTo(requete.getEmail());
        assertThat(resultat.getMotDePasse()).isEqualTo("hash-encode");
        assertThat(resultat.getRoles()).extracting(Role::getNom).containsExactly(RoleType.EMPLOYE);
        verify(emailService).envoyerBienvenue(resultat);
        verify(auditService).enregistrer(eq("INSCRIPTION"), eq("Utilisateur"), any(), any());
    }

    @Test
    void inscrire_rejetteUnEmailDejaUtilise() {
        when(utilisateurRepository.existsByEmail(requete.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> utilisateurService.inscrire(requete))
                .isInstanceOf(ConflitDonneesException.class)
                .hasMessageContaining("existe deja");

        verify(utilisateurRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }
}
