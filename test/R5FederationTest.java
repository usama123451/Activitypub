package it.polito.oop.test;

import it.polito.activitypub.*;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class R5FederationTest {
    private static final String MASTO_UNITO = "masto.unito.it";
    private static final String MASTO_POLITO = "masto.polito.it";
    private static final String BOB = "bob";
    private static final String ALICE = "alice";
    private static final String CARLA = "carla";

    private Server polito;
    private Server unito;
    private NamingService namingService;
    private Actor alice;
    private Actor bob;
    private Actor carla;

    @Before
    public void setup() throws ActivityPubException {
        namingService = ActivityPubUtilities.getNamingService();
        polito = ActivityPubUtilities.createServer(MASTO_POLITO);
        unito = ActivityPubUtilities.createServer(MASTO_UNITO);
        
        namingService.registerServer(MASTO_POLITO, polito);
        namingService.registerServer(MASTO_UNITO, unito);

        alice = polito.createActor(ALICE, "Alice Doe");
        bob = polito.createActor(BOB, "Bob Smith");
        carla = unito.createActor(CARLA, "Carla Bianchi");
    }

    @Test
    public void testServerRegistration() {
        Optional<Server> resolvedPolito = namingService.resolveServer(MASTO_POLITO);
        Optional<Server> resolvedUnito = namingService.resolveServer(MASTO_UNITO);

        assertTrue(resolvedPolito.isPresent());
        assertTrue(resolvedUnito.isPresent());
        
        assertEquals(polito, resolvedPolito.get());
        assertEquals(unito, resolvedUnito.get());
    }

    @Test
    public void testFollowNonLocal() {
        assertThrows(ActivityPubException.class,
                () -> polito.follow(carla.getFullId(), bob.getFullId()));
    }


    @Test
    public void testFollowCrossServer() throws ActivityPubException {
        assertTrue(polito.follow(alice.getFullId(), carla.getFullId()));
        assertTrue(polito.isFollowing(alice.getFullId(), carla.getFullId()));
        assertTrue(unito.getFollowers(carla.getFullId()).contains(alice));
    }

    @Test
    public void testGetFollowing() throws ActivityPubException {
        polito.follow(alice.getFullId(), bob.getFullId());
        polito.follow(alice.getFullId(), carla.getFullId());

        List<Actor> following = polito.getFollowing(alice.getFullId());
        assertEquals(2, following.size());
        assertTrue(following.contains(bob));
        assertTrue(following.contains(carla));
    }

    @Test
    public void testBatchedDelivery() throws ActivityPubException {
        polito.follow(alice.getFullId(), carla.getFullId());
        polito.follow(bob.getFullId(), carla.getFullId());
        
        unito.createActivity(carla.getFullId(), ActivityType.CREATE, "Hi from Carla");
        
        assertEquals(1, polito.getInbox(alice.getFullId()).count());
        assertEquals(1, polito.getInbox(bob.getFullId()).count());
    }


    @Test
    public void testDeleteActorCleanUp() throws ActivityPubException {
        polito.follow(alice.getFullId(), carla.getFullId());
        polito.follow(bob.getFullId(), alice.getFullId());

        assertEquals(1, unito.getFollowers(carla.getFullId()).size());
        assertEquals(1, polito.getFollowing(bob.getFullId()).size());
        assertEquals(1, polito.getFollowers(alice.getFullId()).size());

        assertTrue(polito.deleteActor(ALICE));
        assertTrue(polito.getActor(ALICE).isEmpty());

        assertFalse(polito.isFollowing(alice.getFullId(), carla.getFullId()));
        assertEquals(0, unito.getFollowers(carla.getFullId()).size());
        assertEquals(0, polito.getFollowing(bob.getFullId()).size());
    }


}