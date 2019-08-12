// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
// end::copyright[]

// tag::customConfig[]
package io.openliberty.guides.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.config.spi.ConfigSource;

public class CustomConfigSource implements ConfigSource {

    // !!! This doesn't work in Docker or in production!
    // just going to hard-code stuff for now in order to make progress w/ investigation...
    // String fileLocation = System.getProperty("user.dir").split("target")[0] + "resources/CustomConfigSource.json";

  @Override
  public int getOrdinal() {
    return Integer.parseInt(getProperties().get("config_ordinal"));
  }

  @Override
  public Set<String> getPropertyNames() {
    return getProperties().keySet();
  }

  @Override
  public String getValue(String key) {
    return getProperties().get(key);
  }

  @Override
  public String getName() {
    return "Custom Config Source:";
  }

  public Map<String, String> getProperties() {
    Map<String, String> m = new HashMap<String, String>();
    m.put("config_ordinal", "700");
    m.put("io_openliberty_guides_inventory_inMaintenance", "false");
    return m;
  }

}
// end::customConfig[]
