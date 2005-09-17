/* ========================================================================= *
 *                                                                           *
 *                 The Apache Software License,  Version 1.1                 *
 *                                                                           *
 *         Copyright (c) 1999, 2000  The Apache Software Foundation.         *
 *                           All rights reserved.                            *
 *                                                                           *
 * ========================================================================= *
 *                                                                           *
 * Redistribution and use in source and binary forms,  with or without modi- *
 * fication, are permitted provided that the following conditions are met:   *
 *                                                                           *
 * 1. Redistributions of source code  must retain the above copyright notice *
 *    notice, this list of conditions and the following disclaimer.          *
 *                                                                           *
 * 2. Redistributions  in binary  form  must  reproduce the  above copyright *
 *    notice,  this list of conditions  and the following  disclaimer in the *
 *    documentation and/or other materials provided with the distribution.   *
 *                                                                           *
 * 3. The end-user documentation  included with the redistribution,  if any, *
 *    must include the following acknowlegement:                             *
 *                                                                           *
 *       "This product includes  software developed  by the Apache  Software *
 *        Foundation <http://www.apache.org/>."                              *
 *                                                                           *
 *    Alternately, this acknowlegement may appear in the software itself, if *
 *    and wherever such third-party acknowlegements normally appear.         *
 *                                                                           *
 * 4. The names  "The  Jakarta  Project",  "Tomcat",  and  "Apache  Software *
 *    Foundation"  must not be used  to endorse or promote  products derived *
 *    from this  software without  prior  written  permission.  For  written *
 *    permission, please contact <apache@apache.org>.                        *
 *                                                                           *
 * 5. Products derived from this software may not be called "Apache" nor may *
 *    "Apache" appear in their names without prior written permission of the *
 *    Apache Software Foundation.                                            *
 *                                                                           *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES *
 * INCLUDING, BUT NOT LIMITED TO,  THE IMPLIED WARRANTIES OF MERCHANTABILITY *
 * AND FITNESS FOR  A PARTICULAR PURPOSE  ARE DISCLAIMED.  IN NO EVENT SHALL *
 * THE APACHE  SOFTWARE  FOUNDATION OR  ITS CONTRIBUTORS  BE LIABLE  FOR ANY *
 * DIRECT,  INDIRECT,   INCIDENTAL,  SPECIAL,  EXEMPLARY,  OR  CONSEQUENTIAL *
 * DAMAGES (INCLUDING,  BUT NOT LIMITED TO,  PROCUREMENT OF SUBSTITUTE GOODS *
 * OR SERVICES;  LOSS OF USE,  DATA,  OR PROFITS;  OR BUSINESS INTERRUPTION) *
 * HOWEVER CAUSED AND  ON ANY  THEORY  OF  LIABILITY,  WHETHER IN  CONTRACT, *
 * STRICT LIABILITY, OR TORT  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN *
 * ANY  WAY  OUT OF  THE  USE OF  THIS  SOFTWARE,  EVEN  IF  ADVISED  OF THE *
 * POSSIBILITY OF SUCH DAMAGE.                                               *
 *                                                                           *
 * ========================================================================= *
 *                                                                           *
 * This software  consists of voluntary  contributions made  by many indivi- *
 * duals on behalf of the  Apache Software Foundation.  For more information *
 * on the Apache Software Foundation, please see <http://www.apache.org/>.   *
 *                                                                           *
 * ========================================================================= */
package org.apache.service;

/**
 * The <code>Service</code> interface abstracts the concept of a service
 * provider (usually referred as &quot;daemon&quot; in Unix land).
 *
 * @author <a href="mailto:pier.fumagalli@eng.sun.com">Pier Fumagalli</a>,
 *         Sun Microsystems, Inc.
 * @author Copyright &copy; 1999-2000 <a href="http://www.apache.org">The
 *         Apache Software Foundation</a>
 * @version CVS $Revision$ $Date$
 */
public interface Service {
    /**
     * Initializes this <code>Service</code> instance.
     * <p>
     *   This method must return to the caller as soon as this
     *   <code>Service</code> instance has been initialized.
     * </p>
     *
     * @param manager A <code>ServiceManager</code> instance used by this
     *                to control its restart, shutdown and to log.
     * @param arguments The command line arguments (as they would have been
     *                  passed to the <code>main(...)</code> static method in
     *                  a generic application.
     * @exception ServiceException If this <code>Service</code> instance cannot
     *                             be initialized properly.
     */
    public void init(ServiceManager manager, String arguments[])
    throws ServiceException;

    /**
     * Starts this <code>Service</code> instance.
     * <p>
     *   This method must return to the caller as soon as this
     *   <code>Service</code> instance has been started.
     * </p>
     * <p>
     *   A <code>Service</code> implementation must not try to call this
     *   method directly but rather wait for the underlying native library
     *   to invoke it.
     * </p>
     *
     * @exception ServiceException If this <code>Service</code> instance cannot
     *                             be started properly.
     */
    public void start()
    throws ServiceException;

    /**
     * Stop this <code>Service</code> instance.
     * <p>
     *   This method must return to the caller as soon as this
     *   <code>Service</code> instance has been stopped.
     * </p>
     * <p>
     *   A <code>Service</code> implementation must not try to call this
     *   method directly but rather wait for the underlying native library
     *   to invoke it. In the event a <code>Service</code> wants to stop
     *   itself, the <code>ServiceManager.queryStop()</code> method must
     *   be used.
     * </p>
     *
     * @exception ServiceException If this <code>Service</code> instance cannot
     *                             be stopped properly.
     */
    public void stop()
    throws ServiceException;
    
    /**
     * Restarts this <code>Service</code> instance.
     * <p>
     *   This method must return to the caller as soon as this
     *   <code>Service</code> instance has been been restarted.
     * </p>
     * <p>
     *   A <code>Service</code> implementation must not try to call this
     *   method directly but rather wait for the underlying native library
     *   to invoke it. In the event a <code>Service</code> wants to restart
     *   itself, the <code>ServiceManager.queryRestart()</code> method must
     *   be used.
     * </p>
     *
     * @exception ServiceException If this <code>Service</code> instance cannot
     *                             be restarted properly. (No further attempts
     *                             are made, and the <code>shutdown()</code>
     *                             method is called, and the VM exits).
     * @exception UnsupportedOperationException If this <code>Service</code>
     *                                          instance cannot be restarted.
     *                                          (The <code>shutdown()</code>
     *                                          method is called, the entire
     *                                          VM is restarted, and then the
     *                                          <code>startup()</code> method
     *                                          is finally called).
     */
    public void restart()
    throws ServiceException, UnsupportedOperationException;
}
