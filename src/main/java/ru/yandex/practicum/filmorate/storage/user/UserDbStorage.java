package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component("userDbStorage")
@RequiredArgsConstructor
@Slf4j
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;


    @Override
    public User createUser(User user) {
        log.info("Создание пользователя: {}", user.getLogin());

        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"user_id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);

        int userId = Objects.requireNonNull(keyHolder.getKey()).intValue();
        user.setId(userId);
        log.info("Пользователь создан с id: {}", userId);
        return user;
    }

    @Override
    public User updateUser(User user) {
        log.info("Обновление пользователя с id: {}", user.getId());

        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId());
        log.info("Пользователь с id {} обновлен: {}", user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> getUserById(int id) {
        log.info("Получение пользователя с id: {}", id);
        String sql = "SELECT * FROM users WHERE user_id = ?";

        List<User> users = jdbcTemplate.query(sql, this::mapRowToUser, id);

        if (users.isEmpty()) {
            return Optional.empty();
        }

        User user = users.get(0);
        user.setFriends(getFriendsIds(id));
        log.info("Пользователь найден: {}", user);
        return Optional.of(user);
    }

    @Override
    public List<User> getAllUsers() {
        log.info("Получение всех пользователей");
        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, this::mapRowToUser);

        for (User user : users) {
            user.setFriends(getFriendsIds(user.getId()));
            log.debug("Пользователь с id {} имеет друзей: {}", user.getId(), user.getFriends());

        }
        log.info("Всего пользователей: {}", users.size());
        return users;
    }


    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    }

    private Set<Integer> getFriendsIds(int userId) {
        String sql = "SELECT friend_id FROM friends WHERE user_id = ?";
        List<Integer> friendIds = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("friend_id"), userId);
        return new HashSet<>(friendIds);
    }

    public void resetUserIdSequence() {
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1");
        log.info("Счётчик user_id сброшен.");
    }

    public void deleteAllUsers() {
        jdbcTemplate.update("DELETE FROM users");
        log.info("Все пользователи удалены.");
    }

}
