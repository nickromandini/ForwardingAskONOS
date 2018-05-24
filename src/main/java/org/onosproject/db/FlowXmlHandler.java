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

package org.onosproject.db;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.onosproject.fwdask.Flow;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/* Example of how a Flow is represented in the XML Database
 *
 * <flow>
 *  <vlan>...</vlan>
 *  <ethType>...</ethType>
 *  <srcMac>...</srcMac>
 *  <destMac>...</destMac>
 *  <netProtocol>...</netProtocol>
 *  <srcIp>...</srcIp>
 *  <destIp>...</destIp>
 *  <srcPort>...</srcPort>
 *  <destPort>...</destPort>
 *  <timestamp>...</timestamp>
 * </flow>
 *
 */

/**
 * @class FlowXmlHandler
 * @brief Helper class to parse the result of the queries made to the XML Database
 */
public class FlowXmlHandler extends DefaultHandler {

    List<Flow> flowsList = null;
    Flow flow = null;

    private boolean bVlan = false;
    private boolean bEthType = false;
    private boolean bSrc = false;
    private boolean bDst = false;

    private boolean bNetProtocol = false;
    private boolean bLayer3Source = false;
    private boolean bLayer3Destination = false;

    private boolean bLayer4Source = false;
    private boolean bLayer4Destination = false;

    private boolean bTimestamp = false;

    public List<Flow> getFlowsList() {
        return flowsList;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {

        if (qName.equalsIgnoreCase("flow")) {
            //initialize list
            if (flowsList == null) {
                flowsList = new ArrayList<Flow>();
            }
            if (flow == null) {
                flow = new Flow();
            }
        } else if (qName.equalsIgnoreCase("vlan")) {
            //set boolean values for fields, will be used in setting Flow variables
            bVlan = true;
        } else if (qName.equalsIgnoreCase("ethType")) {
            bEthType = true;
        } else if (qName.equalsIgnoreCase("srcMac")) {
            bSrc = true;
        } else if (qName.equalsIgnoreCase("destMac")) {
            bDst = true;
        } else if (qName.equalsIgnoreCase("netProtocol")) {
            bNetProtocol = true;
        } else if (qName.equalsIgnoreCase("srcIp")) {
            bLayer3Source = true;
        } else if (qName.equalsIgnoreCase("destIp")) {
            bLayer3Destination = true;
        } else if (qName.equalsIgnoreCase("srcPort")) {
            bLayer4Source = true;
        } else if (qName.equalsIgnoreCase("destPort")) {
            bLayer4Destination = true;
        } else if (qName.equalsIgnoreCase("timestamp")) {
            bTimestamp = true;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("flow")) {
            //add Flow object to list
            flowsList.add(flow);
            flow = null;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {

        if (bVlan) {
            flow.setVlanID(Integer.parseInt(new String(ch, start, length)));
            bVlan = false;
        } else if (bEthType) {
            flow.setEthType(Integer.parseInt(new String(ch, start, length)));
            bEthType = false;
        } else if (bSrc) {
            flow.setSourceMac(new String(ch, start, length));
            bSrc = false;
        } else if (bDst) {
            flow.setDestinationMac(new String(ch, start, length));
            bDst = false;
        } else if (bNetProtocol) {
            flow.setNetProtocol(Integer.parseInt(new String(ch, start, length)));
            bNetProtocol = false;
        } else if (bLayer3Source) {
            flow.setNetSource(new String(ch, start, length));
            bLayer3Source = false;
        } else if (bLayer3Destination) {
            flow.setNetDestination(new String(ch, start, length));
            bLayer3Destination = false;
        } else if (bLayer4Source) {
            flow.setTransportSource(Integer.parseInt(new String(ch, start, length)));
            bLayer4Source = false;
        } else if (bLayer4Destination) {
            flow.setTransportDestination(Integer.parseInt(new String(ch, start, length)));
            bLayer4Destination = false;
        } else if (bTimestamp) {
            flow.setTimestamp(Timestamp.valueOf(new String(ch, start, length)));
            bLayer4Destination = false;
        }
    }
}