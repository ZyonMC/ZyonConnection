package net.zyonmc.zyonconnection;

import net.zyonmc.zyonconnection.config.Settings;
import net.zyonmc.zyonconnection.group.GroupRegistry;
import net.zyonmc.zyonconnection.listener.RedirectListener;
import net.zyonmc.zyonconnection.listener.SessionListener;
import net.zyonmc.zyonconnection.storage.LastServerRepository;
import net.zyonmc.zyonconnection.storage.LastServerService;
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
