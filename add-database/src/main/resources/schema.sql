DROP TABLE IF EXISTS friendships, likes, film_genres, films, users, genres, mpa_ratings;

CREATE TABLE IF NOT EXISTS mpa_ratings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS genres (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    login VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    birthday DATE NOT NULL,
    CONSTRAINT chk_email_format CHECK (email LIKE '%@%'),
    CONSTRAINT chk_login_format CHECK (login NOT LIKE '% %')
);

CREATE TABLE IF NOT EXISTS films (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(200),
    release_date DATE NOT NULL,
    duration INT NOT NULL,
    mpa_rating_id INT,
    CONSTRAINT fk_film_mpa FOREIGN KEY (mpa_rating_id) REFERENCES mpa_ratings(id) ON DELETE SET NULL,
    CONSTRAINT chk_duration CHECK (duration > 0)
);

CREATE TABLE IF NOT EXISTS film_genres (
    film_id BIGINT NOT NULL,
    genre_id INT NOT NULL,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS likes (
    film_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS friendships (
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_login ON users(login);
CREATE INDEX idx_films_name ON films(name);
CREATE INDEX idx_friendships_friend_id ON friendships(friend_id);
CREATE INDEX idx_likes_user_id ON likes(user_id);
CREATE INDEX idx_film_genres_genre_id ON film_genres(genre_id);