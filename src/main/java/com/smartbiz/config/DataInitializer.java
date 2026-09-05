package com.smartbiz.config;

import com.smartbiz.model.*;
import com.smartbiz.repository.*;
import com.smartbiz.util.NumeroGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Initialise les roles/permissions systeme et un compte administrateur de
 * demonstration au premier demarrage. Les donnees METIER de demonstration
 * (departements/employes d'exemple) sont volontairement separees et
 * minimales : voir la methode creerDonneesDemo().
 *
 * Compte admin de demo : admin@smartbiz.local / Admin@1234
 * A CHANGER en production.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DepartementRepository departementRepository;
    private final EmployeRepository employeRepository;
    private final ClientRepository clientRepository;
    private final FournisseurRepository fournisseurRepository;
    private final CategorieRepository categorieRepository;
    private final ProduitRepository produitRepository;
    private final CommandeRepository commandeRepository;
    private final FactureRepository factureRepository;
    private final PaiementRepository paiementRepository;
    private final RevenuRepository revenuRepository;
    private final DepenseRepository depenseRepository;
    private final ProjetRepository projetRepository;
    private final TacheRepository tacheRepository;
    private final CongeRepository congeRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String[] PERMISSIONS = {
            "EMPLOYE_VIEW", "EMPLOYE_CREATE", "EMPLOYE_EDIT", "EMPLOYE_DELETE",
            "DEPARTEMENT_MANAGE",
            "FINANCE_VIEW", "FINANCE_MANAGE",
            "RAPPORT_VIEW",
            "UTILISATEUR_MANAGE"
    };

    @Override
    @Transactional
    public void run(String... args) {
        var permissions = Arrays.stream(PERMISSIONS)
                .map(code -> permissionRepository.findByCode(code)
                        .orElseGet(() -> permissionRepository.save(Permission.builder().code(code).build())))
                .collect(Collectors.toSet());

        for (RoleType type : RoleType.values()) {
            roleRepository.findByNom(type).orElseGet(() -> {
                Role role = Role.builder().nom(type).build();
                if (type == RoleType.ADMIN) {
                    role.setPermissions(permissions);
                }
                return roleRepository.save(role);
            });
        }

        Role adminRole = roleRepository.findByNom(RoleType.ADMIN).orElseThrow();

        if (!utilisateurRepository.existsByEmail("admin@smartbiz.local")) {
            Utilisateur admin = Utilisateur.builder()
                    .nom("Administrateur")
                    .prenom("SmartBiz")
                    .email("admin@smartbiz.local")
                    .motDePasse(passwordEncoder.encode("Admin@1234"))
                    .actif(true)
                    .compteGoogle(false)
                    .roles(Set.of(adminRole))
                    .build();
            utilisateurRepository.save(admin);
        }

        creerDonneesDemoRH();
        creerDonneesDemoCommercial();
    }

    private void creerDonneesDemoRH() {
        if (departementRepository.count() > 0) {
            return;
        }

        Departement rh = departementRepository.save(Departement.builder().nom("Ressources Humaines").build());
        Departement compta = departementRepository.save(Departement.builder().nom("Comptabilite").build());
        Departement commercial = departementRepository.save(Departement.builder().nom("Commercial").build());

        Employe employe1 = employeRepository.save(Employe.builder()
                .matricule("EMP-0001").nom("Mballa").prenom("Jean").email("jean.mballa@smartbiz.local")
                .poste("Responsable RH").departement(rh).statut(StatutEmploye.ACTIF)
                .build());

        Employe employe2 = employeRepository.save(Employe.builder()
                .matricule("EMP-0002").nom("Ateba").prenom("Marie").email("marie.ateba@smartbiz.local")
                .poste("Comptable").departement(compta).statut(StatutEmploye.ACTIF)
                .build());

        Employe employe3 = employeRepository.save(Employe.builder()
                .matricule("EMP-0003").nom("Fotso").prenom("Paul").email("paul.fotso@smartbiz.local")
                .poste("Commercial").departement(commercial).statut(StatutEmploye.ACTIF)
                .build());

        creerDonneesDemoProjetsEtConges(employe1, employe2, employe3);
    }

    private void creerDonneesDemoProjetsEtConges(Employe employe1, Employe employe2, Employe employe3) {
        Projet projet = projetRepository.save(Projet.builder()
                .nom("Refonte site vitrine").description("Modernisation du site web de l'entreprise")
                .statut(StatutProjet.EN_COURS)
                .dateDebut(LocalDate.now().minusDays(10)).dateEcheance(LocalDate.now().plusDays(20))
                .membres(new java.util.HashSet<>(java.util.List.of(employe1, employe3)))
                .build());

        tacheRepository.save(Tache.builder()
                .projet(projet).titre("Rediger le cahier des charges").responsable(employe1)
                .priorite(PrioriteTache.HAUTE).statut(StatutTache.TERMINEE)
                .dateLimite(LocalDate.now().minusDays(3)).build());

        tacheRepository.save(Tache.builder()
                .projet(projet).titre("Maquettes des pages principales").responsable(employe3)
                .priorite(PrioriteTache.NORMALE).statut(StatutTache.EN_COURS)
                .dateLimite(LocalDate.now().plusDays(5)).build());

        tacheRepository.save(Tache.builder()
                .projet(projet).titre("Integration Bootstrap").responsable(employe3)
                .priorite(PrioriteTache.NORMALE).statut(StatutTache.A_FAIRE)
                .dateLimite(LocalDate.now().plusDays(12)).build());

        congeRepository.save(Conge.builder()
                .employe(employe2).type(TypeConge.CONGE_PAYE)
                .dateDebut(LocalDate.now().plusDays(15)).dateFin(LocalDate.now().plusDays(20))
                .motif("Vacances annuelles").statut(StatutConge.EN_ATTENTE)
                .build());
    }

    private void creerDonneesDemoCommercial() {
        if (produitRepository.count() > 0) {
            return;
        }

        Client client1 = clientRepository.save(Client.builder()
                .nom("Etablissements Nkoulou").email("contact@nkoulou.cm").ville("Douala").actif(true).build());
        Client client2 = clientRepository.save(Client.builder()
                .nom("Librairie du Centre").email("commandes@librairieducentre.cm").ville("Yaounde").actif(true).build());

        Fournisseur fournisseur1 = fournisseurRepository.save(Fournisseur.builder()
                .nom("SOCOBOIS Distribution").email("ventes@socobois.cm").ville("Douala").actif(true).build());
        Fournisseur fournisseur2 = fournisseurRepository.save(Fournisseur.builder()
                .nom("ImportPlus Cameroun").email("contact@importplus.cm").ville("Douala").actif(true).build());

        Categorie fournitures = categorieRepository.save(Categorie.builder().nom("Fournitures de bureau").build());
        Categorie informatique = categorieRepository.save(Categorie.builder().nom("Informatique").build());

        Produit produit1 = produitRepository.save(Produit.builder()
                .reference("PRD-0001").nom("Ramette papier A4").categorie(fournitures).fournisseur(fournisseur1)
                .prixAchat(new BigDecimal("2500")).prixVente(new BigDecimal("3500"))
                .quantiteStock(40).seuilMinimum(10).actif(true).build());

        produitRepository.save(Produit.builder()
                .reference("PRD-0002").nom("Cle USB 32 Go").categorie(informatique).fournisseur(fournisseur2)
                .prixAchat(new BigDecimal("3000")).prixVente(new BigDecimal("4500"))
                .quantiteStock(4).seuilMinimum(5).actif(true).build());

        produitRepository.save(Produit.builder()
                .reference("PRD-0003").nom("Souris optique USB").categorie(informatique).fournisseur(fournisseur2)
                .prixAchat(new BigDecimal("2000")).prixVente(new BigDecimal("3200"))
                .quantiteStock(0).seuilMinimum(3).actif(true).build());

        creerDonneesDemoVentesEtFinance(client1, client2, produit1);
    }

    private void creerDonneesDemoVentesEtFinance(Client client1, Client client2, Produit produit1) {
        // Une commande confirmee (sans lignes livrees, pour ne pas fausser le stock de demo).
        Commande commande = commandeRepository.save(Commande.builder()
                .client(client2).statut(StatutCommande.CONFIRMEE).notes("Commande initiale de demonstration").build());
        commande.setNumero(NumeroGenerator.generer("CMD", commande.getId()));
        commande.getLignes().add(LigneCommande.builder()
                .commande(commande).produit(produit1).quantite(5).prixUnitaire(produit1.getPrixVente()).build());

        // Une facture emise et partiellement payee.
        Facture facture = factureRepository.save(Facture.builder()
                .client(client1).statut(StatutFacture.EMISE)
                .tauxTaxe(new BigDecimal("19.25")).remiseMontant(BigDecimal.ZERO).build());
        facture.setNumero(NumeroGenerator.generer("FAC", facture.getId()));
        facture.getLignes().add(LigneFacture.builder()
                .facture(facture).produit(produit1).libelle(produit1.getNom()).quantite(10).prixUnitaire(produit1.getPrixVente()).build());

        BigDecimal acompte = new BigDecimal("15000");
        Paiement paiement = paiementRepository.save(Paiement.builder()
                .facture(facture).montant(acompte).modePaiement(ModePaiement.MOBILE_MONEY).reference("MTN-DEMO-001").build());
        facture.getPaiements().add(paiement);
        facture.setMontantPaye(acompte);
        facture.setStatut(StatutFacture.PARTIELLEMENT_PAYEE);

        // Quelques revenus et depenses libres pour peupler les graphiques Finance.
        revenuRepository.save(Revenu.builder()
                .libelle("Prestation conseil").montant(new BigDecimal("120000"))
                .categorie(CategorieRevenu.PRESTATION_SERVICE).date(LocalDate.now().minusDays(10)).build());
        revenuRepository.save(Revenu.builder()
                .libelle("Vente comptant").montant(new BigDecimal("45000"))
                .categorie(CategorieRevenu.VENTE).date(LocalDate.now().minusDays(3)).build());

        depenseRepository.save(Depense.builder()
                .libelle("Loyer local commercial").montant(new BigDecimal("80000"))
                .categorie(CategorieDepense.LOYER).date(LocalDate.now().minusDays(15)).build());
        depenseRepository.save(Depense.builder()
                .libelle("Achat fournitures").montant(new BigDecimal("22000"))
                .categorie(CategorieDepense.FOURNITURES).date(LocalDate.now().minusDays(5)).build());
    }
}
