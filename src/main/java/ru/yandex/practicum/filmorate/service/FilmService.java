package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage filmStorage;

    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage, MpaStorage mpaStorage,
                       GenreStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }


    public void addLike(int filmId, int userId) {
        Film film = getFilmById(filmId);
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с id " + userId + " не найден"));
        film.getLikes().add(userId);
        filmStorage.updateFilm(film);
    }


    public void removeLike(int filmId, int userId) {
        Film film = getFilmById(filmId);
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с id " + userId + " не найден"));

        film.getLikes().remove(userId);

        filmStorage.updateFilm(film);
    }


    public List<Film> getMostPopularFilms(int count) {
        return filmStorage.getAllFilms().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }


    public Film getFilmById(int id) {
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Фильм с id " + id + " не найден"));
    }


    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }


    public Film addFilm(Film film) {
        validateFilm(film);
        if (film.getMpa() == null || !mpaStorage.getMpaById(film.getMpa().getId()).isPresent()) {
            throw new ResourceNotFoundException("MPA с id " + film.getMpa().getId() + " не найден");
        }

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                if (!genreStorage.getGenreById(genre.getId()).isPresent()) {
                    throw new ResourceNotFoundException("Жанр с id " + genre.getId() + " не найден");
                }
            }
        }
        return filmStorage.addFilm(film);
    }


    public Film updateFilm(Film film) {
        if (filmStorage.getFilmById(film.getId()).isEmpty()) {
            throw new ResourceNotFoundException("Фильм с id " + film.getId() + " не найден");
        }
        validateFilm(film);
        return filmStorage.updateFilm(film);
    }


    private void validateFilm(Film film) {
        LocalDate earliestReleaseDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate().isBefore(earliestReleaseDate)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года.");
        }
    }
}
