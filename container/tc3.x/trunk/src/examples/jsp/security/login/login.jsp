<html>
<!--   
    Copyright 1999-2004 The Apache Software Foundation
  
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
  
        http://www.apache.org/licenses/LICENSE-2.0
  
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
-->
<body>
<h1>Login page for examples</h1>

<form method="POST" action='<%= response.encodeURL("j_security_check")%>' >
 Username: <input type="text" name="j_username"><br>
 Password: <input type="password" name="j_password"><br> 
 <br>
 <input type="submit" value="login" name="j_security_check">
</form>

</body>
</html>
