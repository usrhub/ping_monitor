# Ping Monitor - _Watch your digital neighbourhood_

This "Ping Monitor" is not intended nor ready for production. Use at
your own risk. Don't abuse this tool for flooding.

Copyright 2015 All rights reserved.

This software makes use of ICMP4J http://www.icmp4j.org

## Description

"Ping Monitor" enables you to watch up to 20 IPs for availability and
responsiveness. It utilises ICMP4J and relies therefore on native
ping support on Linux and Windows. You can configure each watched
device individually (e. g. ping interval, warning limit, and email for
warning notifications).
The GUI renders live charts for the measured ping times of all
devices. Limit violations and connection / routing problems are easily
visible. In addition to this the mail function can be activated in
order to inform the responsible person via mail. (An alarm message is
sent when the same problem occurs for three pings in a row, the alarm
state is cleared after three succeeding pings)
"Ping Monitor" can be run headless without GUI. It writes all
important events and error messages in a log file.

## Tested environments
- Xubuntu 14.04 (64 Bit), Java 8 - [screenshot](https://raw.githubusercontent.com/usrhub/ping_monitor/master/sreenshot.jpg)
- Win 7 (32 Bit) within a VirtualBox, Java 8 - [screenshot](https://raw.githubusercontent.com/usrhub/ping_monitor/master/sreenshot-windows.jpg)

## Run ping monitor

1. clone project
 - git clone https://github.com/usrhub/ping_monitor.git

2. build project
 - mvn package

3. run application
  - mvn exec:java -Dexec.mainClass=de.steinerix.ping_monitor.gui.PingMonitorGUI
  - mvn exec:java -Dexec.mainClass=de.steinerix.ping_monitor.gui.Headless

4. run application from jar
  - copy config.xml and config.xsd to the working directory of the jar file
  - start with: java -jar ping_monitor-0.2-jar-with-dependencies.jar

5. configuration
  - configuration is done within config.xml
  - obey comments

6. import to eclipse
  - You can import the project as an existing maven project to eclipse.
  - Choose de.steinerix.ping_monitor.gui.PingMonitorGUI for your Run
Configuration.
