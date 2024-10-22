package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


@Component("filmDbStorage")
@Slf4j
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film addFilm(Film film) {
        log.info("Добавление фильма: {}", film.getName());
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) " + "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"film_id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        int filmId = Objects.requireNonNull(keyHolder.getKey()).intValue();
        film.setId(filmId);

        // Сохранение жанров
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            updateFilmGenres(film);
        }
        log.info("Фильм добавлен с id: {}", film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        log.info("Обновление фильма с id: {}", film.getId());
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()), film.getDuration(), film.getMpa().getId(), film.getId());

        // Обновление жанров
        String deleteGenres = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteGenres, film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            updateFilmGenres(film);
        }
        log.info("Обновление фильма с id: {}", film.getId());
        return film;
    }

    @Override
    public Optional<Film> getFilmById(int id) {
        String sql = "SELECT f.film_id, f.name AS film_name, f.description, f.release_date, " + "f.duration, f.mpa_id, m.name AS mpa_name FROM films f " + "JOIN mpa m ON f.mpa_id = m.mpa_id " + "WHERE f.film_id = ?";

        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, id);

        if (films.isEmpty()) {
            return Optional.empty();
        }

        Film film = films.get(0);
        film.setGenres(getGenresByFilmId(film.getId()));
        return Optional.of(film);
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT f.film_id, f.name AS film_name, f.description, f.release_date, " + "f.duration, f.mpa_id, m.name AS mpa_name FROM films f " + "JOIN mpa m ON f.mpa_id = m.mpa_id";

        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm);

        for (Film film : films) {
            film.setGenres(getGenresByFilmId(film.getId()));
        }

        return films;
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("mpa_id"));
        mpa.setName(rs.getString("mpa_name"));

        Film film = new Film();
        film.setId(rs.getInt("film_id"));
        film.setName(rs.getString("film_name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        film.setMpa(mpa);

        return film;
    }

    private void updateFilmGenres(Film film) {
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        List<Object[]> batchArgs = new ArrayList<>();
        for (Genre genre : film.getGenres()) {
            batchArgs.add(new Object[]{film.getId(), genre.getId()});
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private Set<Genre> getGenresByFilmId(int filmId) {
        String sql = "SELECT g.genre_id, g.name FROM film_genres fg " + "JOIN genres g ON fg.genre_id = g.genre_id WHERE fg.film_id = ?";
        List<Genre> genres = jdbcTemplate.query(sql, this::mapRowToGenre, filmId);
        return new HashSet<>(genres);
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        Genre genre = new Genre();
        genre.setId(rs.getInt("genre_id"));
        genre.setName(rs.getString("name"));
        return genre;
    }
//Удаляет все фильмы и связанные записи

    public void deleteAllFilms() {
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM films");
        log.info("Все фильмы и связанные записи удалены.");
    }
//Сбрасывает счетчик автоинкремента для film_id

    public void resetFilmIdSequence() {
        jdbcTemplate.execute("ALTER TABLE films ALTER COLUMN film_id RESTART WITH 1");
        Integer nextVal = jdbcTemplate.queryForObject("SELECT MAX(film_id) FROM films", Integer.class);
        log.info("Счетчик film_id сброшен, текущее значение: {}", nextVal);
    }

}
