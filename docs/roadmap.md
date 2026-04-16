# Roadmap — IA / FactionAgent / Agent LLM

---

## Étape 1 : Mise en place du LLM local

### Features / tickets
- [ ] Installer et configurer Ollama
- [ ] Lancer un modèle (ex : Mistral)
- [ ] Vérifier qu'il répond en local via CLI

### Objectifs pédagogiques
- Comprendre ce qu'est un LLM
- Comprendre la différence entre API distante vs exécution locale
- Comprendre la notion de modèle vs runtime

### Glossaire
| Terme | Définition |
|-------|-----------|
| **LLM** | Large Language Model — modèle génératif basé sur des probabilités de tokens |
| **Token** | Unité de texte (mot ou fragment) |
| **Inference** | Génération de texte par le modèle |
| **Modèle local** | LLM exécuté sur ta machine, sans API externe |

---

## Étape 2 : Création de l'API FactionAgent (LLM orchestré par le code)

### Features / tickets
- [ ] Créer un backend (Java / Spring ou équivalent)
- [ ] Endpoint `/generateFaction`
- [ ] Appel HTTP vers Ollama
- [ ] Retour brut du LLM

### Objectifs pédagogiques
- Comprendre un système où le code pilote le LLM
- Mettre en place une architecture simple : LLM → API → client

### Glossaire
| Terme | Définition |
|-------|-----------|
| **Orchestration** | Le code contrôle le flux et appelle le LLM |
| **Prompt** | Texte envoyé au LLM pour guider la réponse |
| **Backend** | Serveur qui expose une API |
| **Endpoint** | Point d'entrée HTTP |

---

## Étape 3 : Génération de contenu narratif (Faction)

### Features / tickets
- [ ] Prompt de génération de faction
- [ ] Retour texte structuré (nom, type, objectif…)

### Objectifs pédagogiques
- Comprendre comment guider un LLM
- Observer les limites (variabilité, hallucinations)

### Glossaire
| Terme | Définition |
|-------|-----------|
| **Hallucination** | Réponse plausible mais incorrecte |
| **Température** | Paramètre influençant la créativité |
| **Top-p / sampling** | Stratégie de sélection des tokens |

---

## Étape 4 : Introduction du JSON (structuration des sorties)

### Features / tickets
- [ ] Modifier le prompt pour demander du JSON
- [ ] Parser la réponse côté backend

### Objectifs pédagogiques
- Comprendre la difficulté de contraindre un LLM
- Passer de texte libre à données exploitables

### Glossaire
| Terme | Définition |
|-------|-----------|
| **JSON** | Format structuré clé/valeur |
| **Parsing** | Transformation texte → objet exploitable |
| **Schéma** | Structure attendue d'un JSON |

---

## Étape 5 : Parsing tolérant et nettoyage

### Features / tickets
- [ ] Nettoyer les réponses du LLM
- [ ] Extraire le JSON même s'il est bruité
- [ ] Gérer les erreurs

### Objectifs pédagogiques
- Comprendre que le LLM n'est pas fiable nativement
- Introduire de la robustesse côté code

### Glossaire
| Terme | Définition |
|-------|-----------|
| **Post-processing** | Traitement après génération |
| **Regex** | Outil de recherche de motifs |
| **Fallback** | Stratégie alternative en cas d'échec |

---

## Étape 6 : Prompt engineering avancé (fiabilisation)

### Features / tickets
- [ ] Écrire un prompt strict (format imposé)
- [ ] Ajouter des exemples (few-shot)
- [ ] Réduire les réponses parasites

### Objectifs pédagogiques
- Apprendre à contraindre un LLM efficacement
- Comprendre l'impact du prompt sur le comportement

### Glossaire
| Terme | Définition |
|-------|-----------|
| **Prompt engineering** | Conception de prompts efficaces |
| **Few-shot** | Exemples fournis dans le prompt |
| **Zero-shot** | Sans exemple |
| **Instruction tuning** | Adaptation du modèle à suivre des consignes |

---

## Étape 7 : Validation stricte + boucle de correction (pré-agent)

### Features / tickets
- [ ] Valider le JSON (schéma strict)
- [ ] Si invalide → relancer le LLM avec correction
- [ ] Boucle simple côté code

