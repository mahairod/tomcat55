/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.jdbc.test;

import java.sql.Connection;

import org.apache.tomcat.jdbc.pool.interceptor.ResetAbandonedTimer;

public class AbandonPercentageTest extends DefaultTestCase {

    public AbandonPercentageTest(String name) {
        super(name);
    }

    public void testDefaultAbandon() throws Exception {
        this.init();
        this.datasource.setMaxActive(100);
        this.datasource.setMaxIdle(100);
        this.datasource.setInitialSize(0);
        this.datasource.getPoolProperties().setAbandonWhenPercentageFull(0);
        this.datasource.getPoolProperties().setTimeBetweenEvictionRunsMillis(100);
        this.datasource.getPoolProperties().setRemoveAbandoned(true);
        this.datasource.getPoolProperties().setRemoveAbandonedTimeout(1);
        Connection con = datasource.getConnection();
        assertEquals("Number of connections active/busy should be 1",1,datasource.getPool().getActive());
        Thread.sleep(2000);
        assertEquals("Number of connections active/busy should be 0",0,datasource.getPool().getActive());
        con.close();
    }

    public void testMaxedOutAbandon() throws Exception {
        int size = 100;
        this.init();
        this.datasource.setMaxActive(size);
        this.datasource.setMaxIdle(size);
        this.datasource.setInitialSize(0);
        this.datasource.getPoolProperties().setAbandonWhenPercentageFull(100);
        this.datasource.getPoolProperties().setTimeBetweenEvictionRunsMillis(100);
        this.datasource.getPoolProperties().setRemoveAbandoned(true);
        this.datasource.getPoolProperties().setRemoveAbandonedTimeout(1);
        Connection con = datasource.getConnection();
        assertEquals("Number of connections active/busy should be 1",1,datasource.getPool().getActive());
        Thread.sleep(2000);
        assertEquals("Number of connections active/busy should be 1",1,datasource.getPool().getActive());
        con.close();
    }

    public void testResetConnection() throws Exception {
        int size = 1;
        this.init();
        this.datasource.setMaxActive(size);
        this.datasource.setMaxIdle(size);
        this.datasource.setInitialSize(0);
        this.datasource.getPoolProperties().setAbandonWhenPercentageFull(100);
        this.datasource.getPoolProperties().setTimeBetweenEvictionRunsMillis(100);
        this.datasource.getPoolProperties().setRemoveAbandoned(true);
        this.datasource.getPoolProperties().setRemoveAbandonedTimeout(1);
        this.datasource.getPoolProperties().setJdbcInterceptors(ResetAbandonedTimer.class.getName());
        Connection con = datasource.getConnection();
        assertEquals("Number of connections active/busy should be 1",1,datasource.getPool().getActive());
        for (int i=0; i<20; i++) {
            Thread.sleep(200);
            con.isClosed();
        }
        assertEquals("Number of connections active/busy should be 1",1,datasource.getPool().getActive());
        con.close();
    }

    public void testHalfway() throws Exception {
        int size = 100;
        this.init();
        this.datasource.setMaxActive(size);
        this.datasource.setMaxIdle(size);
        this.datasource.setInitialSize(0);
        this.datasource.getPoolProperties().setAbandonWhenPercentageFull(50);
        this.datasource.getPoolProperties().setTimeBetweenEvictionRunsMillis(500);
        this.datasource.getPoolProperties().setRemoveAbandoned(true);
        this.datasource.getPoolProperties().setRemoveAbandonedTimeout(1);
        Connection[] con = new Connection[size];
        con[0] = datasource.getConnection();
        assertEquals("Number of connections active/busy should be 1",1,datasource.getPool().getActive());
        for (int i=1; i<25; i++) {
            con[i] = datasource.getConnection();
        }
        assertEquals("Number of connections active/busy should be 25",25,datasource.getPool().getActive());
        Thread.sleep(2500);
        assertEquals("Number of connections active/busy should be 25",25,datasource.getPool().getActive());
        this.datasource.getPoolProperties().setRemoveAbandonedTimeout(100);
        for (int i=25; i<con.length; i++) {
            con[i] = datasource.getConnection();
        }
        int active = datasource.getPool().getActive();
        System.out.println("Active:"+active);
        assertEquals("Number of connections active/busy should be "+con.length,con.length,datasource.getPool().getActive());
        this.datasource.getPoolProperties().setRemoveAbandonedTimeout(1);
        Thread.sleep(2500);
        assertTrue("Number of connections should be less than 50.", (datasource.getPool().getActive()<50));
        this.datasource.getPoolProperties().setAbandonWhenPercentageFull(0);
        Thread.sleep(2500);
        assertEquals("Number of connections active/busy should be "+0,0,datasource.getPool().getActive());
    }
}
