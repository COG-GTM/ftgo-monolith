package net.chrisrichardson.ftgo.orderservice.api.web;

import java.time.LocalDateTime;

public record OrderAcceptance(LocalDateTime readyBy) {
}
