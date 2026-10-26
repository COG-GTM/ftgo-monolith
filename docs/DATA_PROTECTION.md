# Data protection: the application-to-database path

The `orders` table holds regulated consumer data: `PaymentInformation.paymentToken` and the
`DeliveryInformation` street/city/state/zip of the consumer (see
`ftgo-domain/src/main/java/net/chrisrichardson/ftgo/domain/Order.java`). Everything below applies to
the connection that carries that data.

## Transport

The datasource URL in `ftgo-application/src/main/resources/application.properties` and the
`SPRING_DATASOURCE_URL` in `docker-compose.yml` require TLS:

```
useSSL=true&requireSSL=true&allowPublicKeyRetrieval=false
```

`DB_USE_SSL` / `DB_REQUIRE_SSL` exist only so a local developer can opt out against a throwaway
database. Do not set either to `false` in any environment that holds real consumer data, and do not
re-enable `allowPublicKeyRetrieval`, which lets a party on the network path obtain the server public
key during authentication.

## Credentials

`DB_USERNAME` / `DB_PASSWORD` (or `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD`) must
be injected from the deployment's secret store. The values checked into this repository are local
development defaults for the throwaway `docker-compose` MySQL instance only; they are not valid
credentials for any environment holding real data, and no real credential may be committed here.

## Logging

`logging.level.org.hibernate.SQL=DEBUG` logs statement text with bound parameters replaced by `?`.
Do not raise Hibernate binder logging (`org.hibernate.type.descriptor.sql`) to `TRACE` in an
environment with real data: that logs parameter values, which would write payment tokens and
delivery addresses into application logs.
