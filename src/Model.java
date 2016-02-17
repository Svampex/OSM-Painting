
import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Path2D;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

enum WayType { UNKNOWN, ROAD, WATER }

public class Model extends Observable implements Serializable {
	public static final long serialVersionUID = 20160217;
	List<Shape> data = new ArrayList<>();
	List<Shape> road = new ArrayList<>();
	List<Shape> water = new ArrayList<>();
	float minlat, minlon, maxlat, maxlon;

	public void dirty() {
		setChanged();
		notifyObservers();
	}

	public void add(Shape s) {
		data.add(s);
		dirty();
	}

	public static Model load(String filename) {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
			return (Model) in.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public void save(String filename) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
			out.writeObject(this);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Model(String filename) {
		try {
			InputSource in = null;
			if (filename.matches(".*zip")) {
				ZipInputStream input = new ZipInputStream(new FileInputStream(filename));
				input.getNextEntry();
				in = new InputSource(input);
			} else {
				in = new InputSource(filename);
			}
			XMLReader reader = XMLReaderFactory.createXMLReader();
			reader.setContentHandler(new OSMHandler());
			reader.parse(in);
		} catch (IOException | SAXException e) {
			throw new RuntimeException(e);
		}
	}

	class OSMHandler extends DefaultHandler {
		Map<Long,Point2D> map = new HashMap<>();
		Path2D way;
		WayType type = WayType.UNKNOWN;
		float lonfactor;
		public void startDocument() {}
		public void startElement(String uri, String localName, String qName, Attributes atts) {
			switch (qName) {
				case "way":
					type = WayType.UNKNOWN;
					break;
				case "bounds":
					minlat = Float.parseFloat(atts.getValue("minlat"));
					minlon = Float.parseFloat(atts.getValue("minlon"));
					maxlat = Float.parseFloat(atts.getValue("maxlat"));
					maxlon = Float.parseFloat(atts.getValue("maxlon"));
					lonfactor = (float)Math.cos(Math.PI/180*(minlat + (maxlat - minlat)/2));
					System.out.println(lonfactor);
					minlat = -minlat;
					maxlat = -maxlat;
					minlon *= lonfactor;
					maxlon *= lonfactor;
					break;
				case "node":
					long id = Long.parseLong(atts.getValue("id"));
					float lat = Float.parseFloat(atts.getValue("lat"));
					float lon = Float.parseFloat(atts.getValue("lon"));
					map.put(id, new Point2D.Float(lon*lonfactor, -lat));
					break;
				case "nd":
					id = Long.parseLong(atts.getValue("ref"));
					Point2D p = map.get(id);
					if (way == null) {
						way = new Path2D.Float();
						way.moveTo(p.getX(), p.getY());
					} else {
						way.lineTo(p.getX(), p.getY());
					}
					break;
				case "tag":
					switch (atts.getValue("k")) {
						case "highway":
							type = WayType.ROAD;
							break;
						case "natural":
							if (atts.getValue("v").equals("water")) type = WayType.WATER;
							break;
					}
					break;
				default:
					break;
			}
		}
		public void characters(char[] ch, int start, int length) {}
		public void endElement(String uri, String localName, String qName) {
			switch (qName) {
				case "way":
					switch (type) {
						case ROAD:
							road.add(way);
							break;
						case WATER:
							water.add(way);
							break;
						default:
							//data.add(way);
							break;
					}
					way = null;
					break;
			}
		}
		public void endDocument() {}
	}

}
