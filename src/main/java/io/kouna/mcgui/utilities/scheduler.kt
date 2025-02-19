package io.kouna.mcgui.utilities

import java.io.Closeable

fun runLater(delay: Long, task: () -> Unit): Closeable {
    val plugin = currentPlugin
    val handler = plugin.server.scheduler.runTaskLater(plugin, task, delay)
    return Closeable {
        handler.cancel()
    }
}

fun interval(delay: Long, period: Long, task: (it: Closeable) -> Unit): Closeable {
    val plugin = currentPlugin
    lateinit var handler: org.bukkit.scheduler.BukkitTask
    val closeable = Closeable { handler.cancel() }
    handler = plugin.server.scheduler.runTaskTimer(plugin, Runnable { task(closeable) }, delay, period)
    return closeable
}

fun onTick(task: (it: Closeable) -> Unit) = TickSchedule.schedule(TickSchedule.main, task)
fun onTickEnd(task: (it: Closeable) -> Unit) = TickSchedule.schedule(TickSchedule.end, task)


private val closeableList = mutableListOf<()->Unit>()
fun onDisablePlugin(task: () -> Unit) = closeableList.add(task)
fun closeCurrentPlugin() {
    closeableList.forEach { it() }
}


private object TickSchedule {
    val main = mutableListOf<() -> Unit>()
    val end = mutableListOf<() -> Unit>()

    fun schedule(list: MutableList<() -> Unit>, task: (it: Closeable) -> Unit): Closeable {
        lateinit var closeable: Closeable

        val handler = { task(closeable) }
        closeable = Closeable { list.remove(handler) }

        list.add(handler)

        return closeable
    }


    init {
        interval(0,1) {
            main.forEach { it() }
            end.forEach { it() }
        }
    }
}