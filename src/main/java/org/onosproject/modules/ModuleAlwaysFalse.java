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

public class ModuleAlwaysFalse implements Module {

    public Opinion givesOpinion(Flow pkt, DBQuestioner dbQuestioner) {
        return new Opinion(false, 70);
    }

}
