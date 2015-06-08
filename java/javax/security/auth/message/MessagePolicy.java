/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package javax.security.auth.message;

/**
 * @version $Rev$ $Date$
 */
public class MessagePolicy {

    private final TargetPolicy[] targetPolicies;
    private final boolean mandatory;

    public MessagePolicy(TargetPolicy[] targetPolicies, boolean mandatory) throws IllegalArgumentException {
        if (targetPolicies == null) {
            throw new IllegalArgumentException("targetPolicies is null");
        }
        this.targetPolicies = targetPolicies;
        this.mandatory = mandatory;
    }

    public TargetPolicy[] getTargetPolicies() {
        if (targetPolicies.length == 0) {
            return null;
        }
        return targetPolicies;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public static interface ProtectionPolicy {

        static String AUTHENTICATE_CONTENT = "#authenticateContent";
        static String AUTHENTICATE_RECIPIENT = "#authenticateRecipient";
        static String AUTHENTICATE_SENDER = "#authenticateSender";

        String getID();
    }

    public static interface Target {

        Object get(MessageInfo messageInfo);

        void put(MessageInfo messageInfo, Object data);

        void remove(MessageInfo messageInfo);
    }

    public static class TargetPolicy {

        private final Target[] targets;
        private final ProtectionPolicy protectionPolicy;

        public TargetPolicy(Target[] targets, ProtectionPolicy protectionPolicy) throws IllegalArgumentException {
            if (protectionPolicy == null) {
                throw new IllegalArgumentException("protectionPolicy is null");
            }
            this.targets = targets;
            this.protectionPolicy = protectionPolicy;
        }

        public Target[] getTargets() {
            if (targets == null || targets.length == 0) {
                return null;
            }
            return targets;
        }

        public ProtectionPolicy getProtectionPolicy() {
            return protectionPolicy;
        }
    }
}
