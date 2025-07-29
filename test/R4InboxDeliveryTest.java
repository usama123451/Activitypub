package it.polito.oop.test;

import it.polito.activitypub.*;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class R4InboxDeliveryTest {
    private static final String MASTO_UNITO = "masto.unito.it";
    private static final String MASTO_POLITO = "masto.polito.it";
    private static final String BOB = "bob";
    private static final String ALICE = "alice";
    private static final String CARLA = "carla";

    private Server polito;
    private Server unito;
    private Actor alice;
    private Actor bob;
    private Actor carla;

    @Before
    public void setup() throws ActivityPubException {
        NamingService namingService = ActivityPubUtilities.getNamingService();
        polito = ActivityPubUtilities.createServer(MASTO_POLITO);
        unito = ActivityPubUtilities.createServer(MASTO_UNITO);
        
        namingService.registerServer(MASTO_POLITO, polito);
        namingService.registerServer(MASTO_UNITO, unito);

        alice = polito.createActor(ALICE, "Alice Doe");
        bob = polito.createActor(BOB, "Bob Smith");
        carla = unito.createActor(CARLA, "Carla Bianchi");
    }

    @Test
    public void testInboxDelivery() throws ActivityPubException {
        polito.follow(alice.getFullId(), bob.getFullId());
        Activity activity = polito.createActivity(bob.getFullId(), ActivityType.CREATE, "Hi from Bob");
        
        List<Activity> inbox = polito.getInbox(alice.getFullId()).toList();
        assertEquals(1, inbox.size());
        assertEquals(activity, inbox.get(0));
    }

    @Test
    public void testCrossServerDelivery() throws ActivityPubException {
        polito.follow(alice.getFullId(), carla.getFullId());
        Activity activity = unito.createActivity(carla.getFullId(), ActivityType.CREATE, "Hi from Carla");
        
        List<Activity> inbox = polito.getInbox(alice.getFullId()).toList();
        assertEquals(1, inbox.size());
        assertEquals(activity, inbox.get(0));
    }

    @Test
    public void testMultipleFollowersDelivery() throws ActivityPubException {
        polito.follow(alice.getFullId(), carla.getFullId());
        polito.follow(bob.getFullId(), carla.getFullId());
        
        unito.createActivity(carla.getFullId(), ActivityType.CREATE, "Hi everyone");
        
        assertEquals(1, polito.getInbox(alice.getFullId()).count());
        assertEquals(1, polito.getInbox(bob.getFullId()).count());
    }
}