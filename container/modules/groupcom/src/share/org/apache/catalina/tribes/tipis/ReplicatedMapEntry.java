/*
 * Copyright 1999,2004-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.catalina.tribes.tipis;

import java.io.Serializable;
import java.io.IOException;

/**
 * 
 * For smarter replication, an object can implement this interface to replicate diffs
 * @author Filip Hanik
 * @version 1.0
 */
public interface ReplicatedMapEntry extends Serializable {
    
    /**
     * Has the object changed since last replication
     * and is not in a locked state
     * @return boolean
     */
    public boolean isDirty();
    
    public boolean setDirty(boolean dirty);
    
    /**
     * If this returns true, the map will extract the diff using getDiff()
     * Otherwise it will serialize the entire object.
     * @return boolean
     */
    public boolean isDiffable();
    
    /**
     * Returns a diff and sets the dirty map to false
     * @return byte[]
     * @throws IOException
     */
    public byte[] getDiff() throws IOException;
    
    
    /**
     * Applies a diff to an existing object.
     * @param diff byte[]
     * @param offset int
     * @param length int
     * @throws IOException
     */
    public void applyDiff(byte[] diff, int offset, int length) throws IOException;
    
    
}