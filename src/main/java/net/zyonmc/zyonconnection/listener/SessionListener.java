package net.zyonmc.zyonconnection.listener;

import net.zyonmc.zyonconnection.storage.LastServerService;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;

/** Carga y descarga los datos del jugador segun su sesion en el proxy. */
public final class SessionListener implements Listener {

    private final Plugin plugin;
    private final LastServerService service;

    public SessionListener(Plugin plugin, LastServerService service) {
        this.plugin = plugin;
        this.service = service;
    }

    /** El login espera (intent) a que los datos esten en cache antes de conectar al jugador. */
    @EventHandler
    public void onLogin(LoginEvent event) {
        if (event.isCancelled()) {
            return;
        }
        UUID player = event.getConnection().getUniqueId();
        event.registerIntent(plugin);
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            try {
                service.load(player);
            } finally {
                event.completeIntent(plugin);
            }
        });
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        service.unload(event.getPlayer().getUniqueId());
    }
}
