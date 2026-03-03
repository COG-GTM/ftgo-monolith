package net.chrisrichardson.ftgo.testutils.builders;

import net.chrisrichardson.ftgo.common.PersonName;

/**
 * Builder for creating {@link PersonName} instances in tests.
 *
 * <p>Usage:
 * <pre>{@code
 * PersonName name = PersonNameBuilder.aPersonName()
 *     .withFirstName("John")
 *     .withLastName("Doe")
 *     .build();
 * }</pre>
 */
public class PersonNameBuilder {

    private String firstName = "John";
    private String lastName = "Doe";

    private PersonNameBuilder() {
    }

    public static PersonNameBuilder aPersonName() {
        return new PersonNameBuilder();
    }

    public PersonNameBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public PersonNameBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public PersonName build() {
        return new PersonName(firstName, lastName);
    }
}
