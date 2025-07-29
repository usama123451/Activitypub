package it.polito.activitypub;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ActivityPubUtilities {

    private ActivityPubUtilities() {
    }

    private static NamingService service = null;

    /**
     * Retrieves the NamingService
     * 
     * @return the naming service singleton object
     */
    public static NamingService getNamingService() {
        if (service == null) {
            service = new InMemoryNamingService();
        }
        return service;
    }

    /**
     * Creates a new instance of a local server
     * @param name the name of the server
     * @return reference to the Server
     */
    public static Server createServer(String name) {
        Server server = new Server() {

            // R1 field
            private final Map<String, Actor> actors = new HashMap<>();
            // R2 fields
            private final Map<String, Set<String>> followersMap = new HashMap<>();
            private final Map<String, Set<String>> followingMap = new HashMap<>();
            private final List<Activity> allActivities = new ArrayList<>();
            private final Map<String, List<Activity>> outboxes = new HashMap<>();
            private final Map<String, List<Activity>> inboxes = new HashMap<>();

            private String parseIdToServerName(String fullId) {
                if (fullId.contains("@")) {
                    return fullId.substring(fullId.lastIndexOf('@') + 1);
                }
                return getName();
            }

            private String parseIdToUsername(String id) {
                if (id.contains("@")) {
                    String[] parts = id.substring(1).split("@");
                    if (parts.length > 0)
                        return parts[0];
                }
                return id;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public Actor createActor(String username, String displayName) throws ActivityPubException {
                if (actors.containsKey(username)) {
                    throw new ActivityPubException(
                            "Actor with username '" + username + "' already exists on this server.");
                }
                Actor newActor = new Actor(username, displayName, this.getName());
                actors.put(username, newActor);
                return newActor;
            }

            @Override
            public Optional<Actor> getActor(String id) {
                String user = parseIdToUsername(id);
                String server = parseIdToServerName(id);
                if (this.getName().equals(server)) {
                    return Optional.ofNullable(actors.get(user));
                }
                return Optional.empty();
            }

            @Override
            public List<Actor> listAllActors() {
                return new ArrayList<>(actors.values());
            }

            @Override
            public boolean deleteActor(String username) {
                return actors.remove(username) != null;
            }

            @Override
            public boolean follow(String followerId, String targetId) throws ActivityPubException {
                Actor follower = this.getActor(followerId).orElseThrow(() -> new ActivityPubException("Follower " + followerId + " not found on this server."));

                String targetServerName = parseIdToServerName(targetId);
                Server targetServer = ActivityPubUtilities.getNamingService().resolveServer(targetServerName).orElseThrow(() -> new ActivityPubException("Target server " + targetServerName + " not found."));

                Actor target = targetServer.getActor(targetId).orElseThrow(() -> new ActivityPubException("Target actor " + targetId + " not found."));

                if (targetServer.addFollower(follower.getFullId(), target.getFullId())) {
                    followingMap.computeIfAbsent(follower.getFullId(), k -> new HashSet<>()).add(target.getFullId());
                    return true;
                }
                return false;
            }

            @Override
                public boolean addFollower(String followerId, String targetId) throws ActivityPubException {
                 Actor target = this.getActor(targetId).orElseThrow(() -> new ActivityPubException("Target actor " + targetId + " does not exist on this server."));
                 return followersMap.computeIfAbsent(target.getFullId(), k -> new HashSet<>()).add(followerId);
             }

            @Override
            public boolean unfollow(String followerId, String targetId) throws ActivityPubException {
                Actor follower = this.getActor(followerId).orElseThrow(() -> new ActivityPubException("Follower " + followerId + " not found on this server."));

                String targetServerName = parseIdToServerName(targetId);
                Server targetServer = ActivityPubUtilities.getNamingService().resolveServer(targetServerName).orElseThrow(() -> new ActivityPubException("Target server " + targetServerName + " not found."));

                Actor target = targetServer.getActor(targetId).orElseThrow(() -> new ActivityPubException("Target actor " + targetId + " not found."));

                boolean removedFromTarget = targetServer.removeFollower(follower.getFullId(), target.getFullId());

                boolean removedFromFollower = false;
                Set<String> following = followingMap.get(follower.getFullId());
                if (following != null) {
                    removedFromFollower = following.remove(target.getFullId());
                }

                return removedFromTarget || removedFromFollower;
            }

            @Override
                public boolean removeFollower(String followerId, String targetId) throws ActivityPubException {
                Actor target = this.getActor(targetId).orElseThrow(() -> new ActivityPubException("Target actor " + targetId + " does not exist on this server."));
                Set<String> followers = followersMap.get(target.getFullId());
                if (followers != null) {
                    return followers.remove(followerId);
                }
                return false;
            }

            @Override
            public List<Actor> getFollowers(String id) {
                Optional<Actor> actorOpt = this.getActor(id);
                if (actorOpt.isEmpty()) return Collections.emptyList();
                
                Set<String> followerIds = followersMap.getOrDefault(actorOpt.get().getFullId(), Collections.emptySet());
                return followerIds.stream()
                    .map(follower -> {
                        String serverName = parseIdToServerName(follower);
                        return ActivityPubUtilities.getNamingService().resolveServer(serverName)
                                .flatMap(s -> s.getActor(follower));
                    })
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            }

            @Override
            public List<Actor> getFollowing(String id) {
                Optional<Actor> actorOpt = this.getActor(id);
                if (actorOpt.isEmpty()) return Collections.emptyList();

                Set<String> followingIds = followingMap.getOrDefault(actorOpt.get().getFullId(), Collections.emptySet());
                return followingIds.stream()
                    .map(following -> {
                        String serverName = parseIdToServerName(following);
                        return ActivityPubUtilities.getNamingService().resolveServer(serverName)
                                .flatMap(s -> s.getActor(following));
                    })
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            }

            @Override
            public boolean isFollowing(String followerId, String targetId) {
                Optional<Actor> followerOpt = this.getActor(followerId);
                if (followerOpt.isEmpty()) {
                    return false;
                }

                String targetServerName = parseIdToServerName(targetId);
                Optional<Server> targetServerOpt = ActivityPubUtilities.getNamingService().resolveServer(targetServerName);
                if (targetServerOpt.isEmpty()) {
                    return false; 
                }
                Optional<Actor> targetOpt = targetServerOpt.get().getActor(targetId);
                if (targetOpt.isEmpty()) {
                    return false; 
                }

                Set<String> following = followingMap.get(followerOpt.get().getFullId());
                return following != null && following.contains(targetOpt.get().getFullId());
            }

            @Override
            public Activity createActivity(String actorId, ActivityType type, String content) {
                Actor actor = this.getActor(actorId).orElseThrow(() -> new IllegalArgumentException("Actor " + actorId + " not found on this server."));
                
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                Activity newActivity = new Activity(type, actor.getFullId(), content);
                allActivities.add(newActivity);
                outboxes.computeIfAbsent(actor.getFullId(), k -> new ArrayList<>()).add(newActivity);

                List<Actor> followers = this.getFollowers(actor.getFullId());

                Map<String, List<String>> followersByServer = followers.stream()
                    .collect(Collectors.groupingBy(
                        followerActor -> parseIdToServerName(followerActor.getFullId()),
                        Collectors.mapping(Actor::getFullId, Collectors.toList())
                    ));

                for (Map.Entry<String, List<String>> entry : followersByServer.entrySet()) {
                    String serverName = entry.getKey();
                    List<String> recipientIds = entry.getValue();
                    
                    getNamingService().resolveServer(serverName).ifPresent(recipientServer -> {
                        recipientServer.receiveActivity(newActivity, recipientIds.toArray(new String[0]));
                    });
                }
                return newActivity;
            }

            @Override
            public Stream<Activity> getAllActivities() {
                return allActivities.stream();
            }

            @Override
            public Stream<Activity> getOutbox(String actorId) {
                Optional<Actor> actorOpt = this.getActor(actorId);
                if (actorOpt.isEmpty()) return Stream.empty();
                
                List<Activity> outbox = outboxes.getOrDefault(actorOpt.get().getFullId(), Collections.emptyList());
                return outbox.stream();
            }
            @Override
            public Stream<Activity> getInbox(String actorId) {
                Optional<Actor> actorOpt = this.getActor(actorId);
                if (actorOpt.isEmpty()) return Stream.empty();

                List<Activity> inbox = inboxes.getOrDefault(actorOpt.get().getFullId(), Collections.emptyList());

                List<Activity> reversedInbox = new ArrayList<>(inbox);
                Collections.reverse(reversedInbox);
                return reversedInbox.stream();
            }

            @Override
            public void receiveActivity(Activity activity, String... targetIds) {
                for (String id : targetIds) {
                    
                    if (this.getActor(id).isPresent()) {
                        inboxes.computeIfAbsent(id, k -> new ArrayList<>()).add(activity);
                    }
                }
            }
        };

        getNamingService().registerServer(name, server);
        return server;
    }

    /**
     * converts a local username and the server name into a full actor ID
     * 
     * @param username   the local user name
     * @param serverName the server name
     * @return the full ID
     */
    public static String fullId(String username, String serverName) {
        return "@%s@%s".formatted(username, serverName);
    }

}