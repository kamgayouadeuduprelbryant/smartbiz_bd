package com.smartbiz.service;

import com.smartbiz.dto.PaiementDto;
import com.smartbiz.exception.ConflitDonneesException;
import com.smartbiz.model.*;
import com.smartbiz.notification.NotificationService;
import com.smartbiz.repository.ClientRepository;
import com.smartbiz.repository.FactureRepository;
import com.smartbiz.repository.PaiementRepository;
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
class FactureServiceTest {

    @Mock
    private FactureRepository factureRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ProduitService produitService;
    @Mock
    private PaiementRepository paiementRepository;
    @Mock
    private AuditService auditService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private FactureService factureService;

    private Facture facture;

    @BeforeEach
    void setUp() {
        Client client = Client.builder().id(1L).nom("Etablissements Nkoulou").email("contact@nkoulou.cm").build();

        facture = Facture.builder()
                .id(10L).numero("FAC-000010").client(client)
                .statut(StatutFacture.EMISE)
                .tauxTaxe(BigDecimal.ZERO).remiseMontant(BigDecimal.ZERO)
                .montantPaye(BigDecimal.ZERO)
                .build();

        LigneFacture ligne = LigneFacture.builder()
                .facture(facture).libelle("Prestation").quantite(1).prixUnitaire(new BigDecimal("100000"))
                .build();
        facture.getLignes().add(ligne);

        when(factureRepository.findById(10L)).thenReturn(Optional.of(facture));
        when(paiementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void enregistrerPaiement_paiementPartielPasseLaFactureEnPartiellementPayee() {
        PaiementDto dto = new PaiementDto();
        dto.setMontant(new BigDecimal("40000"));
        dto.setModePaiement(ModePaiement.MOBILE_MONEY);

        Facture resultat = factureService.enregistrerPaiement(10L, dto);

        assertThat(resultat.getStatut()).isEqualTo(StatutFacture.PARTIELLEMENT_PAYEE);
        assertThat(resultat.getMontantPaye()).isEqualByComparingTo("40000");
        assertThat(resultat.getResteAPayer()).isEqualByComparingTo("60000");
        verify(emailService).envoyerConfirmationPaiement(eq(facture), any());
    }

    @Test
    void enregistrerPaiement_paiementCompletPasseLaFactureEnPayee() {
        PaiementDto dto = new PaiementDto();
        dto.setMontant(new BigDecimal("100000"));
        dto.setModePaiement(ModePaiement.VIREMENT);

        Facture resultat = factureService.enregistrerPaiement(10L, dto);

        assertThat(resultat.getStatut()).isEqualTo(StatutFacture.PAYEE);
        assertThat(resultat.getResteAPayer()).isEqualByComparingTo("0");
    }

    @Test
    void enregistrerPaiement_rejetteUnMontantSuperieurAuResteAPayer() {
        PaiementDto dto = new PaiementDto();
        dto.setMontant(new BigDecimal("150000"));
        dto.setModePaiement(ModePaiement.ESPECES);

        assertThatThrownBy(() -> factureService.enregistrerPaiement(10L, dto))
                .isInstanceOf(ConflitDonneesException.class)
                .hasMessageContaining("depasse le reste a payer");

        verify(paiementRepository, never()).save(any());
    }

    @Test
    void enregistrerPaiement_rejetteUnPaiementSurUneFactureBrouillon() {
        facture.setStatut(StatutFacture.BROUILLON);

        PaiementDto dto = new PaiementDto();
        dto.setMontant(new BigDecimal("10000"));
        dto.setModePaiement(ModePaiement.ESPECES);

        assertThatThrownBy(() -> factureService.enregistrerPaiement(10L, dto))
                .isInstanceOf(ConflitDonneesException.class)
                .hasMessageContaining("Emettez d'abord");
    }

    @Test
    void enregistrerPaiement_rejetteUnPaiementSurUneFactureAnnulee() {
        facture.setStatut(StatutFacture.ANNULEE);

        PaiementDto dto = new PaiementDto();
        dto.setMontant(new BigDecimal("10000"));
        dto.setModePaiement(ModePaiement.ESPECES);

        assertThatThrownBy(() -> factureService.enregistrerPaiement(10L, dto))
                .isInstanceOf(ConflitDonneesException.class)
                .hasMessageContaining("annulee");
    }
}
