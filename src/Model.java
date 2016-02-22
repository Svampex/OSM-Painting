
import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Path2D;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

enum WayType { UNKNOWN, ROAD, WATER, PARK, GRASS, SEA, HEDGE, PLAYGROUND, BUILDING, PARKING, RELATION }

public class Model extends Observable implements Serializable {
	public static final long serialVersionUID = 20160217;
	List<Shape> data = new ArrayList<>();
	HashMap<Long,MapPath> ways = new HashMap<Long,MapPath>();
	float minlat, minlon, maxlat, maxlon;

	public void dirty() {
		setChanged();
		notifyObservers();
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
		MapPath way;
		MapPath relation;

		WayType type = WayType.UNKNOWN;
		long wayID;
		float lonfactor;
		public void startDocument() {}
		public void startElement(String uri, String localName, String qName, Attributes atts) {
			switch (qName) {
				case "way":
					wayID = Long.parseLong(atts.getValue("id"));
					type = WayType.UNKNOWN;
					break;
				case "bounds":
					minlat = Float.parseFloat(atts.getValue("minlat"));
					minlon = Float.parseFloat(atts.getValue("minlon"));
					maxlat = Float.parseFloat(atts.getValue("maxlat"));
					maxlon = Float.parseFloat(atts.getValue("maxlon"));
					lonfactor = (float)Math.cos(Math.PI/180*(minlat + (maxlat - minlat)/2));
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
						way = new MapPath();
						way.moveTo(p.getX(), p.getY());
					} else {
						way.lineTo(p.getX(), p.getY());
					}
					break;
				case "relation":
					wayID = Long.parseLong(atts.getValue("id"));
					relation = new MapPath();
					break;
				case "member":
					way = ways.get(Long.parseLong(atts.getValue("ref")));
					break;
				case "tag":
					switch (atts.getValue("k")) {
						case "highway":
							type = WayType.ROAD;
							break;
						case "natural":
							if (atts.getValue("v").equals("water")) type = WayType.WATER;
							if(atts.getValue("v").equals("coastline")) type = WayType.UNKNOWN;
							break;
						case "leisure":
							if(atts.getValue("v").equals("park")) type = WayType.PARK;
							if(atts.getValue("v").equals("playground")) type = WayType.PLAYGROUND;
							break;
						case "barrier":
							if(atts.getValue("v").equals("hedge")) type = WayType.HEDGE;
							break;
						case "building":
							if(atts.getValue("v").equals("yes")) type = WayType.BUILDING;
							break;
						case "amenity":
							if(atts.getValue("v").equals("parking")) type = WayType.PARKING;
							break;
						case "area":
							if(atts.getValue("v").equals("yes")){
								if(way != null){
									way.setArea(true);
								}
							}
							break;
						case "surface":
							if(atts.getValue("v").equals("cobblestone")) type = WayType.UNKNOWN;
							break;
						case "type":
							break;
						case "except":
							//type = WayType.UNKNOWN;
							break;
						default:
							type = WayType.UNKNOWN;
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
							way.setType(WayType.ROAD);
							ways.put(wayID, way);
							break;
						case WATER:
							way.setType(WayType.WATER);
							ways.put(wayID, way);
							break;
						case PARK:
							way.setType(WayType.PARK);
							ways.put(wayID, way);
							break;
						case GRASS:
							way.setType(WayType.GRASS);
							ways.put(wayID, way);
							break;
						case HEDGE:
							way.setType(WayType.HEDGE);
							ways.put(wayID, way);
							break;
						case PLAYGROUND:
							way.setType(WayType.PLAYGROUND);
							ways.put(wayID, way);
							break;
						case BUILDING:
							way.setType(WayType.BUILDING);
							ways.put(wayID, way);
							break;
						case PARKING:
							way.setType(WayType.PARKING);
							ways.put(wayID, way);
							break;
						default:
							ways.put(wayID, way);
							break;
					}
					way = null;
					//type = WayType.UNKNOWN;
					break;
				case "member":
					if(way != null){
						System.out.println("Adding path to relation!");
						relation.getPath().append(way.getPath(), false);
					}
					break;
				case "relation":
					relation.setType(type);
					ways.put(wayID, relation);
					relation = null;
					//type = WayType.UNKNOWN;
					break;
			}
		}
		public void endDocument() {}
	}

}
