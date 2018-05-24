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

import org.onosproject.modules.Module;
import org.onosproject.db.DBQuestioner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onlab.packet.Ethernet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @class PolicyMaker
 * @brief Class that loads decision-making modules
 * and interrogates them to decide whether to accept a flow or not
 */
public class PolicyMaker {


    private final Logger log = LoggerFactory.getLogger(getClass());

    private boolean modulesAreConfigured = false;

    private Map<String, UserForwardTypes> alreadyAsked;
    private Set<Short> unfilteredEthTypes;


    private static final String MODULES_FILE_NAME_PATH = "/modules/modules.txt";

    private List<Module> listModules;
    private UserConfigurator userConfigurator;
    private DBQuestioner dbQuestioner;


    public PolicyMaker(UserConfigurator usrConfig) {

        this.listModules = new ArrayList<Module>();

        this.userConfigurator = usrConfig;

        this.unfilteredEthTypes = new HashSet<Short>(1);

        this.dbQuestioner = new DBQuestioner();

        // Add unfiltered ethtypes
        this.unfilteredEthTypes.add(Ethernet.TYPE_ARP);

        alreadyAsked = new HashMap<String, UserForwardTypes>();

        configureModules();

    }

    /**
     * @method configureModules
     * @brief loads all the modules specified in the source file modules.txt
     * @param none
     * @return none
     */
    private void configureModules() {

        InputStream in = this.getClass().getResourceAsStream(MODULES_FILE_NAME_PATH);
        String moduleName;
        BufferedReader fileReader = null;
        Module modTemp = null;

        if (in == null) {
            log.error(MODULES_FILE_NAME_PATH + " file non trovato");
            return;
        }

        fileReader = new BufferedReader(new InputStreamReader(in));

        try {
            while ((moduleName = fileReader.readLine()) != null) {

                modTemp = null;
                try {
                    modTemp = (Module) Class.forName("org.onosproject.modules." + moduleName).newInstance();
                } catch (InstantiationException e) {
                    log.error(e.getMessage());
                    continue;
                } catch (IllegalAccessException e) {
                    log.error(e.getMessage());
                    continue;
                } catch (ClassNotFoundException e) {
                    log.error(e.getMessage());
                    continue;
                }

                if (modTemp != null) {
                    listModules.add(modTemp);
                }

            }
        } catch (IOException e) {
            log.error(e.getMessage());
            return;
        }

        try {
            fileReader.close();
            in.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        if (!listModules.isEmpty()) {
            modulesAreConfigured = true;
        }

    }


    /**
     * @method askUserToForward
     * @brief interrogates the modules to decide whether to accept a flow or not
     * and ask to the user the final decision
     * @param pkt is a Flow object
     * @return return an UserForwardTypes object
     */
    public UserForwardTypes askToForward(Flow pkt) {
        UserForwardTypes retVal;
        UserForwardTypes userResponse;
        Module.Opinion modulesResponse = null;
        List<Module.Opinion> opinions = new ArrayList<Module.Opinion>();

        if (modulesAreConfigured) {
            for (Module module : this.listModules) {
                opinions.add(module.givesOpinion(pkt, this.dbQuestioner));
            }

            modulesResponse = calculateModulesResponse(opinions);

        }

        userResponse = userConfigurator.askUserToForward(pkt, modulesResponse);

        retVal = userResponse;

        try {
            alreadyAsked.put(pkt.toHash(), retVal);
         } catch (NoSuchAlgorithmException e) {
             log.error("NoSuchAlgorithmException " + e.getMessage());
             return UserForwardTypes.ERROR_FORWARDING;
         }

        if (!this.dbQuestioner.insertFlow(pkt)) {
            log.error("Can't insert flow in the database");
        }

        return retVal;


    }

    /**
     * @method calculateModulesResponse
     * @brief calculate the weighted average of the opinions of the modules
     * @param opinion is list of opinions
     * @return return a Module.Opinion object
     */
    private Module.Opinion calculateModulesResponse(List<Module.Opinion> opinions) {
        float averageYes = 0;
        float averageNo = 0;
        int numOfYes = 0;
        int numOfNo = 0;

        for (Module.Opinion opinion : opinions) {
            if (opinion.wantsFlow()) {
                averageYes += opinion.getConfidence();
                numOfYes++;
            } else {
                averageNo += opinion.getConfidence();
                numOfNo++;
            }
        }

        if (numOfYes > 0 && (numOfNo == 0 || (averageYes / numOfYes) > (averageNo / numOfNo))) {
            return new Module.Opinion(true, averageYes / numOfYes);
        } else {
            return new Module.Opinion(false, averageNo / numOfNo);
        }

    }

    /**
     * @method notFiltered
     * @brief check if the packet should not be filtered out.
     * @param pkt is an UserQuestionFormat object
     * @return return a boolean
     */
    public boolean notFiltered(Flow pkt) {
        return this.unfilteredEthTypes.contains((short) pkt.getEthType());
    }

    /**
     * @method hashAlreadyAsked
     * @brief check if the flow already have a policy in the current
     * config.
     * @param pkt is a Flow object
     * @return return an Flow object
     */
    public UserForwardTypes hashAlreadyAsked(Flow pkt) {
        try {
            UserForwardTypes retVal = alreadyAsked.get(pkt.toHash());
            if (retVal == null) {
                return UserForwardTypes.ERROR_FORWARDING;
            }
            return retVal;

        } catch (NoSuchAlgorithmException e) {
            log.error("NoSuchAlgorithmException " + e.getMessage());
        }

        return null;
    }

}
