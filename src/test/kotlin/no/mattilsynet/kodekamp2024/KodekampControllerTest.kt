package no.mattilsynet.kodekamp2024

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class KodekampControllerTest{

    @Test
    fun isEnemyClose(){

        val kodekamp = KodekampController()

        val x = 0
        val y = 0
        val enemy = testUnit().copy(x=1, y=0)
        val result = kodekamp.isEnemyClose(x, y, listOf(enemy))
        assertEquals(enemy, result)
    }

    @Test
    fun distanceFromEnemy() {
        val kodekamp = KodekampController()

        val result = kodekamp.distanceFromEnemy(testUnit().copy(x = 0, y = 0), testUnit().copy(x = 3, y = 3))
        assertEquals(6, result)
    }

    @Test
    fun distanceFromEnemy2() {
        val kodekamp = KodekampController()

        val result = kodekamp.distanceFromEnemy(testUnit().copy(x = 2, y = 0), testUnit().copy(x = 3, y = 3))
        assertEquals(4, result)
    }

    @Test
    fun distanceFromEnemy3() {
        val kodekamp = KodekampController()

        val result = kodekamp.distanceFromEnemy(testUnit().copy(x = 3, y = 3), testUnit().copy(x = 0, y = 0))
        assertEquals(6, result)
    }

    @Test
    fun distanceFromEnemy4() {
        val kodekamp = KodekampController()

        val result = kodekamp.distanceFromEnemy(testUnit().copy(x = 0, y = 3), testUnit().copy(x = 2, y = 0))
        assertEquals(5, result)
    }

}


fun testUnit() = Unit(y = 1, x = 1, id = "unit-1", attacks = 1, health = 1, side = "enemy", armor = 1, moves = 1, maxHealth = 1, attackStrength = 1, kind = "kind")