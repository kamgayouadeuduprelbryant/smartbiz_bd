package com.smartbiz.service;

import com.smartbiz.model.Conge;
import com.smartbiz.model.Facture;
import com.smartbiz.model.Utilisateur;
import com.smartbiz.util.FacturePdfGenerator;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Service d'envoi d'emails transactionnels (point 29 du cahier des charges).
 * Toutes les methodes avalent les erreurs SMTP et se contentent de logger :
 * un probleme de configuration mail ne doit jamais faire planter une
 * operation metier (creation de compte, facturation, etc.).
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public void envoyerBienvenue(Utilisateur utilisateur) {
        envoyerTexte(
                utilisateur.getEmail(),
                "Bienvenue sur SmartBiz",
                "Bonjour " + utilisateur.getPrenom() + ",\n\n" +
                        "Votre compte SmartBiz a ete cree avec succes.\n" +
                        "Vous pouvez des a present vous connecter avec votre adresse email.\n\n" +
                        "L'equipe SmartBiz"
        );
    }

    public void envoyerFacture(Facture facture) {
        if (facture.getClient().getEmail() == null) {
            log.info("Aucun email pour le client {}, facture {} non envoyee par email.",
                    facture.getClient().getNom(), facture.getNumero());
            return;
        }

        try {
            byte[] pdf = FacturePdfGenerator.generer(facture);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(facture.getClient().getEmail());
            helper.setSubject("Votre facture " + facture.getNumero() + " - SmartBiz");
            helper.setText("Bonjour " + facture.getClient().getNom() + ",\n\nVeuillez trouver ci-joint votre facture "
                    + facture.getNumero() + " d'un montant de " + facture.getTotal() + ".\n\nL'equipe SmartBiz");
            helper.addAttachment(facture.getNumero() + ".pdf", new org.springframework.core.io.ByteArrayResource(pdf));

            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Echec de l'envoi de la facture {} par email : {}", facture.getNumero(), e.getMessage());
        }
    }

    public void envoyerRecuperationMotDePasse(Utilisateur utilisateur, String motDePasseTemporaire) {
        envoyerTexte(
                utilisateur.getEmail(),
                "Reinitialisation de votre mot de passe SmartBiz",
                "Bonjour " + utilisateur.getPrenom() + ",\n\n" +
                        "Voici votre mot de passe temporaire : " + motDePasseTemporaire + "\n" +
                        "Connectez-vous puis changez-le immediatement depuis Parametres > Mon profil > Changer le mot de passe.\n\n" +
                        "Si vous n'etes pas a l'origine de cette demande, ignorez cet email.\n\n" +
                        "L'equipe SmartBiz"
        );
    }

    public void envoyerConfirmationPaiement(Facture facture, String montant) {
        if (facture.getClient().getEmail() == null) {
            return;
        }
        envoyerTexte(
                facture.getClient().getEmail(),
                "Confirmation de paiement - Facture " + facture.getNumero(),
                "Bonjour " + facture.getClient().getNom() + ",\n\n" +
                        "Nous confirmons la reception de votre paiement de " + montant + " pour la facture "
                        + facture.getNumero() + ".\n\nL'equipe SmartBiz"
        );
    }

    public void envoyerDemandeConge(Conge conge, String emailDestinataire) {
        envoyerTexte(
                emailDestinataire,
                "Nouvelle demande de conge",
                conge.getEmploye().getPrenom() + " " + conge.getEmploye().getNom() +
                        " a soumis une demande de conge du " + conge.getDateDebut() + " au " + conge.getDateFin() + "."
        );
    }

    private void envoyerTexte(String destinataire, String sujet, String corps) {
        try {
            var message = new org.springframework.mail.SimpleMailMessage();
            message.setTo(destinataire);
            message.setSubject(sujet);
            message.setText(corps);
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Echec de l'envoi d'email a {} ({}) : {}", destinataire, sujet, e.getMessage());
        }
    }
}