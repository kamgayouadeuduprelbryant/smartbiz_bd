package com.smartbiz.service;

import com.smartbiz.dto.MouvementStockDto;
import com.smartbiz.exception.ConflitDonneesException;
import com.smartbiz.model.Produit;
import com.smartbiz.model.TypeMouvementStock;
import com.smartbiz.model.TypeNotification;
import com.smartbiz.notification.NotificationService;
import com.smartbiz.repository.MouvementStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private MouvementStockRepository mouvementStockRepository;
    @Mock
    private ProduitService produitService;
    @Mock
    private AuditService auditService;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private StockService stockService;

    private Produit produit;

    @BeforeEach
    void setUp() {
        produit = Produit.builder()
                .id(1L).reference("PRD-0001").nom("Ramette papier A4")
                .prixAchat(new BigDecimal("2500")).prixVente(new BigDecimal("3500"))
                .quantiteStock(10).seuilMinimum(5).actif(true)
                .build();
    }

    @Test
    void enregistrerMouvement_entreeAugmenteLeStock() {
        when(produitService.trouverParId(1L)).thenReturn(produit);
        when(mouvementStockRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MouvementStockDto dto = new MouvementStockDto();
        dto.setProduitId(1L);
        dto.setType(TypeMouvementStock.ENTREE);
        dto.setQuantite(20);

        stockService.enregistrerMouvement(dto);

        assertThat(produit.getQuantiteStock()).isEqualTo(30);
        verifyNoInteractions(notificationService);
    }

    @Test
    void enregistrerMouvement_sortieRefuseeSiStockInsuffisant() {
        when(produitService.trouverParId(1L)).thenReturn(produit);

        MouvementStockDto dto = new MouvementStockDto();
        dto.setProduitId(1L);
        dto.setType(TypeMouvementStock.SORTIE);
        dto.setQuantite(50);

        assertThatThrownBy(() -> stockService.enregistrerMouvement(dto))
                .isInstanceOf(ConflitDonneesException.class)
                .hasMessageContaining("Stock insuffisant");

        verify(mouvementStockRepository, never()).save(any());
        assertThat(produit.getQuantiteStock()).isEqualTo(10);
    }

    @Test
    void enregistrerMouvement_sortiePassantSousLeSeuilDeclencheUneNotification() {
        when(produitService.trouverParId(1L)).thenReturn(produit);
        when(mouvementStockRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MouvementStockDto dto = new MouvementStockDto();
        dto.setProduitId(1L);
        dto.setType(TypeMouvementStock.SORTIE);
        dto.setQuantite(8); // 10 - 8 = 2, en dessous du seuil de 5

        stockService.enregistrerMouvement(dto);

        assertThat(produit.getQuantiteStock()).isEqualTo(2);
        verify(notificationService).notifierTous(eq(TypeNotification.STOCK_FAIBLE), any(), any());
    }

    @Test
    void enregistrerMouvement_sortieViderLeStockEstAutorisee() {
        when(produitService.trouverParId(1L)).thenReturn(produit);
        when(mouvementStockRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MouvementStockDto dto = new MouvementStockDto();
        dto.setProduitId(1L);
        dto.setType(TypeMouvementStock.SORTIE);
        dto.setQuantite(10);

        stockService.enregistrerMouvement(dto);

        assertThat(produit.getQuantiteStock()).isZero();
        verify(notificationService).notifierTous(eq(TypeNotification.STOCK_FAIBLE), any(), any());
    }
}
