# Tickets - Etape 10 (Agent minimal viable)

Ce document decrit une implementation progressive pour passer d'un systeme LLM orchestre par le code a un agent minimal ou le LLM pilote les decisions.

## Ticket A10-01 - Creer un AgentOrchestrator (boucle explicite)

### Objectif
Centraliser la boucle agentique dans un service dedie avec les phases `observe -> decide -> act`.

### Taches
- Creer `AgentOrchestratorService` dans `services/`.
- Deplacer la boucle principale actuellement dans `FactionGenerationService.generateRandomLedFaction`.
- Introduire un cycle explicite:
  - `observe(state, json, validationResult)`
  - `decide(state)`
  - `act(action, state)`
- Conserver une limite de securite `maxSteps` cote backend.

### Criteres d'acceptation
- La logique de boucle n'est plus dupliquee.
- Les transitions de phase sont visibles dans les logs.
- Le comportement actuel reste fonctionnel (pas de regression API).

---

## Ticket A10-02 - Definir un contrat de tools agentiques

### Objectif
Faire converger le vocabulaire roadmap et code autour d'un set d'actions stable.

### Taches
- Aligner l'enum des actions avec un contrat cible:
  - `GENERATE_FACTION`
  - `VALIDATE_JSON`
  - `REPAIR_JSON`
  - `STOP`
- Adapter `Decision` et `DecisionService` a ce contrat.
- Gerer une valeur fallback si action inconnue.

### Criteres d'acceptation
- Les actions exposees au LLM correspondent au contrat officiel.
- Les erreurs de mapping n'interrompent pas brutalement la boucle.
- Le contrat est documente (JavaDoc ou doc technique).

---

## Ticket A10-03 - Deleguer la decision d'arret au LLM (sous garde-fous)

### Objectif
Permettre au LLM de choisir quand s'arreter, tout en gardant des garde-fous backend.

### Taches
- Ajouter des regles de decision claires dans le prompt de decision:
  - STOP si JSON valide et contraintes respectees.
  - STOP si impasse apres plusieurs tentatives.
- Conserver un arret de securite backend (`maxSteps`) independant.
- Logger la raison de stop dans `AgentState`.

### Criteres d'acceptation
- L'action `STOP` peut etre retournee et appliquee.
- Le systeme ne boucle jamais indefiniment.
- Les raisons d'arret sont tracables.

---

## Ticket A10-04 - Uniformiser Observation et Validation

### Objectif
Transformer les sorties de parsing/validation en observation normalisee pour la decision.

### Taches
- Introduire un objet d'observation (ex: `AgentObservation`):
  - `rawJson`
  - `parseOk`
  - `validationErrors`
  - `attemptNumber`
- Utiliser cet objet pour construire le prompt de `DecisionService`.
- Eviter les branches implicites disperses entre try/catch et validation.

### Criteres d'acceptation
- Le prompt de decision est construit depuis une observation unique.
- Les cas `parse error` et `business validation error` sont differencies proprement.
- Le code gagne en lisibilite et maintenabilite.

---

## Ticket A10-05 - Ajouter des tests d'integration de la boucle agentique

### Objectif
Securiser le refactoring et valider les chemins critiques de l'agent.

### Taches
- Ecrire des tests d'integration Spring pour:
  - cas nominal (JSON valide -> STOP)
  - cas parse invalide -> REPAIR_JSON -> valide
  - cas erreurs metier -> REGENERATE/GENERATE_FACTION
  - cas limite -> stop de securite backend
- Mock du client LLM pour produire des reponses deterministes.

### Criteres d'acceptation
- Les chemins critiques sont couverts par des tests.
- Le refactoring peut evoluer sans casser la boucle principale.
- Les regressions de decision/action sont detectees rapidement.

---

## Ordre recommande
1. A10-01
2. A10-02
3. A10-04
4. A10-03
5. A10-05

## Definition of Done (Etape 10)
- La boucle `observe -> decide -> act` existe dans un orchestrateur dedie.
- Le LLM choisit explicitement une action et peut choisir `STOP`.
- Le backend garde des garde-fous de securite.
- Les scenarios principaux sont testes.
