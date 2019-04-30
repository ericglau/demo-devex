# liberty:x Demo
This guide was adapted from the [Open Liberty MicroProfile Health Guide](https://openliberty.io/guides/microprofile-health.html).

## Overview of liberty:x mode
* hot deployment with background compilation mode so that java/resource/configuration file changes are picked up while your server is running
* built on top of the [ci.maven](https://github.com/WASdev/ci.maven) plugin

### Specific functionalities:
* default server starts on `mvn liberty:x` and stops on ctl-c
* java source file changes will be picked up dynamically (any java files in the `src/main/java` directory)
* server configuration file changes (any files in the config directory indicated in the pom.xml) will be picked up dynamically 
* resource file changes (any files in the `src/main/resources` directory)
* unit and integration tests on a seperate server and thread after every successful compile  
* debug port opened by default at port: 8787

## How to try out liberty:x mode
1. Clone the modified openliberty-health guide: 
1. Run `mvn compile liberty:x` to start liberty:x mode
1. Add mpHealth-1.0 feature to the server.xml, notice you can now access the /health endpoint (though it's just an empty array)
1. Create the [SystemHealth](https://raw.githubusercontent.com/OpenLiberty/guide-microprofile-health/master/finish/src/main/java/io/openliberty/guides/system/SystemHealth.java) class (notice changes seen at the /health endpoint) 
1. Notice tests are run automatically on a separate thread
1. Create the [InventoryHealth](https://raw.githubusercontent.com/OpenLiberty/guide-microprofile-health/master/finish/src/main/java/io/openliberty/guides/inventory/InventoryHealth.java) class (notice changes seen at the /health endpoint)
1. Change the `in_maintenance` property in `resources/CustomConfigSource.json` to true, notice the difference in the /health endpoint
1. Change the `config_ordinal` value to 800 in the `src/main/resources/META-INF/microprofile-config.properties` and notice the change is picked up and the /health endpoint changes again 
1. Make changes to the `src/main/webapp/index.html` (or any other webapp files) and notice that the home page changes
1. The port 8787 is opened by default for debugging, try connecting to it
1. When you are done use 'ctl-c' to terminate liberty:x mode and stop your server

## How to add liberty:x to an existing project

### To Build
1. Clone [ci.maven](https://github.ibm.com/mp-ls/ci.maven): `git clone git@github.ibm.com:mp-ls/ci.maven.git` and checkout "xMode" branch 
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
2. Ensure you have no compilation errors by running `mvn compile`
3. Provided you have no compilation errors, start liberty:x mode with `mvn liberty:x`
4. Make any code changes to java source files, resource files or configuration files and see that the changes are picked up dynamically while the server is running
5. Attach a debugger, by default the liberty:x mode allows for a debugger to attach to port: 8787.  Note: this will not work if you have a jvmOptions property set in your pom.xml 
6. When you are done use 'ctl-c' to terminate liberty:x mode and stop your server
