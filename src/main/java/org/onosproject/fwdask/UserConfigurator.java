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

import java.io.IOException;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import java.security.NoSuchAlgorithmException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableSet;

import org.onosproject.modules.Module;
import org.onosproject.ui.RequestHandler;
import org.onosproject.ui.UiMessageHandler;

import java.util.Collection;

enum UserForwardTypes {
    OK_TO_FORWARD,
    NOK_TO_FORWARD,
    ERROR_FORWARDING
};

/**
 * @class UserConfigurator
 * @brief Helper class to ask to the user what to do.
 */
public class UserConfigurator extends UiMessageHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private PipedInputStream inputStream;
    private PipedOutputStream outputStream;
    private ReactiveForwardingAskDataRequestHandler msgResponseHandler;

    private boolean configured = false;

    private static final String OK_FEEDBACK_PROMPT = "accepted, installing flow";
    private static final String PROMPT = "New packet detected, accept it? [y/n]";
    private static final String NOK_FEEDBACK_PROMPT = "rejected";
    private static final String ASK_PROMPT = "accept it? [y/n] > ";

    private static final String FWDASK_CUSTOM_DATA_REQ = "fwdaskCustomDataRequest";
    private static final String FWDASK_CUSTOM_DATA_RESP = "fwdaskCustomDataResponse";
    private static final String FWDASK_CUSTOM_DATA_NOTIFICATION = "fwdaskCustomDataNotification";
    private static final String MESSAGE = "message";
    private static final String PAYLOAD = "payload";
    private static final String EVENT = "event";


    public UserConfigurator() {

        this.msgResponseHandler = new ReactiveForwardingAskDataRequestHandler();

        this.outputStream = new PipedOutputStream();

        try {
            this.inputStream = new PipedInputStream(this.outputStream);
            this.msgResponseHandler.setOutputStream(outputStream);
        } catch (IOException e) {
            log.error("IOException " + e.getMessage());
            configured = false;
            return;
        }

        configured = true;


    }


    /**
     * @method askUserToForward
     * @brief ask to the user the sort of a packet (flow).
     * @param pkt is an Flow object
     * @param modulesResponse is a Module.Opinion object
     * @return return an UserForwardTypes object
     */
    public UserForwardTypes askUserToForward(Flow pkt, Module.Opinion modulesResponse) {
        UserForwardTypes retVal;
        String infoForUser = "";
        int response;

        if (!configured) {
            return UserForwardTypes.ERROR_FORWARDING;
        }

        if (modulesResponse != null) {
            if (modulesResponse.wantsFlow()) {
                this.informationsForUser("Modules opinion: ACCEPT THIS FLOW\nConfidence: "
                    + modulesResponse.getConfidence() + "%", true);
            } else {
                this.informationsForUser("Modules opinion: DISCARD THIS FLOW\nConfidence: "
                    + modulesResponse.getConfidence() + "%", true);
            }
        }

        try {
            infoForUser += PROMPT + "\n";
            infoForUser += "\t" + "src: " + pkt.getSourceMac() + "\n";
            infoForUser += "\t" + "dst: " + pkt.getDestinationMac() + "\n";
            infoForUser += "\t" + "vlan: " + pkt.getVlanID() + "\n";
            infoForUser += "\t" + "ethtype: " + pkt.getEthType() + "\n";
            if (pkt.isNetworkInspectionSupported()) {
                infoForUser += "\t" + "net protocol: " + pkt.getNetProtocol() + "\n";
                infoForUser += "\t" + "net src: " + pkt.getNetSource() + "\n";
                infoForUser += "\t" + "net dst: " + pkt.getNetDestination() + "\n";
                if (pkt.isTransportInspectionSupported()) {
                    infoForUser += "\t" + "trs src: " + pkt.getTransportSource() + "\n";
                    infoForUser += "\t" + "trs dst: " + pkt.getTransportDestination() + "\n";
                }
            }

            try {
                infoForUser += "(" + pkt.toHash() + ")\n";
            } catch (NoSuchAlgorithmException e) {
                log.error("NoSuchAlgorithmException " + e.getMessage());
                return UserForwardTypes.ERROR_FORWARDING;
            }
            infoForUser += ASK_PROMPT;
            this.informationsForUser(infoForUser, false);

            response = this.inputStream.read();


            if (response == 1) {
                //this.informationsForUser(OK_FEEDBACK_PROMPT, true);
                retVal = UserForwardTypes.OK_TO_FORWARD;
            } else {
                //this.informationsForUser(NOK_FEEDBACK_PROMPT, true);
                retVal = UserForwardTypes.NOK_TO_FORWARD;
            }


       } catch (IOException e) {
            log.error("IOException " + e.getMessage());

            configured = false;

            return UserForwardTypes.ERROR_FORWARDING;
       }


       return retVal;
    }


    /*
    * Methods related with GUI
    *
    */

    @Override
    protected Collection<RequestHandler> createRequestHandlers() {
        return ImmutableSet.of(msgResponseHandler);
    }

    /**
     * @method informationsForUser
     * @brief send the flow's information or notifications to the GUI
     * @param info is a String that contains flow's information
     * @param isNotification is a boolean that indicates if the message
     * is a notification
     * @return none
     */
    private void informationsForUser(String info, boolean isNotification) {

        ObjectNode result = objectNode();
        ObjectNode payload = objectNode();

        if (isNotification) {
            result.put(EVENT, FWDASK_CUSTOM_DATA_NOTIFICATION);
        } else {
            result.put(EVENT, FWDASK_CUSTOM_DATA_REQ);
        }

        payload.put(MESSAGE, info);
        result.set(PAYLOAD, payload);
        this.sendMessage(result);

    }


    /**
     * @class ReactiveForwardingAskDataRequestHandler
     * @brief Handler class for FWDASK_RESP events
     */
    private final class ReactiveForwardingAskDataRequestHandler extends RequestHandler {

        private String response = null;
        private PipedOutputStream outputStream;

        private ReactiveForwardingAskDataRequestHandler() {
            super(FWDASK_CUSTOM_DATA_RESP);
        }

        public void setOutputStream(PipedOutputStream outStream) {
            this.outputStream = outStream;
        }


        @Override
        public void process(ObjectNode payload) {
            log.debug("Computing data...");

            this.response = payload.get("response").asText();
            try {
                if (response.equals("ok")) {
                     this.outputStream.write(1);
                } else {
                     this.outputStream.write(0);
                }
                this.outputStream.flush();
            } catch (IOException e) {
                log.error("IOException " + e.getMessage());
            }
        }

    }

}
