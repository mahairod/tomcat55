<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/setProperty/positiveSetBooleanObj.jsp" label="positiveSetBooleanObjTest">
  <validate>
    <!--TEST STRATEGY: Using jsp\:useBean, create a new bean instance and set a Boolean property of the bean using a String constant.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/setProperty/positiveSetBooleanObj.html" ignoreWhitespace="true" label="Boolean properties of a Bean can be set with a String  Constant.  Conversion from String to Boolean will be automatically provided by java.lang.Boolean.valueOf(String). JavaServer Pages Specification v1.2, Sec. 4.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/setProperty/positiveSetBooleanPrim.jsp" label="positiveSetBooleanPrimTest">
  <validate>
    <!--TEST STRATEGY: Using jsp\:useBean, create a new bean instance and set a boolean property of the bean using a String constant.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/setProperty/positiveSetBooleanPrim.html" ignoreWhitespace="true" label="Primitive boolean properties of a Bean can be set with a String  Constant.  Conversion from String to boolean will be automatically provided by java.lang.Boolean.valueOf(String). JavaServer Pages Specification v1.2, Sec. 4.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/setProperty/positiveSetByteObj.jsp" label="positiveSetByteObjTest">
  <validate>
    <!--TEST STRATEGY: Using jsp\:useBean, create a new bean instance and set a Byte property of the bean using a String constant.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/setProperty/positiveSetByteObj.html" ignoreWhitespace="true" label="Byte properties of a Bean can be set with a String  Constant.  Conversion from String to Byte will be automatically provided by java.lang.Byte.valueOf(String). JavaServer Pages Specification v1.2, Sec. 4.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/setProperty/positiveSetBytePrim.jsp" label="positiveSetBytePrimTest">
  <validate>
    <!--TEST STRATEGY: Using jsp\:useBean, create a new bean instance and set a byte property of the bean using a String constant.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/setProperty/positiveSetBytePrim.html" ignoreWhitespace="true" label="Primitive byte properties of a Bean can be set with a String  Constant.  Conversion from String to byte will be automatically provided by java.lang.Byte.valueOf(String). JavaServer Pages Specification v1.2, Sec. 4.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/setProperty/positiveSetCharObj.jsp" label="positiveSetCharObjTest">
  <validate>
    <!--TEST STRATEGY: Using jsp\:useBean, create a new bean instance and set a Character property of the bean using a String constant.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/setProperty/positiveSetCharObj.html" ignoreWhitespace="true" label="Character properties of a Bean can be set with a String  Constant.  Conversion from String to Character will be automatically provided by String.charAt(0). JavaServer Pages Specification v1.2, Sec. 4.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/setProperty/positiveSetCharPrim.jsp" label="positiveSetCharPrimTest">
  <validate>
    <!--TEST STRATEGY: Using jsp\:useBean, create a new bean instance and set a char property of the bean using a String constant.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/setProperty/positiveSetCharPrim.html" ignoreWhitespace="true" label="Primitive char properties of a Bean can be set with a String  Constant.  Conversion from String to char will be automatically provided by String.charAt(0). JavaServer Pages Specification v1.2, Sec. 4.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/setProperty/positiveSetDoubleObj.jsp" label="positiveSetDoubleObjTest">
  <validate>
    <!--TEST STRATEGY: Using jsp\:useBean, create a new bean instance and set a Double property of the bean using a String constant.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/setProperty/positiveSetDoubleObj.html" ignoreWhitespace="true" label="Double properties of a Bean can be set with a String  Constant.  Conversion from String to Double will be automatically provided by java.lang.Double.valueOf(String). JavaServer Pages Specification v1.2, Sec. 4.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/setProperty/positiveSetDoublePrim.jsp" label="positiveSetDoublePrimTest">
  <validate>
    <!--TEST STRATEGY: Using jsp\:useBean, create a new bean instance and set a double property of the bean using a String constant.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/setProperty/positiveSetDoublePrim.html" ignoreWhitespace="true" label="Primitive double properties of a Bean can be set with a String  Constant.  Conversion from String to double will be automatically provided by java.lang.Double.valueOf(String). JavaServer Pages Specification v1.2, Sec. 4.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/setProperty/positiveSetFloatObj.jsp" label="positiveSetFloatObjTest">
  <validate>
    <!--TEST STRATEGY: Using jsp\:useBean, create a new bean instance and set a Float property of the bean using a String constant.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/setProperty/positiveSetFloatObj.html" ignoreWhitespace="true" label="Float properties of a Bean can be set with a String  Constant.  Conversion from String to Float will be automatically provided by java.lang.Float.valueOf(String). JavaServer Pages Specification v1.2, Sec. 4.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/setProperty/positiveSetFloatPrim.jsp" label="positiveSetFloatPrimTest">
  <validate>
    <!--TEST STRATEGY: Using jsp\:useBean, create a new bean instance and set a float property of the bean using a String constant.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/setProperty/positiveSetFloatPrim.html" ignoreWhitespace="true" label="Primitive float properties of a Bean can be set with a String  Constant.  Conversion from String to float will be automatically provided by java.lang.Float.valueOf(String). JavaServer Pages Specification v1.2, Sec. 4.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/setProperty/positiveSetIndexedProp.jsp" label="positiveSetIndexedPropTest">
  <validate>
    <!--TEST STRATEGY: Create a bean using useBean tag, use setProperty and set properties using the following array types\:  byte char short int float long double boolean Byte Character Short Integer Float Long Double Boolean Access each of the properties via scripting, iterate through the array, and display the values.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/setProperty/positiveSetIndexedProp.html" ignoreWhitespace="true" label="Indexed properties in a Bean can be set using jsp\:setProperty. When assigning values to indexed properties, the value must be an array.  No Type converstions are applied during assigment. JavaServer Pages Specification v1.2, Sec. 4.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/setProperty/positiveSetIntObj.jsp" label="positiveSetIntObjTest">
  <validate>
    <!--TEST STRATEGY: Using jsp\:useBean, create a new bean instance and set an Integer property of the bean using a String constant.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/setProperty/positiveSetIntObj.html" ignoreWhitespace="true" label="Integer properties of a Bean can be set with a String  Constant.  Conversion from String to Integer will be automatically provided by java.lang.Integer.valueOf(String). JavaServer Pages Specification v1.2, Sec. 4.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/setProperty/positiveSetIntPrim.jsp" label="positiveSetIntPrimTest">
  <validate>
    <!--TEST STRATEGY: Using jsp\:useBean, create a new bean instance and set an int property of the bean using a String constant.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/setProperty/positiveSetIntPrim.html" ignoreWhitespace="true" label="Primitive int properties of a Bean can be set with a String  Constant.  Conversion from String to int will be automatically provided by java.lang.Integer.valueOf(String). JavaServer Pages Specification v1.2, Sec. 4.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/setProperty/positiveSetLongObj.jsp" label="positiveSetLongObjTest">
  <validate>
    <!--TEST STRATEGY: Using jsp\:useBean, create a new bean instance and set an Long property of the bean using a String constant.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/setProperty/positiveSetLongObj.html" ignoreWhitespace="true" label="Long properties of a Bean can be set with a String  Constant.  Conversion from String to Long will be automatically provided by java.lang.Long.valueOf(String). JavaServer Pages Specification v1.2, Sec. 4.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/setProperty/positiveSetLongPrim.jsp" label="positiveSetLongPrimTest">
  <validate>
    <!--TEST STRATEGY: Using jsp\:useBean, create a new bean instance and set a long property of the bean using a String constant.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/setProperty/positiveSetLongPrim.html" ignoreWhitespace="true" label="Primitive long properties of a Bean can be set with a String  Constant.  Conversion from String to long will be automatically provided by java.lang.long.valueOf(String). JavaServer Pages Specification v1.2, Sec. 4.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/setProperty/positiveSetPropAll.jsp?name=Frodo&amp;num=116165&amp;str=Validated" label="positiveSetPropAllTest">
  <validate>
    <!--TEST STRATEGY: Using jsp\:useBean, create a new bean instance and set the property attribute to '*'.  The following properties should be set by the tag\: name, num, str.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/setProperty/positiveSetPropAll.html" ignoreWhitespace="true" label="If the property attribute of jsp\:setProperty is set to '*', the tag will iterate over the current Servlet request parameters, matching parameter names and value type(s) to property names and setter method types(s), setting each matched property to the value of the matching parameter. JavaServer Pages Specification v1.2, Sec. 4.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/setProperty/positiveSetPropNoParam.jsp?str=SAPPOTA" label="positiveSetPropNoParamTest">
  <validate>
    <!--TEST STRATEGY: Using jsp\:useBean, create a new bean instance. jsp\:setProperty only specifies the name and property properties.  The container should set the value of the Bean's property to the value of the request parameter that has the same name as specified by the property attribute.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/setProperty/positiveSetPropNoParam.html" ignoreWhitespace="true" label="When the param attribute is omitted, the request parameter name is assumed to be the same as the Bean property name. JavaServer Pages Specification v1.2, Sec. 4.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/setProperty/positiveSetPropParam.jsp?Name=MANGO" label="positiveSetPropParamTest">
  <validate>
    <!--TEST STRATEGY: Using jsp\:useBean, create a new bean instance. jsp\:setProperty only specifies the param property. The container should set the value of the Bean's property to the value of the request parameter that has the same name as specified by the param attribute.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/setProperty/positiveSetPropParam.html" ignoreWhitespace="true" label="A single bean property can be set using a request  parameter from the Request object.  JavaServer Pages Specification v1.2, Sec. 4.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/setProperty/positiveSetPropReqTimeSingleQuotes.jsp" label="positiveSetPropReqTimeSingleQuotesTest">
  <validate>
    <!--TEST STRATEGY: Using jsp\:useBean, create a new bean instance. Set the value of a bean property using a  request-time attribute expression delimited by single quotes.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/setProperty/positiveSetPropReqTimeSingleQuotes.html" ignoreWhitespace="true" label="The value attribute can accept request-time  attribute expressions (single-quoted) as a value. JavaServer Pages Specification v1.2, Sec. 4.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/setProperty/positiveSetPropReqTimeDoubleQuotes.jsp" label="positiveSetPropReqTimeDoubleQuotesTest">
  <validate>
    <!--TEST STRATEGY: Using jsp\:useBean, create a new bean instance. Set the value of a bean property using a  request-time attribute expression delimited by double quotes.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/setProperty/positiveSetPropReqTimeDoubleQuotes.html" ignoreWhitespace="true" label="The value attribute can accept request-time  attribute expressions (double-quoted) as a value. JavaServer Pages Specification v1.2, Sec. 4.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/setProperty/positiveSetPropValue.jsp" label="positiveSetPropValueTest">
  <validate>
    <!--TEST STRATEGY: Using jsp\:useBean, create a new bean instance. Set the value of a bean property using the value attribute.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/setProperty/positiveSetPropValue.html" ignoreWhitespace="true" label="Properties in bean can be set using a String Constant. JavaServer Pages Specification v1.2, Sec. 4.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/setProperty/positiveBeanPropertyEditor.jsp" label="positiveBeanPropertyEditorTest">
  <validate>
    <!--TEST STRATEGY: Create a bean using useBean tag, use setProperty and  and verfiy results using getProperty.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/setProperty/positiveBeanPropertyEditor.html" ignoreWhitespace="true" label="The container will use a bean's  property editor when setting properties, if  introspection indicates one exists. JavaServer Pages Specification v1.2, Sec. 4.2" />
  </validate>
</request>

