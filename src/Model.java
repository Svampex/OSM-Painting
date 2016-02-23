
import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Path2D;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

enum WayType { UNKNOWN, ROAD, WATER, PARK, GRASS, SEA, HEDGE, PLAYGROUND, BUILDING, PARKING, PEDESTRIAN, BROWNFIELD, UNIVERSITY, COASTLINE, SUBWAY, WALL, RESIDENTIAL, CEMETERY }
enum Surface { NONE, COBBLESTONE}

public class Model extends Observable implements Serializable {
	public static final long serialVersionUID = 20160217;
	List<Shape> data = new ArrayList<>();
	HashMap<Long, MapPath> areas = new HashMap<>();
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
						case "name":
							break;
						case "highway":
							type = WayType.ROAD;
							switch (atts.getValue("v")){
								case "pedestrian":
									type = WayType.PEDESTRIAN;
									break;
								case "footway":
									type = WayType.PEDESTRIAN;
									break;
								case "residential":
									type = WayType.ROAD;
									break;
								case "tertiary":
									type = WayType.ROAD;
									break;
							}
							break;
						case "natural":
							switch (atts.getValue("v")){
								case "water":
									type = WayType.WATER;
									break;
								case "coastline":
									type = WayType.COASTLINE;
									//System.out.println("COASTLINE");
									break;
							}
							break;
						case "water":
							if (atts.getValue("v").equals("pond")) type = WayType.WATER;
							break;
						case "leisure":
							if(atts.getValue("v").equals("park")) type = WayType.PARK;
							if(atts.getValue("v").equals("playground")) type = WayType.PLAYGROUND;
							break;
						case "barrier":
							if(atts.getValue("v").equals("hedge")) type = WayType.HEDGE;
							if(atts.getValue("v").equals("wall")) type = WayType.WALL;
							if(atts.getValue("v").equals("gate")) type = WayType.WALL;
							if(atts.getValue("v").equals("fence")) type = WayType.WALL;
							break;
						case "building":
							type = WayType.BUILDING;
							/*
							if(atts.getValue("v").equals("yes")) type = WayType.BUILDING;
							if(atts.getValue("v").equals("apartments")) type = WayType.BUILDING;
							if(atts.getValue("v").equals("church")) type = WayType.BUILDING;
							if(atts.getValue("v").equals("residential")) type = WayType.BUILDING;
							if(atts.getValue("v").equals("garages")) type = WayType.BUILDING;
							*/
							break;
						case "amenity":
							switch (atts.getValue("v")){
								case "parking":
									type = WayType.PARKING;
									break;
								case "place_of_worship":
									type = WayType.BUILDING;
									break;
								case "arts_centre":
									type = WayType.BUILDING;
									break;
								case "university":
									type = WayType.UNIVERSITY;
									type = WayType.UNIVERSITY;
									break;

							}
							break;
						case "area":
							if(atts.getValue("v").equals("yes")){
								if(way != null){
									way.setArea(true);
								}
							}
							break;
						case "place":
							switch (atts.getValue("v")){
								case "island":
									break;
							}
							break;
						case "railway":
							switch (atts.getValue("v")){
								case "subway":
									type = WayType.SUBWAY;
									break;
							}
							break;
						case "route_master":
							switch (atts.getValue("v")){
								case "subway":
									type = WayType.SUBWAY;
									break;
							}
							break;
						case "route":
							switch (atts.getValue("v")){
								case "subway":
									type = WayType.SUBWAY;
									break;
								default:
									type = WayType.ROAD;
									break;
							}
							break;
						case "surface":
							switch (atts.getValue("v")){
								case "cobblestone":
									type = WayType.PEDESTRIAN;
									break;
								case "asphalt":
									type = WayType.ROAD;
									break;
							}
							break;
						case "type":
							switch (atts.getValue("v")){
								case "multipolygon":
									if(way != null){
										//System.out.println(type + ", ID: " + wayID);
										way.setArea(true);
									}
									break;
							}
							break;
						case "ref":
							break;
						case "website":
							break;
						case "cycleway":
							break;
						case "lit":
							break;
						case "religion":
							break;
						case "source":
							break;
						case "except":
							//type = WayType.UNKNOWN;
							break;
						case "add:housenumber":
							//type = WayType.UNKNOWN;
							break;
						case "addr:street":
							//type = WayType.UNKNOWN;
							break;
						case "addr:city":
							//type = WayType.UNKNOWN;
							break;
						case "addr:postcode":
							//type = WayType.UNKNOWN;
							break;
						case "source:date":
							//type = WayType.UNKNOWN;
							break;
						case "addr:country":
							//type = WayType.UNKNOWN;
							break;
						case "wikipedia":
							//type = WayType.UNKNOWN;
							break;
						case "landuse":
							switch (atts.getValue("v")){
								case "brownfield":
									type = WayType.BROWNFIELD;
									break;
								case "allotments":
									type = WayType.BROWNFIELD;
									break;
								case "grass":
									type = WayType.GRASS;
									break;
								case "residential":
									type = WayType.RESIDENTIAL;
									break;
								case "industrial":
									//Make a new type?
									type = WayType.RESIDENTIAL;
									break;
								case "cemetery":
									type = WayType.CEMETERY;
									break;
							}
							break;
						default:
							//type = WayType.UNKNOWN;
							break;
						//All tag keys: http://taginfo.openstreetmap.org/keys
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
					way.setType(type);
					if(way.isArea()){
						areas.put(wayID, way);
					} else {
						ways.put(wayID, way);
					}
					way = null;
				case "member":
					if(way != null){
						//System.out.println("Adding path to relation!");
						relation.getPath().append(way.getPath(), false);
						ways.values().remove(way);
					}
					break;
				case "relation":
					relation.setType(type);
					relation.getPath().setWindingRule(Path2D.WIND_EVEN_ODD);
					if(relation.isArea()){
						areas.put(wayID, relation);
					}else{
						ways.put(wayID, relation);
					}
					relation = null;
					type = WayType.UNKNOWN;
					break;
				default:
					//type = WayType.UNKNOWN;
					break;
			}
		}
		public void endDocument() {}
	}

}
