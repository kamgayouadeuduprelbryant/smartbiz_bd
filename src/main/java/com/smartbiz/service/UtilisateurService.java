package com.smartbiz.service;

import com.smartbiz.dto.RegisterRequest;
import com.smartbiz.exception.ConflitDonneesException;
import com.smartbiz.model.Role;
import com.smartbiz.model.RoleType;
import com.smartbiz.model.Utilisateur;
import com.smartbiz.repository.RoleRepository;
import com.smartbiz.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private static final String CARACTERES_MOT_DE_PASSE =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final SecureRandom ALEATOIRE = new SecureRandom();

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final EmailService emailService;

    @Transactional
    public Utilisateur inscrire(RegisterRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new ConflitDonneesException("Un compte existe deja avec cet email.");
        }

        Role roleEmploye = roleRepository.findByNom(RoleType.EMPLOYE)
                .orElseThrow(() -> new IllegalStateException("Le role EMPLOYE n'existe pas en base."));

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .actif(true)
                .compteGoogle(false)
                .roles(Set.of(roleEmploye))
                .build();

        Utilisateur enregistre = utilisateurRepository.save(utilisateur);
        auditService.enregistrer("INSCRIPTION", "Utilisateur", enregistre.getId().toString(),
                enregistre.getEmail() + " a cree un compte.");
        emailService.envoyerBienvenue(enregistre);
        return enregistre;
    }

    /**
     * Reinitialise le mot de passe si un compte local existe pour cet email.
     * Ne revele jamais si l'email existe ou non (evite l'enumeration de comptes) :
     * le controller affiche le meme message de succes dans tous les cas.
     */
    @Transactional
    public void demanderReinitialisationMotDePasse(String email) {
        utilisateurRepository.findByEmail(email).ifPresent(utilisateur -> {
            if (utilisateur.isCompteGoogle() || utilisateur.getMotDePasse() == null) {
                return;
            }

            String motDePasseTemporaire = genererMotDePasseTemporaire();
            utilisateur.setMotDePasse(passwordEncoder.encode(motDePasseTemporaire));

            auditService.enregistrer("MODIFICATION", "Utilisateur", utilisateur.getId().toString(),
                    "Mot de passe reinitialise suite a une demande de recuperation pour " + utilisateur.getEmail());
            emailService.envoyerRecuperationMotDePasse(utilisateur, motDePasseTemporaire);
        });
    }

    private String genererMotDePasseTemporaire() {
        StringBuilder motDePasse = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            motDePasse.append(CARACTERES_MOT_DE_PASSE.charAt(ALEATOIRE.nextInt(CARACTERES_MOT_DE_PASSE.length())));
        }
        return motDePasse.toString();
    }
}