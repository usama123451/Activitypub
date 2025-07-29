package it.polito.oop.test;

import it.polito.activitypub.*;
import static it.polito.activitypub.ActivityPubUtilities.fullId;
import org.junit.Before;
import org.junit.Test;
import java.util.Optional;
import static org.junit.Assert.*;

public class R1ActorManagementTest {
    private static final String MASTO_UNITO = "masto.unito.it";
    private static final String MASTO_POLITO = "masto.polito.it";
    private static final String BOB = "bob";
    private static final String ALICE = "alice";

    private Server polito;
    private Server unito;
    private Actor alice;

    @Before
    public void setup() throws ActivityPubException {
        NamingService namingService = ActivityPubUtilities.getNamingService();
        polito = ActivityPubUtilities.createServer(MASTO_POLITO);
        unito = ActivityPubUtilities.createServer(MASTO_UNITO);
        
        namingService.registerServer(MASTO_POLITO, polito);
        namingService.registerServer(MASTO_UNITO, unito);

        alice = polito.createActor(ALICE, "Alice Doe");
        polito.createActor(BOB, "Bob Smith");
    }

    @Test
    public void testServerNames() {
        assertEquals(MASTO_POLITO, polito.getName());
        assertEquals(MASTO_UNITO, unito.getName());
    }

    @Test
    public void testActorCreation() {
        assertNotNull(alice);
        assertEquals(ALICE, alice.getUsername());
        assertEquals(fullId(ALICE, MASTO_POLITO), alice.getFullId());
        assertEquals("Alice Doe", alice.getDisplayName());
    }

    @Test
    public void testDuplicateActor() {
        assertThrows(ActivityPubException.class, () -> 
        polito.createActor(ALICE, "Duplicate Alice")
    );
}

    @Test
    public void testActorRetrieval() {
        Optional<Actor> byUsername = polito.getActor(ALICE);
        assertTrue(byUsername.isPresent());
        assertEquals(alice, byUsername.get());

        Optional<Actor> byFullId = polito.getActor(fullId(ALICE, MASTO_POLITO));
        assertTrue(byFullId.isPresent());
        assertEquals(alice, byFullId.get());
    }

    @Test
    public void testListAllActors() {
        assertEquals(2, polito.listAllActors().size());
    }

    @Test
    public void testDeleteActor() {
        assertTrue(polito.deleteActor(ALICE));
        assertTrue(polito.getActor(ALICE).isEmpty());
    }

    @Test
    public void testDeleteActorNotExistent() {
        assertFalse(polito.deleteActor("**non=existent**"));
    }

}