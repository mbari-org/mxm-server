## Misc notes

### Tools

#### Hasura gq (GraphQurl)

<https://github.com/hasura/graphqurl>

```
npm install -g graphqurl

$ gq http://localhost:8085/graphql \
     -q '{ allProviders { providerId httpEndpoint description} }' | jq .
```

To open GraphiQL interface including an Explorer:

```
gq http://localhost:8085/graphql -i
```

(The Quarkus based GraphiQL UI doesn't provide such explorer feature.)

#### curlie

<https://til.simonwillison.net/graphql/graphql-with-curl>

```
$ curlie post http://localhost:8085/graphql -s \
-d "$(jq -c -n --arg query '
{
   allProviders {
     providerId
     httpEndpoint
   }
}' '{"query":$query}')"
```
