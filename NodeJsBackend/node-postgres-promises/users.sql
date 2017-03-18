DROP DATABASE IF EXISTS users;
CREATE DATABASE users;

\c users;

CREATE TABLE user (
  ID SERIAL PRIMARY KEY,
  name VARCHAR,
  breed VARCHAR,
  age INTEGER,
  sex VARCHAR
);

INSERT INTO user (name, breed, age, sex)
  VALUES ('Tyler', 'Retrieved', 3, 'M');