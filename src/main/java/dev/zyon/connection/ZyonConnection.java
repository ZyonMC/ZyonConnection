package dev.zyon.connection;

import dev.zyon.connection.config.Settings;
import dev.zyon.connection.group.GroupRegistry;
import dev.zyon.connection.listener.RedirectListener;
import dev.zyon.connection.listener.SessionListener;
import dev.zyon.connection.storage.LastServerRepository;
import dev.zyon.connection.storage.LastServerService;
import net.md_5.bungee.api.plugin.Plugin;

/** Punto de entrada: carga la configuracion y conecta las piezas. */
public final class ZyonConnection extends Plugin {

    @Override
    public void onEnable() {
        try {
            Settings settings = Settings.load(getDataFolder(), getClass().getClassLoader());
            LastServerRepository repository = new LastServerRepository(settings.database());
            repository.init();

            GroupRegistry groups = new GroupRegistry(settings.groups());
            LastServerService service = new LastServerService(this, repository);

            getProxy().getPluginManager().registerListener(this, new SessionListener(this, service));
            getProxy().getPluginManager().registerListener(this, new RedirectListener(getProxy(), groups, service));
        } catch (Exception e) {
            getLogger().severe("ZyonConnection desactivado: " + e);
        }
    }
}
