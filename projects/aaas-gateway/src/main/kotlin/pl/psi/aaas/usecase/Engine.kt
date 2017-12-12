package pl.psi.aaas.usecase

typealias MappedTS = List<Pair<String, TS>>

interface Engine {
    fun schedule(calcDef: CalculationDefinition, tsValues: MappedTS)
    // TODO 12.12.2017 kskitek: Should this throw exception? return Future or sth?
}