alter table media_file add column media_file_id varchar(36);

update media_file
set media_file_id = '00000000-0000-0000-0000-' || lpad(cast(id as varchar), 12, '0');

alter table media_file alter column media_file_id set not null;
alter table media_file add constraint uk_media_file_media_file_id unique (media_file_id);

alter table media_file add column owner_media_id varchar(36);

update media_file mf
set owner_media_id = (
    select m.media_id
    from media m
    where m.id = mf.media_id
);

alter table media_file drop constraint if exists fk_media_file_media;
alter table media_file drop column media_id;
alter table media_file rename column owner_media_id to media_id;

alter table media_file add column created_at timestamp with time zone not null default current_timestamp;
alter table media_file add column updated_at timestamp with time zone not null default current_timestamp;
alter table media_file add column version bigint not null default 0;

alter table media_file add constraint fk_media_file_media_public
    foreign key (media_id) references media(media_id) on delete set null;

drop index if exists idx_media_file_media_id;
create index idx_media_file_media_id on media_file (media_id);
