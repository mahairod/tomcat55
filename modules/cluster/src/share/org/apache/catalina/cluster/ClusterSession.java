package org.apache.catalina.cluster;

import org.apache.catalina.Session;

public interface ClusterSession extends Session {
   /**
    * returns true if this session is the primary session, if that is the
    * case, the manager can expire it upon timeout.
    * @return
    */
   public boolean isPrimarySession();

   /**
    * Sets whether this is the primary session or not.
    * @param primarySession
    */
   public void setPrimarySession(boolean primarySession);

}