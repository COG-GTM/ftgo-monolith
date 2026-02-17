use ftgo;

create table outbox_events
(
    id             bigint       not null auto_increment,
    aggregate_type varchar(255) not null,
    aggregate_id   varchar(255) not null,
    event_type     varchar(255) not null,
    payload        text         not null,
    created_at     timestamp    not null default current_timestamp,
    published_at   timestamp    null,
    published      bit          not null default 0,
    primary key (id)
) engine = InnoDB;

create index idx_outbox_events_unpublished on outbox_events (published, created_at);
