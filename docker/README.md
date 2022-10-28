# Building and publishing the images

**NOTE**: These notes combine both building and using the images.
General user-oriented instructions is a TODO.

The user-visible MXM version is the one set in `mxm-ui`'s `package.json` file.

## `mbari/mxm`

**NOTE**: Parent GitHub repo `mbari-org/mxm` takes care of building and publishing this image.
The following instructions are for reference only.

This image contains the mxm-server component and also the mxm-ui component,
which is incorporated via the `quarkus-quinoa` extension.

It's assumed that the `mxm-server` and `mxm-ui` clones are sibling directories.

In the following, assuming `MXM_VERSION` defines the version to be used.

```bash
cd to/this/project/root/directory

just mxm-image-build $MXM_VERSION
just mxm-image-push  $MXM_VERSION
```

## `mbari/mxm-postgres`

The MXM Postgres image is manually created and published as needed.

Note:

- Basic entities are preloaded into the database in this image. 
  Some of this information is taken from the TethysDash system.

- Using the same `$MXM_VERSION` value in general, but this image is not
  necessarily rebuilt with every new version of the main `mxm` image.

```bash
just mxm-image-postgres-build $MXM_VERSION
just mxm-image-postgres-push  $MXM_VERSION
```

# Launching the MXM system

```bash
cd docker
```

Edit the `setenv.sh` file to indicate
the external URLs (HTTP and WS) where the MXM service will be accessible,
the desired password for the "mxm" user in the database,
the host directory for the postgres database,
and other settings.

Review `docker-compose.yml` for any other further adjustments.

Then:

```bash
source setenv.sh
docker-compose up -d
docker logs -f --tail=20 mxm-server
```

The MXM UI will be available at <http://localhost:8080/>.

The GraphQL UI at: <http://localhost:8080/q/graphql-ui>.
