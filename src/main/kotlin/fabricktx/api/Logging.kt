package fabricktx.api

import java.lang.management.ManagementFactory
import java.util.*

class Logger(@PublishedApi internal val name : String,
             @PublishedApi internal val logDebug : Boolean =  ManagementFactory.getRuntimeMXBean().inputArguments.toString().indexOf("-agentlib:jdwp") > 0,
             @PublishedApi internal val logInfo : Boolean = true,@PublishedApi internal val logWarning : Boolean = true) {
    inline fun debug(lazyMessage: () -> String) =
        if (logDebug) println("${Date()} [$name/DEBUG]: ${lazyMessage()}") else Unit
    inline fun info(lazyMessage: () -> String) = if (logInfo) println("${Date()} [$name/INFO]: ${lazyMessage()}") else Unit
    inline fun warning(lazyMessage: () -> String) =
        if (logWarning) println("${Date()} [$name/WARN]: ${lazyMessage()}") else Unit
}

