/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package async;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

public class Async2 extends HttpServlet {
    private static final Log log = LogFactory.getLog(Async2.class);
    public Async2() {
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final AsyncContext actx = req.startAsync();
        actx.setTimeout(30*1000);
        Runnable run = new Runnable() {
            public void run() {
                try {
                    Thread.currentThread().setName("Async2-Thread");
                    log.info("Putting AsyncThread to sleep");
                    Thread.sleep(2*1000);
                    log.info("Writing data.");
                    actx.getResponse().getWriter().write("Output from background thread. Time:"+System.currentTimeMillis()+"\n");
                    actx.complete();
                }catch (InterruptedException x) {
                    log.error("Async2",x);
                }catch (IllegalStateException x) {
                    log.error("Async2",x);
                }catch (IOException x) {
                    log.error("Async2",x);
                }
            }
        };
        Thread t = new Thread(run);
        t.start();
    }
    

}
