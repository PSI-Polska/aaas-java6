package pl.psi.aaas.usecase

import java.util.concurrent.atomic.AtomicInteger
import javax.management.MXBean

@MXBean
interface Statistics {
    val executedTasks: Int
    val currentlyExecuting: Int
    val failedTasks: Int
}

interface SettableStatistics : Statistics {
    val executedTasksNo: AtomicInteger
    val currentlyExecutingNo: AtomicInteger
    val failedTasksNo: AtomicInteger
}

object JmxStatistics : SettableStatistics {
    override val executedTasksNo = AtomicInteger(0)
    override val currentlyExecutingNo = AtomicInteger(0)
    override var failedTasksNo = AtomicInteger(0)

    //    @get:ManagedOperation(id = "Executed tasks number")
    override val executedTasks: Int
        get() = executedTasksNo.get()
    @get:java.beans.BeanProperty
    override val currentlyExecuting: Int
        get() = currentlyExecutingNo.get()
    @get:java.beans.BeanProperty
    override val failedTasks: Int
        get() = failedTasksNo.get()
}