package pl.psi.aaas.usecase

typealias MappedTS = List<Pair<Symbol, TS>>

interface Engine {
    fun schedule(calcDef: CalculationDefinition, tsValues: MappedTS): MappedTS
    // TODO 12.12.2017 kskitek: Should this throw exception? return Future or sth?
}