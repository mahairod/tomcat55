package org.apache.watchdog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class GTestConverter {

    public static void main(String args[]) {

        String xmlFile = args[0];

        try {
            GTestConverter converter = new GTestConverter();
            Document doc = converter.parseGTestFile(xmlFile);
            //System.out.println(converter.documentToString(doc));
            converter.convertJSPToFiles(doc);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public void convertJSPToFiles(Document doc) throws IOException {

        XMLOutputter outputter = new XMLOutputter("  ", true);

        HashSet pathSet = new HashSet();
        ArrayList entList = new ArrayList();

        List list = doc.getRootElement().getContent();
        Iterator contentIterator = list.iterator();
        while (contentIterator.hasNext()) {
            Object o = contentIterator.next();
            if (o instanceof Comment) {
                Comment comment = (Comment) o;
                FileWriter writer = new FileWriter("JSPTests.garbage", true);
                outputter.output(comment,writer);
                writer.write("\n");
                writer.close();
            } else if (o instanceof Element) {
                Element element = (Element) o;

                // generate a new directory structure based on
                // the old path
                String path = element.getAttributeValue("path");
                // remove leading slash and JSP filename
                path = path.substring(1,path.lastIndexOf("/"));

                File file = new File(path + "/tests.mod");
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }

                FileWriter modWriter = new FileWriter(path + "/tests.mod", true);
                outputter.output(element,modWriter);
                modWriter.write("\n\n");
                modWriter.close();


                // add it to the DTD
                if (pathSet.contains(path) == false) {
                    pathSet.add(path);
                    FileWriter dtdWriter = new FileWriter("dtds/jsp-tests.ent",true);
                    dtdWriter.write("<!ENTITY ");

                    StringTokenizer tokenizer = new StringTokenizer(path,"/");
                    StringBuffer entName = new StringBuffer();
                    while (tokenizer.hasMoreTokens()) {
                        String token = tokenizer.nextToken();
                        entName.append(token);
                        if (tokenizer.hasMoreTokens()) {
                            entName.append(".");
                        }
                    }
                    dtdWriter.write(entName.toString());
                    entList.add(entName.toString());

                    dtdWriter.write(" SYSTEM \"");
                    path += "/tests.mod";
                    dtdWriter.write(path);
                    dtdWriter.write("\">\n");
                    dtdWriter.close();
                }

            } else {
                //throw new IOException("Unexcepcted JDOM Node: " + o);
            }
        }

        FileWriter dtdWriter = new FileWriter("dtds/jsp-tests.ent",true);
        dtdWriter.write("\n");
        dtdWriter.write("<!ENTITY jsp-tests.testAll \"\n");

        Iterator entIterator = entList.iterator();
        while (entIterator.hasNext()) {
            String ent = ( String ) entIterator.next();
            dtdWriter.write("&");
            dtdWriter.write(ent);
            dtdWriter.write(";\n");
        }

        dtdWriter.write("\">");
        dtdWriter.close();
    }

    /**
     * Returns the test report converted from JDOM to a
     * text string.
     *
     * @return the test report as an XML string
     * @throws IOException if the XML formatter cannot convert the JDOM object
     *                     to text
     */
    public String documentToString(Document document) throws IOException {
        // get the xml string from the listener
        XMLOutputter outputter = new XMLOutputter("  ", true);
        return outputter.outputString(document);
    }

    public Document parseGTestFile(String xmlFile) 
    throws IOException {
        GTestContentHandler handler = new GTestContentHandler();
        InputSource inputSource = new InputSource(xmlFile);

        SAXParserFactory factory = SAXParserFactory.newInstance();

        try {
            SAXParser parser = factory.newSAXParser();
            parser.setProperty("http://xml.org/sax/properties/lexical-handler", handler);

            parser.parse(inputSource, handler);

        } catch (ParserConfigurationException e) {
            throw new IOException(e.toString());
        } catch (SAXException e) {
            throw new IOException(e.toString());
        }

        return handler.getDocument();
    }

}
