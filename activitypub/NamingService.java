package it.polito.activitypub;

import java.util.Optional;

public interface NamingService {
    /**
     * Register a server with this naming service instance
     * 
     * @param name name of the server
     * @param server the object implementing the Server interface
     */
    void registerServer(String name, Server server);

    /**
     * Retrieves a previously registered server given its name.
     * If the name has not been registered, it returns an empty optional.
     * 
     * @param name the name of the server
     * @return an optional Server object
     */
    Optional<Server> resolveServer(String name);
}
