
# ONOS fwdask
Packet forwarding facility for ONOS with user-feedback and prompt.

# Installation
To install the plug-in place all this project in the ``/apps``
directory of the onos project.

Once you have that in place you can edit the ``pom.xml`` file to
include this plug-in in the package building procedure adding fwdask
to the compile modules.  The results will look like the this
configuration:

```
...
<modules>
    <module>aaa</module>
    ...
    <module>fwdask</module>
    ...
</modules>
...
```

Next you have to compile the source with maven.  In the apps/fwdask
Directory use maven to compile the app:

```
ubuntu@sdnhubvm:~/onos/apps/fwdask[00:46] (master)$ pwd
/home/ubuntu/onos/apps/fwdask
ubuntu@sdnhubvm:~/onos/apps/fwdask[00:46] (master)$ mvn clean
ubuntu@sdnhubvm:~/onos/apps/fwdask[00:46] (master)$ mvn install
```

Then you can install the app in the onos system.  With onos running
uninstall every previous installation of this app:

```
onos>  app uninstall org.onosapp
```

Then, from this app directory, install the app in the running onos
system:
```
ubuntu@sdnhubvm:~/onos/apps/fwdask[00:46] (master)$ ../../tools/dev/bin/onos-app localhost install target/onos-apps-fwdask-1.3.0-SNAPSHOT.oar
```

After that you can finally activate the app in the onos system:
```
onos>  app activate org.onos.openflow
onos>  app activate it.unibo.disisec.onos.fwdask
```

# Usage
To intercept and change the behaviour of the switchs you must connect to
the onos listening port (default 50000, this is configurable using the
configurationPort option).

After that you will recieve a description for every new flow present in
the network.  The system will register your choice and then will not ask
you anymore if the same flow is detected.

## conntrack
You can enable a rough deep packet inspection to separate the network
level (and thus the transport layer) from the data-link one to accept
flows on a connection-based information.  This feature is normally
disabled and you can enable it using the connTrackMode option.

# Composition
The app is composed of two main java class:

## ReactiveForwardingAsk.java
This is the main class which process the incoming packets.  This is a
modfication of the fwd app to ask to the user on a certain point.  The
modification to the logic of the main class are minimal the main
introduction are the function used to invoke the user prompt.

## UserConfigurator.java
This class is the class responsible of prompting to the user if the
system should block or let the packet flow trough (installing a new
flow rule on the switches trough the ReactiveForwardingAsk class).

To save the previously replied flows the class have a global HashMap
with the sha256-hashed packet information as the key.

Every time a new packet is processed the class check if the packet have
already a pre-loaded reply.  If the reply is present the class will
immediately return the pre-loaded reply, otherwise it will ask to the
user for a response, return that to the higher level and save it in
the hash table.
