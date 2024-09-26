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

}


fun testUnit() = Unit(y = 1, x = 1, id = "unit-1", attacks = 1, health = 1, side = "enemy", armor = 1, moves = 1, maxHealth = 1, attackStrength = 1, kind = "kind")