package net.chrisrichardson.ftgo.testutils.builders;

import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Consumer;

/**
 * Builder for creating {@link Consumer} instances in tests.
 *
 * <p>Usage:
 * <pre>{@code
 * Consumer consumer = ConsumerBuilder.aConsumer()
 *     .withFirstName("Jane")
 *     .withLastName("Smith")
 *     .build();
 * }</pre>
 *
 * <p>Or using a pre-built PersonName:
 * <pre>{@code
 * Consumer consumer = ConsumerBuilder.aConsumer()
 *     .withName(PersonNameBuilder.aPersonName().withFirstName("Jane").build())
 *     .build();
 * }</pre>
 */
public class ConsumerBuilder {

    private String firstName = "John";
    private String lastName = "Doe";
    private PersonName name;

    private ConsumerBuilder() {
    }

    public static ConsumerBuilder aConsumer() {
        return new ConsumerBuilder();
    }

    public ConsumerBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public ConsumerBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public ConsumerBuilder withName(PersonName name) {
        this.name = name;
        return this;
    }

    public Consumer build() {
        PersonName personName = this.name != null ? this.name : new PersonName(firstName, lastName);
        return new Consumer(personName);
    }
}
