package com.example.koe.game.shop

import com.example.koe.game.player.Player

/**
 * Standard catalog and factory for King of Elysée / King of Tokyo Power Cards.
 */
object CardCatalog {

    fun createActionCards(): List<PowerCard> = listOf(
        PowerCard(
            id = CardId("bracelet_electronique"),
            name = "Bracelet électronique",
            cost = 3,
            type = CardType.DISCARD,
            description = "Le joueur ciblé ne peut pas gagner d'énergie au prochain tour.",
            effect = { buyer, allPlayers ->
                val opponents = allPlayers.filter { it.id != buyer.id && it.isAlive }
                val target = opponents.maxByOrNull { it.victoryPoints } ?: opponents.firstOrNull()
                if (target != null) {
                    if (target.hasActionImmunity) {
                        target.hasActionImmunity = false
                        CardEffectResult("${target.name} a été protégé par Immunité parlementaire !")
                    } else {
                        target.noEnergyNextTurn = true
                        CardEffectResult("${target.name} ne pourra pas gagner d'énergie au prochain tour")
                    }
                } else {
                    CardEffectResult("Aucun adversaire ciblé")
                }
            }
        ),
        PowerCard(
            id = CardId("carte_france_travail"),
            name = "Carte France Travail",
            cost = 4,
            type = CardType.DISCARD,
            description = "Le joueur ciblé passe son prochain tour.",
            effect = { buyer, allPlayers ->
                val opponents = allPlayers.filter { it.id != buyer.id && it.isAlive }
                val target = opponents.maxByOrNull { it.victoryPoints } ?: opponents.firstOrNull()
                if (target != null) {
                    if (target.hasActionImmunity) {
                        target.hasActionImmunity = false
                        CardEffectResult("${target.name} a été protégé par Immunité parlementaire !")
                    } else {
                        target.skipNextTurn = true
                        CardEffectResult("${target.name} passera son prochain tour")
                    }
                } else {
                    CardEffectResult("Aucun adversaire ciblé")
                }
            }
        ),
        PowerCard(
            id = CardId("fausse_information"),
            name = "Fausse information",
            cost = 4,
            type = CardType.DISCARD,
            description = "Retire 1 PV à tous les adversaires.",
            effect = { buyer, allPlayers ->
                val opponents = allPlayers.filter { it.id != buyer.id && it.isAlive }
                opponents.forEach { opp ->
                    if (opp.hasActionImmunity) {
                        opp.hasActionImmunity = false
                    } else {
                        opp.deductVictoryPoints(1)
                    }
                }
                CardEffectResult("1 PV retiré à tous les adversaires")
            }
        ),
        PowerCard(
            id = CardId("recuperation_politique"),
            name = "Récupération politique",
            cost = 3,
            type = CardType.DISCARD,
            description = "Vole 2 énergie au joueur qui a le plus de PV.",
            effect = { buyer, allPlayers ->
                val opponents = allPlayers.filter { it.id != buyer.id && it.isAlive }
                val target = opponents.maxByOrNull { it.victoryPoints }
                if (target != null) {
                    if (target.hasActionImmunity) {
                        target.hasActionImmunity = false
                        CardEffectResult("${target.name} a été protégé par Immunité parlementaire !")
                    } else {
                        val stolen = minOf(2, target.energy)
                        target.spendEnergy(stolen)
                        buyer.addEnergy(stolen)
                        CardEffectResult("$stolen énergie(s) volée(s) à ${target.name}")
                    }
                } else {
                    CardEffectResult("Aucun adversaire ciblé")
                }
            }
        ),
        PowerCard(
            id = CardId("realite"),
            name = "Réalité",
            cost = 4,
            type = CardType.DISCARD,
            description = "Inflige 2 dégâts au joueur ciblé pour chaque carte Pouvoir qu'il possède.",
            effect = { buyer, allPlayers ->
                val opponents = allPlayers.filter { it.id != buyer.id && it.isAlive }
                val target = opponents.maxByOrNull { it.cards.size } ?: opponents.firstOrNull()
                if (target != null) {
                    if (target.hasActionImmunity) {
                        target.hasActionImmunity = false
                        CardEffectResult("${target.name} a été protégé par Immunité parlementaire !")
                    } else {
                        val dmg = 2 * target.cards.size
                        target.damage(dmg)
                        CardEffectResult("Infligé $dmg dégâts à ${target.name} (2 par carte Pouvoir)")
                    }
                } else {
                    CardEffectResult("Aucun adversaire ciblé")
                }
            }
        ),
        PowerCard(
            id = CardId("vote_utile"),
            name = "Vote utile",
            cost = 1,
            type = CardType.DISCARD,
            description = "Donne 1 PV pour chaque tranche de 3 énergies dépensées lors de cet achat.",
            effect = { buyer, _ ->
                val chunks = buyer.energy / 3
                val spent = chunks * 3
                buyer.spendEnergy(spent)
                buyer.addVictoryPoints(chunks)
                CardEffectResult("Gagné $chunks PV en dépensant $spent énergies supplémentaires")
            }
        ),
        PowerCard(
            id = CardId("conference"),
            name = "Conférence",
            cost = 3,
            type = CardType.DISCARD,
            description = "Payez 2 énergies de moins que le coût indiqué pour la prochaine carte que vous achetez ce tour-ci.",
            effect = { buyer, _ ->
                buyer.nextCardDiscount += 2
                CardEffectResult("Réduction de 2 énergies sur le prochain achat de ce tour")
            }
        ),
        PowerCard(
            id = CardId("manifestation"),
            name = "Manifestation",
            cost = 3,
            type = CardType.DISCARD,
            description = "Inflige 1 dégât à tous les joueurs (y compris l'attaquant).",
            effect = { buyer, allPlayers ->
                buyer.damage(1)
                allPlayers.filter { it.id != buyer.id && it.isAlive }.forEach { player ->
                    if (player.hasActionImmunity) {
                        player.hasActionImmunity = false
                    } else {
                        player.damage(1)
                    }
                }
                CardEffectResult("1 dégât infligé à tous les joueurs")
            }
        ),
        PowerCard(
            id = CardId("changement_d_assemblee"),
            name = "Changement d'assemblée",
            cost = 2,
            type = CardType.DISCARD,
            description = "Défausse les 3 cartes actuellement visibles du marché et en révèle 3 nouvelles (sans payer le coût habituel de renouvellement).",
            effect = { _, _ ->
                CardEffectResult("Marché renouvelé gratuitement")
            }
        ),
        PowerCard(
            id = CardId("denonciation"),
            name = "Dénonciation",
            cost = 3,
            type = CardType.DISCARD,
            description = "Oblige un adversaire au choix à défausser l'une de ses cartes Pouvoir au hasard.",
            effect = { buyer, allPlayers ->
                val opponentsWithCards = allPlayers.filter { it.id != buyer.id && it.isAlive && it.cards.isNotEmpty() }
                val target = opponentsWithCards.maxByOrNull { it.victoryPoints } ?: opponentsWithCards.firstOrNull()
                if (target != null) {
                    if (target.hasActionImmunity) {
                        target.hasActionImmunity = false
                        CardEffectResult("${target.name} a été protégé par Immunité parlementaire !")
                    } else {
                        val cardToDiscard = target.cards.randomOrNull()
                        if (cardToDiscard != null) {
                            target.removeCard(cardToDiscard)
                            CardEffectResult("${target.name} a défaussé la carte ${cardToDiscard.name}")
                        } else {
                            CardEffectResult("Aucune carte à défausser")
                        }
                    }
                } else {
                    CardEffectResult("Aucun adversaire avec des cartes Pouvoir")
                }
            }
        ),
        PowerCard(
            id = CardId("carte_a_strasbourg"),
            name = "Carte à Strasbourg",
            cost = 4,
            type = CardType.DISCARD,
            description = "Donne +2 PV max immédiats et restaure 2 santé à l'achat.",
            effect = { buyer, _ ->
                buyer.increaseMaxHealth(2)
                buyer.heal(2)
                CardEffectResult("+2 Santé max et 2 PV restaurés")
            }
        )
    )

