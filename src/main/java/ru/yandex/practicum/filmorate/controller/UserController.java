package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final List<User> users = new ArrayList<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1); // Генератор ID


    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        user.setId(idGenerator.getAndIncrement()); // Назначаем уникальный ID
        users.add(user);
        log.info("Пользователь создан: {}", user);
        return user;
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable int id, @Valid @RequestBody User user) {
        // Ищем пользователя по ID
        User existingUser = findUserById(id);
        if (existingUser == null) {
            log.error("Пользователь с id {} не найден", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);  // 404
        }
        user.setId(id); // Устанавливаем ID, чтобы обновить правильного пользователя
        updateUserInStorage(user);
        log.info("Пользователь обновлен: {}", user);
        return ResponseEntity.ok(user); // Возвращаем обновлённого пользователя
    }


    private void updateUserInStorage(User user) {
        // Обновляем пользователя в хранилище (списке)
        int index = users.indexOf(findUserById(user.getId()));
        if (index != -1) {
            users.set(index, user);
        }
    }

    @GetMapping
    public List<User> getAllUsers() {
        return users; // Возвращаем всех пользователей
    }

    // Вспомогательный метод для поиска пользователя по ID
    private User findUserById(int id) {
        return users.stream()
                .filter(user -> user.getId() == id)
                .findFirst()
                .orElse(null);
    }
}
