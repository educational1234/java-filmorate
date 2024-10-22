package ru.yandex.practicum.filmorate.storage.friend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class FriendDbStorage implements FriendStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addFriend(int userId, int friendId) {

        log.info("Добавление друга: пользователь {} добавляет пользователя {}", userId, friendId);
        String checkSql = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, friendId, userId);

        if (count == 0) {
            String sql = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, ?)";
            jdbcTemplate.update(sql, userId, friendId, false);
            log.info("Пользователь {} добавил пользователя {} в друзья", userId, friendId);
        } else {
            log.warn("Пользователь {} уже добавил пользователя {} в друзья", userId, friendId);
        }
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        log.info("Удаление друга: пользователь {} удаляет пользователя {}", userId, friendId);
        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, userId, friendId);
        if (rowsAffected > 0) {
            log.info("Пользователь {} удалил пользователя {} из друзей", userId, friendId);
        } else {
            log.warn("Пользователь {} не был другом пользователя {}", userId, friendId);
        }
    }

    @Override
    public Set<Integer> getFriendsIds(int userId) {
        log.info("Получение списка друзей пользователя {}", userId);
        String sql = "SELECT friend_id FROM friends WHERE user_id = ? AND status = false";
        List<Integer> friends = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("friend_id"), userId);
        Set<Integer> friendsSet = new HashSet<>(friends);
        log.debug("Пользователь {} имеет друзей: {}", userId, friendsSet);
        return friendsSet;
    }

    @Override
    public Set<Integer> getCommonFriendsIds(int userId, int otherId) {
        log.info("Получение общих друзей пользователей {} и {}", userId, otherId);
        Set<Integer> userFriends = getFriendsIds(userId);
        Set<Integer> otherFriends = getFriendsIds(otherId);
        userFriends.retainAll(otherFriends);
        log.debug("Общие друзья пользователей {} и {}: {}", userId, otherId, userFriends);
        return userFriends;
    }
}
