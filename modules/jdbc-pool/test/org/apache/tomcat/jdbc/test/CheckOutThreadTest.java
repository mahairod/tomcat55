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
package org.apache.tomcat.jdbc.test;

import java.util.concurrent.CountDownLatch;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.apache.tomcat.jdbc.pool.DataSourceFactory;

/**
 * @author Filip Hanik
 * @version 1.0
 */
public class CheckOutThreadTest extends DefaultTestCase {
    public CheckOutThreadTest(String name) {
        super(name);
    }

    CountDownLatch latch = null;

    public void testDBCPThreads10Connections10() throws Exception {
        init();
        this.datasource.getPoolProperties().setMaxActive(10);
        this.threadcount = 10;
        this.transferProperties();
        this.tDatasource.getConnection().close();
        latch = new CountDownLatch(threadcount);
        long start = System.currentTimeMillis();
        for (int i=0; i<threadcount; i++) {
            TestThread t = new TestThread();
            t.setName("tomcat-dbcp-"+i);
            t.d = this.tDatasource;
            t.start();
        }
        latch.await();
        long delta = System.currentTimeMillis() - start;
        System.out.println("[testDBCPThreads10Connections10]Test complete:"+delta+" ms. Iterations:"+(threadcount*this.iterations));
        tearDown();
    }

    public void testPoolThreads10Connections10() throws Exception {
        init();
        this.datasource.getPoolProperties().setMaxActive(10);
        this.threadcount = 10;
        this.transferProperties();
        this.datasource.getConnection().close();
        latch = new CountDownLatch(threadcount);
        long start = System.currentTimeMillis();
        for (int i=0; i<threadcount; i++) {
            TestThread t = new TestThread();
            t.setName("tomcat-pool-"+i);
            t.d = DataSourceFactory.getDataSource(this.datasource);
            t.start();
        }
        latch.await();
        long delta = System.currentTimeMillis() - start;
        System.out.println("[testPoolThreads10Connections10]Test complete:"+delta+" ms. Iterations:"+(threadcount*this.iterations));
        tearDown();
    }

    public void testDBCPThreads20Connections10() throws Exception {
        init();
        this.datasource.getPoolProperties().setMaxActive(10);
        this.threadcount = 20;
        this.transferProperties();
        this.tDatasource.getConnection().close();
        latch = new CountDownLatch(threadcount);
        long start = System.currentTimeMillis();
        for (int i=0; i<threadcount; i++) {
            TestThread t = new TestThread();
            t.setName("tomcat-dbcp-"+i);
            t.d = this.tDatasource;
            t.start();
        }
        latch.await();
        long delta = System.currentTimeMillis() - start;
        System.out.println("[testDBCPThreads20Connections10]Test complete:"+delta+" ms. Iterations:"+(threadcount*this.iterations));
        tearDown();
    }

    public void testPoolThreads20Connections10() throws Exception {
        init();
        this.datasource.getPoolProperties().setMaxActive(10);
        this.threadcount = 20;
        this.transferProperties();
        this.datasource.getConnection().close();
        latch = new CountDownLatch(threadcount);
        long start = System.currentTimeMillis();
        for (int i=0; i<threadcount; i++) {
            TestThread t = new TestThread();
            t.setName("tomcat-pool-"+i);
            t.d = DataSourceFactory.getDataSource(this.datasource);
            t.start();
        }
        latch.await();
        long delta = System.currentTimeMillis() - start;
        System.out.println("[testPoolThreads20Connections10]Test complete:"+delta+" ms. Iterations:"+(threadcount*this.iterations));
        tearDown();
    }

    public void testPoolThreads20Connections10Fair() throws Exception {
        init();
        this.datasource.getPoolProperties().setMaxActive(10);
        this.datasource.getPoolProperties().setFairQueue(true);
        this.threadcount = 20;
        this.transferProperties();
        this.datasource.getConnection().close();
        latch = new CountDownLatch(threadcount);
        long start = System.currentTimeMillis();
        for (int i=0; i<threadcount; i++) {
            TestThread t = new TestThread();
            t.setName("tomcat-pool-"+i);
            t.d = DataSourceFactory.getDataSource(this.datasource);
            t.start();
        }
        latch.await();
        long delta = System.currentTimeMillis() - start;
        System.out.println("[testPoolThreads20Connections10Fair]Test complete:"+delta+" ms. Iterations:"+(threadcount*this.iterations));
        tearDown();
    }

    
    public void testDBCPThreads10Connections10Validate() throws Exception {
        init();
        this.datasource.getPoolProperties().setMaxActive(10);
        this.datasource.getPoolProperties().setValidationQuery("SELECT 1");
        this.datasource.getPoolProperties().setTestOnBorrow(true);
        this.threadcount = 10;
        this.transferProperties();
        this.tDatasource.getConnection().close();
        latch = new CountDownLatch(threadcount);
        long start = System.currentTimeMillis();
        for (int i=0; i<threadcount; i++) {
            TestThread t = new TestThread();
            t.setName("tomcat-dbcp-validate-"+i);
            t.d = this.tDatasource;
            t.start();
        }
        latch.await();
        long delta = System.currentTimeMillis() - start;
        System.out.println("[testDBCPThreads10Connections10Validate]Test complete:"+delta+" ms. Iterations:"+(threadcount*this.iterations));
        tearDown();
    }

