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

data class Unit(
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

data class Action(
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


        var newGameState = body.copy()

        /*
        val actions = body.friendlyUnits.map { it.id }.flatMap { unitId ->
            val actions = createTurn(unitId, newGameState, listOf())
            actions.forEach { action ->
                newGameState = lagNyGamestate(newGameState, action)
            }

            actions
        }*/

        val actions = mutableListOf<List<Action>>()
        while(moreActionsLeft(newGameState)){
            actions.add(newGameState.friendlyUnits.random().let { unitId ->
                val actions = createTurn(unitId.id, newGameState, listOf())
                actions.forEach { action ->
                    newGameState = lagNyGamestate(newGameState, action)
                }

                actions
            })
        }





//        val actions = mutableListOf<Action>(
//            Action("unit-3", "move", 2, 2),
//            Action("unit-3", "attack", 1, 2),
//            Action("unit-4", "move", 3, 2),
//        )

        return ResponseEntity.ok(actions.flatten())
    }

    private fun moreActionsLeft(newGameState: GameState): Boolean {
        return newGameState.moveActionsAvailable > 0 && newGameState.attackActionsAvailable >0
    }

    private fun createTurn(
        unitId: String,
        gameState: GameState,
        actions: List<Action>,
    ): List<Action> {
        val it = findUnit(unitId, gameState)

        val canAttack = isEnemyClose(it.x, it.y, gameState.enemyUnits) != null
        val canMove = findMove(it, gameState.friendlyUnits, gameState.enemyUnits) != null
        if (!canAttack && !canMove) {
            return actions
        }

        if (it.moves == 0 && it.attacks == 0) {
            return actions
        }

        val action = doAction(it, gameState.friendlyUnits, gameState.enemyUnits)
        if (action != null) {
            val newGameState = lagNyGamestate(gameState, action)
            return createTurn(unitId, newGameState, actions + listOf(action))
        }

        return actions
    }

    fun findUnit(unitId: String, gameState: GameState): Unit {
        return gameState.friendlyUnits.find { it.id == unitId }!!
    }

    fun doAction(nextUnit: Unit, friendlyUnits: List<Unit>, enemyUnits: List<Unit>): Action? {
        if (nextUnit.attacks > 0) {
            if (nextUnit.kind === "archer") {
                val enemies = finnFienderSomKanSkytes(nextUnit, enemyUnits)
                val enemy = finnSkyteFiende(nextUnit, enemies)
                return doAttack(nextUnit, enemy)
            }

            val enemy = isEnemyClose(nextUnit.x, nextUnit.y, enemyUnits)
            if (enemy != null) {
                return doAttack(nextUnit, enemy)
            }
        }

        if (nextUnit.moves == 0) {
            return null
        }

        return findMove(nextUnit, friendlyUnits, enemyUnits)
    }

    fun lagNyGamestate(gameState: GameState, action: Action): GameState {
        return gameState.copy(
            attackActionsAvailable = if(action.action == "attack" ) gameState.attackActionsAvailable-1 else gameState.attackActionsAvailable ,
            moveActionsAvailable = if(action.action == "move" ) gameState.moveActionsAvailable-1 else gameState.moveActionsAvailable,
            friendlyUnits = gameState.friendlyUnits.map {
            if (it.id == action.unit) {
                if (action.action == "attack") {
                    it.copy(attacks = it.attacks - 1)
                } else {
                    it.copy(x = action.x, y = action.y, moves = it.moves - 1)
                }
            } else {
                it
            }
        })
    }

    fun doAttack(unit: Unit, enemy: Unit): Action {
        return Action(unit.id, "attack", enemy.x, enemy.y)
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

    fun findMove(unit: Unit, friendlyUnits: List<Unit>, enemyUnits: List<Unit>): Action? {
        val possibleActions = mutableListOf<Action>()
        val allUnits = listOf(friendlyUnits, enemyUnits).flatten()

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

        val actionsWithinBoard = possibleActions.filter { isWithinBoard(it) }

        val closestEnemy = findClosestEnemy(unit, enemyUnits)
        return findClosestEnemyByAction(closestEnemy, actionsWithinBoard)
    }

    fun findClosestEnemy(unit: Unit, enemyUnits: List<Unit>): Unit {
        return enemyUnits.map { Pair(it, distanceFromEnemy(unit, it)) }.minBy { it.second }.first
    }

    fun findClosestEnemyByAction(enemy: Unit, actions: List<Action>): Action? {
        if (actions.isEmpty()) {
            return null
        }

        return actions.map { Pair(it, (it.x + it.y) - (enemy.x + enemy.y)) }.minBy { it.second }?.first ?: null
    }

    fun distanceFromEnemy(unit: Unit, enemy: Unit): Int {
        return (enemy.x + enemy.y) - (unit.x + unit.y)
    }

    fun finnFienderSomKanSkytes(unit: Unit, enemyUnits: List<Unit>): List<Unit> {
        return enemyUnits.filter {
            distanceFromEnemy(unit, it) <= 4
        }
    }

    fun finnSkyteFiende(unit: Unit, enemyUnits: List<Unit>) = enemyUnits.random()

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
