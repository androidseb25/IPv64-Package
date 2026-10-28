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

Als Modul:

```kotlin
// settings.gradle.kts
include(":ipv64")

// app/build.gradle.kts
dependencies {
    implementation(project(":ipv64"))
}
```

Oder lokal veröffentlichen (`./gradlew :ipv64:publishToMavenLocal`) und dann
`implementation("de.sr:ipv64:1.0.0")` mit `mavenLocal()` verwenden. Für JitPack liegt eine
`jitpack.yml` bei.

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
