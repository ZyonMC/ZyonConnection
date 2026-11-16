package net.zyonmc.zyonconnection.group;

import java.util.List;

/** Una modalidad: conjunto de servidores con un servidor de entrada ({@code main}). */
public record Group(String name, String main, List<String> servers) {

    public Group {
        if (!servers.contains(main)) {
            throw new IllegalArgumentException("El main '" + main + "' no esta en los servidores del grupo '" + name + "'");
        }
        servers = List.copyOf(servers);
    }
}
