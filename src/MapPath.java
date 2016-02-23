import java.awt.*;
import java.awt.geom.Path2D;
import java.io.Serializable;

/**
 * Created by Adrian on 17-02-2016.
 */
public class MapPath implements Serializable{
    private Path2D path;
    private boolean area;
    private Color color;
    private WayType type;

    public MapPath(){
        path = new Path2D.Double();
        type = WayType.UNKNOWN;
        area = false;
        setColor();
    }

    public void lineTo(double x, double y){
        path.lineTo(x, y);
    }

    public void moveTo(double x, double y){
        path.moveTo(x,y);
    }

    public void setArea(boolean b){
        area = b;
    }

    public void setType(WayType t){
        type = t;
        setToArea();
        setColor();
    }

    public WayType getType(){
        return type;
    }

    private void setToArea(){
        //Sets the object to be an area only in some cases:
        switch (type){
            case WATER:
                area = true;
                break;
            case PARK:
                area = true;
                break;
            case GRASS:
                area = true;
                break;
            case SEA:
                area = true;
                break;
            case PLAYGROUND:
                area = true;
                break;
            case BUILDING:
                area = true;
                break;
            case PARKING:
                area = true;
                break;
            case UNIVERSITY:
                //area = true;
                break;
            case BROWNFIELD:
                area = true;
                break;
            case CEMETERY:
                area = true;
                break;
            case RESIDENTIAL:
                //area = true;
                break;
            default:
                area = false;
                break;
        }
    }

    private void setColor(){
        switch (type){
            case WATER:
                color = Colors.water;
                break;
            case PARK:
                color = Colors.park;
                break;
            case GRASS:
                color = Colors.grass;
                break;
            case SEA:
                color = Colors.sea;
                break;
            case ROAD:
                color = Colors.road;
                break;
            case HEDGE:
                color = Colors.hedge;
                break;
            case PLAYGROUND:
                color = Colors.playground;
                break;
            case BUILDING:
                color = Colors.building;
                break;
            case PARKING:
                color = Colors.parking;
                break;
            case PEDESTRIAN:
                color = Colors.pedestrian;
                break;
            case BROWNFIELD:
                color = Colors.brownfield;
                break;
            case UNIVERSITY:
                color = Colors.univeristy;
                break;
            case COASTLINE:
                color = Colors.coastline;
                break;
            case SUBWAY:
                color = Colors.subway;
                break;
            case WALL:
                color = Colors.cobbleStone;
                break;
            case RESIDENTIAL:
                color = Colors.residential;
                break;
            case CEMETERY:
                color = Colors.cemetery;
                break;
            case UNKNOWN:
                color = Color.RED;
                break;
        }
    }

    public boolean isArea(){
        return area;
    }

    public Path2D getPath(){
        return path;
    }

    public Color getColor(){
        return color;
    }


}
