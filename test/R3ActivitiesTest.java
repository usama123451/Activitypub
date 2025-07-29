package it.polito.oop.test;

import it.polito.activitypub.*;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.List;
import static org.junit.Assert.*;

public class R3ActivitiesTest {
    private static final String MASTO_UNITO = "masto.unito.it";
    private static final String MASTO_POLITO = "masto.polito.it";
    private static final String BOB = "bob";
    private static final String ALICE = "alice";

    private Server polito;
    private Server unito;
    private NamingService namingService;
    private Actor alice;
    private Actor bob;

    @Before
    public void setup() throws ActivityPubException {
        namingService = ActivityPubUtilities.getNamingService();
        polito = ActivityPubUtilities.createServer(MASTO_POLITO);
        unito = ActivityPubUtilities.createServer(MASTO_UNITO);
        
        namingService.registerServer(MASTO_POLITO, polito);
        namingService.registerServer(MASTO_UNITO, unito);

        alice = polito.createActor(ALICE, "Alice Doe");
        bob = polito.createActor(BOB, "Bob Smith");
    }

    @Test
    public void testCreateActivity() {
        Instant before = Instant.now();
        Activity post = polito.createActivity(bob.getFullId(), ActivityType.CREATE, "Hello world");
        Instant after = Instant.now();
        assertNotNull(post);
        assertEquals(bob.getFullId(), post.getActor());
        assertEquals("Hello world", post.getContent());
        assertEquals(ActivityType.CREATE, post.getType());
        assertTrue(before.isBefore(post.getTimestamp()) && after.isAfter(post.getTimestamp()));
    }

        @Test
    public void testActivityId() {
        Activity post1 = polito.createActivity(bob.getFullId(), ActivityType.CREATE, "Hello world");
        Activity post2 = polito.createActivity(bob.getFullId(), ActivityType.CREATE, "Hello world");
        assertNotNull(post1);
        assertNotNull(post2);
        assertNotEquals(post1.getId(), post2.getId());
    }

    @Test
    public void testActivityTypes() {
        Activity create = polito.createActivity(alice.getFullId(), ActivityType.CREATE, "New post");
        Activity announce = polito.createActivity(alice.getFullId(), ActivityType.ANNOUNCE, "Sharing");
        Activity like = polito.createActivity(alice.getFullId(), ActivityType.LIKE, "Liked post");

        List<Activity> activities = polito.getAllActivities().toList();
        assertEquals(3, activities.size());
        assertTrue(activities.contains(create));
        assertTrue(activities.contains(announce));
        assertTrue(activities.contains(like));
    }

    @Test
    public void testOutboxRetrieval() {
        Activity post = polito.createActivity(bob.getFullId(), ActivityType.CREATE, "Hello world");
        List<Activity> outbox = polito.getOutbox(bob.getFullId()).toList();
        assertEquals(1, outbox.size());
        assertEquals(post, outbox.get(0));
    }
}