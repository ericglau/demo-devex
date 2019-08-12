package it.io.openliberty.guides.config;

import org.eclipse.microprofile.system.test.SharedContainerConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.microprofile.ComposedMicroProfileApplication;
import org.testcontainers.containers.microprofile.MicroProfileApplication;
import org.testcontainers.junit.jupiter.Container;

public class AppConfig implements SharedContainerConfiguration {

    @Container
    public static MicroProfileApplication<?> app = new ComposedMicroProfileApplication<>()
            .withAppContextRoot("/")
            //.withReadinessPath("/health/readiness"); // mpHealth-2.0 is broken and will be fixed in 19008
            .withReadinessPath("/system/properties");

}
