/*
 * Copyright 2018 Open Networking Foundation
 * Copyright 2018 Davide Berardi, Andrea Melis.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onosproject.fwdask;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;

import org.onlab.packet.Ethernet;
import org.onlab.packet.IPv4;
import org.onlab.packet.IPv6;


/**
 * @class Flow
 * @brief Representation of a flow. The class contains all the information of the flow
 */
public class Flow {


    private int vlan = 0;
    private int ethType = 0;
    private String src = "";
    private String dst = "";

    private int netProtocol = 0;
    public String layer3Source = "";
    public String layer3Destination = "";

    private int layer4Source = 0;
    private int layer4Destination = 0;

    private Timestamp timestamp = null;

    public Flow() {
    }

    // Convert the information to an hash to store and retrive it /more briefly.
    public String toHash() throws NoSuchAlgorithmException {
        MessageDigest mdigest = MessageDigest.getInstance("SHA-256");
        String payload =  String.valueOf(vlan) + src + dst + String.valueOf(ethType) +
                          String.valueOf(netProtocol) + layer3Source + layer3Destination +
                          String.valueOf(layer4Source) + String.valueOf(layer4Destination);
        mdigest.update(payload.getBytes(StandardCharsets.US_ASCII));

        return String.format("%064x", new BigInteger(1, mdigest.digest()));
    }

    // layer 2 get/set
    public int getEthType() {
        return ethType;
    }
    public void setEthType(int ethTypeP) {
        ethType = ethTypeP;
    }

    public int getVlanID() {
        return vlan;
    }
    public void setVlanID(int vlanP) {
        vlan = vlanP;
    }

    public String getSourceMac() {
        return src;
    }
    public void setSourceMac(String srcP) {
        src = srcP;
    }

    public String getDestinationMac() {
        return dst;
    }
    public void setDestinationMac(String dstP) {
        dst = dstP;
    }

    // layer 3 get/set
    public boolean isNetworkInspectionSupported() {
        return ethType == Ethernet.TYPE_IPV4 ||
               ethType == Ethernet.TYPE_IPV6;
    }

    public void setNetProtocol(int protocolP) {
        netProtocol = protocolP;
    }
    public int getNetProtocol() {
        return netProtocol;
    }

    public void setNetSource(String srcaddr) {
        layer3Source = srcaddr;
    }
    public String getNetSource() {
        return layer3Source;
    }

    public void setNetDestination(String dstaddr) {
        layer3Destination = dstaddr;
    }
    public String getNetDestination() {
        return layer3Destination;
    }

    // layer 4 get/set
    public boolean isTransportInspectionSupported() {
        if (ethType == Ethernet.TYPE_IPV4) {
            return netProtocol == IPv4.PROTOCOL_ICMP ||
                   netProtocol == IPv4.PROTOCOL_TCP  ||
                   netProtocol == IPv4.PROTOCOL_UDP;
        }

        if (ethType == Ethernet.TYPE_IPV6) {
            return netProtocol == IPv6.PROTOCOL_ICMP6 ||
                   netProtocol == IPv6.PROTOCOL_TCP   ||
                   netProtocol == IPv6.PROTOCOL_UDP;
        }

        return false;
    }

    public void setTransportSource(int srcport) {
        layer4Source = srcport;
    }
    public int getTransportSource() {
        return layer4Source;
    }

    public void setTransportDestination(int dstport) {
        layer4Destination = dstport;
    }
    public int getTransportDestination() {
        return layer4Destination;
    }

    // Timestamp get/set
    public Timestamp getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        String flowString = "Flow (hash " + this.hashCode() + "):\n";
        flowString += "\t" + "src: " + this.src + "\n";
        flowString += "\t" + "dst: " + this.dst + "\n";
        flowString += "\t" + "vlan: " + this.vlan + "\n";
        flowString += "\t" + "ethtype: " + this.ethType + "\n";
        if (this.isNetworkInspectionSupported()) {
            flowString += "\t" + "net protocol: " + this.netProtocol + "\n";
            flowString += "\t" + "net src: " + this.layer3Source + "\n";
            flowString += "\t" + "net dst: " + this.layer4Destination + "\n";
            if (this.isTransportInspectionSupported()) {
                flowString += "\t" + "trs src: " + this.layer4Source + "\n";
                flowString += "\t" + "trs dst: " + this.layer4Destination + "\n";
            }
        }
        if (this.timestamp != null) {
            flowString += "\t" + "timestamp: " + this.getTimestamp() + "\n";
        }

        return flowString;
    }

}

