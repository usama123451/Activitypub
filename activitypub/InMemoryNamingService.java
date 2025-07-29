package it.polito.activitypub;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryNamingService implements NamingService {
    private final Map<String, Server> servers = new HashMap<>();

    @Override
    public void registerServer(String name, Server server) {
        servers.put(name, server);
    }

    @Override
    public Optional<Server> resolveServer(String name) {
        return Optional.ofNullable(servers.get(name));
    }
}
