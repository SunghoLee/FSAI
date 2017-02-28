package kr.ac.kaist.activity.injection.decompile.parser;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

/**
 * Created by leesh on 21/02/2017.
 */
public class ManifestParser {
    public static String MANIFEST_NAME = "AndroidManifest.xml";

    public Element parse(String path) throws ParserConfigurationException, SAXException, IOException{
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser parser = saxParserFactory.newSAXParser();
        ManifestHandler handler = new ManifestHandler();
        parser.parse(new File(path), handler);
        return handler.getRoot();
    }

    class ManifestHandler extends DefaultHandler {
        private Element root;
        private Element curElement;

        @Override
        public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes) throws SAXException {
            if(qName.equals("manifest"))
                root = curElement = new ManifestElement(qName, new Attributes(attributes));
            else {
                Element newElement = new ComponentElement(qName, new Attributes(attributes), curElement);
                curElement.addChild(newElement);
                curElement = newElement;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if(!qName.equals("manifest"))
                curElement = ((ComponentElement)curElement).parent;
        }

        public Element getRoot(){
            return root;
        }
    }


    static public class Element {
        private final Set<Element> children;
        private final String name;
        private final Attributes attrs;
        private static String INDENT = "\t";

        public Element(String name, Attributes attrs){
            this.children = new HashSet<>();
            this.name = name;
            this.attrs = attrs;
        }

        public Set<Element> getChildren(){
            return this.children;
        }

        public Attributes getAttributes(){
            return this.attrs;
        }

        public void addChild(Element child){
            children.add(child);
        }

        public String getName(){
            return this.name;
        }

        @Override
        public String toString(){
            return toString("");
        }

        private String toString(String indent){
            String res = indent + this.name + "[ " + this.attrs + " ]\n";

            for(Element child : this.children)
                res += child.toString(indent + INDENT);
            return res;
        }

        private String attrsToString(Attributes attrs){
            String res = "";

            for(Iterator<String> iAttr = attrs.iterator(); iAttr.hasNext();){
                String attr = iAttr.next();
                String value = attrs.getValue(attr);
                res += value + " : " + value + ((iAttr.hasNext())? "" : ", ");
            }
            return res;
        }

        public void forEach(Consumer<Element> elementConsumer){
            elementConsumer.accept(this);
            for(Element child : children)
                child.forEach(elementConsumer);
        }
    }


    static final public class ManifestElement extends Element {
        public ManifestElement(String name, Attributes attrs) {
            super(name, attrs);
        }
    }

    static final public class ComponentElement extends Element {
        private final Element parent;

        public ComponentElement(String name, Attributes attrs, Element parent){
            super(name, attrs);
            this.parent = parent;
        }

        public Element getParent(){
            return this.parent;
        }
    }

    static final public class Attributes implements Iterable<String>{
        private final Map<String, String> attrMap;

        public Attributes(org.xml.sax.Attributes attrs){
            attrMap = new HashMap<>();

            for(int i=0; i<attrs.getLength(); i++){
                String type = attrs.getQName(i);
                String value = attrs.getValue(i);
                attrMap.put(type, value);
            }
        }

        public boolean contains(String attrName){
            return attrMap.containsKey(attrName);
        }

        public String getValue(String attrName){
            return attrMap.get(attrName);
        }

        public Set<String> keySet(){
            return attrMap.keySet();
        }

        @Override
        public Iterator<String> iterator() {
            return attrMap.keySet().iterator();
        }

        @Override
        public void forEach(Consumer<? super String> action) {
            attrMap.keySet().forEach(action);
        }

        @Override
        public Spliterator<String> spliterator() {
            return attrMap.keySet().spliterator();
        }

        @Override
        public String toString(){
            String res = "";

            for(Iterator<String> iAttr = this.iterator(); iAttr.hasNext();){
                String attr = iAttr.next();
                String value = this.getValue(attr);
                res += attr + " : " + value + ((iAttr.hasNext())? ", " : "");
            }
            return res;
        }
    }
}
