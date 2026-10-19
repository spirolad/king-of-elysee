import cli.ConsoleGameEventListener
import cli.ConsolePlayerController
import game.GameConfig
import game.GameEngine
import player.AiPlayerController
import player.Monster
import player.Player
import player.PlayerController
import player.PlayerId

fun main() {
    println("Initializing King of Tokyo Game Engine...")

    val player1 = Player(id = PlayerId("p1"), name = "Godzilla", monster = Monster.GIGA_ZAUR)
    val player2 = Player(id = PlayerId("p2"), name = "Mecha-01", monster = Monster.MEKA_DRAGON)
    val player3 = Player(id = PlayerId("p3"), name = "CyberBunny", monster = Monster.CYBER_BUNNY)
    val player4 = Player(id = PlayerId("p4"), name = "AlienInvader", monster = Monster.ALIENOID)

    val players = mutableListOf<Player>(player1, player2, player3)
        //players.add(player4)


        // Using AI controllers for simulation demo (Can easily be swapped for ConsolePlayerController, Android UI controller, etc.)
    val controllers =  mutableMapOf<PlayerId, PlayerController>()
    for (player in players) {
        controllers[player.id] = AiPlayerController()
    }

    if (players.size < 4) {
        controllers[player4.id] = ConsolePlayerController()
        players.add(player4)
    }

    val game = GameEngine(
        players = players,
        controllers = controllers,
        config = GameConfig(
            baseDiceCount = 6,
            maxRerolls = 3,
            winningVictoryPoints = 20
        ),
        eventListener = ConsoleGameEventListener()
    )

    val winner = game.runGame()
    println("\nMatch concluded successfully with winner: ${winner.name}!")
}