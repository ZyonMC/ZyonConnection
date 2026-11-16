package net.zyonmc.zyonconnection.group;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Indice servidor -> grupo. */
public final class GroupRegistry {

    private final Map<String, Group> byServer = new HashMap<>();

    public GroupRegistry(Collection<Group> groups) {
        for (Group group : groups) {
            for (String server : group.servers()) {
                Group previous = byServer.put(server, group);
                if (previous != null) {
                    throw new IllegalArgumentException("El servidor '" + server + "' esta en los grupos '"
                            + previous.name() + "' y '" + group.name() + "'");
                }
            }
        }
    }

    public Optional<Group> groupOf(String server) {
        return Optional.ofNullable(server).map(byServer::get);
    }
}
