package io.nekohasekai.sfa.utils

import io.nekohasekai.libbox.Libbox

object CommandTarget {
    fun standaloneClient(): io.nekohasekai.libbox.CommandClient = Libbox.newStandaloneCommandClient()
}
