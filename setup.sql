drop database if exists orm;
create database orm;

use orm;

create table pets(
  id INTEGER primary key NOT NULL AUTO_INCREMENT,
  name VARCHAR(100),
  gender VARCHAR(10),
  age INTEGER,
  person_id INTEGER
);

create table people(
  id INTEGER primary key NOT NULL AUTO_INCREMENT,
  name VARCHAR(100)
);


create table houses(
  id INTEGER primary key NOT NULL AUTO_INCREMENT,
  name VARCHAR(100)
);

create table doors(
  id INTEGER primary key NOT NULL AUTO_INCREMENT,
  width INTEGER,
  height INTEGER,
  house_id INTEGER
)