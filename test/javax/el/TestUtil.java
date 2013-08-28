/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javax.el;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

public class TestUtil {

    @Test
    public void test01() {
        ELProcessor processor = new ELProcessor();
        processor.defineBean("sb", new StringBuilder());
        Assert.assertEquals("a", processor.eval("sb.append('a'); sb.toString()"));
    }


    @Test
    public void test02() {
        ELProcessor processor = new ELProcessor();
        processor.getELManager().importClass("java.util.Date");
        Date result = (Date) processor.eval("Date(86400)");
        Assert.assertEquals(86400, result.getTime());
    }
}
