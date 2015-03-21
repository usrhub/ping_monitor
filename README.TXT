This "Ping Monitor" is not intended nor ready for production.
(don't confuse with already existing projects, a unique name has to be found yet)

Copyright 2015 All rights reserved.
 
This software makes use of ICMP4J, available under LGPL: http://www.icmp4j.org

============================================================

1. clone project
	 git clone https://github.com/usrhub/ping_monitor
2. build project
	mvn build
3. run application
	mvn exec:java -Dexec.mainClass=de.steinerix.ping_monitor.gui.PingMonitorGUI

You can import the project as an existing maven project to eclipse.
Chose de.steinerix.ping_monitor.gui.PingMonitorGUI for your RunConfiguration.

Before editing the config.xml take a look at the comments, otherwise validation
 will help you maintain a valid config :-)

============================================================

Behave responsible and don't abuse this tool for flooding!