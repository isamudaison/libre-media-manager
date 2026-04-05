alter table media add column parent_id varchar(36);

alter table media add constraint fk_media_parent
    foreign key (parent_id) references media(media_id) on delete set null;

create index idx_media_parent_id on media (parent_id);
