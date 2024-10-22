package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friend.FriendStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service

@Slf4j
public class UserService {

    private final UserStorage userStorage;
    private final FriendStorage friendStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       FriendStorage friendStorage) {
        this.userStorage = userStorage;
        this.friendStorage = friendStorage;
    }


    public void addFriend(int userId, int friendId) {
        getUserById(userId);
        getUserById(friendId);

        friendStorage.addFriend(userId, friendId);
        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        getUserById(userId);
        getUserById(friendId);

        friendStorage.removeFriend(userId, friendId);
        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }


    public List<User> getFriends(int userId) {
        getUserById(userId);

        Set<Integer> friendsIds = friendStorage.getFriendsIds(userId);
        return friendsIds.stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }


    public List<User> getCommonFriends(int userId, int otherId) {
        getUserById(userId);
        getUserById(otherId);

        Set<Integer> commonFriendsIds = friendStorage.getCommonFriendsIds(userId, otherId);
        return commonFriendsIds.stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }


    public User getUserById(int id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с id " + id + " не найден"));

    }


    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }


    public User createUser(User user) {
        return userStorage.createUser(user);
    }


    public User updateUser(User user) {
        if (userStorage.getUserById(user.getId()).isEmpty()) {
            throw new ResourceNotFoundException("Пользователь с id " + user.getId() + " не найден");
        }
        return userStorage.updateUser(user);
    }
}
