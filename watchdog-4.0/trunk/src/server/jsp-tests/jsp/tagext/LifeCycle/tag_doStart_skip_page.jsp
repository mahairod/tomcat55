<%-- Test for doEndTag SKIP_PAGE.  This should get the error page --%>
<%@ taglib uri="/TestLib.tld" prefix="x" %>

<x:count skipPage="false">
once upon a time
</x:count>

<% if(true) throw new Error("Go to the Error page!"); %>

