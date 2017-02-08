# Cahier de liaison RESTful API

## Objectif

Implémentation avec Vert.x 3 du contrat de service d’un cahier de liaison minimaliste et exposition via une API REST sécurisée.

## Présentation

Un cahier de liaison est un système de messagerie dans lequel :

* l’enseignant peut diffuser un message à tous les parents de sa classe
* l’enseignant peut envoyer un message à un parent particulier de sa classe
* le parent peut consulter les messages qui lui sont destinés
* le parent peut confirmer la lecture d’un message envoyé par l’enseignant

## Services exposés

En tant qu’enseignant :

* lister les messages
* voir le détails d’un message
* diffuser un message

En tant que parent :

* lister les messages
* voir le détail d’un message
* confirmer la lecture d’un message

