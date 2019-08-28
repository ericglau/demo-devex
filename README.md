# liberty:dev Demo

## Overview of liberty:dev mode
* hot deployment with background compilation mode so that added pom dependencies, java, resource, and configuration file changes are picked up while your server is running
* built on top of the [ci.maven](https://github.com/OpenLiberty/ci.maven) plugin

### Documentation
https://github.com/OpenLiberty/ci.maven/blob/master/docs/dev.md

### Specific functionalities:
* default server starts on `mvn liberty:dev` and stops on ctl-c
* java source file changes will be picked up dynamically (any java files in the `src/main/java` and `src/test/java` directory)
* server configuration file changes (any files in the config directory indicated in the pom.xml) will be picked up dynamically 
* resource file changes (any files in the `src/main/resources` directory)
* unit and integration tests run on a seperate thread after every successful compile  
* feature dependency changes in pom.xml are picked up dynamically, triggers feature installation
* debug port opened by default at port: 7777, works with any debugger

## How to try out liberty:dev mode
1. Clone this repo.

2. Run `mvn liberty:dev` to start liberty:dev mode

3. Enable the `microprofile-health-api` dependency in the pom.xml.  Notice that the new dependency gets automatically installed.

4. Add `mpHealth-2.0` feature to the server.xml, you can now access the http://localhost:9080/health endpoint (though it's just an empty array)

<details>
    <summary>5. Create the src/main/java/io/openliberty/guides/system/SystemLivenessCheck.java class.  Changes are reflected in the http://localhost:9080/health endpoint.  </summary>

```
package io.openliberty.guides.system;

import javax.enterprise.context.ApplicationScoped;

import java.lang.management.MemoryMXBean;
import java.lang.management.ManagementFactory;

import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Liveness
@ApplicationScoped
public class SystemLivenessCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        long memUsed = memBean.getHeapMemoryUsage().getUsed();
        long memMax = memBean.getHeapMemoryUsage().getMax();
  
        return HealthCheckResponse.named(
            SystemResource.class.getSimpleName() + " liveness check")
                                  .withData("memory used", memUsed)
                                  .withData("memory max", memMax)
                                  .state(memUsed < memMax * 0.9).build();
    }
    
}
```
</details>


6. Go to the console where you started dev mode, and press Enter.  The integration tests are run on a separate thread while dev mode is still active.

<details>
    <summary>7. Create the src/main/java/io/openliberty/guides/system/SystemReadinessCheck.java class.  Changes are reflected in the http://localhost:9080/health endpoint. </summary>

```
package io.openliberty.guides.system;

import javax.enterprise.context.ApplicationScoped;

import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

@Readiness
@ApplicationScoped
public class SystemReadinessCheck implements HealthCheck {

    @Inject
    @ConfigProperty(name = "io_openliberty_guides_system_inMaintenance")
    Provider<String> inMaintenance;
	
    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named(
		SystemResource.class.getSimpleName() + " readiness check");
        if (inMaintenance != null && inMaintenance.get().equalsIgnoreCase("true")) {
            return builder.withData("services", "not available").down().build();
        }
        return builder.withData("services", "available").up().build();
    }
    
}
```
</details>

8. Change the `io_openliberty_guides_system_inMaintenance` property in `resources/CustomConfigSource.json` to true.  Changes are reflected in the http://localhost:9080/health endpoint.


9. Change the `config_ordinal` value to 800 in the `src/main/resources/META-INF/microprofile-config.properties`. Changes are reflected in the http://localhost:9080/health endpoint. Undo steps 8 and 9 afterwards.


10. Make changes to the `src/main/webapp/index.html` (or any other webapp files). Changes are reflected on the home page http://localhost:9080/.

<details>
    <summary>11. Create the src/test/java/io/openliberty/guides/health/HealthEndpointIT.java class as an integration test. Press Enter in the console. The tests are run and should pass. </summary>
    
```
package io.openliberty.guides.health;

import static org.junit.Assert.assertEquals;

import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.provider.jsrjsonp.JsrJsonpProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class HealthEndpointIT {
    
    private static String baseUrl;
    private static final String HEALTH_ENDPOINT = "/health";
    private static final String LIVENESS_ENDPOINT = "/health/live";
    private static final String READINESS_ENDPOINT = "/health/ready";
    
    private Client client;
    private Response response;
    
    @BeforeClass
    public static void oneTimeSetup() {
        String port = System.getProperty("liberty.http.port", "9080");
        baseUrl = "http://localhost:" + port;
    }
    
    @Before
    public void setup() {
        response = null;
        client = ClientBuilder.newClient();
        client.register(JsrJsonpProvider.class);
    }
    
    @After
    public void teardown() {
        response.close();
        client.close();
    }

    @Test
    public void testHealthEndpoint() {
        String healthURL = baseUrl + HEALTH_ENDPOINT;
        response = this.getResponse(baseUrl + HEALTH_ENDPOINT);
        this.assertResponse(healthURL, response);
        
        JsonObject healthJson = response.readEntity(JsonObject.class);
        String expectedOutcome = "UP";
        String actualOutcome = healthJson.getString("status");
        assertEquals("Application should be healthy", expectedOutcome, actualOutcome);
       
        JsonObject healthCheck = healthJson.getJsonArray("checks").getJsonObject(0);
        String healthCheckName = healthCheck.getString("name");
        actualOutcome = healthCheck.getString("status");
        assertEquals(healthCheckName + " wasn't healthy", expectedOutcome, actualOutcome);

        healthCheck = healthJson.getJsonArray("checks").getJsonObject(1);
        healthCheckName = healthCheck.getString("name");
        actualOutcome = healthCheck.getString("status");
        assertEquals(healthCheckName + " wasn't healthy", expectedOutcome, actualOutcome);
    }

    @Test
    public void testLivenessEndpoint() {
        String livenessURL = baseUrl + LIVENESS_ENDPOINT;
        response = this.getResponse(baseUrl + LIVENESS_ENDPOINT);
        this.assertResponse(livenessURL, response);
        
        JsonObject healthJson = response.readEntity(JsonObject.class);
        String expectedOutcome = "UP";
        String actualOutcome = healthJson.getString("status");
        assertEquals("Applications liveness check passed", expectedOutcome, actualOutcome);
    }

    @Test
    public void testReadinessEndpoint() {
        String readinessURL = baseUrl + READINESS_ENDPOINT;
        response = this.getResponse(baseUrl + READINESS_ENDPOINT);
        this.assertResponse(readinessURL, response);
        
        JsonObject healthJson = response.readEntity(JsonObject.class);
        String expectedOutcome = "UP";
        String actualOutcome = healthJson.getString("status");
        assertEquals("Applications readiness check passed", expectedOutcome, actualOutcome);
    }
   
    private Response getResponse(String url) {
        return client.target(url).request().get();
    }

    private void assertResponse(String url, Response response) {
        assertEquals("Incorrect response code from " + url, 200, response.getStatus());
    }

}
```
</details>

12. Connect to the debug port 7777 with a debugger.

13. When you are done use ctl-c to terminate liberty:dev mode and stop your server

## How to use liberty:dev in an existing project

### (Optional) To Build from Source
1. Clone [ci.ant](https://github.com/OpenLiberty/ci.ant): `git clone https://github.com/OpenLiberty/ci.ant.git`
2. Clone [ci.common](https://github.com/OpenLiberty/ci.common): `git clone https://github.com/OpenLiberty/ci.common.git`
3. Clone [ci.maven](https://github.com/OpenLiberty/ci.maven): `git clone https://github.com/OpenLiberty/ci.maven.git`
4. Build ci.ant `mvn clean install -DskipTests`, then ci.common `mvn clean install`, and then ci.maven `mvn clean install` to generate `3.0-M2-SNAPSHOT` of the liberty-maven-plugin

Or in one command from an empty directory:
```
git clone https://github.com/OpenLiberty/ci.ant.git && cd ci.ant && mvn clean install -DskipTests && cd .. && git clone https://github.com/OpenLiberty/ci.common.git && cd ci.common && mvn clean install && cd .. && git clone https://github.com/OpenLiberty/ci.maven.git && cd ci.maven && mvn clean install && cd ..
```

### To Use 
1. Do one of the following:  
   a) Perform the steps in the "To Build from Source", then in your pom.xml specify the following for the liberty-maven-plugin:
```
<plugin>
    <groupId>io.openliberty.tools</groupId>
    <artifactId>liberty-maven-plugin</artifactId>
    <version>3.0-M2-SNAPSHOT</version>
</plugin>
```
*or*  
   b) In your pom.xml specify version `3.0.M1` for the liberty-maven-plugin, which will use the milestone release from Maven Central  
e.g.  
```
<plugin>
    <groupId>net.wasdev.wlp.maven.plugins</groupId>
    <artifactId>liberty-maven-plugin</artifactId>
    <version>3.0.M1</version>
</plugin>
```
2. Start liberty:dev mode with `mvn liberty:dev`
3. Make any code changes to java source files, resource files or configuration files and see that the changes are picked up dynamically while the server is running
4. Attach a debugger, by default the liberty:dev mode allows for a debugger to attach to port: 7777.  Note: this will not work if you have a jvmOptions property set in your pom.xml 
5. When you are done use 'ctl-c' to terminate liberty:dev mode and stop your server
