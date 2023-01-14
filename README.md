# Weaving plugin 
this project is just a simple maven plugin for eclipselink static weaving
during build time.
# this is project is based on [eclipselink-maven-plugin](https://github.com/ethlo/eclipselink-maven-plugin)
#
# usage
* clone or download the project run ```mvn install```.
* add the plugin to your pom.xml file.
* ```build>plugins```.
```xml
<plugin>
    <groupId>org.khaled.plugins.weaving</groupId>
    <artifactId>eclipselink-weaving</artifactId>
    <version>1.0.0</version>        
        <executions>
            <execution>
                <phase>process-classes</phase>
                <goals>
                <goal>weave</goal>                          
                </goals>
        </execution>
    </executions>
</plugin>
```