package no.mattilsynet.kodekamp2024

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.ResponseEntity
import kotlin.math.absoluteValue
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
    companion object {
        private var unitsCanDoStuff = true
    }

    @PostMapping("/kodekamp")
    fun post(
        @RequestBody body: GameState
    ): ResponseEntity<List<Action>> {


        var gameState = body.copy()

        val actions = mutableListOf<List<Action>>()
        while(moreActionsLeft(gameState)) {
            actions.add(gameState.friendlyUnits.random().let { unitId ->
                val (newActions, newGameState) = createTurn(unitId.id, gameState, listOf())
                gameState = newGameState
                newActions
            })
        }

        return ResponseEntity.ok(actions.flatten())
    }

    private fun moreActionsLeft(newGameState: GameState): Boolean {
        println("attac" +newGameState.attackActionsAvailable)
        println("move" +newGameState.moveActionsAvailable)
        return newGameState.moveActionsAvailable > 0 && newGameState.attackActionsAvailable > 0
    }

    private fun createTurn(
        unitId: String,
        gameState: GameState,
        actions: List<Action>,
    ): Pair<List<Action>, GameState> {
        val it = findUnit(unitId, gameState)
        val action = doAction(it, gameState)?: return Pair(actions, gameState)
        val newGameState = lagNyGamestate(gameState, action)
        return createTurn(unitId, newGameState, actions + listOf(action))
    }

    fun findUnit(unitId: String, gameState: GameState): Unit {
        return gameState.friendlyUnits.find { it.id == unitId }!!
    }

    fun doAction(nextUnit: Unit, gameState: GameState): Action? {
        val enemyUnits = gameState.enemyUnits
        val friendlyUnits = gameState.friendlyUnits

        if (nextUnit.attacks > 0 && gameState.attackActionsAvailable > 0) {
            if (nextUnit.kind == "archer") {
                val enemies = finnFienderSomKanSkytes(nextUnit, enemyUnits)
                val enemy = finnSkyteFiende(nextUnit, enemies)
                if (enemy != null)
                    return doAttack(nextUnit, enemy)
            }
            if (nextUnit.kind == "wizard") {
                val enemies = finnFienderSomKanTrolles(nextUnit, enemyUnits)
                val enemy = finnSkyteFiende(nextUnit, enemies)
                if (enemy != null)
                    return doAttack(nextUnit, enemy)
            }

            val enemy = isEnemyClose(nextUnit.x, nextUnit.y, enemyUnits)
            if (enemy != null) {
                return doAttack(nextUnit, enemy)
            }
        }

        if (nextUnit.moves == 0 || gameState.moveActionsAvailable == 0) {
            return null
        }

        return findMove(nextUnit, friendlyUnits, enemyUnits)
    }

    fun lagNyGamestate(gameState: GameState, action: Action): GameState {
        return gameState.copy(
            attackActionsAvailable = decActionsAvailable(gameState.attackActionsAvailable, action, "attack"),
            moveActionsAvailable = decActionsAvailable(gameState.moveActionsAvailable, action, "move"),
            friendlyUnits = newFriendlyUnits(gameState, action)
        )
    }

    private fun newFriendlyUnits(
        gameState: GameState,
        action: Action
    ) = gameState.friendlyUnits.map {
        if (it.id == action.unit) {
            if (action.action == "attack") {
                it.copy(attacks = it.attacks - 1)
            } else {
                it.copy(x = action.x, y = action.y, moves = it.moves - 1)
            }
        } else {
            it
        }
    }

    fun decActionsAvailable(avail: Int, action: Action, actionType: String): Int {
        return if (action.action == actionType) {
            avail - 1
        } else {
            avail
        }
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

        return actions.map { Pair(it, (it.x - enemy.x).absoluteValue + (it.y - enemy.y).absoluteValue) }.minBy { it.second }?.first ?: null
    }

    fun distanceFromEnemy(unit: Unit, enemy: Unit): Int {
        return (unit.x - enemy.x).absoluteValue + (unit.y - enemy.y).absoluteValue
    }

    fun finnFienderSomKanSkytes(unit: Unit, enemyUnits: List<Unit>): List<Unit> {
        return enemyUnits.filter {
            distanceFromEnemy(unit, it) <= 4
        }
    }

    fun finnFienderSomKanTrolles(unit: Unit, enemyUnits: List<Unit>): List<Unit> {
        return enemyUnits.filter {
            distanceFromEnemy(unit, it) <= 3
        }
    }

    fun finnSkyteFiende(unit: Unit, enemyUnits: List<Unit>): Unit? =
        if (enemyUnits.isNotEmpty()) enemyUnits.random() else null

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
