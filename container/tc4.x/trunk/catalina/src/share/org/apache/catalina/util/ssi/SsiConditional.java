/*
 * SsiConditional.java
 * $Header$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */

package org.apache.catalina.util.ssi;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletOutputStream;

/**
 *  SSI command that handles all conditional directives.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public final class SsiConditional extends AbstractSsiCommand {

    /**
     *  Returns true, conditional directives should always
     *  be executed.
     */
    public boolean alwaysExecute() {
        return true;
    }

    /**
     *  Runs this command using the specified parameters.
     *
     *  @param cmdName  The name that was used to lookup this
     *                  command instance.
     *  @param argNames String array containing the parameter
     *                  names for the command.
     *  @param argVals  String array containing the paramater
     *                  values for the command.
     *  @param ssiEnv   The environment to use for command
     *                  execution.
     *  @param out      A convenient place for commands to
     *                  write their output.
     */
    public void execute( String cmdName, String[] argNames,
                         String[] argVals, SsiEnvironment ssiEnv,
                         ServletOutputStream out )
                                    throws SsiCommandException {

        // Retrieve the current state information
        ConditionState state = getState( ssiEnv );
        boolean disableOutput = ssiEnv.isOutputDisabled();

        if ("if".equals( cmdName )) {
            // Do nothing if we are nested in a false branch
            // except count it
            if (disableOutput) {
                state.nestingCount++;
                return;
            }

            state.nestingCount = 0;

            // Evaluate the expression
            if (evaluateArguments(argNames, argVals, ssiEnv)) {
                // No more branches can be taken for this if block
                state.branchTaken = true;
            } else {
                // Do not process this branch
                ssiEnv.setOutputDisabled(true);
                state.branchTaken = false;
            }

        } else if ("elif".equals( cmdName )) {
            // No need to even execute if we are nested in
            // a false branch
            if (state.nestingCount > 0)
                return;

            // If a branch was already taken in this if block
            // then disable output and return
            if (state.branchTaken) {
                ssiEnv.setOutputDisabled(true);
                return;
            }

            // Evaluate the expression
            if (evaluateArguments(argNames, argVals, ssiEnv)) {
                // Turn back on output and mark the branch
                ssiEnv.setOutputDisabled(false);
                state.branchTaken = true;
            } else {
                // Do not process this branch
                ssiEnv.setOutputDisabled(true);
                state.branchTaken = false;
            }

        } else if ("else".equals( cmdName )) {
            // No need to even execute if we are nested in
            // a false branch
            if (state.nestingCount > 0)
                return;

            // If we've already taken another branch then
            // disable output otherwise enable it.
            ssiEnv.setOutputDisabled( state.branchTaken );

            // And in any case, it's safe to say a branch
            // has been taken.
            state.branchTaken = true;

        } else if ("endif".equals( cmdName )) {
            // If we are nested inside a false branch then pop out
            // one level on the nesting count
            if (state.nestingCount > 0) {
                state.nestingCount--;
                return;
            }

            // Turn output back on
            ssiEnv.setOutputDisabled(false);

            // Reset the branch status for any outer if blocks,
            // since clearly we took a branch to have gotten here
            // in the first place.
            state.branchTaken = true;

        } else {
            throw new SsiCommandException( "Not a conditional command:" + cmdName );
        }

    }

    /**
     *  Returns the current ConditionalState as retrieved from
     *  the specified SsiEnvironment.  If no state exists, one
     *  will be created.
     */
    private ConditionState getState( SsiEnvironment ssiEnv ) {
        ConditionState state = null;
        state = (ConditionState)ssiEnv.getCommandVariable( "conditionState" );
        if (state == null) {
            state = new ConditionState();
            ssiEnv.setCommandVariable( "conditionState", state );
        }
        return state;
    }

    /**
     *  Retrieves the expression from the specified arguments
     *  and peforms the necessary evaluation steps.
     */
    private boolean evaluateArguments( String[] names, String[] vals,
                                       SsiEnvironment ssiEnv )
                                            throws SsiCommandException {
        String expr = getExpression( names, vals );
        if (expr == null)
            throw new SsiCommandException( "No expression specified." );

        try {
            ExpressionParseTree tree = new ExpressionParseTree( expr,
                                                                ssiEnv );
            return tree.evaluateTree();
        } catch (ParseException e) {
            throw new SsiCommandException( "Error parsing expression." );
        }
    }

    /**
     *  Returns the "expr" if the arg name is appropriate, otherwise
     *  returns null.
     */
    private String getExpression( String[] argNames, String[] argVals ) {
        if ("expr".equals(argNames[0]))
            return argVals[0];
        return null;
    }

    /**
     *  Stored as a SsiEnvironment configuration variable, this
     *  class contains the state information necessary to process
     *  the nested if's and set the disableOutput variable
     *  appropriately.
     */
    private class ConditionState {

        /**
         *  Set to true if the current conditional has already been
         *  completed, i.e.: a branch was taken.
         */
        boolean branchTaken = false;

        /**
         *  Counts the number of nested false branches.
         */
        int nestingCount = 0;
    }
}
