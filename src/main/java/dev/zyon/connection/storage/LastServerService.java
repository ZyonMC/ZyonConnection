package dev.zyon.connection.storage;

import net.md_5.bungee.api.plugin.Plugin;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Cache en memoria de los jugadores conectados + persistencia asincrona. */
public final class LastServerService {

    private final Plugin plugin;
    private final LastServerRepository repository;
    private final Map<UUID, Map<String, String>> cache = new ConcurrentHashMap<>();

    public LastServerService(Plugin plugin, LastServerRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
    }

    /** Carga los datos del jugador. Bloqueante: llamar desde un hilo asincrono. */
    public void load(UUID player) {
        Map<String, String> data = new ConcurrentHashMap<>();
        try {
            data.putAll(repository.findAll(player));
        } catch (SQLException e) {
            plugin.getLogger().warning("No se pudo leer el ultimo servidor de " + player + ": " + e.getMessage());
        }
        cache.put(player, data);
    }

    public Map<String, String> lastServers(UUID player) {
        return cache.getOrDefault(player, Map.of());
    }

    public void remember(UUID player, String group, String server) {
        cache.computeIfAbsent(player, id -> new ConcurrentHashMap<>()).put(group, server);
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            try {
                repository.save(player, group, server);
            } catch (SQLException e) {
                plugin.getLogger().warning("No se pudo guardar el ultimo servidor de " + player + ": " + e.getMessage());
            }
        });
    }

    public void unload(UUID player) {
        cache.remove(player);
    }
}
