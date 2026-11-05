# AB-566: Swagger 2 → OpenAPI 3 comparison

Base: `origin/devin/ab-561-java21` at `2d9d9663` (Boot 2.7.18).
Reference: AB-563 `docs/upgrade/java21/api-baseline.json`.
Replacement: springdoc-openapi-ui **1.8.0**, not the Boot 3 starter.

## Reproduce

Build and run the application on port 8081 with the migrated MySQL database
(see the environment blueprint). Then, from the repository root:

```sh
curl -fsS localhost:8081/v3/api-docs -o "$HOME/ab-566-openapi.json"
python3 docs/upgrade/java21/compare-openapi.py "$HOME/ab-566-openapi.json"
```

Result:

```text
PASS: 24 paths, 25 operations, 26 schemas unchanged
PASS: 8 request bodies and 25 documented response schemas unchanged
```

The script fails on added/removed operations, changed component schemas, or
changed request/response schemas. It compares all six controller families:
orders, consumers, restaurants, couriers, API tracking, and Sentry configuration.
The checked-in baseline is not modified.

## Expected documentation differences

- Swagger `2.0` becomes OpenAPI `3.0.1`; `/v2/api-docs` is replaced by
  `/v3/api-docs` (approved endpoint change, no v2 alias).
- `definitions` becomes `components.schemas`, with corresponding `$ref` changes.
  Springfox's redundant schema `title` fields disappear. All 26 schemas otherwise
  match exactly, including property names, types, formats, enums, and references.
- Body parameters become `requestBody.content`; response `schema` becomes
  `content[mediaType].schema`. `/config/sentry` now explicitly documents arbitrary
  object-valued map entries (`additionalProperties: {type: object}`), previously
  implicit in `type: object`. The script normalizes only this equivalent map
  representation, titles, and reference prefixes.
- Operation IDs, summaries, tags/descriptions, default API info and server metadata
  are generator metadata, not stable REST contracts. The documented response-code
  sets are unchanged. Runtime error bodies remain covered by AB-563 golden tests.

## Compatibility / AB-570 handoff

- `/swagger-ui.html` returns 302 to `/swagger-ui/index.html` (200); scripts and
  the environment blueprint remain unchanged.
- The `ant_path_matcher` property and handler-mapping BeanPostProcessor are gone.
  Boot 2.7's parsed patterns work with springdoc. `/orders/?consumerId=999999`
  still returns 200 with `[]`; trailing-slash matching must be preserved explicitly
  during the later Spring 6 cut-over.
- Existing `ResponseEntity` responses are unwrapped in the generated spec.
  `OpenApiDocumentationTest` additionally covers nested CompletableFuture and
  DeferredResult response wrappers; neither async wrapper is currently used by
  production controllers. No custom alternate-type rules are necessary.
- The package filter is `springdoc.packages-to-scan=net.chrisrichardson.ftgo`.
  AB-570 must replace the 1.x artifact with the appropriate 2.x Boot 3 starter and
  rerun this comparison, golden tests, and e2e tests after the Jakarta migration.
