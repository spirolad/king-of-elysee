package com.example.koe.game.player

import com.example.koe.game.shop.CardId
import com.example.koe.game.shop.CardType
import com.example.koe.game.shop.PowerCard
import com.example.koe.game.shop.ShopOffer
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AiPlayerControllerTest {

    private val ai = AiPlayerController()

    private fun player(energy: Int) =
        Player(id = PlayerId("p1"), name = "P1", monster = Monster.GIGA_ZAUR, initialEnergy = energy)

    private fun card(id: String, cost: Int, type: CardType = CardType.DISCARD) =
        PowerCard(CardId(id), id, cost, type, "test card")

    @Test
    fun `AI does not buy a card it cannot pay at the real price`() = runTest {
        val offers = listOf(ShopOffer(card("a", cost = 3), cost = 4))

        val action = ai.chooseShopAction(player(energy = 3), offers, sweepCost = 2)

        assertEquals(ShopAction.Pass, action)
    }

    @Test
    fun `AI buys the most expensive affordable offer`() = runTest {
        val cheap = card("cheap", cost = 2)
        val mid = card("mid", cost = 5)
        val dear = card("dear", cost = 6)
        val offers = listOf(
            ShopOffer(cheap, cost = 2),
            ShopOffer(mid, cost = 5),
            ShopOffer(dear, cost = 6)
        )

        val action = ai.chooseShopAction(player(energy = 5), offers, sweepCost = 2)

        assertEquals(mid.id, (action as ShopAction.Buy).card.id)
    }

    @Test
    fun `AI picks a kept Méchanceté card when it can`() = runTest {
        val discard = card("discard", cost = 3, type = CardType.DISCARD)
        val keep = card("keep", cost = 3, type = CardType.KEEP)

        val picked = ai.chooseMechanceteCard(player(energy = 0), 3, listOf(discard, keep))

        assertEquals(keep.id, picked.id)
    }
}
