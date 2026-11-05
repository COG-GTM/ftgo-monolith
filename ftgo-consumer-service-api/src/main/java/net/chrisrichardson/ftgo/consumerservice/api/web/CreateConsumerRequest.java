package net.chrisrichardson.ftgo.consumerservice.api.web;

import net.chrisrichardson.ftgo.common.PersonName;

public record CreateConsumerRequest(PersonName name) {
}
