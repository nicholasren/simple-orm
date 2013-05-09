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


create table houses(
  id INTEGER,
  name VARCHAR(100)
);

create table doors(
  id INTEGER,
  width INTEGER,
  height INTEGER,
  house_id INTEGER
)