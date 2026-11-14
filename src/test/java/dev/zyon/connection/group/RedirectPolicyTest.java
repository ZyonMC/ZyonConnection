package dev.zyon.connection.group;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RedirectPolicyTest {

    private final RedirectPolicy policy = new RedirectPolicy(new GroupRegistry(List.of(
            new Group("survival", "spawn", List.of("spawn", "world", "nether", "end")),
            new Group("skyblock", "sb1", List.of("sb1", "sb2")))));

    private final Map<String, String> last = Map.of("survival", "nether", "skyblock", "sb2");

    @Test
    void lobbyToGroupMainGoesToLastServer() {
        assertEquals(Optional.of("nether"), policy.redirectFor("lobby", "spawn", last));
    }

    @Test
    void joiningProxyGoesToLastServer() {
        assertEquals(Optional.of("nether"), policy.redirectFor(null, "spawn", last));
    }

    @Test
    void otherGroupGoesToItsOwnLastServer() {
        assertEquals(Optional.of("sb2"), policy.redirectFor("spawn", "sb1", last));
    }

    @Test
    void movesInsideTheSameGroupAreUntouched() {
        assertEquals(Optional.empty(), policy.redirectFor("world", "end", last));
    }

    @Test
    void noRememberedServerKeepsRequestedTarget() {
        assertEquals(Optional.empty(), policy.redirectFor("lobby", "spawn", Map.of()));
    }

    @Test
    void requestedEqualsLastIsNotRedirected() {
        assertEquals(Optional.empty(), policy.redirectFor("lobby", "nether", last));
    }

    @Test
    void serversOutsideGroupsAreUntouched() {
        assertEquals(Optional.empty(), policy.redirectFor("spawn", "lobby", last));
    }

    @Test
    void staleLastServerFromAnotherGroupIsIgnored() {
        assertEquals(Optional.empty(), policy.redirectFor("lobby", "spawn", Map.of("survival", "sb1")));
    }

    @Test
    void serverInTwoGroupsIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new GroupRegistry(List.of(
                new Group("a", "x", List.of("x")), new Group("b", "x", List.of("x")))));
    }
}
