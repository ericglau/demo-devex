// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2017, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
// end::copyright[]
// tag::testClass[]
package it.io.openliberty.guides.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.system.test.jupiter.MicroProfileTest;
import org.eclipse.microprofile.system.test.SharedContainerConfig;
import org.junit.jupiter.api.Test;

import io.openliberty.guides.inventory.InventoryResource;
import io.openliberty.guides.system.SystemResource;
import it.io.openliberty.guides.config.AppConfig;

@MicroProfileTest
@SharedContainerConfig(AppConfig.class)
public class InventoryEndpointTest {
    
    @Inject
  public static InventoryResource inventory;
    
    @Inject
  public static SystemResource system;

  // tag::tests[]
  // tag::testSuite[]
  @Test
  public void testSuite() {
    //this.testEmptyInventory();
    this.testHostRegistration();
    this.testSystemPropertiesMatch();
    this.testUnknownHost();
  }
  // end::testSuite[]

  // tag::testEmptyInventory[]
  // TODO: temporarily disabled to allow tests to be re-run on the same server 
  /*public void testEmptyInventory() {
    Response response = this.getResponse(baseUrl + INVENTORY_SYSTEMS);
    this.assertResponse(baseUrl, response);

    JsonObject obj = response.readEntity(JsonObject.class);

    int expected = 0;
    int actual = obj.getInt("total");
    assertEquals("The inventory should be empty on application start but it wasn't",
                 expected, actual);

    response.close();
  }*/
  // end::testEmptyInventory[]

    // tag::testHostRegistration[]
    public void testHostRegistration() {
        inventory.getPropertiesForHost("localhost");

        Map<String, Object> systems = inventory.listContents().readEntity(Map.class);

        assertEquals("1", systems.get("total").toString(), "The inventory should have one entry for localhost");

        ArrayList<Map<String, String>> localhostProps = (ArrayList<Map<String, String>>) systems.get("systems");
        assertEquals("localhost", localhostProps.get(0).get("hostname"),
                "A host was registered, but it was not localhost");
    }
    // end::testHostRegistration[]

    // tag::testSystemPropertiesMatch[]
    public void testSystemPropertiesMatch() {
        Map<String, String> inventoryProps = inventory.getPropertiesForHost("localhost").readEntity(Map.class);
        Map<String, String> systemProps = system.getProperties().readEntity(Map.class);

        assertTrue("Linux".equals(inventoryProps.get("os.name")) || System.getProperty("os.name").equals(inventoryProps.get("os.name")),
                "Expected 'os.name' to be 'Linux' (docker env) or " + System.getProperty("os.name") + ", but was " + inventoryProps.get("os.name"));

        assertTrue("default".equals(inventoryProps.get("user.name")) || System.getProperty("user.name").equals(inventoryProps.get("user.name")),
                "Expected 'user.name' to be 'default' (docker env) or " + System.getProperty("user.name") + ", but was " + inventoryProps.get("user.name"));
        assertEquals(inventoryProps.get("user.name"), systemProps.get("user.name"));
    }
    // end::testSystemPropertiesMatch[]

    // tag::testUnknownHost[]
    public void testUnknownHost() {
        Response r = inventory.getPropertiesForHost("badhostname");
        assertEquals(404, r.getStatus());
        String msg = r.readEntity(String.class);
        assertTrue(msg.contains("ERROR"), "Did not find 'ERROR' token in response: " + msg);
    }

  // end::testUnknownHost[]
  // end::tests[]
}
// end::testClass[]
