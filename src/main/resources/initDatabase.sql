drop database if exists pokemon_trading;

create database pokemon_trading;

use pokemon_trading;

create table trade
(
    id           binary(16) unique primary key not null,
    date         datetime                      not null,
    user_id      varchar(128)                  not null,
    new_user_id  varchar(128)                  not null,
    inventory_id varchar(128)                  not null
);
