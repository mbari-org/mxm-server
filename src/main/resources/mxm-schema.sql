create type provider_api_type as enum (
  'REST0',
  'GRAPHQL'
  )
;

create table if not exists providers
(
  provider_id               varchar               not null,
  http_endpoint             varchar               not null,
  api_type                  provider_api_type     not null,
  description               varchar,
  uses_sched                boolean default false not null,
  can_validate              boolean default false not null,
  uses_units                boolean default false not null,
  can_report_mission_status boolean default false not null,
  description_format        varchar,
  primary key (provider_id)
)
;

create table if not exists asset_classes
(
  provider_id varchar not null,
  class_name  varchar not null,
  description varchar,
  foreign key (provider_id) references providers on update cascade on delete cascade,
  primary key (provider_id, class_name)
)
;

create table if not exists assets
(
  provider_id varchar not null,
  asset_id    varchar not null,
  class_name  varchar not null,
  description varchar,
  foreign key (provider_id, class_name) references asset_classes on update cascade on delete cascade,
  primary key (provider_id, asset_id)
)
;

create table if not exists mission_tpls
(
  provider_id    varchar not null,
  mission_tpl_id varchar not null,
  description    varchar,
  retrieved_at   timestamp with time zone,
  foreign key (provider_id) references providers on update cascade on delete cascade,
  primary key (provider_id, mission_tpl_id)
)
;

create or replace function mission_tpls_set_retrieved_at() returns trigger as
$$
begin
  -- set retrieved_at only if not yet set and this is an actual template (not a directory)
  if new.retrieved_at is null and new.mission_tpl_id !~ '^.*/$' then
    new.retrieved_at = now();
  end if;
  return new;
end
$$ language plpgsql;

drop trigger if exists mission_tpls_set_retrieved_at on mission_tpls;
create trigger mission_tpls_set_retrieved_at
  before insert
  on mission_tpls
  for each row
execute procedure mission_tpls_set_retrieved_at();

create table if not exists mission_tpl_asset_class
(
  provider_id      varchar not null,
  mission_tpl_id   varchar not null check (mission_tpl_id !~ '^.*/$'),
  asset_class_name varchar not null,
  foreign key (provider_id, mission_tpl_id) references mission_tpls on update cascade on delete cascade,
  foreign key (provider_id, asset_class_name) references asset_classes on update cascade on delete cascade,
  primary key (provider_id, mission_tpl_id, asset_class_name)
)
;

create table if not exists units
(
  provider_id  varchar not null,
  unit_name    varchar not null,
  abbreviation varchar,
  base_unit    varchar,
  foreign key (provider_id) references providers on update cascade on delete cascade,
  foreign key (provider_id, base_unit) references units on update cascade on delete cascade,
  primary key (provider_id, unit_name)
)
;

create table if not exists parameters
(
  provider_id         varchar               not null,
  mission_tpl_id      varchar               not null check (mission_tpl_id !~ '^.*/$'),
  param_name          varchar               not null,
  type                varchar               not null,
  required            boolean default false not null,
  default_value       varchar,
  default_units       varchar,
  value_can_reference varchar,
  description         varchar,
  param_order         serial,
  foreign key (provider_id, mission_tpl_id) references mission_tpls on update cascade on delete cascade,
  foreign key (provider_id, default_units) references units on update cascade on delete cascade,
  primary key (provider_id, mission_tpl_id, param_name)
)
;

create type mission_status_type as enum (
  'DRAFT',
  'SUBMITTED',
  'QUEUED',
  'RUNNING',
  'TERMINATED'
  )
;

create type mission_sched_type as enum (
  'ASAP',
  'QUEUE',
  'DATE'
  )
;

create table if not exists missions
(
  provider_id    varchar             not null,
  mission_tpl_id varchar             not null check (mission_tpl_id !~ '^.*/$'),
  mission_id     varchar             not null,
  mission_status mission_status_type not null,
  asset_id       varchar             not null,
  description    varchar,
  sched_type     mission_sched_type  not null,
  sched_date     timestamp with time zone,
  start_date     timestamp with time zone,
  end_date       timestamp with time zone,
  updated_date   timestamp with time zone,
  foreign key (provider_id, mission_tpl_id) references mission_tpls on update cascade on delete cascade,
  foreign key (provider_id, asset_id) references assets on update cascade on delete cascade,
  primary key (provider_id, mission_tpl_id, mission_id)
)
;

create table if not exists arguments
(
  provider_id    varchar not null,
  mission_tpl_id varchar not null check (mission_tpl_id !~ '^.*/$'),
  mission_id     varchar not null,
  param_name     varchar not null,
  param_value    varchar not null,
  param_units    varchar,
  foreign key (provider_id, mission_tpl_id, mission_id) references missions on update cascade on delete cascade,
  foreign key (provider_id, mission_tpl_id, param_name) references parameters on update cascade on delete cascade,
  primary key (provider_id, mission_tpl_id, mission_id, param_name)
)
;
