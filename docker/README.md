# Building and publishing the images

**NOTE**: These notes combine both building and using the images.
General user-oriented instructions is a TODO.

The user-visible MXM version is the one set in `mxm-ui`'s `package.json` file.

## `mbari/mxm-server`

This image contains not only the mxm-server component,
but also the mxm-ui component via the `quarkus-quinoa` extension.

### Building the image

It's assumed that the `mxm-server` and `mxm-ui` clones are sibling directories.

```bash
cd to/this/project/root/directory

./mvnw package
docker build -f src/main/docker/Dockerfile.jvm -t mbari/mxm-server .
```

## `mbari/mxm-postgres:x.y.z`

The MXM Postgres image is manually created and published as needed.

```bash
cd docker
docker build -f Dockerfile-postgres -t mbari/mxm-postgres:0.9.0-alpine .
docker push mbari/mxm-postgres:0.9.0-alpine
```

# Launching the MXM system

```bash
cd docker
```

Edit the `setenv.sh` file to indicate
the external URL where the MXM service will be accessible,
the desired password for the "mxm" user in the database,
and the host directory for the postgres database.

Review `docker-compose.yml` for any other further adjustments.

Then:

```bash
source setenv.sh
docker-compose up -d
docker logs -f --tail=20 mxm-server
```

The MXM UI will be available at <http://localhost:8080/>.

The GraphQL UI at: <http://localhost:8080/q/graphql-ui>.
