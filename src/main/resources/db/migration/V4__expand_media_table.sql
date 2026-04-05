alter table media add column original_title varchar(255);
alter table media add column media_type varchar(32) not null default 'OTHER';
alter table media add column status varchar(32) not null default 'ACTIVE';
alter table media add column summary varchar(4000);
alter table media add column release_date date;
alter table media add column runtime_minutes integer;
alter table media add column language varchar(16);
alter table media add column created_at timestamp with time zone not null default current_timestamp;
alter table media add column updated_at timestamp with time zone not null default current_timestamp;
alter table media add constraint chk_media_runtime_minutes_positive
    check (runtime_minutes is null or runtime_minutes > 0);

create index idx_media_media_type on media (media_type);
create index idx_media_status on media (status);
create index idx_media_release_date on media (release_date);
