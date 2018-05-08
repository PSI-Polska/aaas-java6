package pl.psi.aaas.usecase

import pl.psi.aaas.usecase.timeseries.TSCalcDef
import pl.psi.aaas.usecase.timeseries.TSCalcDefWithValuesDTO
import java.time.ZonedDateTime

val ValidDefinition = TSCalcDef(
        mapOf("A" to 1L, "B" to 2L, "C" to 3L),
        mapOf("Y" to 101L, "Z" to 102L),
        ZonedDateTime.now(),
        ZonedDateTime.now().plusDays(1),
        "validScriptPath")

val TS1 = doubleArrayOf(1.0, 1.0, 1.0, 1.0)
val TS2 = doubleArrayOf(2.0, 2.0, 2.0, 2.0)
val TS3 = doubleArrayOf(3.0, 3.0, 3.0, 3.0)

val TS1Res = doubleArrayOf(-1.0, -1.0, -1.0, -1.0)
val TS1ResM = "Y" to TS1Res
val TS2Res = doubleArrayOf(-2.0, -2.0, -2.0, -2.0)
val TS2ResM = "Z" to TS2Res

val ValidDefinitionWithTS = TSCalcDefWithValuesDTO(
        ValidDefinition,
        listOf("A" to TS1, "B" to TS2, "C" to TS3))

