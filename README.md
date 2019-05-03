# liberty:x Demo

[Video of Demo](https://ibm.box.com/s/go8y0v9ls3lgxfcwymdd0vd20kp586nj)

## Overview of liberty:x mode
* hot deployment with background compilation mode so that java, resource, and configuration file changes are picked up while your server is running
* built on top of the [ci.maven](https://github.com/WASdev/ci.maven) plugin

### Specific functionalities:
* default server starts on `mvn liberty:x` and stops on ctl-c
* java source file changes will be picked up dynamically (any java files in the `src/main/java` and `src/test/java` directory)
* server configuration file changes (any files in the config directory indicated in the pom.xml) will be picked up dynamically 
* resource file changes (any files in the `src/main/resources` directory)
* unit and integration tests on a seperate server and thread after every successful compile  
* debug port opened by default at port: 8787, works with any debugger (tested with VS Code and Eclipse)

## How to try out liberty:x mode
1. Clone this repo `git clone git@github.ibm.com:mp-ls/liberty-x-demo.git`

2. Run `mvn install liberty:x` to start liberty:x mode

3. Add mpHealth-1.0 feature to the server.xml, you can now access the http://localhost:9080/health endpoint (though it's just an empty array)

<details>
    <summary>4. Create the src/main/java/io/openliberty/guides/system/SystemHealth.java class.  Changes are reflected in the http://localhost:9080/health endpoint.  </summary>

```
package io.openliberty.guides.system;

import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Health
@ApplicationScoped
public class SystemHealth implements HealthCheck {
  @Override
  public HealthCheckResponse call() {
    if (!System.getProperty("wlp.server.name").startsWith("defaultServer")) {
      return HealthCheckResponse.named(SystemResource.class.getSimpleName())
                                .withData("default server", "not available").down()
                                .build();
    }
    return HealthCheckResponse.named(SystemResource.class.getSimpleName())
                              .withData("default server", "available").up().build();
  }
}
```
</details>


5. The console reflects integration tests are running automatically on a separate thread.

<details>
    <summary>6. Create the src/main/java/io/openliberty/guides/inventory/InventoryHealth.java class.  Changes are reflected in the http://localhost:9080/health endpoint. </summary>

```
package io.openliberty.guides.inventory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Health
@ApplicationScoped
public class InventoryHealth implements HealthCheck {
  @Inject
  InventoryConfig config;

  public boolean isHealthy() {
    if (config.isInMaintenance()) {
      return false;
    }
    try {
      String url = InventoryUtils.buildUrl("http", "localhost",
          Integer.parseInt(System.getProperty("default.http.port")),
          "/system/properties");
      Client client = ClientBuilder.newClient();
      Response response = client.target(url).request(MediaType.APPLICATION_JSON)
                                .get();
      if (response.getStatus() != 200) {
        return false;
      }
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public HealthCheckResponse call() {
    if (!isHealthy()) {
      return HealthCheckResponse.named(InventoryResource.class.getSimpleName())
                                .withData("services", "not available").down()
                                .build();
    }
    return HealthCheckResponse.named(InventoryResource.class.getSimpleName())
                              .withData("services", "available").up().build();
  }

}
```
</details>

7. Change the `in_maintenance` property in `resources/CustomConfigSource.json` to true.  Changes are reflected in the http://localhost:9080/health endpoint.

8. Change the `config_ordinal` value to 800 in the `src/main/resources/META-INF/microprofile-config.properties`. Changes are reflected in the http://localhost:9080/health endpoint.

9. Make changes to the `src/main/webapp/index.html` (or any other webapp files). Changes are reflected on the home page http://localhost:9080/.

<details>
    <summary>10. Create the src/test/java/it/io/openliberty/guides/health/HealthTest.java class as an integration test. The tests are run and should pass. </summary>
    
```
package it.io.openliberty.guides.health;

import static org.junit.Assert.assertEquals;
import java.util.HashMap;
import javax.json.JsonArray;
import org.junit.After;
import org.junit.Test;

public class HealthTest {

    private JsonArray servicesStates;
    private static HashMap<String, String> dataWhenServicesUP;
    private static HashMap<String, String> dataWhenInventoryDown;

    static {
        dataWhenServicesUP = new HashMap<String, String>();
        dataWhenInventoryDown = new HashMap<String, String>();

        dataWhenServicesUP.put("SystemResource", "UP");
        dataWhenServicesUP.put("InventoryResource", "UP");

        dataWhenInventoryDown.put("SystemResource", "UP");
        dataWhenInventoryDown.put("InventoryResource", "DOWN");
    }

    @Test
    public void testIfServicesAreUp() {
        servicesStates = HealthTestUtil.connectToHealthEnpoint(200);
        checkStates(dataWhenServicesUP, servicesStates);
    }

    @Test
    public void testIfInventoryServiceIsDown() {
        servicesStates = HealthTestUtil.connectToHealthEnpoint(200);
        checkStates(dataWhenServicesUP, servicesStates);
        HealthTestUtil.changeInventoryProperty(HealthTestUtil.INV_MAINTENANCE_FALSE, 
                                               HealthTestUtil.INV_MAINTENANCE_TRUE);
        servicesStates = HealthTestUtil.connectToHealthEnpoint(503);
        checkStates(dataWhenInventoryDown, servicesStates);
    }

    private void checkStates(HashMap<String, String> testData, JsonArray servStates) {
        testData.forEach((service, expectedState) -> {
            assertEquals("The state of " + service + " service is not matching.", 
                         expectedState, 
                         HealthTestUtil.getActualState(service, servStates));
        });
    }

    @After
    public void teardown() {
        HealthTestUtil.cleanUp();
    }

}
```
</details>

11. Connect to the debug port 8787 with a debugger.

12. When you are done use ctl-c to terminate liberty:x mode and stop your server

## How to add liberty:x to an existing project

### To Build
1. Clone the liberty:x development version of [ci.maven](https://github.ibm.com/mp-ls/ci.maven) on the xMode branch: `git clone -b xMode git@github.ibm.com:mp-ls/ci.maven.git`
2. Clone [ci.ant](https://github.com/WASdev/ci.ant): `git clone git@github.com:WASdev/ci.ant.git`
3. Build ci.ant `mvn clean install` and then ci.maven `mvn clean install` to generate `2.6.5-SNAPSHOT` of the liberty-maven plugin


### To Use 
1. In your pom.xml specify version `2.6.5-SNAPSHOT` for the liberty-maven-plugin 
```
<plugin>
    <groupId>net.wasdev.wlp.maven.plugins</groupId>
    <artifactId>liberty-maven-plugin</artifactId>
    <version>2.6.5-SNAPSHOT</version>
```
2. Ensure you have no compilation errors by running `mvn install`
3. Provided you have no compilation errors, start liberty:x mode with `mvn liberty:x`
4. Make any code changes to java source files, resource files or configuration files and see that the changes are picked up dynamically while the server is running
5. Attach a debugger, by default the liberty:x mode allows for a debugger to attach to port: 8787.  Note: this will not work if you have a jvmOptions property set in your pom.xml 
6. When you are done use 'ctl-c' to terminate liberty:x mode and stop your server
