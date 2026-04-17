package com.faction.faction_agent.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.faction.faction_agent.models.FactionDraft;
import com.faction.faction_agent.services.FactionGenerationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller exposant les endpoints de génération de factions via un LLM.
 *
 * <p>
 * Ce contrôleur agit comme une façade HTTP au-dessus de
 * {@link FactionGenerationService},
 * en exposant plusieurs niveaux de génération :
 * </p>
 *
 * <ul>
 * <li><b>Draft</b> : données simulées ou statiques (sans appel LLM)</li>
 * <li><b>Raw</b> : réponse brute du LLM sans validation</li>
 * <li><b>Generate</b> : pipeline fiable avec parsing et retry</li>
 * <li><b>Random</b> : génération guidée avec feedback basé sur les erreurs</li>
 * </ul>
 *
 * <p>
 * Ces endpoints permettent de :
 * </p>
 * <ul>
 * <li>Tester différents niveaux de contrôle du LLM</li>
 * <li>Comparer la qualité des outputs selon les stratégies</li>
 * <li>Déboguer les prompts et la structure JSON</li>
 * </ul>
 *
 * <p>
 * Ce contrôleur est principalement destiné à un usage de développement,
 * d’expérimentation et d’intégration frontend (ex : Fractal Compagnon).
 * </p>
 */
@RestController
@RequestMapping("/faction")
@Tag(name = "Faction", description = "Endpoints for faction generation using LLM")
@CrossOrigin(origins = "http://localhost:4200")
public class FactionController {

    private final FactionGenerationService factionGenerationService;

    public FactionController(FactionGenerationService factionGenerationService) {
        this.factionGenerationService = factionGenerationService;
    }

    /**
     * Retourne une faction de test (draft).
     *
     * <p>
     * Cette méthode ne fait pas appel au LLM.
     * Elle est utilisée pour :
     * </p>
     * <ul>
     * <li>Tester rapidement le frontend</li>
     * <li>Valider le format des données</li>
     * <li>Servir de fallback en cas d’indisponibilité du LLM</li>
     * </ul>
     *
     * @return un objet {@link FactionDraft} statique
     */
    @GetMapping("/draft")
    @Operation(summary = "Generate a draft faction", description = "Generates a preliminary faction draft from the LLM before full validation or correction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Draft faction successfully generated"),
            @ApiResponse(responseCode = "500", description = "Error during generation")
    })
    public FactionDraft generateFactionDraft() {
        return factionGenerationService.geneFactionDraft();
    }

    /**
     * Retourne la réponse brute du LLM.
     *
     * <p>
     * Aucun parsing ni validation n’est appliqué.
     * </p>
     *
     * <p>
     * Utile pour :
     * </p>
     * <ul>
     * <li>Analyser le comportement du modèle</li>
     * <li>Tester la qualité du prompt</li>
     * <li>Déboguer les erreurs de format JSON</li>
     * </ul>
     *
     * <p>
     * ⚠️ Non fiable pour un usage en production.
     * </p>
     *
     * @return réponse brute du LLM
     */
    @GetMapping("/raw")
    @Operation(summary = "Generate raw LLM output", description = "Returns the raw unprocessed response from the LLM. Useful for debugging prompt behavior and parsing issues.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Raw response returned"),
            @ApiResponse(responseCode = "500", description = "Error during LLM call")
    })
    public String generateFactionRaw() {
        return factionGenerationService.generateFactionRaw();
    }

    /**
     * Génère une faction validée via un pipeline fiable.
     *
     * <p>
     * Ce endpoint utilise :
     * </p>
     * <ul>
     * <li>Parsing JSON vers {@link FactionDraft}</li>
     * <li>Validation métier</li>
     * <li>Retry en cas d’échec</li>
     * </ul>
     *
     * <p>
     * Il s'agit du endpoint recommandé pour un usage applicatif standard.
     * </p>
     *
     * @return une faction valide et structurée
     */
    @GetMapping("/generate")
    @Operation(summary = "Generate a validated faction", description = "Generates a faction using the LLM with validation, JSON parsing, and retry logic to ensure a clean structured output")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Faction successfully generated"),
            @ApiResponse(responseCode = "500", description = "Generation or parsing failed")
    })
    public FactionDraft generateFaction() {
        return factionGenerationService.generateFaction();
    }

    /**
     * Génère une faction en utilisant une approche guidée par contexte et erreurs.
     *
     * <p>
     * Fonctionnement :
     * </p>
     * <ul>
     * <li>Type de faction et contexte aléatoires</li>
     * <li>Validation métier détaillée</li>
     * <li>Feedback envoyé au LLM en cas d’erreur</li>
     * <li>Correction ou régénération dynamique</li>
     * </ul>
     *
     * <p>
     * Ce endpoint produit des résultats plus variés et potentiellement plus
     * cohérents,
     * au prix d’une complexité plus élevée.
     * </p>
     *
     * <p>
     * Il s'agit d'une étape intermédiaire vers un système agentique.
     * </p>
     *
     * @return une faction cohérente avec son contexte
     */
    @GetMapping("/random")
    @Operation(summary = "Generate a random-driven faction", description = "Generates a faction using randomized prompt variations or parameters to increase diversity of outputs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Random faction successfully generated"),
            @ApiResponse(responseCode = "500", description = "Error during generation")
    })
    public FactionDraft generateRandomLedFaction() {
        return factionGenerationService.generateRandomLedFaction();
    }

}
