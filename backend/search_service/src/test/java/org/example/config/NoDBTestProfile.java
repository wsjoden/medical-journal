package org.example.config;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

public class NoDBTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "quarkus.datasource.devservices.enabled", "false",
                "quarkus.datasource.jdbc.url", "jdbc:postgresql://none",
                "quarkus.hibernate-orm.enabled", "false"
        );
    }
}