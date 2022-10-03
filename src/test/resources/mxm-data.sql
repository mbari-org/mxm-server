-----------------------------------------------------------------------------
-- Assets --

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

insert into units (unit_name, abbreviation, base_unit) values ('meter', 'm', null);
insert into units (unit_name, abbreviation, base_unit) values ('centimeter', 'cm', 'meter');
insert into units (unit_name, abbreviation, base_unit) values ('millimeter', 'mm', 'meter');
insert into units (unit_name, abbreviation, base_unit) values ('kilometer', 'km', 'meter');

insert into units (unit_name, abbreviation, base_unit) values ('radian', 'rad', null);
insert into units (unit_name, abbreviation, base_unit) values ('degree', 'arcdeg', 'radian');

insert into units (unit_name, abbreviation, base_unit) values ('ampere_second', 'As', null);
insert into units (unit_name, abbreviation, base_unit) values ('ampere_hour', 'Ah', 'ampere_second');
