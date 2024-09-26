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
public class KodekampController {
    @PostMapping("/kodekamp")
    fun post(
        @RequestBody body: GameState
    ): ResponseEntity<List<Action>> {



        val actions = body.friendlyUnits.flatMap {
            val enemy = isEnemyClose(it.x, it.y, body.enemyUnits)
            val move = findMove(it, body.friendlyUnits)

            val actions = mutableListOf<Action>()
            if (enemy != null) {
                actions.add(Action(it.id, "attack", enemy.x, enemy.y),)
            }

            actions.add(move)


            actions
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

    fun isEnemyClose(x: Int, y: Int, enemyUnits: List<Unit>): Unit? {
        return enemyUnits.find {
            it.x == x + 1 && it.y == y ||
            it.x == x - 1 && it.y == y ||
            it.y == y + 1 && it.x == x ||
            it.y == y - 1 && it.x == x
        }
    }

    fun findMove(unit: Unit, allUnits: List<Unit>): Action {
        val possibleActions = mutableListOf<Action>()

        if (!allUnits.any { unit.x + 1 == it.x && unit.y == it.y }) {
            possibleActions.add(Action(unit.id, "move", unit.x + 1, unit.y))
        }

        if (!allUnits.any { unit.x == it.x && unit.y + 1 == it.y }) {
            possibleActions.add(Action(unit.id, "move", unit.x, unit.y + 1))
        }

        if (!allUnits.any { unit.x - 1 == it.x && unit.y == it.y }) {
            possibleActions.add(Action(unit.id, "move", unit.x - 1, unit.y))
        }

        if (!allUnits.any { unit.x == it.x && unit.y - 1 == it.y }) {
            possibleActions.add(Action(unit.id, "move", unit.x, unit.y - 1))
        }

        var action = possibleActions.random()
        while (!isWithinBoard(action)) {
           action = possibleActions.random()
        }

        return action
    }

    fun isWithinBoard(action: Action): Boolean {
        return action.x in 0..3 && action.y in 0..3
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
