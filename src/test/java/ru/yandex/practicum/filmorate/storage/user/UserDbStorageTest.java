package ru.yandex.practicum.filmorate.storage.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
public class UserDbStorageTest {

    @Autowired
    private UserDbStorage userDbStorage;

    @Test
    public void testCreateAndFindUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        userDbStorage.createUser(user);

        Optional<User> retrievedUser = userDbStorage.getUserById(user.getId());
        assertThat(retrievedUser).isPresent().hasValueSatisfying(u -> assertThat(u).hasFieldOrPropertyWithValue("email", "test@example.com"));
    }

    @Test
    public void testUpdateUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        userDbStorage.createUser(user);

        Optional<User> retrievedUser = userDbStorage.getUserById(user.getId());
        assertThat(retrievedUser).isPresent();


        user.setName("Updated User");
        userDbStorage.updateUser(user);

        retrievedUser = userDbStorage.getUserById(user.getId());
        assertThat(retrievedUser).isPresent().hasValueSatisfying(u -> assertThat(u).hasFieldOrPropertyWithValue("name", "Updated User"));
    }
}
