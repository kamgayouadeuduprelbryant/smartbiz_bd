package com.smartbiz.service;

import com.smartbiz.dto.ChangerMotDePasseDto;
import com.smartbiz.dto.ProfilDto;
import com.smartbiz.exception.ConflitDonneesException;
import com.smartbiz.exception.ResourceNotFoundException;
import com.smartbiz.model.Utilisateur;
import com.smartbiz.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfilService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public Utilisateur trouverParEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + email));
    }

    @Transactional
    public Utilisateur modifierProfil(String emailActuel, ProfilDto dto) {
        Utilisateur utilisateur = trouverParEmail(emailActuel);

        if (!utilisateur.getEmail().equalsIgnoreCase(dto.getEmail())
                && utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new ConflitDonneesException("Cette adresse email est deja utilisee.");
        }

        utilisateur.setNom(dto.getNom());
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setTelephone(dto.getTelephone());

        auditService.enregistrer("MODIFICATION", "Utilisateur", utilisateur.getId().toString(),
                "Mise a jour du profil de " + utilisateur.getEmail());
        return utilisateur;
    }

    @Transactional
    public void changerMotDePasse(String email, ChangerMotDePasseDto dto) {
        Utilisateur utilisateur = trouverParEmail(email);

        if (utilisateur.getMotDePasse() == null) {
            throw new ConflitDonneesException("Ce compte est connecte via Google et n'a pas de mot de passe local.");
        }
        if (!passwordEncoder.matches(dto.getMotDePasseActuel(), utilisateur.getMotDePasse())) {
            throw new ConflitDonneesException("Le mot de passe actuel est incorrect.");
        }
        if (!dto.getNouveauMotDePasse().equals(dto.getConfirmation())) {
            throw new ConflitDonneesException("La confirmation ne correspond pas au nouveau mot de passe.");
        }

        utilisateur.setMotDePasse(passwordEncoder.encode(dto.getNouveauMotDePasse()));
        auditService.enregistrer("MODIFICATION", "Utilisateur", utilisateur.getId().toString(),
                "Changement de mot de passe pour " + utilisateur.getEmail());
    }
}
