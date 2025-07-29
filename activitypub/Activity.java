package it.polito.activitypub;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class Activity  implements Serializable {
    public final String id;
    public final ActivityType type;
    public final String actor;
    public final String content;
    public final Instant timestamp;

    public Activity(ActivityType type, String actor, String content) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.actor = actor;
        this.content = content;
        this.timestamp = Instant.now();
    }

    public String getId() {
        return id;
    }

    public ActivityType getType() {
        return type;
    }

    public String getActor() {
        return actor;
    }

    public String getContent() {
        return content;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
