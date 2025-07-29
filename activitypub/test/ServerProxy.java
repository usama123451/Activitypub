package it.polito.activitypub.test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import it.polito.activitypub.Activity;
import it.polito.activitypub.ActivityPubException;
import it.polito.activitypub.ActivityType;
import it.polito.activitypub.Actor;
import it.polito.activitypub.Server;

public class ServerProxy implements Server {

    private Server remote;
    
    ServerProxy(Server remote){
        this.remote = remote;
    }

    @Override
    public String getName() {
        return remote.getName();
    }

    @Override
    public Actor createActor(String username, String displayName) throws ActivityPubException {
        return remote.createActor(username, displayName);
    }

    @Override
    public Optional<Actor> getActor(String username) {
        return remote.getActor(username);
    }

    @Override
    public List<Actor> listAllActors() {
        return remote.listAllActors();
    }

    @Override
    public boolean deleteActor(String username) {
        return remote.deleteActor(username);
    }

    @Override
    public boolean follow(String followerId, String targetId) throws ActivityPubException {
        return remote.follow(followerId, targetId);
    }

    @Override
    public boolean addFollower(String followerId, String targetId) throws ActivityPubException {
        return remote.addFollower(followerId, targetId);
    }

    @Override
    public boolean unfollow(String followerId, String targetId) throws ActivityPubException {
        return remote.unfollow(followerId, targetId);
    }

    @Override
    public boolean removeFollower(String followerId, String targetId) throws ActivityPubException {
        return remote.removeFollower(followerId, targetId);
    }

    @Override
    public List<Actor> getFollowers(String fullId) {
        return remote.getFollowers(fullId);
    }

    @Override
    public List<Actor> getFollowing(String fullId) {
        return remote.getFollowers(fullId);
    }

    @Override
    public boolean isFollowing(String followerId, String targetId) {
        return remote.isFollowing(followerId, targetId);
    }

    @Override
    public Activity createActivity(String actorId, ActivityType type, String content) {
        return remote.createActivity(actorId, type, content);
    }

    @Override
    public Stream<Activity> getAllActivities() {
        return remote.getAllActivities();
    }

    @Override
    public Stream<Activity> getInbox(String actorId) {
        return remote.getInbox(actorId);
    }

    @Override
    public Stream<Activity> getOutbox(String actorId) {
        return remote.getOutbox(actorId);
    }

    @Override
    public void receiveActivity(Activity activity, String... targetId) {
        remote.receiveActivity(activity, targetId);
    }

}
