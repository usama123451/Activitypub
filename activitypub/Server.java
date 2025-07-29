package it.polito.activitypub;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface Server {
    /**
     * Retrieves the server name.
     * 
     * @return the name of this server
     */
    String getName();

    /**
     * Creates a new actor on this server with the specified username and display name.
     * The full identifier of the actor will be generated in the format @username@serverName.
     *
     * @param username the local username for the actor
     * @param displayName the display name for the actor
     * @return the created Actor instance
     * @throws ActivityPubException if the username is already taken or invalid
     */
    Actor createActor(String username, String displayName) throws ActivityPubException;

    /**
     * Retrieves an actor by their ID/username or full ID.
     * The username can be either local ID/username or full ID in the format @username@serverName.
     *
     * @param username the local username or full ID of the actor
     * @return an Optional containing the Actor if found, or empty if not found
     */
    Optional<Actor> getActor(String username);

    /**
     * Lists all actors registered on this server.
     *
     * @return a list of all actors registered on this server
     */
    List<Actor> listAllActors();

    /**
     * Deletes an actor from this server.
     *
     * @param username the local username of the actor to delete
     * @return true if the actor was successfully deleted, false if the actor didn't exist
     */
    boolean deleteActor(String username);

    /**
     * Establishes a follow relationship from a follower to a target actor.
     * The follower must be local to this server.
     * This method must invoke the {@link #addFollower(String, String)} on the target actor's server.
     *
     * @param followerId the ID of the following actor (must be local to this server)
     * @param targetId the ID of the actor to be followed
     * @return true if the follow relationship was established successfully
     * @throws ActivityPubException if either actor doesn't exist or if the follower is not local
     */
    boolean follow(String followerId, String targetId) throws ActivityPubException;

    /**
     * Adds a follower to a local actor's followers' list.
     * The target actor must be local to this server.
     * This method is typically invoked by the {@link #follow(String, String)} method of the follower server.
     *
     * @param followerId the ID of the following actor
     * @param targetId the ID of the target actor (must be local to this server)
     * @return true if the follower was added successfully
     * @throws ActivityPubException if either actor doesn't exist or if the target is not local
     */
    boolean addFollower(String followerId, String targetId) throws ActivityPubException;

    /**
     * Removes a follow relationship from a follower to a target actor.
     * The follower must be local to this server.
     * This method must invoke {@link #removeFollower(String, String)} on the target actor's server.
     *
     * @param followerId the ID of the following actor (must be local to this server)
     * @param targetId the ID of the actor to be unfollowed
     * @return true if the follow relationship was removed successfully
     * @throws ActivityPubException if either actor doesn't exist or if the follower is not local
     */
    boolean unfollow(String followerId, String targetId) throws ActivityPubException;

    /**
     * Removes a follower from a local actor's followers' list.
     * The target actor must be local to this server.
     * This method is typically invoked by the {@link #unfollow(String, String)} method of the follower server.
     *
     * @param followerId the ID of the following actor
     * @param targetId the ID of the target actor (must be local to this server)
     * @return true if the follower was removed successfully
     * @throws ActivityPubException if either actor doesn't exist or if the target is not local
     */
    boolean removeFollower(String followerId, String targetId) throws ActivityPubException;

    /**
     * Gets all actors who follow the specified actor.
     *
     * @param fullId the ID of the actor
     * @return a list of actors who follow the specified actor
     */
    List<Actor> getFollowers(String fullId);

    /**
     * Gets all actors that the specified actor follows.
     *
     * @param fullId the ID of the actor
     * @return a list of actors followed by the specified actor
     */
    List<Actor> getFollowing(String fullId);

    /**
     * Checks if a follow relationship exists between two actors.
     *
     * @param followerId the ID of the potential follower
     * @param targetId the ID of the potential target
     * @return true if followerId follows targetId, false otherwise
     */
    boolean isFollowing(String followerId, String targetId);

    /**
     * Creates a new activity for an actor and delivers it to all their followers.
     * The activity is stored in the actor's outbox and automatically delivered to followers' inboxes.
     *
     * @param actorId the ID of the actor creating the activity
     * @param type the type of activity (CREATE, ANNOUNCE, LIKE, FOLLOW, UNFOLLOW)
     * @param content the textual content of the activity
     * @return the created Activity instance
     */
    Activity createActivity(String actorId, ActivityType type, String content);

    /**
     * Gets all activities on this server.
     *
     * @return a stream of all activities stored on this server
     */
    Stream<Activity> getAllActivities();

    /**
     * Gets the inbox stream for a specific actor.
     *
     * @param actorId the ID of the actor
     * @return a stream of activities in the actor's inbox
     */
    Stream<Activity> getInbox(String actorId);

    /**
     * Gets the outbox stream for a specific actor.
     *
     * @param actorId the ID of the actor
     * @return a stream of activities in the actor's outbox
     */
    Stream<Activity> getOutbox(String actorId);

    /**
     * Receives an activity and stores it in the specified actor's inbox.
     * This method is called by other servers {@link #createActivity} method to deliver activities to followers.
     *
     * @param targetId the IDs of the recipient actors
     * @param activity the activity to be delivered to the inbox
     */
    void receiveActivity(Activity activity, String... targetId);
}