import java.awt.*;
import java.awt.geom.Path2D;

/**
 * Created by Adrian on 17-02-2016.
 */
public class MapPath {
    private Path2D path;
    private boolean area;
    private Color color;
    WayType type;

    public MapPath(WayType _type, boolean _area){
        path = new Path2D.Double();
        type = _type;
        area = _area;
        switch (type){
            case WATER:
                color = Colors.water;
                break;
            case PARK:
                color = Colors.park;
                break;
            case GRASS:
                break;
            case SEA:
                color = Colors.sea;
                break;
            case ROAD:
                color = Colors.road;
                break;
            case UNKNOWN:
                color = Color.RED;
                break;
        }
    }

    public void draw(Graphics2D g){
        g.setColor(color);
        if(area){
            g.fill(path);
        } else{
            g.draw(path);
        }
    }

    public void moveTo(double x, double y){
        path.moveTo(x, y);
    }

    public void lineTo(double x, double y){
        path.lineTo(x, y);
    }

}
