# Packet based Load Balancing Technique using SDN
The primary operation of this project is explained in the paper (pdf) uploaded in this repository.

The objective of this project is to balance the traffic load on the switches in an SDN enabled Data Center.
The topology is considered to be Fat-Tree topology which is commonly used in Data Centers. The topology is created using python and executed in Mininet Environment (see topo.py).

The controller used on this SDN project is Open Network Operating System (ONOS). The application is created by using the following link:
https://wiki.onosproject.org/display/ONOS/Template+Application+Tutorial

This link creates a simple application in an OSGI bundle. 

The actual code was replaced in /foo/src/main/java/org/foo/app/AppCommand.java file which can be referred in this repository, following the same sequence of the folders. Except this there is no change in the bundle created using the above mentioned link. 

The application bundle is foo.rar
