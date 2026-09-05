package com.smartbiz.service;

import com.smartbiz.dto.CommandeDto;
import com.smartbiz.exception.ConflitDonneesException;
import com.smartbiz.model.*;
import com.smartbiz.notification.NotificationService;
import com.smartbiz.repository.ClientRepository;
import com.smartbiz.repository.CommandeRepository;
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
class CommandeServiceTest {

    @Mock
    private CommandeRepository commandeRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ProduitService produitService;
    @Mock
    private StockService stockService;
    @Mock
    private AuditService auditService;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CommandeService commandeService;

    private Commande commande;
    private Produit produit;

    @BeforeEach
    void setUp() {
        Client client = Client.builder().id(1L).nom("Librairie du Centre").build();
        produit = Produit.builder().id(1L).reference("PRD-0001").nom("Ramette papier A4")
                .prixAchat(new BigDecimal("2500")).prixVente(new BigDecimal("3500"))
                .quantiteStock(20).seuilMinimum(5).build();

        commande = Commande.builder().id(5L).numero("CMD-000005").client(client)
                .statut(StatutCommande.CONFIRMEE).stockDeduit(false).build();
        commande.getLignes().add(LigneCommande.builder()
                .commande(commande).produit(produit).quantite(5).prixUnitaire(produit.getPrixVente()).build());

        when(commandeRepository.findById(5L)).thenReturn(Optional.of(commande));
    }

    @Test
    void changerStatut_versLivreeDeduitLeStockEtMarqueLaCommande() {
        commandeService.changerStatut(5L, StatutCommande.LIVREE);

        verify(stockService).enregistrerMouvement(argThat(dto ->
                dto.getProduitId().equals(1L)
                        && dto.getType() == TypeMouvementStock.SORTIE
                        && dto.getQuantite().equals(5)));
        assertThat(commande.isStockDeduit()).isTrue();
        assertThat(commande.getStatut()).isEqualTo(StatutCommande.LIVREE);
    }

    @Test
    void changerStatut_versLivreeNeDeduitPasDeuxFoisLeStock() {
        commande.setStockDeduit(true);

        commandeService.changerStatut(5L, StatutCommande.LIVREE);

        verifyNoInteractions(stockService);
        assertThat(commande.getStatut()).isEqualTo(StatutCommande.LIVREE);
    }

    @Test
    void changerStatut_versLivreeSansLignesEstRefusee() {
        commande.getLignes().clear();

        assertThatThrownBy(() -> commandeService.changerStatut(5L, StatutCommande.LIVREE))
                .isInstanceOf(ConflitDonneesException.class)
                .hasMessageContaining("sans lignes");

        verifyNoInteractions(stockService);
    }

    @Test
    void creer_genereUnNumeroDeCommandeApresEnregistrement() {
        Client client = Client.builder().id(2L).nom("Nouveau Client").build();
        when(clientRepository.findById(2L)).thenReturn(Optional.of(client));
        when(commandeRepository.save(any(Commande.class))).thenAnswer(inv -> {
            Commande c = inv.getArgument(0);
            c.setId(99L);
            return c;
        });

        CommandeDto dto = new CommandeDto();
        dto.setClientId(2L);

        Commande resultat = commandeService.creer(dto);

        assertThat(resultat.getNumero()).isEqualTo("CMD-000099");
        verify(notificationService).notifierTous(eq(TypeNotification.NOUVELLE_COMMANDE), any(), any());
    }
}
