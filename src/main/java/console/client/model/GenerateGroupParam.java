/*
 * Copyright 2014-2020  [fisco-dev]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package console.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import console.common.ConsoleUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GenerateGroupParam {
    private static final String GROUP_PEERS_KEY = "peers";
    private static final String SEALER_LIST_KEY = "sealerList";
    private static final String TIMESTAMP_KEY = "timestamp";
    private final java.util.Calendar calendar = java.util.Calendar.getInstance();
    private Map<String, String> genesis;
    private Map<String, Object> groupPeers;
    private Map<String, Object> consensus;
    private String timestamp;

    public GenerateGroupParam() {}

    public boolean checkGenerateGroupParam() {
        if (getGroupPeerInfo() == null || getGroupPeerInfo().size() == 0) {
            System.out.println("The group peer list must be not empty!");
            return false;
        }
        List<String> peerInfo = (List<String>) groupPeers.get(GROUP_PEERS_KEY);
        for (String peer : peerInfo) {
            if (!ConsoleUtils.checkEndPoint(peer)) {
                return false;
            }
        }
        return true;
    }

    public Long getTimestamp() {
        if (timestamp != null) {
            return Long.valueOf(timestamp);
        }
        if (genesis == null
                || genesis.get(TIMESTAMP_KEY) == null
                || genesis.get(TIMESTAMP_KEY).equals("")) {
            int zoneOffset = calendar.get(java.util.Calendar.ZONE_OFFSET);
            int dstOffset = calendar.get(java.util.Calendar.DST_OFFSET);
            calendar.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
            timestamp = String.valueOf(calendar.getTimeInMillis());
        } else {
            timestamp = genesis.get(TIMESTAMP_KEY);
        }
        return Long.valueOf(timestamp);
    }

    public Map<String, String> getGenesis() {
        return genesis;
    }

    public void setGenesis(Map<String, String> genesis) {
        this.genesis = genesis;
    }

    public Map<String, Object> getGroupPeers() {
        return groupPeers;
    }

    public List<String> getGroupPeerInfo() {
        if (groupPeers == null || groupPeers.get(GROUP_PEERS_KEY) == null) {
            return new ArrayList<>();
        }
        return (List<String>) groupPeers.get(GROUP_PEERS_KEY);
    }

    public void setGroupPeers(Map<String, Object> groupPeers) {
        this.groupPeers = groupPeers;
    }

    public Map<String, Object> getConsensus() {
        return consensus;
    }

    public List<String> getSealerListInfo() {
        if (consensus == null || consensus.get(SEALER_LIST_KEY) == null) {
            return new ArrayList<>();
        }
        return (List<String>) consensus.get(SEALER_LIST_KEY);
    }

    public void setConsensus(Map<String, Object> consensus) {
        this.consensus = consensus;
    }
}
