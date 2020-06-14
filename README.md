# PFbot [![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0) [![Discord](https://img.shields.io/discord/577547170748563496?label=Discord)](https://discord.gg/deYQmPV) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/e6ef8e0546bc4c8d8872fcf5691a513a)](https://www.codacy.com/manual/ilinpl/PFbot?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=hevav/PFbot&amp;utm_campaign=Badge_Grade)
Powerful Open-Source Module-Based Music/Moderation bot for Discord. Current version: `v1.3`<br>
[Invite bot to your server](https://discordapp.com/oauth2/authorize?client_id=538670331938865163&permissions=36990272&scope=bot)<br>
[Rate on top.gg](https://top.gg/bot/538670331938865163)

## Current modules
-   `Admin` - deletes messages
-   `Help` - shows help page 
-   `Music` - plays music from YouTube, SoundCloud, Twitch and more
-   `Status` - shows debug info

## Languages
PFbot is multilingual bot, and you can help to add more languages. Now PFbot is translated to these languages:
-   [x] Russian
-   [x] English
-   [ ] Brazilian
-   [ ] Indian
-   [ ] Japanese
-   [ ] Chinese

## Requirements
-   `Docker`
-   `Discord token`
-   `YouTube Data API token`

## Installation
To run PFbot in production, it is recommended to use Docker.
Just pull PFbot image from Docker Hub and run it with command:
```shell script
 $ docker run --env pf_bot_token={{Discord bot token}} --env pf_yt_token={{YouTube Data API token}} --env pf_bot_prefix={{Modules commands prefix}} hevavdev/pfbot:latest
```
You can also use jar file from releases:
```shell script
 $ java -jar pfbot.jar bot_token={{Discord bot token}} yt_token={{YouTube Data API token}} bot_token={{Modules commands prefix}} 
```

## Logging
Use `pf_log_level` env variable in docker or `log_level=` in jar.
You can set `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR` and `FATAL` log level. Default is `INFO`. 

## Writing modules
### Basic
-   Make a class that implements `dev.hevav.pfbot.types.Module`
-   Add module to `Boot.modules`
-   Profit
### Localization
PFbot has a `LocalizedString` class, that translates Strings by guild's region. It supports Russian, English, Brazilian, Indian, Japanese and Chinese language.

### Embeds
PFbot has a `EmbedHelper` class, that makes modules' response in PFbot style.

## Used libraries
-   [`log4j2`](https://github.com/apache/logging-log4j2) - Logging
-   [`JSoup`](https://jsoup.org/) - Parsing YouTube Search
-   [`JDA`](https://github.com/Javacord/Javacord) - Discord API implementation
-   [`lavaplayer`](https://github.com/sedmelluq/lavaplayer) - Player for Music module
 
## Thanks to
-   [TrainPix](https://github.com/Russia9/TrainPix)
-   [lavaplayer demo](https://github.com/sedmelluq/lavaplayer)