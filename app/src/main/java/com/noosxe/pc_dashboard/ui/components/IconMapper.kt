package com.noosxe.pc_dashboard.ui.components

import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.SimpleIcons
import compose.icons.simpleicons.*

object IconMapper {
    /**
     * Maps an app icon name (from D-Bus) to a SimpleIcons ImageVector.
     * Tier 1 Resolution.
     */
    fun getSimpleIcon(iconName: String): ImageVector? {
        return when (iconName.lowercase()) {
            "discord" -> SimpleIcons.Discord
            "spotify" -> SimpleIcons.Spotify
            "firefox", "firefox-esr", "firefox-bin" -> SimpleIcons.Firefox
            "google-chrome", "google-chrome-stable", "chrome" -> SimpleIcons.Googlechrome
            "brave-browser" -> SimpleIcons.Brave
            "steam" -> SimpleIcons.Steam
            "github" -> SimpleIcons.Github
            "telegram", "telegram-desktop" -> SimpleIcons.Telegram
            "whatsapp", "whatsapp-desktop" -> SimpleIcons.Whatsapp
            "signal", "signal-desktop" -> SimpleIcons.Signal
            "intellij-idea", "intellij-idea-community" -> SimpleIcons.Intellijidea
            "android-studio" -> SimpleIcons.Androidstudio
            "obs", "obs-studio" -> SimpleIcons.Obsstudio
            "thunderbird" -> SimpleIcons.Thunderbird
            "bitwarden" -> SimpleIcons.Bitwarden
            "dropbox" -> SimpleIcons.Dropbox
            "google-drive" -> SimpleIcons.Googledrive
            "zoom", "zoom-desktop" -> SimpleIcons.Zoom
            "notion" -> SimpleIcons.Notion
            "trello" -> SimpleIcons.Trello
            "figma" -> SimpleIcons.Figma
            "blender" -> SimpleIcons.Blender
            "inkscape" -> SimpleIcons.Inkscape
            "gimp" -> SimpleIcons.Gimp
            "vba", "virtualbox" -> SimpleIcons.Virtualbox
            "docker" -> SimpleIcons.Docker
            "postman" -> SimpleIcons.Postman
            "insomnia" -> SimpleIcons.Insomnia
            "redis" -> SimpleIcons.Redis
            "mongodb" -> SimpleIcons.Mongodb
            "mysql" -> SimpleIcons.Mysql
            "postgresql" -> SimpleIcons.Postgresql
            "sqlite" -> SimpleIcons.Sqlite
            "python" -> SimpleIcons.Python
            "node", "nodejs" -> SimpleIcons.Nodedotjs
            "react" -> SimpleIcons.React
            "angular" -> SimpleIcons.Angular
            "vue", "vuejs" -> SimpleIcons.Vuedotjs
            "kotlin" -> SimpleIcons.Kotlin
            "go", "golang" -> SimpleIcons.Go
            "rust" -> SimpleIcons.Rust
            "php" -> SimpleIcons.Php
            "ruby" -> SimpleIcons.Ruby
            "swift" -> SimpleIcons.Swift
            "apple" -> SimpleIcons.Apple
            "linux" -> SimpleIcons.Linux
            "ubuntu" -> SimpleIcons.Ubuntu
            "fedora" -> SimpleIcons.Fedora
            "archlinux", "arch" -> SimpleIcons.Archlinux
            "debian" -> SimpleIcons.Debian
            else -> null
        }
    }
}