    fun createPowerCards(): List<PowerCard> = listOf(
        PowerCard(
            id = CardId("affichage"),
            name = "Affichage",
            cost = 3,
            type = CardType.KEEP,
            description = "À la fin de ton tour, gagne 1 PV si tu possèdes au moins 3 énergies.",
            effect = { _, _ -> CardEffectResult("Effet passif Affichage actif") }
        ),
        PowerCard(
            id = CardId("summer_camp_politique"),
            name = "Summer camp politique",
            cost = 3,
            type = CardType.KEEP,
            description = "Chaque dé [1] obtenu rapporte +1 énergie supplémentaire.",
            effect = { _, _ -> CardEffectResult("Effet passif Summer camp politique actif") }
        ),
        PowerCard(
            id = CardId("campagne"),
            name = "Campagne",
            cost = 4,
            type = CardType.KEEP,
            description = "Tes attaques infligent +1 dégât tant que tu n'es pas au pouvoir (hors de l'Élysée).",
            effect = { _, _ -> CardEffectResult("Effet passif Campagne actif") }
        ),
        PowerCard(
            id = CardId("censure"),
            name = "Censure",
            cost = 4,
            type = CardType.KEEP,
            description = "Réduit de 1 tous les dégâts subis (minimum 1 dégât subi).",
            effect = { _, _ -> CardEffectResult("Effet passif Censure actif") }
        ),
        PowerCard(
            id = CardId("49_3"),
            name = "49.3",
            cost = 3,
            type = CardType.KEEP,
            description = "Une fois par tour, tu peux payer 3 énergies pour forcer ton entrée au pouvoir (prendre l'Élysée) sans attaquer.",
            effect = { _, _ -> CardEffectResult("Effet passif 49.3 actif") }
        ),
        PowerCard(
            id = CardId("vu_a_la_tv"),
            name = "Vu à la TV",
            cost = 3,
            type = CardType.KEEP,
            description = "Gagne +1 PV chaque fois que tu entres au pouvoir (à l'Élysée).",
            effect = { _, _ -> CardEffectResult("Effet passif Vu à la TV actif") }
        ),
        PowerCard(
            id = CardId("phrase_culte"),
            name = "Phrase culte",
            cost = 4,
            type = CardType.KEEP,
            description = "Obtiens +1 lancer de dés à chaque tour.",
            effect = { _, _ -> CardEffectResult("Effet passif Phrase culte actif") }
        ),
        PowerCard(
            id = CardId("embrigadement_de_la_jeunesse"),
            name = "Embrigadement de la jeunesse",
            cost = 4,
            type = CardType.KEEP,
            description = "La première carte Pouvoir que tu achètes à chaque tour coûte 2 énergies de moins.",
            effect = { _, _ -> CardEffectResult("Effet passif Embrigadement de la jeunesse actif") }
        ),
        PowerCard(
            id = CardId("detournement"),
            name = "Détournement",
            cost = 4,
            type = CardType.KEEP,
            description = "Chaque fois qu'un adversaire gagne de l'énergie, tu gagnes 1 énergie de la réserve.",
            effect = { _, _ -> CardEffectResult("Effet passif Détournement actif") }
        ),
        PowerCard(
            id = CardId("populisme"),
            name = "Populisme",
            cost = 4,
            type = CardType.KEEP,
            description = "Chaque dé [1] ou [2] conservé à la fin de votre tour vous rapporte +1 point de victoire au lieu de 0.",
            effect = { _, _ -> CardEffectResult("Effet passif Populisme actif") }
        ),
        PowerCard(
            id = CardId("clientelisme"),
            name = "Clientélisme",
            cost = 3,
            type = CardType.KEEP,
            description = "Vos achats de cartes au marché coûtent 1 énergie de moins (minimum 1).",
            effect = { _, _ -> CardEffectResult("Effet passif Clientélisme actif") }
        ),
        PowerCard(
            id = CardId("loi_liberticide"),
            name = "Loi liberticide",
            cost = 4,
            type = CardType.KEEP,
            description = "Les adversaires doivent payer +1 énergie supplémentaire pour acheter n'importe quelle carte au marché.",
            effect = { _, _ -> CardEffectResult("Effet passif Loi liberticide actif") }
        ),
        PowerCard(
            id = CardId("gerontocratie"),
            name = "Gérontocratie",
            cost = 3,
            type = CardType.KEEP,
            description = "Vos dégâts subis sont réduits de 1 tant que vous avez au moins 6 PV.",
            effect = { _, _ -> CardEffectResult("Effet passif Gérontocratie actif") }
        ),
        PowerCard(
            id = CardId("cabinet_de_conseil"),
            name = "Cabinet de conseil",
            cost = 3,
            type = CardType.KEEP,
            description = "Vous pouvez conserver 1 dé supplémentaire lors de votre tout premier lancer de tour.",
            effect = { _, _ -> CardEffectResult("Effet passif Cabinet de conseil actif") }
        ),
        PowerCard(
            id = CardId("campagne_d_invectives"),
            name = "Campagne d'invectives",
            cost = 4,
            type = CardType.KEEP,
            description = "Quand vous quittez le pouvoir (l'Élysée), le joueur qui vous remplace subit immédiatement 2 dégâts.",
            effect = { _, _ -> CardEffectResult("Effet passif Campagne d'invectives actif") }
        ),
        PowerCard(
            id = CardId("monopole_mediatique"),
            name = "Monopole médiatique",
            cost = 3,
            type = CardType.KEEP,
            description = "À la fin de votre tour, si vous êtes au pouvoir (à l'Élysée), gagnez +1 énergie.",
            effect = { _, _ -> CardEffectResult("Effet passif Monopole médiatique actif") }
        ),
        PowerCard(
            id = CardId("privatisation"),
            name = "Privatisation",
            cost = 4,
            type = CardType.KEEP,
            description = "Tant que vous êtes au pouvoir (à l'Élysée), gagner des points de victoire par les dés vous rapporte +1 PV bonus à chaque fois.",
            effect = { _, _ -> CardEffectResult("Effet passif Privatisation actif") }
        ),
        PowerCard(
            id = CardId("scandale_d_etat"),
            name = "Scandale d'État",
            cost = 4,
            type = CardType.KEEP,
            description = "Quand vous infligez au moins 1 dégât à un adversaire, il perd aussi 1 énergie (s'il en a).",
            effect = { _, _ -> CardEffectResult("Effet passif Scandale d'État actif") }
        ),
        PowerCard(
            id = CardId("trafic_d_influence"),
            name = "Trafic d'influence",
            cost = 4,
            type = CardType.KEEP,
            description = "Chaque fois qu'un adversaire achète une carte au marché, vous gagnez 1 énergie.",
            effect = { _, _ -> CardEffectResult("Effet passif Trafic d'influence actif") }
        ),
        PowerCard(
            id = CardId("immunite_parlementaire"),
            name = "Immunité parlementaire",
            cost = 4,
            type = CardType.KEEP,
            description = "Vous êtes totalement immunisé contre l’effet de la premiere cartes Action jouées par les adversaires jusqu’à votre prochain tour.",
            effect = { buyer, _ ->
                buyer.hasActionImmunity = true
                CardEffectResult("Immunité parlementaire activée")
            }
        ),
        PowerCard(
            id = CardId("paradis_fiscal"),
            name = "Paradis fiscal",
            cost = 4,
            type = CardType.KEEP,
            description = "Chaque fois que vous gagnez de l'énergie (via les dés ou une carte), gagnez +1 énergie supplémentaire.",
            effect = { _, _ -> CardEffectResult("Effet passif Paradis fiscal actif") }
        )
    )

