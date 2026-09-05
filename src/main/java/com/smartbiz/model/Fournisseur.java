package com.smartbiz.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.Formula;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fournisseurs", indexes = {
        @Index(name = "idx_fournisseur_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fournisseur extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String nom;

    @Email
    @Column(length = 150)
    private String email;

    @Column(length = 30)
    private String telephone;

    @Column(length = 255)
    private String adresse;

    @Column(length = 100)
    private String ville;

    @Column(length = 500)
    private String notes;

    @Builder.Default
    @Column(nullable = false)
    private boolean actif = true;

    @Builder.Default
    @OneToMany(mappedBy = "fournisseur", fetch = FetchType.LAZY)
    private List<Produit> produits = new ArrayList<>();

    /**
     * Nombre de produits calcule directement en base (sous-requete) au lieu
     * de charger toute la collection "produits" : evite le
     * LazyInitializationException dans fournisseurs/liste (open-in-view
     * desactive) sans les inconvenients d'un fetch join sur une collection
     * paginee (pagination en memoire, doublons).
     */
    @Formula("(select count(p.id) from produits p where p.fournisseur_id = id)")
    private Long nombreProduits;
}