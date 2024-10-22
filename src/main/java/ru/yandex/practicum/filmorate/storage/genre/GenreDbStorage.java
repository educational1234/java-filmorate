package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;


@Component
@RequiredArgsConstructor
@Slf4j
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Genre> getAllGenres() {
        log.info("Получение всех жанров");
        String sql = "SELECT * FROM genres";
        List<Genre> genres = jdbcTemplate.query(sql, this::mapRowToGenre);
        log.info("Найдено жанров: {}", genres.size());
        return genres;
    }

    @Override
    public Optional<Genre> getGenreById(int id) {
        log.info("Получение жанра с id: {}", id);
        String sql = "SELECT * FROM genres WHERE genre_id = ?";
        List<Genre> genres = jdbcTemplate.query(sql, this::mapRowToGenre, id);
        if (genres.isEmpty()) {
            log.warn("Жанр с id {} не найден", id);
            return Optional.empty();
        }
        Genre genre = genres.get(0);
        log.info("Жанр найден: {}", genre);
        return Optional.of(genre);
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        Genre genre = new Genre();
        genre.setId(rs.getInt("genre_id"));
        genre.setName(rs.getString("name"));
        return genre;
    }
}
