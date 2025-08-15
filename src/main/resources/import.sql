--This SQL script will be used to initialize the PostgreSQL database inside the Docker container.
--It creates two tables: `user_profiles` and `profile_photos`.

CREATE TABLE IF NOT EXISTS user_profiles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    bank_id BIGINT NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS profile_photos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    photo_data LONGBLOB,
    content_type VARCHAR(50),
    CONSTRAINT fk_user
        FOREIGN KEY(user_id)
        REFERENCES user_profiles(id)
        ON DELETE CASCADE
);