package com.famelive.common.followermanagement

import com.famelive.common.command.slotmanagement.FetchMostPopularUsersCommand
import com.famelive.common.user.User

class Follow {

    User performer
    User follower
    Boolean isFollower = true
    Date dateCreated

    static constraints = {
        performer nullable: false
        follower nullable: false
    }

    Follow() {}

    Follow(User performer, User follower) {
        this.performer = performer
        this.follower = follower
    }

    static List<User> fetchUserFollowers(User performer, Boolean isFollow = true) {
        createCriteria().list {
            eq('performer', performer)
            eq('isFollower', isFollow)
            projections {
                property('follower')
            }
        } as List<User> ?: []
    }

    static List<User> fetchUserFollowings(User follower, Boolean isFollow = true) {
        createCriteria().list {
            eq('follower', follower)
            eq('isFollower', isFollow)
            projections {
                property('performer')
            }
        } as List<User> ?: []
    }

    static Boolean isUserFollowingPerformer(User follower, User performer) {
        createCriteria().count() {
            eq('follower', follower)
            eq('performer', performer)
            eq('isFollower', true)
        } as Boolean
    }

    static List<String> getChannelsForPerformer(User performer) {
        List channelsForPerformer = createCriteria().list {
            eq('performer', performer)
            createAlias 'follower', 'f'
            projections {
                property('f.channel')
            }
            eq('isFollower', true)
        } as List ?: []
        return channelsForPerformer
    }

    static List<String> getFollowersEmailIds(Long userId) {
        List<String> followersEmailIds = createCriteria().list {
            projections {
                eq('performer', User.get(userId))
                follower {
                    property('email')
                }
            }
        } as List<String>
        return followersEmailIds
    }

    static namedQueries = {

        filteredPerformer { FetchMostPopularUsersCommand fetchMostPopularUsersCommand ->
            projections {
                groupProperty('performer')
            }
            rowCount('followerCount')
            order('followerCount', 'desc')
        }

        filterFollowersEmailIds { Long userId ->

        }

    }
}
