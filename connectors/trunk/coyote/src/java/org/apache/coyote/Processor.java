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

package org.apache.coyote;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Processor.
 *
 * @author Remy Maucherat
 */
public interface Processor {


    public void setAdapter(Adapter adapter);


    public Adapter getAdapter();


    public void process(InputStream input, OutputStream output)
        throws IOException;


}
