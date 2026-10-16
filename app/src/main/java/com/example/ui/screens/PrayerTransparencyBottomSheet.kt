package com.example.ui.screens
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizedUiText
import com.example.ui.localization.LocalAppLocale

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AsrJuristicMethod
import com.example.domain.model.CalculationMethod
import com.example.prayer.AstronomicalPrayerCalculator
import com.example.prayer.HijriRegion
import com.example.ui.components.AppCard
import com.example.ui.components.IconBadge
import com.example.ui.components.Spacing
import com.example.viewmodel.MainUiState
import com.example.viewmodel.MainViewModel
import java.util.Calendar
import java.util.TimeZone

private enum class TransparencyCategory(val label: String) {
    ASTRONOMICAL("Astronomical Math"),
    JURISTIC("Juristic Rules"),
    PRIVACY_OBSERVABILITY("Privacy & Logs")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTransparencyBottomSheet(
    uiState: MainUiState,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    com.example.core.analytics.AnalyticsScreen("prayer_transparency_sheet")
    var selectedCategory by remember { mutableStateOf(TransparencyCategory.ASTRONOMICAL) }
    val locale = LocalAppLocale.current
    // Hemisphere letters follow the sign, so southern and western cities are not shown as N/E.
    val coordinates = uiState.selectedCity.let { city ->
        String.format(
            locale, "%.4f° %s, %.4f° %s",
            kotlin.math.abs(city.latitude), if (city.latitude < 0) "S" else "N",
            kotlin.math.abs(city.longitude), if (city.longitude < 0) "W" else "E"
        )
    }

    val calendar = remember(uiState.selectedCity) {
        Calendar.getInstance(TimeZone.getTimeZone(uiState.selectedCity.timeZoneId))
    }
    val ephemeris = remember(uiState.selectedCity, calendar) {
        AstronomicalPrayerCalculator.calculateSolarParameters(
            calendar = calendar,
            latitude = uiState.selectedCity.latitude,
            longitude = uiState.selectedCity.longitude
        )
    }

    ModalBottomSheet(
        onDismissRequest = com.example.core.analytics.AppAnalytics.trackAction("prayer_transparency_bottom_sheet", "dismiss_sheet", onDismiss),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("prayer_transparency_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBadge(icon = Icons.Default.Info, accent = MaterialTheme.colorScheme.primary, size = 44.dp)
                Spacer(Modifier.width(Spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    LocalizedText(
                        text = "Why This Time?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    LocalizedText(
                        text = "Real-time astronomical & juristic transparency",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = com.example.core.analytics.AppAnalytics.trackAction("prayer_transparency_bottom_sheet", "close_sheet", onDismiss)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = localizedUiText("Close"))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(TransparencyCategory.values(), key = { it.name }) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        shape = RoundedCornerShape(50),
                        leadingIcon = if (selectedCategory == category) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null,
                        onClick = { com.example.core.analytics.AppAnalytics.interaction("prayer_transparency_bottom_sheet", "select_transparency_category", "tap", item = category.name); selectedCategory = category },
                        label = { LocalizedText(category.label) },
                        modifier = Modifier.testTag("transparency_chip_${category.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                when (selectedCategory) {
                    TransparencyCategory.ASTRONOMICAL -> {
                        item {
                            AppCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                                outlined = false,
                                contentPadding = PaddingValues(14.dp)
                            ) {
                                Column {
                                    LocalizedText(
                                        text = "On-Device Solar Ephemeris Engine",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LocalizedText(
                                        text = "Computed locally via high-precision solar coordinates with zero external API calls.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        item {
                            TransparencyRow(
                                title = "Geographical Coordinates",
                                value = coordinates,
                                note = "Location: ${uiState.selectedCity.name}, ${uiState.selectedCity.stateOrCountry} (Timezone: ${uiState.selectedCity.timeZoneId})"
                            )
                        }

                        item {
                            TransparencyRow(
                                title = "Solar Declination (δ)",
                                value = "${String.format(locale, "%.4f", ephemeris.declinationDeg)}°",
                                note = "Current angle between the sun's rays and the plane of the Earth's equator."
                            )
                        }

                        item {
                            TransparencyRow(
                                title = "Equation of Time (EoT)",
                                value = "${String.format(locale, "%.2f", ephemeris.equationOfTimeMinutes)} minutes",
                                note = "Difference between apparent solar time (sundial) and mean solar clock time."
                            )
                        }

                        item {
                            TransparencyRow(
                                title = "Julian Day Number (JDN)",
                                value = String.format(locale, "%.4f", ephemeris.julianDate),
                                note = "Continuous astronomical epoch count used to prevent calendar drift."
                            )
                        }

                        item {
                            TransparencyRow(
                                title = "Astronomical Solar Noon (Dhuhr Base)",
                                value = AstronomicalPrayerCalculator.formatHoursToAmPm(ephemeris.solarNoonHours, locale),
                                note = "Exact moment the sun crosses the local meridian (T₀ = 12:00 + TZ - λ/15 - EoT)."
                            )
                        }

                        item {
                            TransparencyRow(
                                title = "Atmospheric Refraction Standard",
                                value = "-0.833° altitude below horizon",
                                note = "Used for accurate Sunrise and Sunset/Maghrib calculations (accounting for 34' refraction + 16' solar semi-diameter)."
                            )
                        }
                    }

                    TransparencyCategory.JURISTIC -> {
                        item {
                            TransparencyRow(
                                title = "Calculation Authority",
                                value = uiState.prayerConfig.method.title,
                                note = uiState.prayerConfig.method.ishaIntervalMinutes?.let { interval ->
                                    "Fajr twilight angle: ${uiState.prayerConfig.method.fajrAngle}° | " +
                                        "Isha: Maghrib + $interval min (+30 min in Ramadan)"
                                } ?: "Fajr twilight angle: ${uiState.prayerConfig.method.fajrAngle}° | " +
                                    "Isha angle: ${uiState.prayerConfig.method.ishaAngle}°"
                            )
                        }

                        item {
                            TransparencyRow(
                                title = "Asr Juristic Calculation",
                                value = uiState.prayerConfig.asrMethod.title,
                                note = uiState.prayerConfig.asrMethod.description
                            )
                        }

                        item {
                            TransparencyRow(
                                title = "High-latitude adjustment",
                                value = uiState.prayerConfig.highLatitudeRule.title,
                                note = "Bounds Fajr and Isha using the actual sunset-to-sunrise night when twilight is absent or unusually long."
                            )
                        }

                        item {
                            TransparencyRow(
                                title = "Hijri Date Conversion",
                                value = "${uiState.hijriDate.day} ${uiState.hijriDate.monthNameEnglish} ${uiState.hijriDate.year} AH (${uiState.hijriDate.monthNameArabic})",
                                note = "${HijriRegion.forTimeZone(uiState.selectedCity.timeZoneId).methodDescription} " +
                                    "User moon-sighting adjustment: ${if (uiState.hijriAdjustmentDays >= 0) "+${uiState.hijriAdjustmentDays}" else "${uiState.hijriAdjustmentDays}"} days."
                            )
                        }

                        item {
                            AppCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                                outlined = false,
                                contentPadding = PaddingValues(14.dp)
                            ) {
                                Column {
                                    LocalizedText(
                                        text = "Distinction: Salah Opening vs. Iqamah",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LocalizedText(
                                        text = "Calculated Salah time is the astronomical start of the valid prayer window. Mosque Iqamah is the scheduled congregational prayer time decided by local masjid committees.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    TransparencyCategory.PRIVACY_OBSERVABILITY -> {
                        item {
                            TransparencyRow(
                                title = "Local Prayer Calculations",
                                value = "100% On-Device Computation",
                                note = "Prayer calculations run on your device. Optional Google Analytics does not receive exact coordinates. Nearby masjid services use location when requested; see the privacy policy."
                            )
                        }

                        item {
                            TransparencyRow(
                                title = "Privacy-First Log Sanitization",
                                value = "Active (SensitiveDataRedactor)",
                                note = "Internal log pipelines automatically scrub GPS coordinates, email identifiers, and auth tokens to prevent diagnostic leakage."
                            )
                        }

                        item {
                            TransparencyRow(
                                title = "Prayer Notification Architecture",
                                value = if (uiState.isNotificationEnabled) "Active Local Alarms" else "Disabled",
                                note = "Implemented via Android AlarmManager & BroadcastReceiver. Alerts trigger strictly on-device without remote push messaging servers."
                            )
                        }

                        item {
                            TransparencyRow(
                                title = "Data Architecture Standard",
                                value = "Clean Domain-Driven Modular Monolith",
                                note = "Decoupled domain rules, immutable Quranic references, and typed Result models built for 10+ year longevity."
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/** One fact in the sheet: a small label, the value in the brand colour and a short explanation. */
@Composable
private fun TransparencyRow(title: String, value: String, note: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .padding(horizontal = 14.dp, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        LocalizedText(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LocalizedText(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        LocalizedText(
            text = note,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
