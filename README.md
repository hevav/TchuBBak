# TchuBBak [![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0) [![Discord](https://img.shields.io/discord/577547170748563496?label=Discord)](https://discord.gg/deYQmPV) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/e6ef8e0546bc4c8d8872fcf5691a513a)](https://www.codacy.com/manual/ilinpl/TchuBBak?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=hevav/TchuBBak&amp;utm_campaign=Badge_Grade)
Powerful Open-Source Module-Based Music/Moderation bot for Discord. Current version: `v2.0.0`<br>
[Invite bot to your server](https://discordapp.com/oauth2/authorize?client_id=538670331938865163&permissions=36990272&scope=bot)<br>
[Rate on top.gg](https://top.gg/bot/538670331938865163)

## Current modules
-   `Moderation` - moderation features like (temp/perm) warn, mute, ban
-   `Help` - shows help page 
-   `Voice` - allows multiplugin voice, voice recognition for Russian language
-   `Music` - plays music from YouTube, SoundCloud, Twitch and more
-   `Status` - shows debug info

## Languages
TchuBBak is multilingual bot, and you can help to add more languages(by modifying dev.hevav.tchubbot.i18n.strings). Now TchuBBak is translated to these languages:
-   [x] Russian
-   [x] English
-   [ ] Brazilian
-   [ ] Indian
-   [ ] Japanese
-   [ ] Chinese

## Requirements
-   `Docker`
-   `Vosk server`
-   `MongoDB server`
-   `Discord token`
-   `YouTube Data API token`

## Installation
To run TchuBBak in production, it is recommended to use Docker.
Just pull TchuBBak image from Docker Hub and run it with command:
```shell script
 $ docker run --env pf_bot_token={{Discord bot token}} --env pf_yt_token={{YouTube Data API token}} --env pf_bot_prefix={{Modules commands prefix}} hevavdev/TchuBBak:latest
```
You can also use jar file from releases:
```shell script
 $ java -jar TchuBBak.jar bot_token={{Discord bot token}} yt_token={{YouTube Data API token}} bot_token={{Modules commands prefix}} 
```

## Logging
Use `pf_log_level` env variable in docker or `log_level=` in jar.
You can set `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR` and `FATAL` log level. Default is `INFO`. 

## Writing modules/plugins
### Basic module
-   Make a class that extends `dev.hevav.tchubbot.modules.Module`
-   Add module to `dev.hevav.tchubbot.Config.modules`
-   Profit

### Basic plugin
Plugins are not supported yet. 

### Localization
TchuBBak has a `LocalizedString` class, which translates Strings by guild's region. It supports Russian, English, Brazilian, Indian, Japanese and Chinese language.

### Embeds
TchuBBak has a `EmbedHelper` class, which makes modules' response in TchuBBak style.

### Voice
TchuBBak has a `VoiceAdapter` class to change AudioHandlers (temp/perm)

## Todo:
-   Translate VoiceRecognition to other languages

## Used libraries
-   [`log4j2`](https://github.com/apache/logging-log4j2) - Logging
-   [`JSoup`](https://jsoup.org/) - Parsing YouTube Search
-   [`JDA`](https://github.com/DV8FromTheWorld/JDA) - Discord API implementation
-   [`lavaplayer`](https://github.com/sedmelluq/lavaplayer) - Player for the Music module
-   [`mongo-java-driver`](https://github.com/mongodb/mongo-java-driver) - Bot's database
-   [`gson`](https://github.com/google/gson) - Bot's database helper

 
## Thanks to
-   [meetinger](https://github.com/meetinger)
-   [lavaplayer demo](https://github.com/sedmelluq/lavaplayer)