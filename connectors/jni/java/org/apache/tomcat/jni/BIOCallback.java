/*
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.jni;

/** Open SSL BIO Callback Interface
 *
 * @author Mladen Turk
 * @version $Revision$, $Date$
 */

public interface BIOCallback {

    /**
     * Write data
     * @param data String to write
     * @return Number of characters written
     */
    public int write(String data);

    /**
     * Read data
     * @param len Maximum number of characters to read
     * @return String with up to len bytes readed
     */
    public String read(int len);

    /**
     * Puts string
     * @param data String to write
     * @return Number of characters written
     */
    public int puts(String data);

    /**
     * Read string up to the len or CLRLF
     * @param len Maximum number of characters to read
     * @return String with up to len bytes readed
     */
    public String gets(int len);

}
