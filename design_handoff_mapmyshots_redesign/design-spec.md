# MapMyShots — Design-Spezifikation "Field Log"

Status: Entwurf für Review · Zielplattformen: Android + iOS (Compose Multiplatform) · Basis: aktueller Stand `feature/redesign` (Commit `a544749`)

## 1. Kontext & Aufgabe der App

MapMyShots durchsucht die Fotobibliothek nach Bildern **ohne GPS-Standort** und lässt die Nutzer:in sie zügig „reparieren":

- Standort von einem **zeitlich nahen Foto** übernehmen (Zeitfenster 1 / 4 / 12 Std.), oder
- Standort **manuell auf einer Karte** wählen.

Daneben: Fotos ignorieren (aus der Review-Liste ausblenden, später wiederherstellbar) oder löschen. Ein Hintergrund-Scan füllt die Liste progressiv (Fortschrittsanzeige „scanning"). Das ist im Kern ein **Aufräum-/Triage-Werkzeug** — die aktuelle Umsetzung (helles Blau/Weiß-System, Pillen-Chips, Material3-Basis mit eigenen Tokens in `MapMyShotsDesignTokens.kt`) ist funktional klar, aber visuell generisch.

**Entscheidung des Auftraggebers:** komplett neue Bildsprache, kein Feintuning des Bestehenden.

## 2. Design-Vision: „Field Log"

**Metapher:** das Feldtagebuch/Logbuch eines Kartografen bzw. Expeditions-Fotografen. Jedes Foto ohne Standort ist ein offener Log-Eintrag; das Zuordnen eines Ortes ist der Akt, ihn **abzustempeln** — wie ein Passierschein, der abgestempelt wird, sobald der Ort feststeht.

Warum das passt:
- „MapMyShots" ist wörtlich Karte + Foto — die Metapher ist nicht aufgesetzt, sondern liegt im Produktnamen.
- Der Kernloop (viele kleine Entscheidungen schnell treffen) verträgt sich gut mit einem **Ritual-Gefühl** (stempeln, abhaken) statt mit generischem App-Chrome.
- Koordinaten, Zeitstempel, Dateinamen sind ohnehin im Produkt vorhanden — die Ästhetik feiert diese „rohen Felddaten" (Mono-Zahlen, Wegpunkt-Karten) statt sie zu verstecken.

**Stimmung in Worten:** warmes Papier statt klinisches Weiß; tiefes Tinten-Navy statt Hellblau; Terracotta/Rost als Signal für „fehlt noch" statt knalliges Rot/Warngelb; Moosgrün statt Standard-Grün für „erledigt"; dünne Stichlinien und gestempelte Ecken statt reiner Schatten; eine Prise Gold für den „angewendet"-Moment.

## 3. Design-Prinzipien

1. **Ein Foto, eine Entscheidung.** Jeder Screen fokussiert auf genau eine Triage-Entscheidung; keine konkurrierenden CTAs.
2. **Fortschritt wie ein Logbuch.** Zähler (scanned/open/ignored) lesen sich wie Einträge in einem Feldjournal, nicht wie ein Dashboard.
3. **Rohdaten sind Teil des Charakters.** Koordinaten, Zeit-Deltas, Dateinamen bewusst in Mono-Schrift zeigen statt zu kaschieren.
4. **Leise Technik, warme Textur.** Datendichte UI, aber warme, papierhafte Oberflächen statt sterilem Weiß.
5. **Ein Daumen genügt.** Primäraktionen unten/thumb-reachable, große Zielflächen (≥44dp, bestehende Konvention beibehalten).

## 4. Farbsystem

| Token | Hex | Verwendung |
|---|---|---|
| `ink/900` | `#1B2A41` | Primärfarbe, Haupttext, aktive Zustände |
| `ink/700` | `#33475B` | Sekundärtext |
| `ink/500` | `#6B7E92` | Gedämpfter Text, Labels, Platzhalter |
| `paper/50` | `#FBF7EF` | App-Hintergrund (warmes Off-White/Parchment) |
| `paper/100` | `#F3ECDD` | Oberflächen/Karten (leicht dunkler als Hintergrund) |
| `paper/200` | `#E4D9C3` | Haarlinien, Ränder, Trenner |
| `rust/500` | `#C1502E` | „Kein Standort" / braucht Aufmerksamkeit |
| `rust/100` | `#F6E3D8` | Hintergrund-Tint für Rust-Badges |
| `moss/600` | `#3F7D4F` | „Standort gesetzt" / Erfolg |
| `moss/100` | `#E3EFE1` | Hintergrund-Tint für Erfolg |
| `gold/500` | `#B98A2E` | Akzent für „Angewendet"-Stempel, Highlights |
| `ink/overlay` | `#1B2A41` @ 45% | Scrim auf Bildern (Zeit/Status-Overlay) |

Kontrast-Vorgabe: alle Text-auf-Fläche-Kombinationen ≥ WCAG AA (4.5:1 für Fließtext, 3:1 für große/UI-Text). `rust/500` und `moss/600` auf `paper/50` beide vorab gegengeprüft — beide bestehen AA für 16sp+ UI-Text; für Fließtext ggf. `rust/600`/`moss/700` (dunklere Variante) verwenden.

Statusfarbe nie alleiniger Träger von Bedeutung: Badges immer Text/Icon **plus** Farbe (bestehendes Muster aus `StatusBadge` beibehalten).

## 5. Typografie

| Rolle | Font | Fallback (KMP-Ressourcen-Risiko) |
|---|---|---|
| Display/Headline (Zahlen, Screen-Titel) | Serif/Slab mit Charakter, z. B. **Fraunces** | System-Serif |
| UI/Fließtext (Buttons, Labels, Body) | Humanistische Grotesk, z. B. **Inter** | System-Sans (bereits Default) |
| Mono (Koordinaten, Zeit-Deltas, Dateiname, Zähler-Werte) | **IBM Plex Mono** / **JetBrains Mono** | System-Mono |

Hinweis: Custom Fonts benötigen Compose-Multiplatform-Font-Ressourcen für Android **und** iOS (Lizenz/Embedding prüfen — siehe Offene Punkte). Falls das im ersten Schritt zu teuer ist: Serif nur für 2–3 große Headline-Momente einsetzen (Gallery-Titel, Detail-Heldenbild-Badge), Rest bleibt System-Sans + Mono für Zahlen — das trägt bereits 80 % der neuen Anmutung.

Skala (aufbauend auf bestehenden `MapMyShotsTypography`-Werten, erweitert):

| Token | sp | Beispiel |
|---|---|---|
| caption | 12 | Metadaten-Label, Zeit-Delta-Suffix |
| body | 14 | Dialog-Text |
| label | 15–16 | Karten-Datum, Metadaten-Werte (Mono) |
| subtitle | 17 | Untertitel, Stat-Werte |
| sectionTitle | 20–22 | Abschnittstitel („Ähnliche Fotos mit Standort") |
| detailTitle | 24–25 | Foto-Detail Kontext |
| heroTitle | 30–32 | Gallery-Screen-Titel („Review" / „Ignored") — in Serif |

## 6. Spacing, Formen, Elevation, Motion

**Spacing:** bestehendes 4pt-Raster (`xxxs..bottomSpacer`, 2–24dp) unverändert übernehmen — es ist solide und muss für ein Bildsprachen-Update nicht neu erfunden werden.

**Formen:**
- Fotokarten: weiterhin stark abgerundet (18–24dp), zusätzlich **eine gestempelte Ecke**: kleines Rust- oder Moos-Tag (12×12dp, leicht rotiert −4°) statt der heutigen dunklen Overlay-Pille für Status.
- Tab-Chips: von reinen Pillen zu **„Lesezeichen"-Tabs** — oben abgerundet, unten gerade, mit 1px `paper/200`-Kante, aktiver Tab in `ink/900` mit `paper/50`-Text.
- Trenner: gestrichelte „Stich"-Linie (`paper/200`, 1px, dash 4/3) statt durchgezogener Divider — verstärkt das Journal-Gefühl in Metadaten-Karten und zwischen Vorschlagskarten.
- Dialoge/Sheets: „Notizkarten"-Stil — oberer Rand leicht ungleichmäßig (per SVG-Clip-Path oder einfacher: 2px `paper/200`-Doppelrand), sonst wie bestehende Card-Shape (18dp).

**Elevation:** Schatten warm tönen (`ink/900` @ 8 % statt reinem Schwarz), reduziert-diffus, kein hartes Material-Drop-Shadow.

**Motion:**
- Screen-Wechsel Gallery → Detail: leichtes Slide + Scale (98 % → 100 %), wie „eine Seite umblättern"; Zurück-Swipe-Geste (bereits implementiert in `PhotoDetailsScreenContent`) bleibt erhalten.
- Standort angewendet: Status-Badge „stempelt" ein — Scale 0.6→1.05→1.0 mit Rotation −8°→0°, ~220ms, Overshoot-Easing.
- Success-Banner: rutscht wie ein abgerissenes Ticket von oben ein (bestehende `SuccessBanner`-Position beibehalten, nur Timing/Easing anpassen).
- Lade-/Scan-Zustand: rotierender Kompass-Indikator statt generischem `CircularProgressIndicator`.

## 7. Iconografie & Bildbehandlung

Eigenes, reduziertes Icon-Set (Lucide/Phosphor als Ausgangsbasis, Strichstärke 1.5–2px, `ink/700`):
- Kompass (Scan/Laden)
- Pin mit Punkt (Standort gesetzt)
- Pin mit Fragezeichen (kein Standort)
- Stempel/Häkchen (angewendet)
- Lupe mit gepunkteter Spur (ähnliche Fotos suchen)
- Ablage-Symbol (ignorieren)

Fotokarten erhalten einen 1px `paper/200`-Rand wie ein physischer Abzug/Passepartout, statt randlos auf Weiß zu schweben.

## 8. Informationsarchitektur & Flows (unverändert in der Struktur, neu im Ausdruck)

```
Gallery (Tabs: Review · Ignored)
  ├─ Foto antippen ─────────────► Foto-Detail
  │                                   ├─ Vorschlag antippen → „Standort übernehmen?" → Erfolg → zurück, Foto verlässt Review-Liste
  │                                   ├─ „Auf Karte wählen" → Karten-Picker → Koordinaten anwenden → Erfolg
  │                                   ├─ Löschen → Bestätigung → zurück, Foto entfernt
  │                                   └─ Ignorieren/Wiederherstellen → zurück, Foto wechselt Tab
  └─ Lange drücken ──────────────► Aktions-Dialog (Ignorieren / Löschen)
```

Diese Struktur bleibt unverändert — das Redesign betrifft Ausdruck, nicht Navigation.

## 9. Screen-Spezifikationen

### 9.1 Gallery / Review-Liste (`PhotoListScreenContent`)

- **Header:** Screen-Titel in Serif-Display (`bottom_nav_review` / `bottom_nav_ignored`), darunter Untertitel in `ink/500` mit Foto-Anzahl (`photos_without_location_count[_loading]` / `ignored_photos_count`).
- **Log-Stempel-Zähler** (ersetzt `StatCard`): drei Karten „scanned / open / ignored" — Zahl in Mono-Display-Größe (28sp), Label klein darunter, Karte in `paper/100` mit `paper/200`-Rand, leicht wie ein Stempelabdruck gestaltet (dezente innere Umrandung).
- **Tabs:** „Lesezeichen"-Chips (siehe §6), nur sichtbar wenn `ignoredCount > 0` (bestehende Logik beibehalten).
- **Grid:** adaptiv, `minSize` 164dp (unverändert), Karten im „Print"-Stil: Foto + Datum/Zeit-Overlay unten (Scrim `ink/overlay`), Status-Tag oben (Rust „kein Standort" — im Review-Tab immer, da nur Fotos ohne Standort gelistet werden) als gestempelte Ecke statt Pille.
- **Nachladen:** Inline-Kompass-Loader am Grid-Ende (`isLoadingMore`).
- **Leerzustände:** `empty_review_photos` / `empty_ignored_photos` — Illustration: offenes, leeres Logbuch; Ton freundlich, kein Fehler-Look.
- **Vollbild-Ladezustand:** Kompass-Rotation zentriert, Copy optional „Katalogisiere Fotos …".

### 9.2 Foto-Detail (`PhotoDetailsScreenContent`)

- **Top-Bar:** minimalistisch, Ink-Icons (zurück, löschen, ignorieren/wiederherstellen), transparent über Statusleiste (`statusBarsPadding` bleibt).
- **Hero:** Foto großformatig (270dp, `hero`-Shape 24dp), Status-Stempel-Badge oben links (`location_set_badge` moos / `missing_location_badge` rust), warmer Schatten statt hartem Drop-Shadow.
- **Erfolgsbanner:** „Ticket"-Streifen (`apply_location_success`), rutscht ein, moos-getönt.
- **Metadaten-Karte** (`MetadataCard`): Zeilen Datum/Zeit/Datei/Standort — Werte in Mono, Labels in `ink/500`, gestrichelte Stich-Trennlinien statt Divider.
- **Abschnittstitel** „Ähnliche Fotos mit Standort" (`similar_photos_title`) in Serif.
- **Zeitfenster-Wahl** (`TimeWindowSelector`, 1/4/12 Std.): als „Zeitstrahl-Dial" — horizontale Segment-Kontrolle mit Mono-Beschriftung, aktives Segment `ink/900`.
- **Vorschlagskarten** (`SuggestionCard`, „Waypoint-Karten"): Thumbnail links, Ortsname, Zeit-Delta-Badge (`minutes_away`/`hours_away`/`hours_minutes_away`, Mono), „Apply"/„Applied"-Button rechts (Applied-Zustand in Gold statt Grau).
- **Leer-/Ladezustand** der Vorschlagsliste: `no_similar_photos`, Kompass-Loader.
- **Karten-Standortwahl** (`LocationMapPickerDialog`, Strings `choose_location*`): **Lücke im aktuellen Screen** — siehe §14.

### 9.3 Dialoge

| Dialog | Strings | Neue Behandlung |
|---|---|---|
| Standort übernehmen | `apply_location_title/message` | Notizkarten-Stil, Quelle-Name hervorgehoben in Mono |
| Foto löschen | `delete_photo_title/message`, `delete`/`cancel` | Rust-Akzent auf destruktivem Button |
| Foto-Aktionen (ignorieren) | `ignore_photo_title/message`, `ignore_photo_action`/`restore_photo_action` | Neutral, Ink-Icons |
| Fehler | `error_title`, `delete_failed`/`load_failed`/`apply_location_failed`, `ok` | Rust-Icon, ruhiger Ton, kein Rot-Alarm-Look |
| Karten-Standortwahl | `choose_location_title/hint`, `choose_location_coordinates` | Koordinaten-Readout in Mono groß, Vorschläge als Pins |

## 10. Komponenten-Mapping (alt → „Field Log")

| Bestehende Komponente | Neue Rolle |
|---|---|
| `StatCard` (in `GalleryHeader`) | Log-Stempel-Zähler |
| `HeaderTabChip` | Lesezeichen-Tab |
| `PhotoGridCard` | Print-Karte mit gestempelter Ecke |
| `StatusBadge` | Ink-Stempel-Badge |
| `SuccessBanner` | Ticket-Streifen |
| `SuggestionCard` | Waypoint-Karte |
| `TimeWindowSelector` | Zeitstrahl-Dial |
| `MetadataCard` | Feldnotiz-Karte |
| `DetailTopBar` | reduzierte Ink-Icon-Leiste |
| Dialoge (`ApplyLocationDialog`, `ConfirmDeletePhotoDialog`, `ErrorDialog`, `PhotoListAssetActionsDialog`, `LocationMapPickerDialog`) | Notizkarten-Modals |

## 11. Content/Copy

Bestehende `strings.xml`-Texte bleiben inhaltlich unverändert (keine i18n-Umbrüche nötig) — das Redesign ist visuell, nicht textlich. Einzige empfohlene Ergänzung: sichtbarer CTA „Auf Karte wählen" (`choose_location`) im Detail-Screen, siehe §14.

## 12. Barrierefreiheit

- Kontrastprüfung wie in §4 beschrieben; bei Zweifel dunklere Statusfarb-Variante nutzen.
- Status nie nur über Farbe — Icon + Text bleibt Pflicht (bestehendes Muster).
- Zieltouchflächen ≥ 44dp (bestehende `iconButton`-Größe beibehalten).
- Dynamic-Type-Verträglichkeit: Serif-Headlines dürfen bei großen Systemschriftgrößen umbrechen, nicht abschneiden.
- Gestrichelte Trennlinien/Texturen dürfen Screenreader-Fokusreihenfolge nicht beeinflussen (rein dekorativ, `contentDescription = null`).

## 13. Plattform-Hinweise (Compose Multiplatform)

- Bestehende Edge-Swipe-Zurück-Geste (`PhotoDetailsScreenContent`, Zeilen 73–109) unverändert erhalten — Field-Log-Redesign ändert nur Oberflächen, keine Interaktion.
- `statusBarsPadding()` beibehalten; auf iOS zusätzlich Safe-Area unten für Home-Indicator bei Bottom-CTAs prüfen (z. B. neuer „Auf Karte wählen"-Button).
- Custom Fonts über Compose-Resources für beide Targets einbinden (Android + iOS Font-Loading unterscheidet sich technisch — Aufwand vorab abschätzen).
- Dark Mode: nicht Teil dieses Passes (aktuelle Richtung ist bewusst hell/Papier-basiert); als Folgearbeit vormerken, falls gewünscht.

## 14. Empfehlungen / erkannte Lücken

- **Fehlender Einstieg für manuelle Kartenwahl:** `LocationMapPickerDialog` und die zugehörigen Strings (`choose_location*`) existieren im Code, aber `PhotoDetailsScreenContent` hat aktuell keinen sichtbaren Button, der `showLocationPicker = true` setzt. Empfehlung: sekundären Button „Auf Karte wählen" unterhalb der Vorschlagsliste bzw. im Zeitstrahl-Bereich ergänzen.
- **Custom Fonts** sind der größte Aufwandstreiber dieser Richtung — Vorschlag: Phase 1 nur Farben/Formen/Mono-Zahlen umsetzen (System-Fonts), Phase 2 Serif-Display nachziehen.

## 15. Nächster Schritt

Aus dieser Spezifikation werden als Nächstes Claude-Design-Artboards erzeugt:
1. Gallery — Review-Tab (gefüllt)
2. Gallery — Leerzustand
3. Gallery — Ignored-Tab
4. Foto-Detail — ohne Standort + Vorschlagsliste
5. Foto-Detail — Standort angewendet + Erfolgsbanner
6. Dialog-Set (Standort übernehmen / Löschen / Ignorieren / Fehler)
7. Karten-Standortwahl
8. Komponenten-/Stilblatt (Farben, Typografie, Badges, Karten, Buttons)
