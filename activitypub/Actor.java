package it.polito.activitypub;
import static it.polito.activitypub.ActivityPubUtilities.fullId;

import java.io.Serializable;

public class Actor implements Serializable{
    public final String username;
    public final String displayName;
    public final String fullId;

    Actor(String username, String displayName, String serverName) {
        this.username = username;
        this.displayName = displayName;
        this.fullId = fullId(username, serverName);
    }

    public String getFullId() {
        return fullId;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

}
