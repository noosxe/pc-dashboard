package com.noosxe.pc_dashboard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppTheme {
    TokyoNight,
    TokyoNightStorm,
    TokyoNightMoon,
    TokyoNightDay,
    CatppuccinMocha,
    CatppuccinMacchiato,
    CatppuccinFrappe,
    CatppuccinLatte
}

private val TokyoNightColorScheme = darkColorScheme(
    primary = TokyoNightBlue,
    secondary = TokyoNightPurple,
    tertiary = TokyoNightCyan,
    background = TokyoNightBackground,
    surface = TokyoNightSurface,
    onPrimary = TokyoNightBackground,
    onSecondary = TokyoNightBackground,
    onTertiary = TokyoNightBackground,
    onBackground = TokyoNightForeground,
    onSurface = TokyoNightForeground
)

private val TokyoNightStormColorScheme = darkColorScheme(
    primary = TokyoNightBlue,
    secondary = TokyoNightPurple,
    tertiary = TokyoNightCyan,
    background = TokyoNightStormBackground,
    surface = TokyoNightStormSurface,
    onPrimary = TokyoNightStormBackground,
    onSecondary = TokyoNightStormBackground,
    onTertiary = TokyoNightStormBackground,
    onBackground = TokyoNightStormForeground,
    onSurface = TokyoNightStormForeground
)

private val TokyoNightMoonColorScheme = darkColorScheme(
    primary = TokyoNightBlue,
    secondary = TokyoNightPurple,
    tertiary = TokyoNightCyan,
    background = TokyoNightMoonBackground,
    surface = TokyoNightMoonSurface,
    onPrimary = TokyoNightMoonBackground,
    onSecondary = TokyoNightMoonBackground,
    onTertiary = TokyoNightMoonBackground,
    onBackground = TokyoNightMoonForeground,
    onSurface = TokyoNightMoonForeground
)

private val TokyoNightDayColorScheme = lightColorScheme(
    primary = TokyoNightDayPrimary,
    secondary = TokyoNightDaySecondary,
    tertiary = TokyoNightCyan,
    background = TokyoNightDayBackground,
    surface = TokyoNightDaySurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TokyoNightDayForeground,
    onSurface = TokyoNightDayForeground
)

private val CatppuccinMochaColorScheme = darkColorScheme(
    primary = CatppuccinLavender,
    secondary = CatppuccinMauve,
    tertiary = CatppuccinSapphire,
    background = CatppuccinMochaBase,
    surface = CatppuccinMochaSurface,
    onPrimary = CatppuccinMochaBase,
    onSecondary = CatppuccinMochaBase,
    onTertiary = CatppuccinMochaBase,
    onBackground = CatppuccinMochaText,
    onSurface = CatppuccinMochaText
)

private val CatppuccinMacchiatoColorScheme = darkColorScheme(
    primary = CatppuccinLavender,
    secondary = CatppuccinMauve,
    tertiary = CatppuccinSapphire,
    background = CatppuccinMacchiatoBase,
    surface = CatppuccinMacchiatoSurface,
    onPrimary = CatppuccinMacchiatoBase,
    onSecondary = CatppuccinMacchiatoBase,
    onTertiary = CatppuccinMacchiatoBase,
    onBackground = CatppuccinMacchiatoText,
    onSurface = CatppuccinMacchiatoText
)

private val CatppuccinFrappeColorScheme = darkColorScheme(
    primary = CatppuccinLavender,
    secondary = CatppuccinMauve,
    tertiary = CatppuccinSapphire,
    background = CatppuccinFrappeBase,
    surface = CatppuccinFrappeSurface,
    onPrimary = CatppuccinFrappeBase,
    onSecondary = CatppuccinFrappeBase,
    onTertiary = CatppuccinFrappeBase,
    onBackground = CatppuccinFrappeText,
    onSurface = CatppuccinFrappeText
)

private val CatppuccinLatteColorScheme = lightColorScheme(
    primary = CatppuccinLatteLavender,
    secondary = CatppuccinLatteMauve,
    tertiary = CatppuccinLatteSapphire,
    background = CatppuccinLatteBase,
    surface = CatppuccinLatteSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = CatppuccinLatteText,
    onSurface = CatppuccinLatteText
)

@Composable
fun PCDashboardTheme(
    appTheme: AppTheme = AppTheme.TokyoNight,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.TokyoNight -> TokyoNightColorScheme
        AppTheme.TokyoNightStorm -> TokyoNightStormColorScheme
        AppTheme.TokyoNightMoon -> TokyoNightMoonColorScheme
        AppTheme.TokyoNightDay -> TokyoNightDayColorScheme
        AppTheme.CatppuccinMocha -> CatppuccinMochaColorScheme
        AppTheme.CatppuccinMacchiato -> CatppuccinMacchiatoColorScheme
        AppTheme.CatppuccinFrappe -> CatppuccinFrappeColorScheme
        AppTheme.CatppuccinLatte -> CatppuccinLatteColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
