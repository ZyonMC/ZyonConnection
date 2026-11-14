package dev.zyon.connection.group;

import java.util.Map;
import java.util.Optional;

/**
 * Decide si una conexion debe desviarse al ultimo servidor recordado.
 * Logica pura (sin API de BungeeCord) para poder probarla.
 */
public final class RedirectPolicy {

    private final GroupRegistry groups;

    public RedirectPolicy(GroupRegistry groups) {
        this.groups = groups;
    }

    /**
     * @param currentServer servidor actual del jugador, {@code null} si se esta uniendo al proxy
     * @param requestedServer servidor al que intenta conectarse
     * @param lastByGroup ultimo servidor del jugador por nombre de grupo
     * @return servidor alternativo, o vacio si se respeta el destino pedido
     */
    public Optional<String> redirectFor(String currentServer, String requestedServer, Map<String, String> lastByGroup) {
        Optional<Group> target = groups.groupOf(requestedServer);
        if (target.isEmpty()) {
            return Optional.empty();
        }
        Optional<Group> origin = groups.groupOf(currentServer);
        if (origin.equals(target)) {
            return Optional.empty(); // movimiento interno (portales): no se toca
        }
        return Optional.ofNullable(lastByGroup.get(target.get().name()))
                .filter(last -> !last.equals(requestedServer))
                .filter(last -> target.get().servers().contains(last));
    }
}
