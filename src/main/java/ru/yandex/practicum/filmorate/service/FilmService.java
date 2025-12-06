package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.dto.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.validation.FilmValidator;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenresStorage;
import ru.yandex.practicum.filmorate.storage.LikesStorage;
import ru.yandex.practicum.filmorate.storage.RatesStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    @Autowired
    @Qualifier("db-films")
    private FilmStorage filmStorage;
    @Autowired
    @Qualifier("db")
    private UserStorage userStorage;
    @Autowired
    @Qualifier("db-genres")
    private GenresStorage genresStorage;
    @Autowired
    @Qualifier("db-likes")
    private LikesStorage likeStorage;
    @Autowired
    @Qualifier("db-rates")
    private RatesStorage ratesStorage;
    @Autowired
    private FilmValidator filmValidator;

    public Collection<FilmDto> findAll() {
        return filmStorage.getAll().stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public FilmDto find(Long filmId) {
        return filmStorage.find(filmId)
                .map(FilmMapper::mapToFilmDto)
                .orElseThrow(() -> {
                    log.warn("Не найден фильм с ID = {}", filmId);
                    return new NotFoundException("Фильм не найден с ID: " + filmId);
                });
    }

    public FilmDto create(NewFilmRequest newFilm) {
        Film film = FilmMapper.mapToFilm(newFilm);
        filmValidator.checkFilmIsValid(film);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> genreIds = film.getGenres().stream()
                    .map(genre -> genre.getId())
                    .collect(Collectors.toSet());

            Set<Long> existingGenreIds = genresStorage.findAllExistingIds(genreIds);

            Set<Long> missingGenreIds = new HashSet<>(genreIds);
            missingGenreIds.removeAll(existingGenreIds);

            if (!missingGenreIds.isEmpty()) {
                log.warn("Не найдены жанры с ID = {}", missingGenreIds);
                throw new NotFoundException("Не найдены жанры с ID: " + missingGenreIds);
            }
        }

        Long rateId = film.getRating().getId();
        if (rateId != null) {
            ratesStorage.find(rateId)
                    .orElseThrow(() -> {
                        log.warn("Не найден рейтинг с ID = {}", rateId);
                        return new NotFoundException("Не найден рейтинг с ID: " + rateId);
                    });
        }

        film = filmStorage.create(film);

        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto update(UpdateFilmRequest updateFilm) {
        Film film = FilmMapper.mapToFilm(updateFilm);
        filmValidator.checkFilmIsValid(film);

        Film oldFilm = filmStorage.find(updateFilm.getId())
                .orElseThrow(() -> {
                    log.warn("Не найден фильм с ID = {}", updateFilm.getId());
                    return new NotFoundException("Фильм не найден с ID: " + updateFilm.getId());
                });

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> genreIds = film.getGenres().stream()
                    .map(genre -> genre.getId())
                    .collect(Collectors.toSet());

            Set<Long> existingGenreIds = genresStorage.findAllExistingIds(genreIds);

            Set<Long> missingGenreIds = new HashSet<>(genreIds);
            missingGenreIds.removeAll(existingGenreIds);

            if (!missingGenreIds.isEmpty()) {
                log.warn("Не найдены жанры с ID = {}", missingGenreIds);
                throw new NotFoundException("Не найдены жанры с ID: " + missingGenreIds);
            }
        }

        film = FilmMapper.updateFilmFields(oldFilm, updateFilm);

        return FilmMapper.mapToFilmDto(filmStorage.update(film));
    }

    public void addLike(Long filmId, Long userId) {
        filmStorage.find(filmId)
                .orElseThrow(() -> {
                    log.warn("Не найден фильм с ID = {}", filmId);
                    return new NotFoundException("Не найден фильм с ID: " + filmId);
                });

        userStorage.find(userId)
                .orElseThrow(() -> {
                    log.warn("Не найден пользователь с ID = {}", userId);
                    return new NotFoundException("Не найден пользователь с ID: " + userId);
                });

        likeStorage.addLikeOnFilm(filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        filmStorage.find(filmId)
                .orElseThrow(() -> {
                    log.warn("Не найден фильм с ID = {}", filmId);
                    return new NotFoundException("Не найден фильм с ID: " + filmId);
                });

        userStorage.find(userId)
                .orElseThrow(() -> {
                    log.warn("Не найден пользователь с ID = {}", userId);
                    return new NotFoundException("Не найден пользователь с ID: " + userId);
                });

        likeStorage.removeLikeOnFilm(filmId, userId);
    }

    public Collection<FilmDto> getPopularFilms(int count) {
        Collection<Film> films = filmStorage.getAll();

        Set<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());

        Map<Long, Integer> likesCountByFilmId = likeStorage.getLikesCountForFilms(filmIds);

        return films.stream()
                .sorted((f1, f2) -> {
                    int likes1 = likesCountByFilmId.getOrDefault(f1.getId(), 0);
                    int likes2 = likesCountByFilmId.getOrDefault(f2.getId(), 0);
                    return Integer.compare(likes2, likes1);
                })
                .limit(count)
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }
}
