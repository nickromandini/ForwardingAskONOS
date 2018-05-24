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

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.onosproject.fwdask.Flow;
import org.slf4j.Logger;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @class DBQuestioner
 * @brief Helper class to interact with a XML Database in which are saved all flows
 */
public class DBQuestioner {

    private final Logger log = getLogger(getClass());

    private boolean configurated = false;
    private BaseXClient session = null;
    private SAXParser saxParser = null;
    private FlowXmlHandler handler = null;

    public DBQuestioner() {

        try {
            this.session = new BaseXClient("localhost", 1984, "FWDASK", "fwdask");
            this.saxParser = SAXParserFactory.newInstance().newSAXParser();
            this.handler = new FlowXmlHandler();
        } catch (IOException e) {
            log.error(e.getMessage());
            return;
        } catch (ParserConfigurationException e) {
            log.error(e.getMessage());
            return;
        } catch (SAXException e) {
            log.error(e.getMessage());
            return;
        }

        configurated = true;
    }

    /**
     * @method insertFlow
     * @brief insert a flow in the XML Database
     * @param pkt is a Flow object
     * @return return a boolean: true if the flow was inserted, false otherwise
     */
    public boolean insertFlow(Flow pkt) {

        String insertQuery = "insert node <flow>"
                + "<vlan>" + pkt.getVlanID() + "</vlan>"
                + "<ethType>" + pkt.getEthType() + "</ethType>"
                + "<srcMac>" + pkt.getSourceMac() + "</srcMac>"
                + "<destMac>" + pkt.getDestinationMac() + "</destMac>"
                + "<netProtocol>" + pkt.getNetProtocol() + "</netProtocol>"
                + "<srcIp>" + pkt.getNetSource() + "</srcIp>"
                + "<destIp>" + pkt.getNetDestination() + "</destIp>"
                + "<srcPort>" + pkt.getTransportSource() + "</srcPort>"
                + "<destPort>" + pkt.getTransportDestination() + "</destPort>"
                + "<timestamp>" + pkt.getTimestamp() + "</timestamp>"
                + "</flow> into doc('fwdaskdb/fwdaskdb.xml')//flows";

        if (!configurated) {
            return false;
        }

        try {
            session.query(insertQuery).execute();
        } catch (IOException e) {
            log.error(e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * @method flowAlreadyExist
     * @brief check in the XML Database if the flow is already in it
     * @param flow is a Flow object
     * @return boolean
     */
    public boolean flowAlreadyExist(Flow flow) {

        // not implemented yet
        return false;

    }

    /**
     * @method getFlowsBySrcIp
     * @brief return all the flows that have as source Ip the string passed as argument
     * @param srcIp is a String
     * @return return the list of the flows
     */
    public List<Flow> getFlowsBySrcIp(String srcIp) {

        String resultAsString;
        List<Flow> queryResult;
        BaseXClient.Query query;

        String getQuery = "for $flow in doc('fwdaskdb/fwdaskdb.xml')//flows/flow "
                + "where $flow/srcIp=" + srcIp + " return $flow";

        if (!configurated) {
            return null;
        }

        try {
            query = session.query(getQuery);
            resultAsString = query.execute();
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }

        if (resultAsString.isEmpty()) {
            return null;
        } else {
            try {
                this.saxParser.parse(new InputSource(
                        new StringReader("<flows>" + resultAsString + "</flows>")), this.handler);
            } catch (SAXException e) {
                log.error(e.getMessage());
                return null;
            } catch (IOException e) {
                log.error(e.getMessage());
                return null;
            }

            queryResult = this.handler.getFlowsList();
        }
        return queryResult;

    }

    /**
     * @method getFlowsByDestIp
     * @brief return all the flows that have as destination Ip the string passed as argument
     * @param destIp is a String
     * @return return the list of the flows
     */
    public List<Flow> getFlowsByDestIp(String destIp) {

        String resultAsString;
        List<Flow> queryResult;
        BaseXClient.Query query;

        String getQuery = "for $flow in doc('fwdaskdb/fwdaskdb.xml')//flows/flow "
                + "where $flow/destIp=" + destIp + " return $flow";

        if (!configurated) {
            return null;
        }

        try {
            query = session.query(getQuery);
            resultAsString = query.execute();
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }

        if (resultAsString.isEmpty()) {
            return null;
        } else {
            try {
                this.saxParser.parse(new InputSource(
                        new StringReader("<flows>" + resultAsString + "</flows>")), this.handler);
            } catch (SAXException e) {
                log.error(e.getMessage());
                return null;
            } catch (IOException e) {
                log.error(e.getMessage());
                return null;
            }

            queryResult = this.handler.getFlowsList();
        }
        return queryResult;

    }

}
