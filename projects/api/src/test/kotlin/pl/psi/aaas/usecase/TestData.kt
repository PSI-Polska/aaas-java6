package pl.psi.aaas.usecase

import java.time.ZonedDateTime

val ValidDefinition = CalculationDefinition(
        mapOf("A" to 1L, "B" to 2L, "C" to 3L),
        mapOf("Y" to 101L, "Z" to 102L),
        ZonedDateTime.now(),
        ZonedDateTime.now().plusDays(1),
        "validScriptPath")

val TS1 = arrayOf<Double?>(1.0, 1.0, 1.0, 1.0)
val TS2 = arrayOf<Double?>(2.0, 2.0, 2.0, 2.0)
val TS3 = arrayOf<Double?>(3.0, 3.0, 3.0, 3.0)

val TS1Res = arrayOf<Double?>(-1.0, -1.0, -1.0, -1.0)
val TS1ResM = "Y" to TS1Res
val TS2Res = arrayOf<Double?>(-2.0, -2.0, -2.0, -2.0)
val TS2ResM = "Z" to TS2Res
