set dotenv-load := true

# Just a few devel convenient recipes.
# More complete in parent 'mxm' and via github actions.

# List recipes
list:
	@just --list --unsorted

# quarkus dev
dev:
	quarkus dev

# ./mvnw clean
clean:
	./mvnw clean

# Format code
format:
	./mvnw spotless:apply

# Run tests
test:
	./mvnw test

# Show version
version:
	@head -n 10 pom.xml | rg "<version>(.*)</version>" -or '$1'

################################################################
## Docker images

# Build mbari/mxm image
mxm-image-build version:
  ./mvnw package
  docker build -f src/main/docker/Dockerfile.jvm -t mbari/mxm:{{version}} .

# Build mbari/mxm-postgres image
mxm-image-postgres-build version: update-sql-init-scripts
  #!/usr/bin/env bash
  cd docker
  docker build -f Dockerfile-postgres -t mbari/mxm-postgres:{{version}} .

# Push mbari/mxm-postgres image
mxm-image-postgres-push version:
  docker push      mbari/mxm-postgres:{{version}}
  docker image tag mbari/mxm-postgres:{{version}} mbari/mxm-postgres:0.9
  docker push      mbari/mxm-postgres:0.9

################################################################
## Misc utilities

## (occassionally, the launched UI on 8086 (via quarkus dev) seems to not be
## released after exiting quarkus; so this helps find the PID and kill it)
# What process is listening on a port?
lsof port='8086':
  #!/usr/bin/env bash
  tmp=$(mktemp)
  lsof -nP -iTCP:{{port}} > $tmp 2>/dev/null
  if [ "$?" -eq "0" ]; then
    rg LISTEN $tmp
  else
    echo "No process listening on port {{port}}"
  fi


################################################################
## Recipes to get some data for DB initialization scripts,
## prior to updating the 'mbari/mxm-postgres' image.

# Update sql scripts for db initialization
update-sql-init-scripts: units-get-and-sql assets-get-and-sql

# Get units from TethysDash and create sql for db initialization
units-get-and-sql: units-get units-to-sql

units-get:
  #!/usr/bin/env bash
  mkdir -p tmp
  curlie https://okeanids.mbari.org/TethysDash/api/mxm/units > tmp/units.json

units-to-sql:
  #!/usr/bin/env bash
  # convert to tmp/units.sql
  cat tmp/units.json | jq .result | jq -r '(.[] | [.name, .abbreviation,
     (if .baseUnit == null then null else  "'\''" + .baseUnit + "'\''" end)
   ]) | @sh' |
  xargs printf "insert into units (unit_name, abbreviation, base_unit) values ('%s', '%s', %s);\n"  > tmp/units.sql

  # NOTE: some LRAUV missions use parameters with units that a *derived*, not a *base* units.
  # For convenience, let's just add them as base units here:
  printf "insert into units (unit_name, abbreviation, base_unit) values ('arcdeg', 'arcdeg', 'radian');\n"  >> tmp/units.sql
  printf "insert into units (unit_name, abbreviation, base_unit) values ('psu', 'psu', 'kilogram_per_kilogram');\n"  >> tmp/units.sql

  # then, first output the base units (those lines having base_unit==null):
  OUT="docker/mxm-10-data-units.sql"
  echo "-- Generated by : just units-to-sql" > "${OUT}"
  grep    ", null);" tmp/units.sql >> "${OUT}"
  # then, the others:
  grep -v ", null);" tmp/units.sql >> "${OUT}"

# Get assets from TethysDash and create sql for db initialization
assets-get-and-sql: assets-get assets-to-sql

assets-get:
  #!/usr/bin/env bash
  mkdir -p tmp
  curlie https://okeanids.mbari.org/TethysDash/api/mxm/assetclasses > tmp/td_assetclasses.json

assets-to-sql:
  #!/usr/bin/env bash
  OUT="docker/mxm-25-data-assets-tethysdash.sql"
  echo "-- Generated by : just assets-to-sql" > "${OUT}"
  # we know there's only one class ("LRAUV"), and we look only at the assets:
  cat tmp/td_assetclasses.json | jq '.result[0].assets' | jq -r '(.[] | [.assetId]) | @sh' | sort |
  xargs printf "insert into assets (asset_id, class_name, description) values ('%s', 'LRAUV', null);\n" >> "${OUT}"
