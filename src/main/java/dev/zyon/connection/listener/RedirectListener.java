package dev.zyon.connection.listener;

import dev.zyon.connection.group.GroupRegistry;
import dev.zyon.connection.group.RedirectPolicy;
import dev.zyon.connection.storage.LastServerService;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Desvia al ultimo servidor del grupo, con respaldo al destino original, y registra los cambios. */
public final class RedirectListener implements Listener {

    private final ProxyServer proxy;
    private final GroupRegistry groups;
    private final RedirectPolicy policy;
    private final LastServerService service;
    /** Destino original de los jugadores desviados, por si el servidor recordado esta caido. */
    private final Map<UUID, ServerInfo> fallbacks = new ConcurrentHashMap<>();

    public RedirectListener(ProxyServer proxy, GroupRegistry groups, LastServerService service) {
        this.proxy = proxy;
        this.groups = groups;
        this.policy = new RedirectPolicy(groups);
        this.service = service;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onConnect(ServerConnectEvent event) {
        if (event.isCancelled()) {
            return;
        }
        ProxiedPlayer player = event.getPlayer();
        String current = player.getServer() == null ? null : player.getServer().getInfo().getName();
        ServerInfo requested = event.getTarget();

        policy.redirectFor(current, requested.getName(), service.lastServers(player.getUniqueId()))
                .map(proxy::getServerInfo)
                .ifPresent(last -> {
                    fallbacks.put(player.getUniqueId(), requested);
                    event.setTarget(last);
                });
    }

    @EventHandler
    public void onKick(ServerKickEvent event) {
        ServerInfo fallback = fallbacks.remove(event.getPlayer().getUniqueId());
        if (fallback != null && event.getState() == ServerKickEvent.State.CONNECTING) {
            event.setCancelled(true);
            event.setCancelServer(fallback);
        }
    }

    @EventHandler
    public void onSwitch(ServerSwitchEvent event) {
        ProxiedPlayer player = event.getPlayer();
        fallbacks.remove(player.getUniqueId());
        if (player.getServer() == null) {
            return;
        }
        String server = player.getServer().getInfo().getName();
        groups.groupOf(server).ifPresent(group -> service.remember(player.getUniqueId(), group.name(), server));
    }
}
