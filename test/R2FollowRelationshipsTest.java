package it.polito.oop.test;

import it.polito.activitypub.*;
import static it.polito.activitypub.ActivityPubUtilities.fullId;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class R2FollowRelationshipsTest {
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
    public void testFollowSameServer() throws ActivityPubException {
        assertEquals(0, polito.getFollowers(bob.getFullId()).size());
        assertEquals(0, polito.getFollowing(alice.getFullId()).size());

        assertTrue(polito.follow(alice.getFullId(), bob.getFullId()));

        assertTrue(polito.isFollowing(alice.getFullId(), bob.getFullId()));
        assertEquals(1, polito.getFollowers(bob.getFullId()).size());
        assertEquals(1, polito.getFollowing(alice.getFullId()).size());
    }

    @Test
    public void testFollowSameServerTwice() throws ActivityPubException {
        assertTrue(polito.follow(alice.getFullId(), bob.getFullId()));
        assertFalse(polito.follow(alice.getFullId(), bob.getFullId()));
    }

    @Test
    public void testFollowNonExistent() {
        assertThrows(ActivityPubException.class,
                () -> polito.follow(fullId("**NON-EXISTENT**", polito.getName()), bob.getFullId()));
    }

    @Test
    public void testFollowNonExistent2() {
        assertThrows(ActivityPubException.class,
                () -> polito.follow(bob.getFullId(), fullId("**NON-EXISTENT**", polito.getName())));
    }

    @Test
    public void testIsFollowingNonExistent() {
        assertFalse(polito.isFollowing(alice.getFullId(), fullId("**NON-EXISTENT**", polito.getName())));
        assertFalse(polito.isFollowing(fullId("**NON-EXISTENT**", polito.getName()), alice.getFullId()));
    }


    @Test
    public void testUnfollow() throws ActivityPubException {
        polito.follow(alice.getFullId(), bob.getFullId());
        assertTrue(polito.unfollow(alice.getFullId(), bob.getFullId()));
        assertFalse(polito.isFollowing(alice.getFullId(), bob.getFullId()));
    }

    @Test
    public void testFollowNonexistentActor() {
        assertThrows(ActivityPubException.class, () ->
                polito.follow(alice.getFullId(), "@nonexistent@" + MASTO_UNITO)
        );
    }

    @Test
    public void testUnfollowNonexistent() throws ActivityPubException {
        polito.follow(alice.getFullId(), bob.getFullId());

        assertThrows(ActivityPubException.class,
                     ()->polito.unfollow(alice.getFullId(), "@nonexistent@" + MASTO_POLITO));
    }

    @Test
    public void testFollowNonLocalActor() {
        assertThrows(ActivityPubException.class, () ->
                polito.unfollow(carla.getFullId(), alice.fullId)
        );
    }
}