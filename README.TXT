This "Ping Monitor" is not intended nor ready for production.  (don't
confuse with already existing projects, a unique name has to be found
yet)

Copyright 2015 All rights reserved.
 
This software makes use of ICMP4J which is available under LGPL:
http://www.icmp4j.org

============================================================

"Ping Monitor" enables you to watch up to 20 IPs for availability and
responsiveness.  It utilises ICMP4J and relies therefore on native
ping support on Linux and Windows.  You can configure each watched
device individually (e. g. ping interval, warning limit, and email for
warning notifications).

The GUI renders live charts for the measured ping times of all
devices. Limit violations and connection / routing problems are easily
visible. In addition to this the mail function can be activated in
order to inform the responsible person via mail.  (An alarm message is
sent when the same problem occurs for three pings in a row, the alarm
state is cleared after three succeeding pings)

"Ping Monitor" can be run headless without GUI. It writes all
important events and error messages in a log file.

============================================================

"Ping Monitor" was tested with Java8 on Xubuntu 14.04 (64 Bit) Win 7
(32 Bit) within a VirtualBox

============================================================

1. clone project 
 git clone https://github.com/usrhub/ping_monitor.git

2. build project
 mvn build

3a) run application
 mvn exec:java -Dexec.mainClass=de.steinerix.ping_monitor.gui.PingMonitorGUI

 mvn exec:java -Dexec.mainClass=de.steinerix.ping_monitor.gui.Headless

3b) run application (from jar archive)
 mvn package 

Copy the resulting fat jar (with dependencies) to a folder of your
choice and don't forget to put the config.xml and the config.xsd in
the same folder.

 run with: java -jar ping_monitor-0.2-jar-with-dependencies.jar

(note: the config.xml is also stored in the jar. Currently it is there
without effect, you need to place a config.xml and config.xsd in the
WD of the jar.)

4. import to eclipse
You can import the project as an existing maven project to eclipse.
Choose de.steinerix.ping_monitor.gui.PingMonitorGUI for your Run
Configuration.

5. configure
Before editing the config.xml take a look at the comments, otherwise
validation will help you maintain a valid config :-)

============================================================

Behave responsible and don't abuse this tool for flooding!
