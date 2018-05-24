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

package org.onosproject.modules;

import org.onosproject.db.DBQuestioner;
import org.onosproject.fwdask.Flow;

/**
 * @interface Module
 * @brief Interface that represent a Module and that every Module must implements
 */
public interface Module {

    /**
     * @class Opinion
     * @brief Class that represent an opinion
     */
    public class Opinion {

        // true if you want to accept flow, false otherwise
        private boolean boolOpinion;
        // percentage of confidence
        private float confidence;

        public Opinion(boolean boolOpinion, float confidence) {
            this.boolOpinion = boolOpinion;
            if (confidence < 0 || confidence > 100) {
                throw new IllegalArgumentException("The second parameter must be a percentage"
                        + " => float between 0 and 100");
            }
            this.confidence = confidence;
        }

        public boolean wantsFlow() {
            return boolOpinion;
        }

        public float getConfidence() {
            return confidence;
        }

    }

    /**
     * @method givesOpinion
     * @brief gives an Opinion on the Flow
     * @param pkt is a Flow object
     * @param dbQuestioner is a DBQUestioner object
     * used to interact with the XML Database
     * @return Module.Opinion
     */
    public Opinion givesOpinion(Flow pkt, DBQuestioner dbQuestioner);

}
