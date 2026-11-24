# Consumer service (AB-588)

`ftgo-consumer-service-main` runs the Consumer aggregate as a standalone Spring Boot application.
The monolith still loads the same consumer library, uses `ftgo.consumers`, and calls
`ConsumerService.validateOrderForConsumer` in process. The standalone runtime scans only
consumer entities and repositories.

## Run locally

Use Java 21 and the Gradle wrapper. On a fresh Compose MySQL instance, the init script
provisions `ftgo_consumer` and `ftgo_consumer_user`; it does not run again on an existing
MySQL data directory. For an existing instance, provision the schema and a schema-scoped
user before starting the service.

```sh
docker compose up -d --build --wait mysql
./gradlew :ftgo-consumer-service-main:bootJar
FTGO_CONSUMER_DB_PASSWORD=ftgo_consumer_pw java -jar ftgo-consumer-service-main/build/libs/ftgo-consumer-service-main.jar
```

The jar listens on port 8082 by default. Override the datasource with
`SPRING_DATASOURCE_URL` and `SPRING_DATASOURCE_USERNAME` as needed. The above password
is only for the local Compose user. Flyway runs at startup from
`classpath:db/consumer/migration`; Hibernate validates the resulting schema.

V1 preserves the monolith column definitions and uses the same
`@SequenceGenerator(sequenceName = "hibernate_sequence", allocationSize = 1)`. MySQL
implements it via the service-owned `ftgo_consumer.hibernate_sequence` table, seeded with
`next_val = 1`. Existing consumer rows can be copied without transformation; for a later
cutover ensure `next_val` is at least the source sequence's next value and greater than
the largest copied `consumers.id` before serving writes. This ticket does not move data
or route traffic.

## HTTP contract

| Method and path | Request and response |
| --- | --- |
| `POST /consumers` | Send `{"name":{"firstName":"Ada","lastName":"Lovelace"}}`. Returns `200` with `{"consumerId":1}` on an empty service schema. |
| `GET /consumers/{consumerId}` | Returns `200` with `{"consumerId":0,"name":{"firstName":"Ada","lastName":"Lovelace"}}` for an existing consumer or an empty `404` if missing. The response's `consumerId: 0` is the existing monolith quirk, not the persisted ID. Use the ID returned by create for lookup and validation. |
| `POST /consumers/{consumerId}/validate-order` | Standalone-only internal operation for the AB-592 remote adapter. Send the total as a decimal string, e.g. `{"orderTotal":"24.68"}`. Returns `204` when validation succeeds, `404` for a missing consumer, `400` for a missing or invalid total, or `422` if the domain rejects the order. No business validation rule was added: today an existing consumer passes. Do not expose this route through the public gateway. |

Run `./gradlew :ftgo-consumer-service-main:test` with Docker to verify startup and migration
against an empty MySQL schema, internal validation, and direct byte-for-byte parity with
the running monolith for the public requests.

## Wave 0 and architecture review

AB-588 provides only the consumer-specific deployable, MySQL init, and Flyway wiring that
overlap with AB-587; AB-587 can generalize the shared runtime and conventions later.
AB-586's proposed [ADR 0003 and ARB submission](https://github.com/COG-GTM/ftgo-monolith/pull/419)
already describe the deployable, schema, and internal contract. This is not ARB approval;
the epic still requires sign-off before merging to the default branch.
