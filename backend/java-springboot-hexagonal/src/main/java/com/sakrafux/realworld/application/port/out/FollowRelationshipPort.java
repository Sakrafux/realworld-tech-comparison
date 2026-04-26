package com.sakrafux.realworld.application.port.out;

public interface FollowRelationshipPort {
    boolean isFollowing(Long followerId, Long followeeId);
    void follow(Long followerId, Long followeeId);
    void unfollow(Long followerId, Long followeeId);
}
