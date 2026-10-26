package test

import dice.DiceRoller
import dice.DieFace
import game.DiceResolver
import game.GameConfig
import game.GameEngine
import game.GameEventListener
import game.TokyoBoard
import game.TokyoZone
import game.WinReason
import player.AiPlayerController
import player.Monster
import player.Player
import player.PlayerController
import player.PlayerId
import player.ShopAction
import shop.CardCatalog
import shop.CardId
import shop.CardType
import shop.PowerCard
import shop.Shop

fun main() {
    println("Running King of Tokyo Test Suite...")
    var passed = 0
    var failed = 0

    fun test(name: String, block: () -> Unit) {
        try {
            block()
            println("  [PASS] $name")
            passed++
        } catch (e: Throwable) {
            println("  [FAIL] $name: ${e.message}")
            e.printStackTrace()
            failed++
        }
    }

    // 1. Player Tests
    test("Player health clamping and damage") {
        val player = Player(id = PlayerId("p1"), name = "Giga", monster = Monster.GIGA_ZAUR, maxHealth = 10, initialHealth = 10)
        check(player.isAlive)
        val dealt = player.damage(4)
        check(dealt == 4 && player.health == 6)
        val overkill = player.damage(10)
        check(overkill == 6 && player.health == 0 && !player.isAlive)
    }

    test("Player healing does not exceed maxHealth") {
        val player = Player(id = PlayerId("p1"), name = "Giga", monster = Monster.GIGA_ZAUR, maxHealth = 10, initialHealth = 6)
        val healed = player.heal(3)
        check(healed == 3 && player.health == 9)
        val overHealed = player.heal(5)
        check(overHealed == 1 && player.health == 10)
    }

    test("Player energy and VP mutations") {
        val player = Player(id = PlayerId("p1"), name = "Giga", monster = Monster.GIGA_ZAUR)
        player.addEnergy(5)
        check(player.energy == 5)
        check(player.spendEnergy(3))
        check(player.energy == 2)
        check(!player.spendEnergy(5))
        check(player.energy == 2)

        player.addVictoryPoints(15)
        check(player.victoryPoints == 15)
        player.deductVictoryPoints(5)
        check(player.victoryPoints == 10)
    }

    // 2. Dice Resolver Tests
    test("DiceResolver 3-of-a-kind scoring") {
        // Three 1s -> 1 VP
        val res1 = DiceResolver.resolve(listOf(DieFace.ONE, DieFace.ONE, DieFace.ONE, DieFace.ENERGY, DieFace.ENERGY, DieFace.ATTACK), isInTokyo = false)
        check(res1.victoryPoints == 1 && res1.energy == 2 && res1.attackDamage == 1)

        // Four 2s -> 2 + 1 = 3 VP
        val res2 = DiceResolver.resolve(listOf(DieFace.TWO, DieFace.TWO, DieFace.TWO, DieFace.TWO, DieFace.HEAL, DieFace.HEAL), isInTokyo = false)
        check(res2.victoryPoints == 3 && res2.actualHeal == 2)

        // Five 3s -> 3 + 2 = 5 VP
        val res3 = DiceResolver.resolve(listOf(DieFace.THREE, DieFace.THREE, DieFace.THREE, DieFace.THREE, DieFace.THREE, DieFace.ATTACK), isInTokyo = false)
        check(res3.victoryPoints == 5 && res3.attackDamage == 1)
    }

    test("DiceResolver Tokyo heal restriction") {
        val resOutside = DiceResolver.resolve(listOf(DieFace.HEAL, DieFace.HEAL), isInTokyo = false)
        check(resOutside.actualHeal == 2)

        val resInside = DiceResolver.resolve(listOf(DieFace.HEAL, DieFace.HEAL), isInTokyo = true)
        check(resInside.heartsRolled == 2 && resInside.actualHeal == 0)
    }

    // 3. Tokyo Board Tests
    test("TokyoBoard enter, leave, attack routing") {
        val board = TokyoBoard(hasTokyoBay = false)
        val p1 = Player(id = PlayerId("p1"), name = "P1", monster = Monster.GIGA_ZAUR)
        val p2 = Player(id = PlayerId("p2"), name = "P2", monster = Monster.MEKA_DRAGON)
        val p3 = Player(id = PlayerId("p3"), name = "P3", monster = Monster.ALIENOID)
        val all = listOf(p1, p2, p3)

        check(!board.isInsideTokyo(p1))
        val zone = board.enter(p1)
        check(zone == TokyoZone.TOKYO_CITY)
        check(board.isInsideTokyo(p1))
        check(board.isFull)

        // Attacker p2 (outside) attacks -> targets should be p1 (in Tokyo)
        val targetsForP2 = board.getTargetsForAttack(p2, all)
        check(targetsForP2 == listOf(p1))

        // Attacker p1 (inside) attacks -> targets should be p2, p3 (outside Tokyo)
        val targetsForP1 = board.getTargetsForAttack(p1, all)
        check(targetsForP1 == listOf(p2, p3))

        // p1 yields/leaves Tokyo
        val leftZone = board.leave(p1)
        check(leftZone == TokyoZone.TOKYO_CITY)
        check(!board.isInsideTokyo(p1))
        check(board.hasEmptySlot)
    }

    // 4. Shop Tests
    test("Shop purchase and sweep") {
        val testCards = listOf(
            PowerCard(CardId("c1"), "Energy Drink", 2, CardType.DISCARD, "Gain 3 energy", { _, _ -> shop.CardEffectResult("Boost", energyDelta = 3) }),
            PowerCard(CardId("c2"), "Big Armor", 4, CardType.KEEP, "Armor test"),
            PowerCard(CardId("c3"), "Nuclear Blast", 5, CardType.DISCARD, "Nuke"),
            PowerCard(CardId("c4"), "Spare Card", 1, CardType.DISCARD, "Spare")
        )
        val shop = Shop(initialCards = testCards, marketSize = 3, sweepCost = 2)
        val buyer = Player(id = PlayerId("buyer"), name = "Buyer", monster = Monster.KRAKEN, initialEnergy = 5)

        val cardToBuy = shop.faceUpCards.first { it.id == CardId("c1") }
        val result = shop.buyCard(buyer, cardToBuy, listOf(buyer))
        check(result != null)
        check(buyer.energy == 5 - 2 + 3) // spent 2, gained 3
        check(shop.faceUpCards.size == 3) // refilled with c4

        // Sweep shop
        val swept = shop.sweep(buyer)
        check(swept)
        check(buyer.energy == 6 - 2) // spent 2
    }

    // 5. GameEngine Deterministic Flow Test
    test("GameEngine deterministic victory by VP") {
        val p1 = Player(id = PlayerId("p1"), name = "P1", monster = Monster.GIGA_ZAUR)
        val p2 = Player(id = PlayerId("p2"), name = "P2", monster = Monster.MEKA_DRAGON)
        val players = listOf(p1, p2)

        // Mock deterministic dice roller rolling all 3s
        val deterministicRoller = object : DiceRoller {
            override fun roll(count: Int): List<DieFace> = List(count) { DieFace.THREE }
            override fun rollSingle(): DieFace = DieFace.THREE
        }

        val controllers = mapOf(
            p1.id to AiPlayerController(),
            p2.id to AiPlayerController()
        )

        val engine = GameEngine(
            players = players,
            controllers = controllers,
            config = GameConfig(winningVictoryPoints = 10),
            diceRoller = deterministicRoller
        )

        val winner = engine.runGame()
        check(winner.id == p1.id)
        check(winner.victoryPoints >= 10)
    }

    test("GameEngine deterministic victory by elimination") {
        val p1 = Player(id = PlayerId("p1"), name = "P1", monster = Monster.GIGA_ZAUR)
        val p2 = Player(id = PlayerId("p2"), name = "P2", monster = Monster.MEKA_DRAGON, initialHealth = 2)
        val players = listOf(p1, p2)

        // Mock deterministic roller rolling all Attacks
        val attackRoller = object : DiceRoller {
            override fun roll(count: Int): List<DieFace> = List(count) { DieFace.ATTACK }
            override fun rollSingle(): DieFace = DieFace.ATTACK
        }

        // p2 never yields Tokyo
        val stubbornController = object : PlayerController {
            override fun chooseDiceToKeep(player: Player, rollNumber: Int, currentDice: List<DieFace>, currentlyKept: List<Boolean>): List<Int> = currentDice.indices.toList()
            override fun shouldYieldTokyo(player: Player, attacker: Player, damage: Int): Boolean = false
            override fun chooseShopAction(player: Player, availableCards: List<PowerCard>, sweepCost: Int): ShopAction = ShopAction.Pass
        }

        val engine = GameEngine(
            players = players,
            controllers = mapOf(p1.id to stubbornController, p2.id to stubbornController),
            diceRoller = attackRoller
        )

        // First turn: p1 enters Tokyo (as Tokyo is empty)
        // Second turn: p2 rolls attacks from outside and damages p1 in Tokyo
        // Next turns continue until elimination
        val winner = engine.runGame()
        check(winner.isAlive)
        check(engine.isGameOver)
    }

    // 6. Automated Game Tester (100 Games)
    test("AutomaticTester generates and verifies 100 games without invariant failures") {
        val report = AutomaticTester.run100Games()
        check(report.totalGames == 100) { "Expected 100 games, got ${report.totalGames}" }
        check(report.successfulGames == 100) { "Expected all 100 games to pass, but ${report.failedGames} failed" }
        check(report.failedGames == 0)
        check(report.vpWins + report.eliminationWins == 100)
    }

    // 7. Thematic Card Catalog Tests
    test("CardCatalog deck contains all 42 specified cards") {
        val deck = CardCatalog.createStandardDeck()
        check(deck.size == 42) { "Expected 42 cards in standard deck, found ${deck.size}" }
        val actions = CardCatalog.createActionCards()
        val powers = CardCatalog.createPowerCards()
        val wickedness = CardCatalog.createWickednessTiles()
        check(actions.size == 11) { "Expected 11 action cards, found ${actions.size}" }
        check(powers.size == 21) { "Expected 21 power cards, found ${powers.size}" }
        check(wickedness.size == 10) { "Expected 10 wickedness tiles, found ${wickedness.size}" }
    }

    test("Action cards effects: Fausse information, Carte à Strasbourg, Récupération politique") {
        val buyer = Player(id = PlayerId("b"), name = "Buyer", monster = Monster.GIGA_ZAUR, initialEnergy = 10, initialVictoryPoints = 5)
        val opp1 = Player(id = PlayerId("o1"), name = "Opp1", monster = Monster.ALIENOID, initialEnergy = 4, initialVictoryPoints = 8)
        val opp2 = Player(id = PlayerId("o2"), name = "Opp2", monster = Monster.MEKA_DRAGON, initialEnergy = 2, initialVictoryPoints = 6)
        val all = listOf(buyer, opp1, opp2)

        // Fausse info
        val fausseInfo = CardCatalog.createActionCards().first { it.id == CardId("fausse_information") }
        fausseInfo.effect.apply(buyer, all)
        check(opp1.victoryPoints == 7 && opp2.victoryPoints == 5)

        // Récupération politique (steals from highest VP opp1)
        val recup = CardCatalog.createActionCards().first { it.id == CardId("recuperation_politique") }
        recup.effect.apply(buyer, all)
        check(opp1.energy == 2 && buyer.energy == 12)

        // Carte à Strasbourg
        val strasbourg = CardCatalog.createActionCards().first { it.id == CardId("carte_a_strasbourg") }
        val initialMax = buyer.maxHealth
        strasbourg.effect.apply(buyer, all)
        check(buyer.maxHealth == initialMax + 2)
    }

    test("Power and Wickedness effects: Censure damage reduction and Rayon antimatière") {
        val p = Player(id = PlayerId("p"), name = "Player", monster = Monster.CYBER_BUNNY, initialVictoryPoints = 7)
        val censure = CardCatalog.createPowerCards().first { it.id == CardId("censure") }
        p.addCard(censure)
        val dmg = p.damage(4)
        check(dmg == 3) // reduced by 1
        check(p.health == 7)

        val rayon = CardCatalog.createWickednessTiles().first { it.id == CardId("rayon_antimatiere") }
        rayon.effect.apply(p, listOf(p))
        check(p.victoryPoints == 14) // doubled
    }

    println("\n==========================================")
    println("TEST RESULTS: $passed PASSED, $failed FAILED")
    println("==========================================")
    if (failed > 0) kotlin.system.exitProcess(1)
}
