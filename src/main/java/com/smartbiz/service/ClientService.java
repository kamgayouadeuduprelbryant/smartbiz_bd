package com.smartbiz.service;

import com.smartbiz.exception.ResourceNotFoundException;
import com.smartbiz.model.Client;
import com.smartbiz.repository.ClientRepository;
import com.smartbiz.specification.ClientSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public Page<Client> rechercher(String recherche, Boolean actif, Pageable pageable) {
        return clientRepository.findAll(ClientSpecification.avecFiltres(recherche, actif), pageable);
    }

    @Transactional(readOnly = true)
    public Client trouverParId(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable : " + id));
    }

    @Transactional
    public Client creer(Client client) {
        Client enregistre = clientRepository.save(client);
        auditService.enregistrer("CREATION", "Client", enregistre.getId().toString(),
                "Creation du client " + enregistre.getNom());
        return enregistre;
    }

    @Transactional
    public Client modifier(Long id, Client donnees) {
        Client existant = trouverParId(id);
        existant.setNom(donnees.getNom());
        existant.setEmail(donnees.getEmail());
        existant.setTelephone(donnees.getTelephone());
        existant.setAdresse(donnees.getAdresse());
        existant.setVille(donnees.getVille());
        existant.setNotes(donnees.getNotes());
        auditService.enregistrer("MODIFICATION", "Client", id.toString(),
                "Modification du client " + existant.getNom());
        return existant;
    }

    @Transactional
    public void supprimer(Long id) {
        Client existant = trouverParId(id);
        clientRepository.delete(existant);
        auditService.enregistrer("SUPPRESSION", "Client", id.toString(),
                "Suppression du client " + existant.getNom());
    }

    @Transactional(readOnly = true)
    public long compterActifs() {
        return clientRepository.countByActifTrue();
    }
}
