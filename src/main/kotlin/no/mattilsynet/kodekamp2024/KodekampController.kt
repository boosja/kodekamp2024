package no.mattilsynet.kodekamp2024

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.ResponseEntity
import kotlin.random.Random

data class RequestData(
    val name: String
)

data class GameState(
    val turnNumber: Int,
    val yourId: String,
    val enemyUnits: List<Unit>,
    val boardSize: BoardSize,
    val player2: String,
    val player1: String,
    val moveActionsAvailable: Int,
    val attackActionsAvailable: Int,
    val friendlyUnits: List<Unit>,
    val uuid: String
)

data class Unit (
    val y: Int,
    val moves: Int,
    val maxHealth: Int,
    val attackStrength: Int,
    val id: String,
    val kind: String,
    val health: Int,
    val side: String,
    val armor: Int,
    val x: Int,
    val attacks: Int
)

data class BoardSize(val w: Int, val h: Int)

data class Action (
    val unit: String,
    val action: String,
    val x: Int,
    val y: Int
)

//[{"it": "it-2", "action": "move",   "x": 2, "y": 2},
//{"it": "it-2", "action": "attack", "x": 2, "y": 1},
//{"it": "it-1", "action": "move",   "x": 1, "y": 2}]

@RestController
class KodekampController {
    @PostMapping("/kodekamp")
    fun post(
        @RequestBody body: GameState
    ): ResponseEntity<List<Action>> {



        val actions = body.friendlyUnits.flatMap {
            val xMove = move(it.x)
            val yMove = move(it.y)
            listOf(
                Action(it.id, "attack", move(it.x), move(it.y)),
                Action(it.id, "move", xMove, yMove),
                Action(it.id, "move", move(it.x), move(it.y)),
            )
        }


//        val actions = mutableListOf<Action>(
//            Action("unit-3", "move", 2, 2),
//            Action("unit-3", "attack", 1, 2),
//            Action("unit-4", "move", 3, 2),
//        )

        return ResponseEntity.ok(actions)
    }

    fun move(x: Int): Int {
        val random = Random.nextInt(0, 1)
        val newMove = x - random
        return if (newMove < 0) {
            x + random
        } else {
            newMove
        }
    }

    @GetMapping
    fun index(): ResponseEntity<String> {
        return ResponseEntity.ok()
            .header("Content-Type", "text/html")
            .body(
                """
                <ul>
                    <li>POST /</li>
                    <li>GET /ping</li>
                </ul>
        """.trimIndent()
            )
    }

    @GetMapping("/ping")
    fun ping(
        @RequestParam(value = "name", defaultValue = "World")
        name: String
    ): String {
        return "Kodekamp!";
    }
}
