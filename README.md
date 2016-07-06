# SPRING-I18N-CONVERTER
Spring's message_?(\[a-zA-Z_\]+)?.properties convert to JS file

* message.properties & default.js
![capture](https://raw.githubusercontent.com/tinywind/SPRING-I18N-CONVERTER/master/capture1.png "comment")
* message_ko_KR.properties & ko_KR.js 
![capture](https://raw.githubusercontent.com/tinywind/SPRING-I18N-CONVERTER/master/capture2.png "comment")
* message_ko_KR.properties & ko_KR.xlsx 
![capture](https://raw.githubusercontent.com/tinywind/SPRING-I18N-CONVERTER/master/capture3.png "comment")

# Features
* **messages.properties** to **{locale}.js** : refer https://github.com/tinywind/SPRING-I18N-CONVERTER/tree/master/apply-converted-js
* **{locale}.js** to **messages.properties**
* **messages.properties** to **{locale}.xlsx** 
* **{locale}.xlsx** to **messages.properties** 

# configure maven
    <build>
        <plugins>
            <plugin>
                <groupId>org.tinywind</groupId>
                <artifactId>spring-i18n-converter-maven</artifactId>
                <version>0.1.1</version>
                <executions>
                    <execution>
                        <phase>generate-sources</phase>
                        <goals>
                            <goal>generate</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <sources>
                        <source>
                            <converter>org.tinywind.springi18nconverter.converter.JavascriptConverter</converter>
                            <toMessageProperties>false</toMessageProperties>
                            <sourceDir>${project.basedir}/src/main/resources/i18n</sourceDir>
                            <targetDir>${project.basedir}/src/main/webapp/!resources/js/i18n</targetDir>
                            <targetEncoding>UTF-8</targetEncoding>
                            <describeByNative>true</describeByNative>
                            <overwrite>true</overwrite>
                        </source>
                        <source>
                            <converter>org.tinywind.springi18nconverter.converter.ExcelConverter</converter>
                            <toMessageProperties>false</toMessageProperties>
                            <sourceDir>${project.basedir}/src/main/resources/i18n</sourceDir>
                            <targetDir>${project.basedir}/doc</targetDir>
                            <targetEncoding>UTF-8</targetEncoding>
                            <describeByNative>true</describeByNative>
                            <overwrite>true</overwrite>
                        </source>
                    </sources>
                </configuration>
            </plugin>
        </plugins>
    </build>

# result
    "C:\Program Files\Java\jdk1.8.0_73\bin\java" "-Dmaven.home=C:\Program Files (x86)\JetBrains\IntelliJ IDEA 2016.1.1\plugins\maven\lib\maven3" "-Dclassworlds.conf=C:\Program Files (x86)\JetBrains\IntelliJ IDEA 2016.1.1\plugins\maven\lib\maven3\bin\m2.conf" -Didea.launcher.port=7540 "-Didea.launcher.bin.path=C:\Program Files (x86)\JetBrains\IntelliJ IDEA 2016.1.1\bin" -Dfile.encoding=UTF-8 -classpath "C:\Program Files (x86)\JetBrains\IntelliJ IDEA 2016.1.1\plugins\maven\lib\maven3\boot\plexus-classworlds-2.4.jar;C:\Program Files (x86)\JetBrains\IntelliJ IDEA 2016.1.1\lib\idea_rt.jar" com.intellij.rt.execution.application.AppMain org.codehaus.classworlds.Launcher -Didea.version=2016.1.3 org.tinywind:spring-i18n-converter-maven:0.1.1:generate -P local
    [INFO] Scanning for projects...
    [WARNING] 
    [WARNING] Some problems were encountered while building the effective model for iruseum:tenbillion:war:0.0.1-SNAPSHOT
    [WARNING] 'dependencies.dependency.(groupId:artifactId:type:classifier)' must be unique: org.apache.tomcat.embed:tomcat-embed-jasper:jar -> duplicate declaration of version ${org.apache.tomcat.version} @ line 100, column 21
    [WARNING] 
    [WARNING] It is highly recommended to fix these problems because they threaten the stability of your build.
    [WARNING] 
    [WARNING] For this reason, future Maven versions might no longer support building such malformed projects.
    [WARNING] 
    [INFO]                                                                         
    [INFO] ------------------------------------------------------------------------
    [INFO] Building Iruseum Tenbillion Webapp 0.0.1-SNAPSHOT
    [INFO] ------------------------------------------------------------------------
    [INFO] 
    [INFO] --- spring-i18n-converter-maven:0.1.1:generate (default-cli) @ tenbillion ---
       converted: C:\Users\tinywind\IdeaProjects\guidemon\src\main\resources\i18n\messages.properties -> C:\Users\tinywind\IdeaProjects\guidemon\src\main\webapp\!resources\js\i18n\default.js
       converted: C:\Users\tinywind\IdeaProjects\guidemon\src\main\resources\i18n\messages_ko.properties -> C:\Users\tinywind\IdeaProjects\guidemon\src\main\webapp\!resources\js\i18n\ko.js
       converted: C:\Users\tinywind\IdeaProjects\guidemon\src\main\resources\i18n\messages.properties -> C:\Users\tinywind\IdeaProjects\guidemon\doc\default.xlsx
       converted: C:\Users\tinywind\IdeaProjects\guidemon\src\main\resources\i18n\messages_ko.properties -> C:\Users\tinywind\IdeaProjects\guidemon\doc\ko.xlsx
    [INFO] Complete SPRING-I18N-CONVERTER
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    [INFO] Total time: 0.834s
    [INFO] Finished at: Tue Jun 28 02:43:18 KST 2016
    [INFO] Final Memory: 8M/245M
    [INFO] ------------------------------------------------------------------------
    
    Process finished with exit code 0


# LICENSE
**The MIT License (MIT)**