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
package async;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import async.Stockticker.Stock;
import async.Stockticker.TickListener;

public class AsyncStockServlet extends HttpServlet implements TickListener, AsyncListener{

    public static final String POLL = "POLL";
    public static final String LONG_POLL = "LONG-POLL";
    public static final String STREAM = "STREAM";
    
    static ArrayList<Stock> ticks = new ArrayList<Stock>();
    static ConcurrentLinkedQueue<AsyncContext> clients = new ConcurrentLinkedQueue<AsyncContext>();
    static AtomicInteger clientcount = new AtomicInteger(0);
    static Stockticker ticker = new Stockticker();
    
    public AsyncStockServlet() {
        System.out.println("AsyncStockServlet created");
    }
    
    

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        //get the client Id, it should be in the URL
        String clientId = req.getParameter("clientId");
        //get the method this client prefers
        String method = req.getParameter("method");

        //if the client doesn't have a method defined, then we will assume it is POLLING
        if (method==null) method = POLL;
        //if the client hasn't specified its own clientId, abort
//        if (clientId==null) {
//            resp.sendError(404,"Client not found.");
//            return;
//        }
        
        if (req.isAsyncStarted()) {
            req.getAsyncContext().complete();
        } else if (req.isAsyncSupported()) {
            AsyncContext actx = req.startAsync();
            req.addAsyncListener(this);
            resp.setContentType("text/plain");
            clients.add(actx);
            if (this.clientcount.incrementAndGet()==1) {
                ticker.addTickListener(this);
            }
        } else {
            new Exception("Async Not Supported").printStackTrace();
            resp.sendError(400,"Async is not supported.");
        }
    }



    @Override
    public void tick(Stock stock) {
        ticks.add((Stock)stock.clone());
        Iterator<AsyncContext> it = clients.iterator();
        while (it.hasNext()) {
            AsyncContext actx = it.next();
            writeStock(actx, stock);
        }
    }
    
    public void writeStock(AsyncContext actx, Stock stock) {
        HttpServletResponse response = (HttpServletResponse)actx.getResponse();
        try {
            PrintWriter writer = response.getWriter();
            writer.write("STOCK#");//make client parsing easier
            writer.write(stock.getSymbol());
            writer.write("#");
            writer.write(stock.getValueAsString());
            writer.write("#");
            writer.write(stock.getLastChangeAsString());
            writer.write("#");
            writer.write(String.valueOf(stock.getCnt()));
            writer.write("\n");
            writer.flush();
            response.flushBuffer();
        }catch (IOException x) {
            try {actx.complete();}catch (Exception ignore){}
        }
    }

    @Override
    public void onComplete(AsyncEvent event) throws IOException {
        if (clients.remove(event.getRequest().getAsyncContext()) && clientcount.decrementAndGet()==0) {
            ticker.removeTickListener(this);
        }
    }

    @Override
    public void onError(AsyncEvent event) throws IOException {
        event.getRequest().getAsyncContext().complete();
    }

    @Override
    public void onTimeout(AsyncEvent event) throws IOException {
        event.getRequest().getAsyncContext().complete();
    }
    
    
    
    
    

}
