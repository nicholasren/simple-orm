drop database if exists orm;
create database orm;

use orm;

create table pets(
  id INTEGER,
  name VARCHAR(100),
  gender VARCHAR(10),
  age INTEGER,
  person_id INTEGER
);

create table people(
  id INTEGER,
  name VARCHAR(100)
);