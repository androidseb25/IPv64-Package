# IPv64 – Android/Kotlin SDK für die IPv64.net API v2 und Node64

Das SDK enthält zwei Clients mit gemeinsamem Kern:

- **`IPv64`** – die Cloud-API [IPv64.net API v2](https://ipv64.net/api/v2/docs) (DynDNS, DNS,
  Healthchecks, DNS-Failover, CDN, Cloud Router, DNS64, Healthchecks 2.0, Nodes, Honeypot64, Threat).
  Bildet alle 192 Operationen der [OpenAPI-Spezifikation](https://ipv64.net/api/v2/openapi.json) ab.
- **`Node64`** – die lokale API einer Node64-Node im eigenen Netz (Healthchecks, Status-Seiten,
  Docker/Proxmox, DNS64, PROXY64, Zertifikate, DynDNS, Cluster, Honeypot64). Bildet alle
  290 Operationen der Node64-Spezifikation (v1.2.0) ab. Siehe [Node64](#node64).

- 100 % Kotlin, `suspend`-API (Coroutines). Alle Aufrufe sind main-safe.
- OkHttp 5 + kotlinx.serialization, keine Reflection.
- Explicit API Mode, typisierte Modelle, Enums mit Fallback für unbekannte Werte.
- Fehler nach RFC 9457 als `IPv64ApiException` mit `code`, `detail`, `field` und `retryAfterSeconds`.
- minSdk 26, compileSdk 37, Java 17.

## Einbinden

[![JitPack](https://jitpack.io/v/androidseb25/IPv64-Package.svg)](https://jitpack.io/#androidseb25/IPv64-Package)

Das Package wird über [JitPack](https://jitpack.io/#androidseb25/IPv64-Package) verteilt.

**1. JitPack als Repository eintragen** (`settings.gradle.kts`):

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

**2. Abhängigkeit hinzufügen** – mit Version Catalog (`gradle/libs.versions.toml`):

```toml
[versions]
ipv64 = "1.0.0"

[libraries]
ipv64 = { module = "com.github.androidseb25:IPv64-Package", version.ref = "ipv64" }
```

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(libs.ipv64)
}
```

oder direkt:

```kotlin
implementation("com.github.androidseb25:IPv64-Package:1.0.0")
```

**3. Voraussetzungen der App:** `compileSdk = 37` (verlangt von OkHttp 5.5), `minSdk` ≥ 26,
Java 17. Die `INTERNET`-Berechtigung bringt das Package selbst mit. OkHttp, Coroutines und
kotlinx.serialization kommen transitiv mit.

Alternativ als lokales Modul (`include(":ipv64")` + `implementation(project(":ipv64"))`) oder per
`./gradlew :ipv64:publishToMavenLocal` als `de.sr:ipv64:1.0.0` aus `mavenLocal()`.

## Schnellstart

```kotlin
val ipv64 = IPv64(apiKey = BuildConfig.IPV64_API_KEY)

lifecycleScope.launch {
    try {
        val me = ipv64.account.me()
        val domains = ipv64.domains.list()
        ipv64.dynDns.update("meinname.ipv64.de")
    } catch (e: IPv64ApiException) {
        when {
            e.isUnauthorized -> showLogin()
            e.isRateLimited -> retryIn(e.retryAfterSeconds ?: 10)
            else -> showError(e.detail ?: e.message)
        }
    } catch (e: IPv64NetworkException) {
        showOffline()
    }
}
```

Erzeuge **eine** Instanz pro API-Key und verwende sie wieder (z. B. per DI als Singleton).
Instanzen sind thread-safe und teilen sich den Connection-Pool.

## Beispiel: Implementierung in einer App (MVVM + Compose)

Ein vollständiges Beispiel mit Repository, ViewModel und Compose-Screen. Den API-Key nicht im
Code hinterlegen, sondern z. B. vom Nutzer eingeben lassen und verschlüsselt speichern.

**Client bereitstellen** – eine Instanz für die ganze App (hier ohne DI-Framework):

```kotlin
import de.sr.ipv64.IPv64
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object Ipv64Provider {
    @Volatile private var client: IPv64? = null

    /** Liefert den Client für [apiKey]; bei neuem Key wird er neu erstellt. */
    fun get(apiKey: String): IPv64 =
        client?.takeIf { it.apiKey == apiKey } ?: synchronized(this) {
            client?.takeIf { it.apiKey == apiKey } ?: IPv64(
                apiKey = apiKey,
                okHttpClient = OkHttpClient.Builder()
                    .callTimeout(30, TimeUnit.SECONDS)
                    .build(),
            ).also { client = it }
        }
}
```

Mit Hilt/Koin entsprechend als `@Singleton` bzw. `single { IPv64(apiKey = get()) }`.

**Repository** – kapselt das SDK und übersetzt Fehler in ein eigenes Ergebnis:

```kotlin
import de.sr.ipv64.IPv64
import de.sr.ipv64.IPv64ApiException
import de.sr.ipv64.IPv64Exception
import de.sr.ipv64.IPv64NetworkException
import de.sr.ipv64.model.Domain
import de.sr.ipv64.model.DynDnsResult

class DomainRepository(private val ipv64: IPv64) {

    suspend fun domains(): Result<List<Domain>> = call { ipv64.domains.list() }

    /** Aktualisiert die Domain mit der aktuellen öffentlichen IP (vom Server erkannt). */
    suspend fun updateDynDns(domain: String): Result<DynDnsResult> =
        call { ipv64.dynDns.update(domain) }

    private suspend fun <T> call(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: IPv64Exception) {
        Result.failure(e)
    }
}

/** Nutzerfreundliche Fehlermeldung für die UI. */
fun Throwable.userMessage(): String = when (this) {
    is IPv64ApiException -> when {
        isUnauthorized -> "API-Key ungültig – bitte neu anmelden."
        isRateLimited -> "Zu viele Anfragen, bitte in ${retryAfterSeconds ?: 10} s erneut versuchen."
        else -> detail ?: "Fehler $status"
    }
    is IPv64NetworkException -> "Keine Verbindung zu ipv64.net."
    else -> message ?: "Unbekannter Fehler"
}
```

**ViewModel** – hält den UI-Zustand als `StateFlow`:

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.sr.ipv64.model.Domain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DomainsUiState(
    val loading: Boolean = false,
    val domains: List<Domain> = emptyList(),
    val message: String? = null,
)

class DomainsViewModel(private val repository: DomainRepository) : ViewModel() {

    private val _state = MutableStateFlow(DomainsUiState())
    val state: StateFlow<DomainsUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, message = null) }
            repository.domains()
                .onSuccess { list -> _state.update { it.copy(loading = false, domains = list) } }
                .onFailure { e -> _state.update { it.copy(loading = false, message = e.userMessage()) } }
        }
    }

    fun updateDynDns(domain: String) {
        viewModelScope.launch {
            repository.updateDynDns(domain)
                .onSuccess { result ->
                    val text = if (result.changed == true) "IP von $domain aktualisiert" else "$domain ist aktuell"
                    _state.update { it.copy(message = text) }
                }
                .onFailure { e -> _state.update { it.copy(message = e.userMessage()) } }
        }
    }
}
```

Ohne DI das ViewModel über eine Factory erzeugen:

```kotlin
val viewModel: DomainsViewModel = viewModel {
    DomainsViewModel(DomainRepository(Ipv64Provider.get(apiKey)))
}
```

**Compose-Screen**:

```kotlin
@Composable
fun DomainsScreen(viewModel: DomainsViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.message?.let { Text(it, color = MaterialTheme.colorScheme.secondary) }

        LazyColumn {
            items(state.domains, key = { it.id ?: it.name.orEmpty().hashCode().toLong() }) { domain ->
                ListItem(
                    headlineContent = { Text(domain.name.orEmpty()) },
                    supportingContent = { Text("${domain.kind?.value ?: "?"} · Updates: ${domain.updateCount ?: 0}") },
                    trailingContent = {
                        TextButton(onClick = { viewModel.updateDynDns(domain.name.orEmpty()) }) {
                            Text("Update")
                        }
                    },
                )
            }
        }
    }
}
```

**Node64 im selben Projekt** – z. B. alle Checks einer Node, die gerade down sind:

```kotlin
import de.sr.ipv64.Node64ApiException
import de.sr.ipv64.node64.Node64
import de.sr.ipv64.node64.model.CheckStateValue

val node = Node64(
    baseUrl = "https://node.example.home64.de/api",
    apiKey = nodeApiKey,
    actor = "Android-App",
)

viewModelScope.launch {
    try {
        val down = node.checks.list(state = CheckStateValue.DOWN)
        down.forEach { check -> println("${check.name}: down seit ${check.state?.since}") }
    } catch (e: Node64ApiException) {
        if (e.isNotFound) println("Healthchecks-Modul ist auf der Node ausgeschaltet")
        else println(e.error)
    }
}
```

Für `http://`-Adressen im LAN zusätzlich Klartext für den Host der Node erlauben
(`res/xml/network_security_config.xml`):

```xml
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="false">192.168.1.10</domain>
    </domain-config>
</network-security-config>
```

und im Manifest `android:networkSecurityConfig="@xml/network_security_config"` am
`<application>`-Element setzen.

## Konfiguration

```kotlin
val ipv64 = IPv64(
    apiKey = "…",
    actAs = null,                              // X-IPv64-Act-As für freigegebene Accounts
    authScheme = IPv64AuthScheme.Bearer,       // oder ApiKeyHeader (X-API-Key)
    okHttpClient = myOkHttpClient,             // z. B. mit Logging-Interceptor
)

// Anfragen im freigegebenen Account eines anderen Nutzers ausführen
val shared = ipv64.withActAs("kunde@example.com")
shared.domains.list()
```

Öffentliche Endpunkte (`ipv64.meta`, Teile von `ipv64.threat`) funktionieren auch ohne API-Key:
`IPv64().meta.health()`.

## Bereiche

| Property | Inhalt |
|---|---|
| `meta` | Service-Info, Health, eigene IP, OpenAPI-Dokument |
| `account` | Account-Daten, Logs, Account-Freigaben |
| `apiKeys` | API-Keys verwalten |
| `domains` | DynDNS- und eigene Domains |
| `dynDns` | DynDNS-Updates |
| `records` | DNS-Records |
| `healthchecks` | Healthchecks (klassisch) |
| `integrations` | Benachrichtigungen |
| `failover` | DNS-Failover-Policies |
| `statusPages` | Statusseiten |
| `offlinePages` | Offline-Pages inkl. Icon-Upload |
| `cdn` | CDN, Mappings, Access-Lists |
| `cloudRouters` | Cloud Router, Routen, Firewall, Public IPs |
| `gateways` | Gateways, OSPF, Port-Forwards |
| `peers` | WireGuard-Peers inkl. Config |
| `dns64` | DNS64-Clients |
| `hc2` | Healthchecks 2.0 (`hc2.checks`, `hc2.channels`, `hc2.maintenance`) |
| `nodes` | Node64-Nodes |
| `honeypot` | Honeypot64 |
| `threat` | Threat-Intelligence, Watchlist, Feed, Passwort-Prüfung |

## Beispiele

```kotlin
// DNS-Record anlegen
ipv64.records.create("meinname.ipv64.de", type = RecordType.TXT, content = "hello", prefix = "_test")

// Healthchecks 2.0 – Ereignisse seitenweise laden
var page = ipv64.hc2.events(limit = 100)
while (page.meta?.hasMore == true) {
    page = ipv64.hc2.events(limit = 100, before = page.meta?.nextBefore)
}

// WireGuard-Config eines Peers
val config: String = ipv64.peers.config(peerToken)

// Threat-Feed mit ETag (304 Not Modified liefert feed = null)
val result = ipv64.threat.feed(ifNoneMatch = lastEtag)
if (!result.notModified) { process(result.feed); lastEtag = result.etag }

// Passwort gegen geleakte Hashes prüfen (k-Anonymität, nur 5 SHA-1-Zeichen verlassen das Gerät)
val leaks = ipv64.threat.passwordCount("hunter2")
```

## Node64

```kotlin
val node = Node64(
    baseUrl = "http://node.example.home64.de/api",   // inkl. /api
    apiKey = "node64_…",                               // Einstellungen > API-Keys
    actor = "Android-App",                             // optional: Name im Audit-Log (X-Agent-Actor)
)

val status = node.node.status()
val checks = node.checks.list()
node.dns64.records.list()
node.proxy.hosts.list()
val pem: String = node.certificates.chain(certId)
```

| Property | Inhalt |
|---|---|
| `node` | Status, Version, Module, Metriken, Einstellungen, Audit, Logs, Backup/Restore, API-Keys, Export/Import, Dashboard |
| `edge` | Ports, Port-Probe, Edge-Konfiguration |
| `certificates` | Zertifikate, DNS-Accounts (ACME), eigene CAs, ACME-CAs |
| `cluster` | Node-Gruppe, Beitritts-Code, Mitglieder, Sync |
| `link` | Verknüpfung mit dem ipv64.net-Account |
| `checks` | Healthchecks, Events, Incidents, Check-Typen, Push-URLs |
| `onlineChecks` | Online-Checks über ipv64.net |
| `maintenance` | Wartungsfenster |
| `statusPages` | Status-Seiten inkl. Hinweisen und Logo |
| `docker` / `proxmox` | Docker- und Proxmox-VE-Monitoring |
| `channels` | Benachrichtigungskanäle und Zustellungen |
| `dynDns` | DynDNS-Updater der Node |
| `dns64` | DNS64-Resolver (`records`, `groups`, `forwards`, `rewrites`, `zones`) |
| `proxy` | PROXY64 (`hosts`, `streams`, `accessLists`), Schutz, Limits, Online-Veröffentlichung |
| `honeypot` | Honeypot64-Sensor |

Unterschiede zur Cloud-API:

- Antworten haben keine `data`-Hülle; Fehler kommen als `{"error": …, "field": …}` und werden als
  `Node64ApiException` (mit `error`, `field`, `retryAfterSeconds`) geworfen. Ein 404 kann auch
  bedeuten, dass das Modul auf der Node ausgeschaltet ist.
- Dateien (Backups, Zertifikats-Bundles, P12, mobileconfig, Exporte) werden als `ByteArray`
  geliefert, PEM-Ketten und Zonefiles als `String`.
- Wo die Spezifikation den Erfolgs-Body nicht dokumentiert, liefert das SDK `JsonElement?`.
- Nodes laufen im LAN oft über `http://`. Android blockiert Klartext-Verkehr standardmäßig – erlaube
  ihn in der App per `network_security_config.xml` gezielt für den Host der Node oder nutze HTTPS.
  Für selbstsignierte Zertifikate einen eigenen `OkHttpClient` übergeben.

## Fehlerbehandlung

| Exception | Bedeutung |
|---|---|
| `IPv64ApiException` | IPv64: HTTP-Status ≠ 2xx. `status`, `problem`, `code`, `detail`, `field`, `retryAfterSeconds`, `rawBody` |
| `Node64ApiException` | Node64: HTTP-Status ≠ 2xx. `status`, `error`, `field`, `retryAfterSeconds`, `rawBody` |
| `IPv64NetworkException` | Keine Verbindung, Timeout, Abbruch beim Lesen |
| `IPv64SerializationException` | Antwort passt nicht zum erwarteten Modell |

Alle erben von der `sealed class IPv64Exception`. Coroutine-Abbruch wird nicht verpackt und bricht
den HTTP-Call ab.

## Hinweise zum Modell-Design

- Alle Antwortfelder sind nullable mit Default, Listen sind standardmäßig leer. Neue Felder des
  Servers werden ignoriert, unbekannte Enum-Werte werden zu `null`. Ältere SDK-Versionen brechen
  dadurch nicht.
- Optionale Felder in Requests werden nicht gesendet, wenn sie `null` sind. Bei `PATCH` bedeutet
  `null` also „nicht ändern“.
- Zeitstempel (`date-time`) sind `kotlin.time.Instant`.
- Klassen mit Geheimnissen (API-Keys, Update-Tokens, Schlüssel) schwärzen diese in `toString()`.

## Entwicklung

```bash
./gradlew :ipv64:testDebugUnitTest   # Unit-Tests gegen MockWebServer
./gradlew :ipv64:assembleRelease     # AAR bauen
```
