package net.chrisrichardson.ftgo.jpa;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@AutoConfiguration
@EntityScan(basePackages = "net.chrisrichardson.ftgo.jpa")
public class FtgoJpaConfiguration {
}
