package it.polito.activitypub.test;

import it.polito.activitypub.Activity;
import it.polito.activitypub.ActivityPubException;
import it.polito.activitypub.ActivityPubUtilities;
import it.polito.activitypub.ActivityType;
import it.polito.activitypub.Actor;
import it.polito.activitypub.NamingService;
import it.polito.activitypub.Server;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;



public class ActivityPubTest {

    private static final String MASTO_UNITO = "masto.unito.it";
    private static final String MASTO_POLITO = "masto.polito.it";
    private static final String BOB = "bob";
    private static final String ALICE = "alice";
    private static final String CARLA = "carla";

    private Server polito;
    private NamingService namingService;
    private Actor alice;
    private Actor bob;

    private String fullId(String username, String server){
        return "@" + username + "@" + server;
    }

    @Before
    public void setup() throws ActivityPubException {
        namingService = ActivityPubUtilities.getNamingService();
        polito = ActivityPubUtilities.createServer(MASTO_POLITO);
        
        namingService.registerServer(MASTO_POLITO, polito);

        alice = polito.createActor(ALICE, "Alice Doe");
        bob = polito.createActor(BOB, "Bob Smith");
    }

    @Test
    public void testActorManagement() {
        Optional<Actor> retrieved = polito.getActor(ALICE);

        assertTrue(retrieved.isPresent());
        assertEquals(ALICE, retrieved.get().getUsername());
        assertEquals(2, polito.listAllActors().size());

        assertNotNull(alice);
        assertEquals(alice, retrieved.get());
        String fullId = alice.getFullId();
        assertNotNull(fullId);
        assertEquals(fullId(ALICE,MASTO_POLITO), fullId);

        assertTrue(polito.deleteActor(ALICE));
        assertTrue(polito.getActor(ALICE).isEmpty());
    }

    @Test
    public void testFollowUnfollow() throws ActivityPubException {
        boolean success = polito.follow(ALICE, BOB);
        assertTrue(success);
        assertTrue(polito.isFollowing(ALICE, BOB));
        assertEquals(1, polito.getFollowers(BOB).size());

        polito.unfollow(ALICE, BOB);
        assertFalse(polito.isFollowing(ALICE, BOB));
    }

    @Test
    public void testDoubleUnfollow() throws ActivityPubException {
        polito.follow(alice.getFullId(), bob.getFullId());
        assertTrue(polito.unfollow(ALICE, BOB));
        assertFalse(polito.unfollow(ALICE, BOB));
    }

    @Test
    public void testActivityCreationAndDelivery() {
        Activity post = polito.createActivity(BOB, ActivityType.CREATE, "Hello world");

        assertNotNull(post);
        List<Activity> activities = polito
                                    .getOutbox(bob.getFullId())
                                    .toList();
        assertEquals(1, activities.size());
        assertEquals("Hello world", activities.get(0).getContent());
    }

    @Test
    public void testInboxOutbox() throws ActivityPubException {
        polito.follow(ALICE, BOB);

        Activity activity = polito.createActivity(BOB, ActivityType.CREATE, "Hi from Bob");

        List<Activity> inbox = polito.getInbox(ALICE).toList();
        List<Activity> outbox = polito.getOutbox(BOB).toList();

        assertEquals(1, inbox.size());
        assertTrue(outbox.contains(activity));
        assertEquals("Hi from Bob", inbox.get(0).getContent());
        assertEquals(1, outbox.size());
    }

    @Test
    public void testFederation() throws ActivityPubException {
        Server unito = new ServerProxy(ActivityPubUtilities.createServer(MASTO_UNITO));
        namingService.registerServer(MASTO_UNITO, unito);

        Actor carla = unito.createActor(CARLA, "Carla Bianchi");

        polito.follow(alice.getFullId(), carla.getFullId());
        polito.follow(bob.getFullId(), carla.getFullId());

        unito.createActivity(carla.getFullId(), ActivityType.CREATE, "Hi from Carla");

        List<Activity> outbox = unito.getOutbox(carla.getFullId()).toList();
        List<Activity> inbox = polito.getInbox(alice.getFullId()).toList();
        List<Activity> inboxb = polito.getInbox(bob.getFullId()).toList();

        assertEquals(1, outbox.size());
        assertEquals(1, inbox.size());
        assertEquals("Hi from Carla", inbox.get(0).getContent());
        assertEquals(1, inboxb.size());
    }
}
