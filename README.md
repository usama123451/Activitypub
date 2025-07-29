# Activitypub
Write an application to simulate a federated social network using the ActivityPub protocol.
The application must allow the definition of actors hosted on different servers, support inter-server subscriptions (follows), and delivery of activities to followers' inboxes.

## R1 – Actor Management

Actors represent users or services in the system. Each `Server` object is responsible for managing its own set of actors.

Servers are created through the method `createServer()` of class `ActivityPubUtilities`.
Once a server is created it must be registered with the method `registerServer()` of the naming service.
The server returns its name via `getName()`.

To define a new actor, the method `createActor()` of class `Server` is used, accepting the actor's local `username` and `displayName`. The full identifier of an actor is generated following the format `@username@serverName`.
This method throws an `ActivityPubException` if the actor already exists on the server.

Actors can be retrieved through the method `getActor()` providing either the local `username` or the full ID. This method returns an `Optional<Actor>`, allowing for safe handling of non-existent actors.
In addition, actors can be listed through the method `listAllActors()`, which returns all actors registered on a given server.

An actor can be deleted using the method `deleteActor()`, which removes the actor from the server's registry.

Actors are represented by objects of class `Actor`, which provide accessors to retrieve the username, display name, and full ID (in the format `@username@serverName`).


## R2 – Follow and Unfollow Relationships

Actors can follow or unfollow other actors, even across different servers.

The method `follow()` of class `Server` establishes a subscription from a local actor (follower) to a target actor, both identified by their IDs or full IDs. 
The method `unfollow()` removes a previously established subscription. Both methods return boolean values to indicate success/failure.
The two methods require existing actors, otherwise an `ActivityPubException` is thrown. 

To retrieve follower information, the method `getFollowers()` returns the list of actors who are following the specified actor. Conversely, `getFollowing()` returns the actors followed by a given user. The method `isFollowing()` checks whether a specific follow relationship exists.

Follow relationships are stored on both the source and destination servers, and inter-server resolution is handled via the `NamingService`.


## R3 – Activities and Outboxes

Actors can publish activities such as posts or likes, which are modeled by the `Activity` class ([already provided](src/it/polito/activitypub/Activity.java)) that has the following fields:

- `String id` 
- `ActivityType type` 
- `String actor`
- `String content`
- `Instant timestamp` 

To create an activity, the method `createActivity()` of class `Server` is used. It accepts the actor's full ID, an `ActivityType` (enum: `CREATE`, `ANNOUNCE`, `LIKE`), and the textual content of the activity.

Each activity has a unique identifier (UUID) and a timestamp. Created activities are stored in the actor's outbox.

Outbox contents can be retrieved using method `getOutbox()`, returning a `Stream<Activity>` to support filtering and pagination. The method `getAllActivities()` returns all activities on the server.


## R4 – Inbox Management and Delivery

When an activity is created, it is automatically delivered to the inbox of all followers, even if they reside on other servers.

The delivery is handled by the method `receiveActivity()` on the follower's server to store the activity in their inbox.
The method receives the activity and the full IDs of the followers. It checks if the follower exists on the server and then adds the activity to their inbox.
The method must be invoked by the `createActivity()`.

Inbox contents can be accessed using `getInbox()`, which returns a `Stream<Activity>` for that actor. 
The stream returns the most recent activities first, allowing for easy retrieval of the latest activities.


## R5 – Federation and Naming Service

The interface `NamingService` is responsible for coordinating communication between servers.
The singleton instance of naming service can be retrieved with method `getNamingService()` of class `ActivityPubUtilities`.

To work in a federation, servers must be registered using the method `registerServer()`, providing a server name and a reference to the `Server` object. 
The method `resolveServer()` returns the `Server` object given its name.

A simple implementation of `NamingService` is provided as class `InMemoryNamingService` that manages local in memory servers and can be used for testing purposes.

In general, the `Service` object returned can be a remote server, only the methods defined in the `Server` interface can be used to interact with it. 

The methods described in the requirement above, in addition to accepting the ID/username, should be able to accept the full ID and interact with other servers in the federation if required.

In particular, the following relationships are stored on both the source and destination servers. When an actor follows another actor:
1. The `follow()` method is called on the follower's server
2. This server then calls `addFollower()` on the target actor's server to complete the relationship

Similarly, when unfollowing:
1. The `unfollow()` method is called on the follower's server
2. This server then calls `removeFollower()` on the target actor's server

The methods `addFollower()` and `removeFollower()` are internal federation methods that maintain the follower lists on the target server. They should not be called directly by clients but are used internally by the `follow()` and `unfollow()` methods to synchronize the follow relationship across servers.

In order to minimize network calls, the calls to `receiveActivity()` are batched, meaning that when many followers are present on the same remote server, the method should be called only once per activity listing all the followers whose inbox will receive it.

An actor account can be deleted using the method `deleteActor()`, which removes the actor from the server's registry and removes the actor from the following lists of all actors that were following it and from the followers' lists of all actors that it was following.