### Objectifs pédagogiques
- Introduire la notion de boucle
- Comprendre l'auto-correction guidée

### Glossaire
| Terme | Définition |
|-------|-----------|
| **Validation** | Vérification de conformité des données |
| **Retry loop** | Boucle de tentative |
| **Self-healing** | Capacité à corriger une erreur |

---

## Étape 8 : Intégration avec Fractal Compagnon

### Features / tickets
- [ ] Appel de FactionAgent depuis le front
- [ ] Création d'une Faction dans l'app

### Objectifs pédagogiques
- Intégrer un LLM dans un produit réel
- Comprendre les contraintes d'usage

### Glossaire
| Terme | Définition |
|-------|-----------|
| **Client** | Application qui consomme l'API |
| **CORS** | Règles de sécurité entre domaines |
| **DTO** | Objet de transfert de données |

---

## Étape 9 : Introduction des outils (Tool use)

### Features / tickets
- [ ] Définir des actions possibles : `GENERATE_FACTION`, `VALIDATE_JSON`, `RETRY`
- [ ] LLM retourne une intention

### Objectifs pédagogiques
- Comprendre comment un LLM peut piloter des actions
- Préparer un système agentique

### Glossaire
| Terme | Définition |
|-------|-----------|
| **Tool use** | Utilisation d'outils par le LLM |
| **Action** | Instruction exécutable par le système |
| **Function calling** | Mapping LLM → fonction |

---

## Étape 10 : Passage à un système agentique réel

### Features / tickets
- [ ] Le LLM décide : quoi faire, quand relancer, quand s'arrêter
- [ ] Boucle : Observation → Décision → Action

### Objectifs pédagogiques
- Implémenter un vrai agent
- Inverser le contrôle (LLM pilote)

### Glossaire
| Terme | Définition |
|-------|-----------|
| **Agent** | Système autonome piloté par un LLM |
| **Planification** | Choix des étapes |
| **State** | État courant du système |
| **Feedback loop** | Boucle d'amélioration |

---

## Étape 11 : Mémoire et contexte (stateful agent)

### Features / tickets
- [ ] Stocker les tentatives
- [ ] Fournir l'historique au LLM
- [ ] Améliorer les corrections

### Objectifs pédagogiques
- Comprendre la notion de mémoire dans un agent
- Améliorer la cohérence des décisions

### Glossaire
| Terme | Définition |
|-------|-----------|
| **Mémoire courte** | Contexte de la conversation |
| **Mémoire longue** | Stockage persistant |
| **Contexte** | Informations envoyées au LLM |

---

## Étape 12 : Différence client vs LLM local pur

### Features / tickets
- [ ] Comparer : appel API externe (OpenAI, etc.) vs usage local via Ollama
- [ ] Adapter FactionAgent

### Objectifs pédagogiques
- Comprendre les enjeux : latence, coût, confidentialité

### Glossaire
| Terme | Définition |
|-------|-----------|
| **SaaS LLM** | Modèle accessible via API distante |
| **On-premise** | Exécuté localement |
| **Latency** | Temps de réponse |

---

## Étape 13 : Introduction au RAG *(optionnel avancé)*

### Features / tickets
- [ ] Ajouter une base de connaissances (lore Fractal)
- [ ] Injecter du contexte dans le prompt

### Objectifs pédagogiques
- Comprendre comment enrichir un LLM
- Réduire les hallucinations

### Glossaire
| Terme | Définition |
|-------|-----------|
| **RAG** | Retrieval-Augmented Generation — génération augmentée par recherche |
| **Embedding** | Représentation vectorielle d'un texte |
| **Vector store** | Base de données vectorielle |

---

## Étape 14 : Agent final autonome *(objectif atteint)*

### Features / tickets
- [ ] Agent capable de : générer une faction, valider, corriger, livrer un JSON propre
- [ ] Intégration complète avec Fractal Compagnon

### Objectifs pédagogiques
- Maîtriser un système agentique complet
- Comprendre toute la chaîne : LLM → prompt → orchestration → agent → produit

### Glossaire
| Terme | Définition |
|-------|-----------|
| **Autonomie** | Capacité à atteindre un objectif sans intervention externe |
| **Pipeline** | Chaîne de traitement |
| **Robustesse** | Capacité à gérer les erreurs |