package io.nekohasekai.sfa.database

import androidx.room.Room
import io.nekohasekai.sfa.Application
import io.nekohasekai.sfa.bg.ProxyService
import io.nekohasekai.sfa.bg.VPNService
import io.nekohasekai.sfa.constant.Path
import io.nekohasekai.sfa.constant.ServiceMode
import io.nekohasekai.sfa.constant.SettingsKey
import io.nekohasekai.sfa.database.preference.KeyValueDatabase
import io.nekohasekai.sfa.database.preference.RoomPreferenceDataStore
import io.nekohasekai.sfa.ktx.boolean
import io.nekohasekai.sfa.ktx.int
import io.nekohasekai.sfa.ktx.long
import io.nekohasekai.sfa.ktx.map
import io.nekohasekai.sfa.ktx.string
import io.nekohasekai.sfa.ktx.stringSet
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File

object Settings {
    @OptIn(DelicateCoroutinesApi::class)
    private val instance by lazy {
        Application.application.getDatabasePath(Path.SETTINGS_DATABASE_PATH).parentFile?.mkdirs()
        Room.databaseBuilder(
            Application.application,
            KeyValueDatabase::class.java,
            Path.SETTINGS_DATABASE_PATH,
        ).allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .enableMultiInstanceInvalidation()
            .setQueryExecutor { GlobalScope.launch { it.run() } }
            .build()
    }
    val dataStore = RoomPreferenceDataStore(instance.keyValuePairDao())
    var selectedProfile by dataStore.long(SettingsKey.SELECTED_PROFILE) { -1L }
    var serviceMode by dataStore.string(SettingsKey.SERVICE_MODE) { ServiceMode.NORMAL }
    var startedByUser by dataStore.boolean(SettingsKey.STARTED_BY_USER)

    var dynamicNotification by dataStore.boolean(SettingsKey.DYNAMIC_NOTIFICATION) { true }
    var disableDeprecatedWarnings by dataStore.boolean(SettingsKey.DISABLE_DEPRECATED_WARNINGS) { false }

    const val PER_APP_PROXY_DISABLED = 0
    const val PER_APP_PROXY_EXCLUDE = 1
    const val PER_APP_PROXY_INCLUDE = 2

    var autoRedirect by dataStore.boolean(SettingsKey.AUTO_REDIRECT) { false }
    var perAppProxyEnabled by dataStore.boolean(SettingsKey.PER_APP_PROXY_ENABLED) { false }
    var perAppProxyMode by dataStore.int(SettingsKey.PER_APP_PROXY_MODE) { PER_APP_PROXY_EXCLUDE }
    var perAppProxyList by dataStore.stringSet(SettingsKey.PER_APP_PROXY_LIST) { emptySet() }
    var perAppProxyManagedMode by dataStore.boolean(SettingsKey.PER_APP_PROXY_MANAGED_MODE) { false }
    var perAppProxyManagedList by dataStore.stringSet(SettingsKey.PER_APP_PROXY_MANAGED_LIST) { emptySet() }

    const val PACKAGE_QUERY_MODE_SHIZUKU = "SHIZUKU"
    const val PACKAGE_QUERY_MODE_ROOT = "ROOT"
    var perAppProxyPackageQueryMode by dataStore.string(SettingsKey.PER_APP_PROXY_PACKAGE_QUERY_MODE) { PACKAGE_QUERY_MODE_SHIZUKU }

    fun getEffectivePerAppProxyMode(): Int = if (perAppProxyManagedMode) {
        PER_APP_PROXY_EXCLUDE
    } else {
        perAppProxyMode
    }

    fun getEffectivePerAppProxyList(): Set<String> = if (perAppProxyManagedMode) {
        perAppProxyManagedList
    } else {
        perAppProxyList
    }

    var allowBypass by dataStore.boolean(SettingsKey.ALLOW_BYPASS) { false }
    var systemProxyEnabled by dataStore.boolean(SettingsKey.SYSTEM_PROXY_ENABLED) { true }

    var privilegeSettingsEnabled by dataStore.boolean(SettingsKey.PRIVILEGE_SETTINGS_ENABLED) { false }
    var privilegeSettingsList by dataStore.stringSet(SettingsKey.PRIVILEGE_SETTINGS_LIST) { emptySet() }
    var privilegeSettingsInterfaceRenameEnabled by dataStore.boolean(
        SettingsKey.PRIVILEGE_SETTINGS_INTERFACE_RENAME_ENABLED,
    ) { false }
    var privilegeSettingsInterfacePrefix by dataStore.string(SettingsKey.PRIVILEGE_SETTINGS_INTERFACE_PREFIX) { "wlan" }

    var oomKillerEnabled by dataStore.boolean(SettingsKey.OOM_KILLER_ENABLED) { false }
    var oomKillerDisabled by dataStore.boolean(SettingsKey.OOM_KILLER_DISABLED) { true }
    var oomMemoryLimitMB by dataStore.int(SettingsKey.OOM_MEMORY_LIMIT_MB) { 50 }

    var dashboardItemOrder by dataStore.string(SettingsKey.DASHBOARD_ITEM_ORDER) { "" }
    var dashboardDisabledItems by dataStore.stringSet(SettingsKey.DASHBOARD_DISABLED_ITEMS) { emptySet() }

    var activeRemoteServerId by dataStore.long(SettingsKey.ACTIVE_REMOTE_SERVER_ID) { 0L }

    // Tailscale SSH
    var tailscaleSSHRememberedUsernames by dataStore.map(SettingsKey.TAILSCALE_SSH_REMEMBERED_USERNAMES)
    var tailscaleSSHQuickConnectPeers by dataStore.stringSet(SettingsKey.TAILSCALE_SSH_QUICK_CONNECT_PEERS)
    var tailscaleSSHLightTheme by dataStore.string(SettingsKey.TAILSCALE_SSH_LIGHT_THEME) { "base16-3024-light" }
    var tailscaleSSHDarkTheme by dataStore.string(SettingsKey.TAILSCALE_SSH_DARK_THEME) { "argonaut" }
    var tailscaleSSHFontFamily by dataStore.string(SettingsKey.TAILSCALE_SSH_FONT_FAMILY)
    var tailscaleSSHFontSize by dataStore.int(SettingsKey.TAILSCALE_SSH_FONT_SIZE) { 14 }
    var tailscaleSSHCustomFontPath by dataStore.string(SettingsKey.TAILSCALE_SSH_CUSTOM_FONT_PATH)

    fun serviceClass(): Class<*> = when (serviceMode) {
        ServiceMode.VPN -> VPNService::class.java
        else -> ProxyService::class.java
    }

    suspend fun rebuildServiceMode(): Boolean {
        var newMode = ServiceMode.NORMAL
        try {
            if (needVPNService()) {
                newMode = ServiceMode.VPN
            }
        } catch (_: Exception) {
        }
        if (serviceMode == newMode) {
            return false
        }
        serviceMode = newMode
        return true
    }

    private suspend fun needVPNService(): Boolean {
        val selectedProfileId = selectedProfile
        if (selectedProfileId == -1L) return false
        val profile = ProfileManager.get(selectedProfile) ?: return false
        val content = JSONObject(File(profile.typed.path).readText())
        val inbounds = content.getJSONArray("inbounds")
        for (index in 0 until inbounds.length()) {
            val inbound = inbounds.getJSONObject(index)
            if (inbound.getString("type") == "tun") {
                return true
            }
        }
        return false
    }
}
