package com.smartbiz.service;

import com.smartbiz.exception.ResourceNotFoundException;
import com.smartbiz.model.Fournisseur;
import com.smartbiz.repository.FournisseurRepository;
import com.smartbiz.specification.FournisseurSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FournisseurService {

    private final FournisseurRepository fournisseurRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public Page<Fournisseur> rechercher(String recherche, Boolean actif, Pageable pageable) {
        return fournisseurRepository.findAll(FournisseurSpecification.avecFiltres(recherche, actif), pageable);
    }

    @Transactional(readOnly = true)
    public Fournisseur trouverParId(Long id) {
        return fournisseurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public Fournisseur trouverParIdAvecProduits(Long id) {
        return fournisseurRepository.trouverParIdAvecProduits(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur introuvable : " + id));
    }

    @Transactional
    public Fournisseur creer(Fournisseur fournisseur) {
        Fournisseur enregistre = fournisseurRepository.save(fournisseur);
        auditService.enregistrer("CREATION", "Fournisseur", enregistre.getId().toString(),
                "Creation du fournisseur " + enregistre.getNom());
        return enregistre;
    }

    @Transactional
    public Fournisseur modifier(Long id, Fournisseur donnees) {
        Fournisseur existant = trouverParId(id);
        existant.setNom(donnees.getNom());
        existant.setEmail(donnees.getEmail());
        existant.setTelephone(donnees.getTelephone());
        existant.setAdresse(donnees.getAdresse());
        existant.setVille(donnees.getVille());
        existant.setNotes(donnees.getNotes());
        auditService.enregistrer("MODIFICATION", "Fournisseur", id.toString(),
                "Modification du fournisseur " + existant.getNom());
        return existant;
    }

    @Transactional
    public void supprimer(Long id) {
        Fournisseur existant = trouverParId(id);
        fournisseurRepository.delete(existant);
        auditService.enregistrer("SUPPRESSION", "Fournisseur", id.toString(),
                "Suppression du fournisseur " + existant.getNom());
    }

    @Transactional(readOnly = true)
    public long compterActifs() {
        return fournisseurRepository.countByActifTrue();
    }
}