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
