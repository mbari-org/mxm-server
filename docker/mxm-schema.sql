-----------------------------------------------------------------------------
-- Assets --

-- NOTE: This part while integration with TrackingDB (or relevant alternative) is done.

create table if not exists asset_classes
(
  class_name varchar not null,
  description varchar,
  primary key (class_name)
)
;

create table if not exists assets
(
  asset_id    varchar not null,
  class_name  varchar not null,
  description varchar,
  foreign key (class_name) references asset_classes on update cascade on delete cascade,
  primary key (asset_id) -- TODO: Or, (asset_id, class_name), Or, with use of additional identity column...
)
;

-- For dev/test convenience, add some initial asset classes and assets:

-- TethysDash:
insert into asset_classes (class_name, description) values ('LRAUV', 'Long-Range Autonomous Underwater Vehicle');
insert into assets (asset_id, class_name, description) values ('sim', 'LRAUV', 'LRAUV simulator');
insert into assets (asset_id, class_name, description) values ('daphne', 'LRAUV', 'LRAUV Daphne');
insert into assets (asset_id, class_name, description) values ('pontus', 'LRAUV', 'LRAUV Pontus');
insert into assets (asset_id, class_name, description) values ('tethys', 'LRAUV', 'LRAUV Tethys');

-- TSAUV Front tracking:
insert into asset_classes (class_name, description) values ('Waveglider', 'LR Wave Glider');
insert into assets (asset_id, class_name, description) values ('Tiny', 'Waveglider', 'WG Tiny');
insert into assets (asset_id, class_name, description) values ('Sparky', 'Waveglider', 'WG Sparky');
insert into assets (asset_id, class_name, description) values ('SV3-117', 'Waveglider', 'WG SV3-117');
insert into assets (asset_id, class_name, description) values ('SV3-127', 'Waveglider', 'WG SV3-127');

-- mxm-provider-example:
insert into asset_classes (class_name, description) values ('AcmeDevice', 'Used by mxm-provider-example');
insert into asset_classes (class_name, description) values ('FooPlat', 'Used by mxm-provider-example');
insert into assets (asset_id, class_name, description) values ('acme1', 'AcmeDevice', 'Acme device 1');
insert into assets (asset_id, class_name, description) values ('acme2', 'AcmeDevice', 'Acme device 2');
insert into assets (asset_id, class_name, description) values ('acme3', 'AcmeDevice', 'Acme device 3');
insert into assets (asset_id, class_name, description) values ('foo1', 'FooPlat', 'Foo platform 1');
insert into assets (asset_id, class_name, description) values ('foo2', 'FooPlat', 'Foo platform 2');

-----------------------------------------------------------------------------
-- Units --

-- To be pre-populated from the LRAUV/TethysDash system for general use
create table if not exists units
(
  unit_name    varchar not null,
  abbreviation varchar,
  base_unit    varchar,
  foreign key (base_unit) references units on update cascade on delete cascade,
  primary key (unit_name)
)
;

insert into units (unit_name, abbreviation, base_unit) values ('meter', 'm', null);
insert into units (unit_name, abbreviation, base_unit) values ('centimeter', 'cm', 'meter');
insert into units (unit_name, abbreviation, base_unit) values ('ampere_second', 'As', null);
insert into units (unit_name, abbreviation, base_unit) values ('ampere_hour', 'Ah', 'ampere_second');

-----------------------------------------------------------------------------
-- Rest of MXM modeling follows --

create type provider_api_type as enum (
  'REST',
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

create table if not exists mission_tpl_asset_class
(
  provider_id      varchar not null,
  mission_tpl_id   varchar not null check (mission_tpl_id !~ '^.*/$'),
  asset_class_name varchar not null,
  foreign key (provider_id, mission_tpl_id) references mission_tpls on update cascade on delete cascade,
  foreign key (asset_class_name) references asset_classes on update cascade on delete cascade,
  primary key (provider_id, mission_tpl_id, asset_class_name)
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
  param_order         integer,
  foreign key (provider_id, mission_tpl_id) references mission_tpls on update cascade on delete cascade,
  foreign key (default_units) references units on update cascade on delete cascade,
  primary key (provider_id, mission_tpl_id, param_name)
)
;

create type mission_status_type as enum (
  'DRAFT',
  'SUBMITTED',
  'QUEUED',
  'RUNNING',
  'COMPLETED',
  'TERMINATED',
  'FAILED',
  'CANCELLED',
  'UNKNOWN'
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
  provider_id         varchar             not null,
  mission_tpl_id      varchar             not null check (mission_tpl_id !~ '^.*/$'),
  mission_id          varchar             not null,

  provider_mission_id varchar,
  mission_status      mission_status_type not null,
  asset_id            varchar             not null,
  description         varchar,
  sched_type          mission_sched_type  not null,
  sched_date          timestamp with time zone,
  start_date          timestamp with time zone,
  end_date            timestamp with time zone,
  updated_date        timestamp with time zone,
  foreign key (provider_id, mission_tpl_id) references mission_tpls on update cascade on delete cascade,
  foreign key (asset_id) references assets on update cascade on delete cascade,
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
