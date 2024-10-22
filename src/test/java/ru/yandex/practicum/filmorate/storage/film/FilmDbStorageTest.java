package ru.yandex.practicum.filmorate.storage.film;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
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

    @BeforeEach
    void resetDatabase() {
        filmDbStorage.deleteAllFilms(); // Удаляем все фильмы
        filmDbStorage.resetFilmIdSequence(); // Сбрасываем счетчик film_id
    }

    @AfterEach
    void cleanUp() {
        filmDbStorage.deleteAllFilms();
        filmDbStorage.resetFilmIdSequence();
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
        Mpa mpa = new Mpa();
        mpa.setId(1);


        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(120);
        film1.setMpa(mpa);
        film1.getLikes().add(1);
        film1.getLikes().add(2);


        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(100);
        film2.setMpa(mpa);
        film2.getLikes().add(1);


        filmDbStorage.addFilm(film1);
        filmDbStorage.addFilm(film2);

        List<Film> popularFilms = filmDbStorage.getAllFilms().stream().sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size())).limit(1).collect(Collectors.toList());


        assertThat(popularFilms).hasSize(1);
        assertThat(popularFilms.get(0).getName()).isEqualTo("Film 1");
    }

}
