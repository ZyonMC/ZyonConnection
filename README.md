# ZyonConnection

BungeeCord plugin that sends players back to the **last server they used in each game mode**.

Servers are grouped into modes (`survival`, `skyblock`, ...). When a player enters a group from outside it
(joining the proxy, coming from the lobby or from another mode), they are redirected to the last server they used in
that group. Moves *inside* a group (e.g. nether/end portals) are left alone. If the remembered server is down, the
player falls back to the group's `main` server.

The last server per group is stored in MariaDB, so it survives proxy restarts.

## Requirements

- BungeeCord 1.19+ (or a fork), Java 17
- MariaDB (the JDBC driver is downloaded by the proxy at startup)

## Configuration

`plugins/ZyonConnection/config.yml`:

```yaml
mariadb:
  host: 127.0.0.1
  port: 3306
  database: zyon
  user: root
  password: ""

groups:
  survival:
    main: spawn                      # entry server, used when there is no data or the last one is down
    servers: [spawn, world, nether, end]   # must include main
```

Server names are the ones defined in the proxy's `config.yml`.

## Build

```bash
./gradlew build     # jar in build/libs/
./gradlew test
```

Package: `net.zyonmc.zyonconnection`
