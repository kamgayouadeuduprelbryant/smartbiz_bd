# SmartBiz

**Plateforme intelligente de gestion d'entreprise**
*"Pilotez votre entreprise. Simplement. Intelligemment."*

## Presentation

SmartBiz est une application web monolithique (Spring Boot + Thymeleaf) destinee aux PME
pour gerer leur organisation, leurs employes, leurs clients, leurs produits, leurs stocks,
leurs commandes, leurs factures, leurs finances, leurs projets et bien plus.

> **Etat actuel du projet (Partie 1)** : les fondations sont posees et fonctionnelles —
> authentification (formulaire + Google OAuth2), securite par role, gestion des
> Departements et des Employes (CRUD complet), dashboard avec statistiques reelles et
> rafraichissement Fetch API. Les modules Clients/Fournisseurs/Produits/Stock/Commandes/
> Factures/Finances/Projets/Conges/Presences/Rapports seront ajoutes dans les parties
> suivantes, en s'appuyant sur cette meme architecture.

## Fonctionnalites (partie livree)

- Connexion par email/mot de passe **et** "Continuer avec Google" (OAuth2)
- Inscription, roles (ADMIN, MANAGER, RH, COMPTABLE, COMMERCIAL, EMPLOYE) verifies cote serveur
- Gestion des departements (CRUD)
- Gestion des employes (CRUD, recherche, filtres, pagination, tri)
- Dashboard avec statistiques reelles issues de PostgreSQL, rafraichies via Fetch API
- Journal d'audit (creations/modifications/suppressions)
- Pages d'erreur 400/403/404/500 personnalisees
- Interface responsive (sidebar en menu mobile via Bootstrap Offcanvas)

## Technologies

Java 21 - Spring Boot 3.3 - Spring MVC - Thymeleaf - Spring Data JPA / Hibernate -
PostgreSQL - Spring Security - OAuth2 Client (Google) - Bootstrap 5 - Bootstrap Icons -
Chart.js - Fetch API - Maven - Lombok

## Architecture

```
src/main/java/com/smartbiz/
├── SmartBizApplication.java
├── config/            # JPA auditing, donnees de demarrage (roles, admin, demo)
├── security/          # Spring Security, UserDetails, OAuth2, filtres
├── controller/        # Controleurs MVC (Thymeleaf)
├── service/           # Logique metier
├── repository/        # Spring Data JPA
├── model/             # Entites JPA
├── dto/                # Objets de transfert pour les formulaires
├── specification/     # Recherche/filtre dynamique (JPA Specification)
├── exception/         # Exceptions metier + gestion globale des erreurs
├── mapper/            # (reserve pour les futurs modules)
├── notification/      # (reserve pour le systeme de notifications temps reel)
└── util/               # (reserve)

src/main/resources/
├── templates/          # Vues Thymeleaf (fragments reutilisables : sidebar, header, pagination, modales)
├── static/css, js/     # Styles et scripts organises par domaine
└── application.properties
```

## Installation

### Prerequis

- JDK 21
- Maven 3.9+
- PostgreSQL 14+

### 1. Creer la base de donnees

```sql
CREATE DATABASE smartbiz;
CREATE USER smartbiz_user WITH ENCRYPTED PASSWORD 'changeme';
GRANT ALL PRIVILEGES ON DATABASE smartbiz TO smartbiz_user;
```

### 2. Configurer les variables d'environnement

Copiez `src/main/resources/application-local.properties.example` en
`application-local.properties` (ignore par git) et renseignez vos valeurs, **ou**
exportez directement les variables d'environnement :

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/smartbiz
export DATABASE_USERNAME=smartbiz_user
export DATABASE_PASSWORD=changeme
export GOOGLE_CLIENT_ID=xxxxx.apps.googleusercontent.com
export GOOGLE_CLIENT_SECRET=xxxxx
```

### 3. Configurer Google OAuth2

1. Rendez-vous sur [Google Cloud Console](https://console.cloud.google.com/apis/credentials).
2. Creez un identifiant OAuth 2.0 de type "Application Web".
3. Ajoutez comme URI de redirection autorisee :
   `http://localhost:8080/login/oauth2/code/google`
4. Recuperez le `Client ID` et le `Client Secret` et placez-les dans vos variables d'environnement.

### 4. Lancer l'application

```bash
mvn spring-boot:run
```

Ouvrez ensuite : http://localhost:8080

## Comptes de demonstration

| Role  | Email                    | Mot de passe |
|-------|---------------------------|--------------|
| ADMIN | admin@smartbiz.local       | Admin@1234   |

**A changer immediatement en environnement de production.**

## Structure du projet

Voir le dossier `src/main` : l'application suit une architecture en couches
(controller -> service -> repository -> model), avec DTO pour les formulaires et
Specifications JPA pour les recherches/filtres dynamiques (evite les requetes
fragiles construites a la main et les problemes de N+1 grace au lazy loading maitrise).

## Suite du projet

Les parties suivantes ajouteront, module par module et en respectant cette meme
architecture : Clients, Fournisseurs, Produits/Categories, Stock/Mouvements,
Commandes/Lignes, Facturation (PDF), Finances (Revenus/Depenses), Projets/Taches
(vue Kanban), Conges/Absences/Presences, Notifications temps reel, Rapports
(PDF/Excel), Recherche globale, Import/Export, Emails transactionnels, et les tests
JUnit/Mockito.
