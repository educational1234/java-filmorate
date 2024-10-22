package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Mpa> getAllMpa() {
        log.info("Получение всех рейтингов MPA");
        String sql = "SELECT * FROM mpa";
        List<Mpa> mpas = jdbcTemplate.query(sql, this::mapRowToMpa);
        log.info("Найдено рейтингов MPA: {}", mpas.size());
        return mpas;
    }

    @Override
    public Optional<Mpa> getMpaById(int id) {
        log.info("Получение рейтинга MPA с id: {}", id);
        String sql = "SELECT * FROM mpa WHERE mpa_id = ?";
        List<Mpa> mpas = jdbcTemplate.query(sql, this::mapRowToMpa, id);
        if (mpas.isEmpty()) {
            log.warn("MPA с id {} не найден", id);
            return Optional.empty();
        }

        Mpa mpa = mpas.get(0);
        log.info("Рейтинг MPA найден: {}", mpa);
        return Optional.of(mpa);
    }

    private Mpa mapRowToMpa(ResultSet rs, int rowNum) throws SQLException {
        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("mpa_id"));
        mpa.setName(rs.getString("name"));
        return mpa;
    }
}
