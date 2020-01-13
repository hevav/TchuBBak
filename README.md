# PFbot [![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0) [![Discord](https://img.shields.io/discord/577547170748563496?label=Discord)](https://discord.gg/deYQmPV)
Powerful Open-Source Module-Based Music/Moderation bot for Discord. [Invite bot to your server](https://discordapp.com/oauth2/authorize?client_id=538670331938865163&permissions=36990272&scope=bot)

## Current modules
- `Admin` - deletes messages
- `Help` - shows help page 
- `Music` - plays music from YouTube, SoundCloud, Twitch and more

## Requirements
- `Docker`
- `Discord token`
- `YouTube Data API token`

## Installation
To run PFbot in production, it is recommended to use Docker.
Just pull PFbot image from Docker Hub and run it with command:
```shell script
 $ docker run --env pf_bot_token={{Discord bot token}} --env pf_yt_token={{YouTube Data API token}} --env pf_bot_prefix={{Modules commands prefix}} hevavdev/pfbot:latest
```
You can also use jar file from releases:
```shell script
 $ java -jar pfbot-1.0.jar bot_token={{Discord bot token}} yt_token={{YouTube Data API token}} bot_token={{Modules commands prefix}} 
```
## Debugging
Use `pf_log_level` env variable in docker or `log_level=` in jar 
## Used libraries
- [`log4j2`](https://github.com/apache/logging-log4j2) - Logging
- [`JSoup`](https://jsoup.org/) - Parsing YouTube Search
- [`JDA`](https://github.com/Javacord/Javacord) - Discord API implementation
- [`lavaplayer`](https://github.com/sedmelluq/lavaplayer) - Player for Music module
 
## Thanks to
- [TrainPix](https://github.com/Russia9/TrainPix)
- [lavaplayer demo](https://github.com/sedmelluq/lavaplayer)