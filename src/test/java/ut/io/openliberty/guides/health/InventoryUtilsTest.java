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
package ut.io.openliberty.guides.health;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import io.openliberty.guides.inventory.InventoryUtils;

public class InventoryUtilsTest {

  @Test
  public void testBuildUrl() {
    assertEquals("http://localhost:9080/mypath", InventoryUtils.buildUrl("http", "localhost", 9080, "/mypath"));
  }
}
