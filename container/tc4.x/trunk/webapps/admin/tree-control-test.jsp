<%@ taglib uri="/WEB-INF/tree-control.tld" prefix="tree" %>
<html>
<head>
<title>Tree Control Test</title>
<link rel="stylesheet" type="text/css" href="tree-control-test.css">
</head>
<body bgcolor="white">
This is the current state of the tree control:
<hr>
<tree:render tree="treeControlTest"
           action="treeControlTest.do?tree=${name}"
            style="tree-control"
    styleSelected="tree-control-selected"
  styleUnselected="tree-control-unselected"
/>
<hr>
</body>
</html>
