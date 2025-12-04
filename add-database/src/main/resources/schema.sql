CREATE TABLE IF NOT EXISTS mpa (
  id INT PRIMARY KEY,
  name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS genre (
  id INT PRIMARY KEY,
  name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(255) NOT NULL,
  login VARCHAR(100) NOT NULL,
  name VARCHAR(255),
  birthday DATE
);

CREATE TABLE IF NOT EXISTS film (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(1000),
  release_date DATE,
  duration INT,
  mpa_id INT,
  CONSTRAINT fk_film_mpa FOREIGN KEY (mpa_id) REFERENCES mpa(id)
);

CREATE TABLE IF NOT EXISTS film_genre (
  film_id BIGINT NOT NULL,
  genre_id INT NOT NULL,
  PRIMARY KEY (film_id, genre_id),
  CONSTRAINT fk_fg_film FOREIGN KEY (film_id) REFERENCES film(id) ON DELETE CASCADE,
  CONSTRAINT fk_fg_genre FOREIGN KEY (genre_id) REFERENCES genre(id)
);

CREATE TABLE IF NOT EXISTS film_like (
  film_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  PRIMARY KEY (film_id, user_id),
  CONSTRAINT fk_fl_film FOREIGN KEY (film_id) REFERENCES film(id) ON DELETE CASCADE,
  CONSTRAINT fk_fl_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_friend (
  user_id BIGINT NOT NULL,
  friend_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, friend_id),
  CONSTRAINT fk_uf_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_uf_friend FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_film_mpa ON film(mpa_id);
CREATE INDEX IF NOT EXISTS idx_users_login ON users(login);
