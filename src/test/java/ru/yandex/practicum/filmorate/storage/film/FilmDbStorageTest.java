package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@Transactional
@Slf4j
public class FilmDbStorageTest {

    @Autowired
    @Qualifier("filmDbStorage")
    private FilmDbStorage filmDbStorage;
    @Autowired
    private MpaDbStorage mpaDbStorage;

    @Autowired
    private GenreDbStorage genreDbStorage;

    @Autowired
    private UserDbStorage userDbStorage;
    @Autowired
    private FilmLikeDbStorage filmLikeDbStorage;
    @Autowired
    private FilmService filmService;


    @BeforeEach
    void resetDatabase() {
        filmDbStorage.deleteAllFilms(); // Удаляем все фильмы
        filmDbStorage.resetFilmIdSequence(); // Сбрасываем счетчик film_id
        userDbStorage.deleteAllUsers(); // Удаляем всех пользователей
        userDbStorage.resetUserIdSequence(); // Сбрасываем счетчик user_id
    }

    @AfterEach
    void cleanUp() {
        filmDbStorage.deleteAllFilms();
        filmDbStorage.resetFilmIdSequence();
        userDbStorage.deleteAllUsers(); // Удаляем всех пользователей
        userDbStorage.resetUserIdSequence(); // Сбрасываем счетчик user_id
    }


    @Test
    @Order(1)
    public void testCreateAndFindFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        Mpa mpa = new Mpa();
        mpa.setId(3);
        film.setMpa(mpa);
        Genre genre = new Genre();
        genre.setId(1); // Комедия
        film.getGenres().add(genre);

        Film createdFilm = filmDbStorage.addFilm(film);
        assertThat(createdFilm.getId()).isGreaterThan(0);

        Film retrievedFilm = filmDbStorage.getFilmById(createdFilm.getId()).orElse(null);
        assertThat(retrievedFilm).isNotNull();
        assertThat(retrievedFilm.getId()).isEqualTo(1);
        assertThat(retrievedFilm.getName()).isEqualTo("Test Film");
        assertThat(retrievedFilm.getMpa().getName()).isEqualTo("PG-13");
        assertThat(retrievedFilm.getGenres()).hasSize(1);
        assertThat(retrievedFilm.getGenres().iterator().next().getName()).isEqualTo("Комедия");
    }


    @Test
    @Order(2)
    public void testGetAllFilms() {
        Mpa mpa = new Mpa();
        mpa.setId(1);
        mpa.setName("G");

        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(120);
        film1.setMpa(mpa);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(100);
        film2.setMpa(mpa);

        filmDbStorage.addFilm(film1);
        filmDbStorage.addFilm(film2);

        List<Film> films = filmDbStorage.getAllFilms();
        assertThat(films).hasSize(2);
        assertThat(films).extracting(Film::getName).containsExactlyInAnyOrder("Film 1", "Film 2");
    }

    @Test
    @Order(3)
    public void testGetPopularFilms() {
        // Создаем пользователя с user_id = 1
        log.info("Создаем пользователя с user_id = 1");
        User user = new User();
        user.setId(1);
        user.setEmail("testuser@mail.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        userDbStorage.createUser(user); // Добавляем пользователя в базу
        log.info("Пользователь 1 создан: {}", user);
        // Создаем второго пользователя с user_id = 2
        log.info("Создаем пользователя с user_id = 2");
        User user2 = new User();
        user2.setId(2);
        user2.setEmail("testuser2@mail.com");
        user2.setLogin("testuser2");
        user2.setName("Test User 2");
        user2.setBirthday(LocalDate.of(1992, 1, 1));
        userDbStorage.createUser(user2); // Добавляем второго пользователя
        log.info("Пользователь 2 создан: {}", user2);
        Mpa mpa = new Mpa();
        mpa.setId(1);

        log.info("Создаем фильм 1");
        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(120);
        film1.setMpa(mpa);
        film1.getLikes().add(1); // Пользователь 1 лайкает фильм
        film1.getLikes().add(2); // Пользователь 2 лайкает фильм
        log.info("Фильм 1 создан: {}", film1);
        log.info("Создаем фильм 2");
        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(100);
        film2.setMpa(mpa);
        film2.getLikes().add(1); // Пользователь 1 лайкает фильм
        log.info("Фильм 2 создан: {}", film2);
        // Добавляем фильмы в базу
        filmDbStorage.addFilm(film1);
        filmDbStorage.addFilm(film2);
        log.info("Фильмы добавлены в базу");
        // Тестируем получение популярных фильмов
        List<Film> popularFilms = filmDbStorage.getAllFilms().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(1)
                .collect(Collectors.toList());
        log.info("Популярные фильмы: {}", popularFilms);
        assertThat(popularFilms).hasSize(1);
        assertThat(popularFilms.get(0).getName()).isEqualTo("Film 1");
    }
}
