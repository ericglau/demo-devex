//tag::copyright[]
/*******************************************************************************
* Copyright (c) 2017, 2019 IBM Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     IBM Corporation - initial API and implementation
*******************************************************************************/
// end::copyright[]
package it.io.openliberty.guides.system;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import javax.inject.Inject;

import org.eclipse.microprofile.system.test.jupiter.MicroProfileTest;
import org.eclipse.microprofile.system.test.SharedContainerConfig;
import org.junit.jupiter.api.Test;

import io.openliberty.guides.system.SystemResource;
import it.io.openliberty.guides.config.AppConfig;

@MicroProfileTest
@SharedContainerConfig(AppConfig.class)
public class SystemEndpointTest {
    
    @Inject
    public static SystemResource systemSvc;

    @Test
    public void testGetProperties() {
        Map<String, String> obj = systemSvc.getProperties().readEntity(Map.class);
        assertTrue("Linux".equals(obj.get("os.name")) || System.getProperty("os.name").equals(obj.get("os.name")),
                "The system property for the local and remote JVM should match, or be Linux (for a Docker env)");
    }

}