    public void testPoolThreads10Connections10Validate() throws Exception {
        init();
        this.datasource.getPoolProperties().setMaxActive(10);
        this.datasource.getPoolProperties().setValidationQuery("SELECT 1");
        this.datasource.getPoolProperties().setTestOnBorrow(true);
        this.threadcount = 10;
        this.transferProperties();
        this.datasource.getConnection().close();
        latch = new CountDownLatch(threadcount);
        long start = System.currentTimeMillis();
        for (int i=0; i<threadcount; i++) {
            TestThread t = new TestThread();
            t.setName("tomcat-pool-validate-"+i);
            t.d = DataSourceFactory.getDataSource(this.datasource);
            t.start();
        }
        latch.await();
        long delta = System.currentTimeMillis() - start;
        System.out.println("[testPoolThreads10Connections10Validate]Test complete:"+delta+" ms. Iterations:"+(threadcount*this.iterations));
        tearDown();
    }

    public void testDBCPThreads20Connections10Validate() throws Exception {
        init();
        this.datasource.getPoolProperties().setMaxActive(10);
        this.datasource.getPoolProperties().setValidationQuery("SELECT 1");
        this.datasource.getPoolProperties().setTestOnBorrow(true);
        this.threadcount = 20;
        this.transferProperties();
        this.tDatasource.getConnection().close();
        latch = new CountDownLatch(threadcount);
        long start = System.currentTimeMillis();
        for (int i=0; i<threadcount; i++) {
            TestThread t = new TestThread();
            t.setName("tomcat-dbcp-validate-"+i);
            t.d = this.tDatasource;
            t.start();
        }
        latch.await();
        long delta = System.currentTimeMillis() - start;
        System.out.println("[testDBCPThreads20Connections10Validate]Test complete:"+delta+" ms. Iterations:"+(threadcount*this.iterations));
        tearDown();
    }

    public void testPoolThreads10Connections20Validate() throws Exception {
        init();
        this.datasource.getPoolProperties().setMaxActive(10);
        this.datasource.getPoolProperties().setValidationQuery("SELECT 1");
        this.datasource.getPoolProperties().setTestOnBorrow(true);
        this.threadcount = 20;
        this.transferProperties();
        this.datasource.getConnection().close();
        latch = new CountDownLatch(threadcount);
        long start = System.currentTimeMillis();
        for (int i=0; i<threadcount; i++) {
            TestThread t = new TestThread();
            t.setName("tomcat-pool-validate-"+i);
            t.d = DataSourceFactory.getDataSource(this.datasource);
            t.start();
        }
        latch.await();
        long delta = System.currentTimeMillis() - start;
        System.out.println("[testPoolThreads20Connections10Validate]Test complete:"+delta+" ms. Iterations:"+(threadcount*this.iterations));
        tearDown();
    }
    
    public class TestThread extends Thread {
        protected DataSource d;
        protected String query = null;
        public void run() {
            long max = -1, totalmax=0, totalcmax=0, cmax = -1, nroffetch = 0, totalruntime = 0;
            try {
                for (int i = 0; i < CheckOutThreadTest.this.iterations; i++) {
                    long start = System.nanoTime();
                    Connection con = null;
                    try {
                        con = d.getConnection();
                        long delta = System.nanoTime() - start;
                        totalmax += delta;
                        max = Math.max(delta, max);
                        nroffetch++;
                        if (query!=null) {
                            Statement st = con.createStatement();
                            ResultSet rs = st.executeQuery(query);
                            while (rs.next()) {
                            }
                            rs.close();
                            st.close();
                        }
                    } finally {
                        long cstart = System.nanoTime();
                        if (con!=null) try {con.close();}catch(Exception x) {x.printStackTrace();}
                        long cdelta = System.nanoTime() - cstart;
                        totalcmax += cdelta;
                        cmax = Math.max(cdelta, cmax);
                    }
                    totalruntime+=(System.nanoTime()-start);
                }

            } catch (Exception x) {
                x.printStackTrace();
            } finally {
                CheckOutThreadTest.this.latch.countDown();
            }
            if (System.getProperty("print-thread-stats")!=null) {
                System.out.println("["+getName()+"] "+
                    "\n\tMax time to retrieve connection:"+(((float)max)/1000f/1000f)+" ms."+
                    "\n\tTotal time to retrieve connection:"+(((float)totalmax)/1000f/1000f)+" ms."+
                    "\n\tAverage time to retrieve connection:"+(((float)totalmax)/1000f/1000f)/(float)nroffetch+" ms."+
                    "\n\tMax time to close connection:"+(((float)cmax)/1000f/1000f)+" ms."+
                    "\n\tTotal time to close connection:"+(((float)totalcmax)/1000f/1000f)+" ms."+
                    "\n\tAverage time to close connection:"+(((float)totalcmax)/1000f/1000f)/(float)nroffetch+" ms."+
                    "\n\tRun time:"+(((float)totalruntime)/1000f/1000f)+" ms."+
                    "\n\tNr of fetch:"+nroffetch);
            }
        }
    }
}
