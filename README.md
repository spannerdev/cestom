
# 🛣 cestom

_цестом_ is an implementation of a [minestom-ce](https://github.com/hollow-cube/minestom-ce) server.

## Table of Contents
- [Features](#features)
- [Usage](#usage)
  - [Configuration](#configuration)
  - [Commands](#commands)
  - [Logging](#logging)
  - [Extensions](#extensions)
- [Credits](#credits)

## Features
- Stop command
- Re-added logging implementation
- Re-added extension manager

## Usage

### Configuration
Edit configuration in `cestom.properties`. This file will automatically be created in the working directory if it doesn't already exist.
```properties
# host address to listen on; beware of using "localhost" if behind docker
server.listen.host=0.0.0.0
# port to listen on
server.listen.port=25565
# mode any of { OFFLINE, ONLINE, BUNGEECORD, VELOCITY }
server.mode=ONLINE
# tickspeed to run at in ticks per second
server.tps=20
# distance that chunks will be sent to the client (in chunks)
server.view_distance.chunk=8
# distance that entities will be sent to the client (in chunks)
server.view_distance.entity=5
# secret for servers behind velocity
velocity.secret=""
```

### Commands
- #### `stop`  
  Aliases: `shutdown`  
  Permissions: `cestom.stop`  
  Description: Stops the server. Can be run at any time from the terminal, or through a player with matching permissions.

### Logging
Logging works in the same way as in the original Minestom. Get the logger from `MinecraftServer.LOGGER`. Supports `Component`s via Minestom's ANSI converter.

### Extensions
Extensions work exactly the same way as the original implementation. See [the Minestom wiki](https://wiki.minestom.net/expansion/extensions).

## Credits
Thanks to [Minestom](https://github.com/Minestom/Minestom) for the server, and the extension & logging system this project uses.
Thanks to the [Hollow Cube](https://github.com/hollow-cube) devs for the Minestom fork
