Vaadin Sass Compiler jsass version
==================================
This version has been hacked to use the jsass compiler http://jsass.rtfd.org/ 
https://github.com/bit3/jsass .The compiler automatically 
outputs css.map files so that one can find the source (scss) of the css.
If you have resources in your theme the urls need to be changed so that 
they are relative to the generated styles.css.

This project has previously a part of the Vaadin Framework

Building the project
====================
Running

  mvn package

will compile the project and run all tests

Including in your project
=========================
run
  mvn install
  
then include the built maven dependency 
```xml
        <dependency>
            <groupId>com.vaadin</groupId>
            <artifactId>vaadin-sass-compiler</artifactId>
            <version>1.0.0.jsass.20170930.1</version>
        </dependency>
```

Contributing
=============
Your contributions are more than welcome. Please read
https://vaadin.com/contribute for more information on practicalities.
Because we are using Gerrit we cannot accept pull requests in GitHub.

License
=======

Copyright 2012 Vaadin Ltd.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License. You may obtain a copy of the
License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed
under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.
