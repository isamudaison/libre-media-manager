alter table media_file add column label varchar(255);
alter table media_file add column mime_type varchar(255);
alter table media_file add column size_bytes bigint;
alter table media_file add column duration_seconds integer;
alter table media_file add column primary_file boolean not null default false;
alter table media_file add constraint chk_media_file_size_bytes_positive
    check (size_bytes is null or size_bytes > 0);
alter table media_file add constraint chk_media_file_duration_seconds_positive
    check (duration_seconds is null or duration_seconds > 0);
