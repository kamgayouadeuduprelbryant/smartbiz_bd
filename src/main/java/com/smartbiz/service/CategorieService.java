package com.smartbiz.service;

import com.smartbiz.exception.ConflitDonneesException;
import com.smartbiz.exception.ResourceNotFoundException;
import com.smartbiz.model.Categorie;
import com.smartbiz.repository.CategorieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategorieService {

    private final CategorieRepository categorieRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<Categorie> listerToutes() {
        return categorieRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Categorie trouverParId(Long id) {
        return categorieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categorie introuvable : " + id));
    }

    @Transactional
    public Categorie creer(Categorie categorie) {
        if (categorieRepository.existsByNomIgnoreCase(categorie.getNom())) {
            throw new ConflitDonneesException("Une categorie porte deja ce nom.");
        }
        Categorie enregistree = categorieRepository.save(categorie);
        auditService.enregistrer("CREATION", "Categorie", enregistree.getId().toString(),
                "Creation de la categorie " + enregistree.getNom());
        return enregistree;
    }

    @Transactional
    public void supprimer(Long id) {
        Categorie existante = trouverParId(id);
        categorieRepository.delete(existante);
        auditService.enregistrer("SUPPRESSION", "Categorie", id.toString(),
                "Suppression de la categorie " + existante.getNom());
    }
}
