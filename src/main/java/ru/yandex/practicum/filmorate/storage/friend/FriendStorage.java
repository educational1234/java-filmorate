package ru.yandex.practicum.filmorate.storage.friend;

import java.util.Set;

public interface FriendStorage {
    void addFriend(int userId, int friendId);

    void removeFriend(int userId, int friendId);

    Set<Integer> getFriendsIds(int userId);

    Set<Integer> getCommonFriendsIds(int userId, int otherId);
}
