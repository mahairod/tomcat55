/*
 * $Header: /home/cvspublic/jakarta-tomcat-4.0/catalina/src/test/org/apache/catalina/realm/JNDIRealmTestCase.java
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
package org.apache.catalina.realm;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author Jeff Tulley
 */
public class JNDIRealmTestCase extends TestCase {
    public JNDIRealmTestCase(String arg0) {
        super(arg0);
    }

    public void testParseSimpleUserPattern() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath = "o=somepath";
        String[] expected = { searchPath };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void testParseSimpleUserPatternWithParens() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath = "(o=somepath)";
        String[] expected = { "o=somepath" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    // would somebody do this?  Probably not, but it wouldn't hurt to support it for resiliency sake
    public void testParseSimpleUserPatternWithParensAndLDAPOr() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath = "(|(o=somepath))";
        String[] expected = { "o=somepath" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void testParseSimpleUserPatternWithParensPathological() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath = "(o=so\\(me\\)path)";
        String[] expected = { "o=so\\(me\\)path" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void testParseSimpleUserPatternWithParensReallyPathological() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath = "(o=so\\(mepath\\))";
        String[] expected = { "o=so\\(mepath\\)" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void testParseTwoPartUserPattern() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath = "(o=somepath)(o=someotherpath)";
        String[] expected = { "o=somepath", "o=someotherpath" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    // people will probably try this, need to handle it
    public void testParseLegitLDAPTwoPartUserPattern() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath = "(|(o=somepath)(o=someotherpath))";
        String[] expected = { "o=somepath", "o=someotherpath" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void testParseTwoPartUserPatternIgnoreWhitespace() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath = " (o=somepath)   (o=someotherpath)    ";
        String[] expected = { "o=somepath", "o=someotherpath" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    // people will probably try this, need to handle it
    public void testParseLegitLDAPTwoPartUserPatternIgnoreWhitespace() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath = "(| (o=somepath)         (o=someotherpath)  )";
        String[] expected = { "o=somepath", "o=someotherpath" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void testParseTwoPartUserPatternPathological() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath = "(o=somepath)(o=someo\\(ther\\(path)";
        String[] expected = { "o=somepath", "o=someo\\(ther\\(path" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void testParseLegitLDAPTwoPartUserPatternPathological() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath = "(|(o=somepath)(o=someo\\(ther\\(path))";
        String[] expected = { "o=somepath", "o=someo\\(ther\\(path" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void testParseMultiPartUserPattern() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath =
            "(o=somepath)(o=someotherpath)(o=somepath2)(o=someotherpath2)(o=somepath3)(o=someotherpath3)";
        String[] expected =
            {
                "o=somepath",
                "o=someotherpath",
                "o=somepath2",
                "o=someotherpath2",
                "o=somepath3",
                "o=someotherpath3" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void testParseComplexMultiPartUserPattern() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath =
            "(cn={0},ou=ourou,o=somepath)(cn={0},ou=ourou,o=somepath2)(cn={0},ou=ourou,o=someotherpath2)";
        String[] expected =
            {
                "cn={0},ou=ourou,o=somepath",
                "cn={0},ou=ourou,o=somepath2",
                "cn={0},ou=ourou,o=someotherpath2" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void testParseLegitLDAPComplexMultiPartUserPattern() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath =
            "(|(cn={0},ou=ourou,o=somepath)(cn={0},ou=ourou,o=somepath2)(cn={0},ou=ourou,o=someotherpath2))";
        String[] expected =
            {
                "cn={0},ou=ourou,o=somepath",
                "cn={0},ou=ourou,o=somepath2",
                "cn={0},ou=ourou,o=someotherpath2" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void testParseComplexMultiPartUserPatternEdirectorySyntax() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath =
            "(|(cn={0}.ou=ourou.o=somepath)(cn={0}.ou=ourou.o=somepath2)(cn={0}.ou=ourou.o=someotherpath2))";
        String[] expected =
            {
                "cn={0}.ou=ourou.o=somepath",
                "cn={0}.ou=ourou.o=somepath2",
                "cn={0}.ou=ourou.o=someotherpath2" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void testParseComplexMultiPartUserPatternTypelessEdirectorySyntax() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath =
            "(|({0}.ourou.somepath)({0}.ourou.somepath2)({0}.ourou.someotherpath2))";
        String[] expected =
            {
                "{0}.ourou.somepath",
                "{0}.ourou.somepath2",
                "{0}.ourou.someotherpath2" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void testParseEmptyUserPattern() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath = "";
        String[] expected = { "" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void testParseEmptyUserPatternWithParens() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath = "()";
        String[] expected = { "" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void assertStringArraysEquals(String[] expected, String[] actual) {
        Assert.assertTrue("not null", actual != null);
        Assert.assertEquals("array count is wrong", expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            Assert.assertEquals("element " + i + " is wrong", expected[i], actual[i]);
        }
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JNDIRealmTestCase.class);
    }
}