    /**
     * Méchanceté cards, handed out by [MechanceteDeck] when a player reaches a tier instead of being sold at the market.
     */
    fun createWickednessTiles(): List<PowerCard> = listOf(
        // Niveau 3
        PowerCard(
            id = CardId("infatigable"),
            name = "Infatigable",
            cost = 3,
            type = CardType.KEEP,
            description = "Au début de votre tour, gagnez 1 Énergie.",
            effect = { _, _ -> CardEffectResult("Effet Infatigable actif") }
        ),
        PowerCard(
            id = CardId("eternel"),
            name = "Éternel",
            cost = 3,
            type = CardType.KEEP,
            description = "Au début de votre tour, gagnez 1 PV (Santé).",
            effect = { _, _ -> CardEffectResult("Effet Éternel actif") }
        ),
        PowerCard(
            id = CardId("furtif"),
            name = "Furtif",
            cost = 3,
            type = CardType.KEEP,
            description = "Gagnez 1 Point de Victoire supplémentaire lorsque vous obtenez au moins trois [1].",
            effect = { _, _ -> CardEffectResult("Effet Furtif actif") }
        ),
        PowerCard(
            id = CardId("sournois"),
            name = "Sournois",
            cost = 3,
            type = CardType.KEEP,
            description = "Gagnez un lancer supplémentaire à chaque tour.",
            effect = { _, _ -> CardEffectResult("Effet Sournois actif") }
        ),
        // Niveau 6
        PowerCard(
            id = CardId("regeneration_totale"),
            name = "Régénération totale",
            cost = 6,
            type = CardType.KEEP,
            description = "Vous pouvez avoir jusqu'à 12 PV (Santé). Récupérez tous vos PV quand vous gagnez cette tuile.",
            effect = { buyer, _ ->
                buyer.increaseMaxHealthTo(12)
                buyer.heal(12)
                CardEffectResult("Santé max augmentée à 12 et tous les PV récupérés")
            }
        ),
        PowerCard(
            id = CardId("panique_generale"),
            name = "Panique générale",
            cost = 6,
            type = CardType.DISCARD,
            description = "Tous les autres Monstres perdent 4 Points de Victoire, puis défaussez cette tuile.",
            effect = { buyer, allPlayers ->
                allPlayers.filter { it.id != buyer.id && it.isAlive }.forEach {
                    it.deductVictoryPoints(4)
                }
                CardEffectResult("Tous les autres monstres ont perdu 4 PV")
            }
        ),
        PowerCard(
            id = CardId("repaire_malefique"),
            name = "Repaire maléfique",
            cost = 6,
            type = CardType.KEEP,
            description = "Le prix des cartes Énergie que vous achetez est diminué de 1 Énergie.",
            effect = { _, _ -> CardEffectResult("Effet Repaire maléfique actif") }
        ),
        PowerCard(
            id = CardId("cybercerveau"),
            name = "Cybercerveau",
            cost = 6,
            type = CardType.KEEP,
            description = "Jouez avec un dé supplémentaire.",
            extraDice = 1,
            effect = { _, _ -> CardEffectResult("Effet Cybercerveau (+1 dé) actif") }
        ),
        // Niveau 10
        PowerCard(
            id = CardId("rayon_stellaire"),
            name = "Rayon stellaire",
            cost = 10,
            type = CardType.KEEP,
            description = "Gagnez 1 Énergie supplémentaire pour chaque [Éclair] et 1 PV (Santé) supplémentaire pour chaque [Cœur].",
            effect = { _, _ -> CardEffectResult("Effet Rayon stellaire actif") }
        ),
        PowerCard(
            id = CardId("rayon_antimatiere"),
            name = "Rayon antimatière",
            cost = 10,
            type = CardType.DISCARD,
            description = "Doublez vos Points de Victoire.",
            effect = { buyer, _ ->
                val currentVp = buyer.victoryPoints
                buyer.addVictoryPoints(currentVp)
                CardEffectResult("Points de victoire doublés (+${currentVp} PV)")
            }
        )
    )

    /**
     * Market deck: action and power cards. Méchanceté cards are kept apart, see [createWickednessTiles].
     */
    fun createStandardDeck(): List<PowerCard> =
        createActionCards() + createPowerCards()
}
