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
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Random;
import java.sql.ResultSet;

import org.apache.tomcat.jdbc.pool.interceptor.ResetAbandonedTimer;

public class CreateTestTable extends DefaultTestCase {
    
    public static volatile boolean recreate = Boolean.getBoolean("recreate");
    
    public CreateTestTable(String name) {
        super(name);
    }

    public void testCreateTestTable() throws Exception {
        this.init();
        Connection con = datasource.getConnection();
        Statement st = con.createStatement();
        try {
            st.execute("create table test(id int not null, val1 varchar(255), val2 varchar(255), val3 varchar(255), val4 varchar(255))");
        }catch (Exception ignore) {}
        st.close();
        con.close();
    }
    
    public int testCheckData() throws Exception {
        int count = 0;
        String check = "select count (*) from test";
        this.init();
        Connection con = datasource.getConnection();
        Statement st = con.createStatement();
        try {
            ResultSet rs = st.executeQuery(check);
            
            if (rs.next())
                count = rs.getInt(1);
            System.out.println("Count:"+count);
        }catch (Exception ignore) {}
        return count;
    }
    
    public void testPopulateData() throws Exception {
        init();
        datasource.setJdbcInterceptors(ResetAbandonedTimer.class.getName());
        System.out.println("FILIP Using URL:"+this.datasource.getUrl());
        String insert = "insert into test values (?,?,?,?,?)";
        this.init();
        this.datasource.setRemoveAbandoned(false);
        Connection con = datasource.getConnection();
        if (recreate) {
            Statement st = con.createStatement();
            try {
                st.execute("drop table test");
            }catch (Exception ignore) {}
            st.execute("create table test(id int not null, val1 varchar(255), val2 varchar(255), val3 varchar(255), val4 varchar(255))");
            st.close();
        }
        PreparedStatement ps = con.prepareStatement(insert);
        ps.setQueryTimeout(0);
        for (int i=testCheckData(); i<100000; i++) {
            ps.setInt(1,i);
            String s = getRandom();
            ps.setString(2, s);
            ps.setString(3, s);
            ps.setString(4, s);
            ps.setString(5, s);
            ps.addBatch();
            ps.clearParameters();
            if ((i+1) % 1000 == 0) {
                System.out.print(".");
            }
            if ((i+1) % 10000 == 0) {
                System.out.print("\n"+(i+1));
                ps.executeBatch();
                ps.close();
                ps = con.prepareStatement(insert);
                ps.setQueryTimeout(0);
            }

        }
        ps.close();
        con.close();
    }
    
    public static Random random = new Random(System.currentTimeMillis());
    public static String getRandom() {
        StringBuffer s = new StringBuffer(256);
        for (int i=0;i<254; i++) {
            int b = Math.abs(random.nextInt() % 29);
            char c = (char)(b+65);
            s.append(c);
        }
        return s.toString();
    }
    
    public static void main(String[] args) throws Exception {
        recreate = true;
        CreateTestTable test = new CreateTestTable("CreateTestTable");
        test.testPopulateData();
    }

}
