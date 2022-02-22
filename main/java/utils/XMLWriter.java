package utils;

import javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class XMLWriter extends IndentingXMLStreamWriter {
    private String sourceName = "";

    public XMLWriter(String resourceName) {
        this(FileOutputStreamOrNull(resourceName));
        sourceName = resourceName;
    }

    private static OutputStream FileOutputStreamOrNull(String resourceName) {
        try {
            return new FileOutputStream(resourceName);
        } catch (FileNotFoundException ex) {
            SLF4J.logException("Cannot create file " + resourceName, ex);
            return null;
        }
    }

    public XMLWriter(OutputStream outputStream) {
        super(createXMLStreamWriter(outputStream));
    }

    public static XMLStreamWriter createXMLStreamWriter(OutputStream output) {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter xmlStreamWriter = null;
        try {
            xmlStreamWriter = outputFactory.createXMLStreamWriter(output);
        } catch (XMLStreamException e) {
            SLF4J.logException("Cannot attach XMLStreamWriter to file stream handle", e);
        }
        return xmlStreamWriter;
    }
}
