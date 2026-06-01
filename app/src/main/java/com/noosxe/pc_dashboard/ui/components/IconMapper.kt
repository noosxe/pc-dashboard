package com.noosxe.pc_dashboard.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.SimpleIcons
import compose.icons.simpleicons.*

object IconMapper {
    /**
     * Maps an app icon name (from D-Bus) to a SimpleIcons or Material ImageVector.
     * Tier 1 Resolution.
     */
    fun getSimpleIcon(iconName: String): ImageVector? {
        return when (iconName.lowercase()) {
            "power", "power-profile" -> Icons.Default.Bolt
            "discord" -> SimpleIcons.Discord
            "spotify" -> SimpleIcons.Spotify
            "firefox", "firefox-esr", "firefox-bin" -> SimpleIcons.Firefox
            "google-chrome", "google-chrome-stable", "chrome" -> SimpleIcons.Googlechrome
            "brave-browser" -> SimpleIcons.Brave
            "visual-studio-code", "code", "vscode", "vscodium" -> SimpleIcons.Vscodium
            "steam" -> SimpleIcons.Steam
            "github" -> SimpleIcons.Github
            "telegram", "telegram-desktop", "materialgram" -> SimpleIcons.Telegram
            "whatsapp", "whatsapp-desktop" -> SimpleIcons.Whatsapp
            "signal", "signal-desktop" -> SimpleIcons.Signal
            "intellij-idea", "intellij-idea-community", "idea", "intellij idea" -> SimpleIcons.Intellijidea
            "android-studio", "jetbrains-studio", "com.google.androidstudio", "androidstudio", "android studio" -> SimpleIcons.Androidstudio
            "pycharm" -> SimpleIcons.Pycharm
            "webstorm" -> SimpleIcons.Webstorm
            "clion" -> SimpleIcons.Clion
            "rider" -> SimpleIcons.Rider
            "goland" -> SimpleIcons.Goland
            "phpstorm" -> SimpleIcons.Phpstorm
            "rubymine" -> SimpleIcons.Rubymine
            "datagrip" -> SimpleIcons.Datagrip
            "cursor" -> SimpleIcons.Cursor
            "zed" -> SimpleIcons.Zedindustries
            "obs", "obs-studio" -> SimpleIcons.Obsstudio
            "thunderbird" -> SimpleIcons.Thunderbird
            "bitwarden" -> SimpleIcons.Bitwarden
            "vlc" -> SimpleIcons.Vlcmediaplayer
            "wireshark" -> SimpleIcons.Wireshark
            "sublime-text" -> SimpleIcons.Sublimetext
            "vim" -> SimpleIcons.Vim
            "neovim" -> SimpleIcons.Neovim
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
            "tidal" -> SimpleIcons.Tidal
            "deezer" -> SimpleIcons.Deezer
            "plex" -> SimpleIcons.Plex
            "jellyfin" -> SimpleIcons.Jellyfin
            "kodi" -> SimpleIcons.Kodi
            "youtube-music" -> SimpleIcons.Youtubemusic
            "x", "twitter" -> SimpleIcons.X
            "mastodon" -> SimpleIcons.Mastodon
            "bluesky" -> SimpleIcons.Bluesky
            "epic-games" -> SimpleIcons.Epicgames
            "gog" -> SimpleIcons.Gogdotcom
            "origin" -> SimpleIcons.Origin
            "ubisoft" -> SimpleIcons.Ubisoft
            "battle.net" -> SimpleIcons.Battledotnet
            "ghostty" -> SimpleIcons.Ghostty
            "alacritty" -> SimpleIcons.Alacritty
            "wezterm" -> SimpleIcons.Wezterm
            "iterm2" -> SimpleIcons.Iterm2
            "apple" -> SimpleIcons.Apple
            "linux" -> SimpleIcons.Linux
            "ubuntu" -> SimpleIcons.Ubuntu
            "fedora" -> SimpleIcons.Fedora
            "archlinux", "arch" -> SimpleIcons.Archlinux
            "debian" -> SimpleIcons.Debian
            else -> null
        }
    }

    /**
     * Returns true if the icon name corresponds to a brand icon (SimpleIcons)
     * which should not be tinted.
     */
    fun isBrandIcon(iconName: String): Boolean {
        val name = iconName.lowercase()
        // Generic system icons that should be tinted
        if (name == "power" || name == "power-profile") return false
        
        // If it's in our mapping and not generic, it's a brand icon
        return getSimpleIcon(name) != null
    }
}
