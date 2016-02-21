import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.*;
import javax.swing.*;
import java.util.*;
 
public class View extends JFrame implements Observer {
	public static final long serialVersionUID = 20160216;
	Canvas canvas;
	JPanel leftPanel;
	boolean antia = false;
	Model model;
	static AffineTransform trans = new AffineTransform();


	public View(Model m) {
		model = m;
		model.addObserver(this);
		canvas = new Canvas();
		setLayout(new BorderLayout());
		add(canvas, BorderLayout.CENTER);
		initializeLeftPanel();
		setSize(512, 512);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
		pan(-model.minlon, -model.maxlat);
		zoom(canvas.getWidth()/Math.max(model.maxlon-model.minlon, model.minlat-model.maxlat),0,0);
	}

	private void initializeLeftPanel(){
		leftPanel = new JPanel();
		leftPanel.setBackground(Colors.panelColor);
		add(leftPanel, BorderLayout.WEST);
		JButton btn = new JButton("AA");
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				toggleAA();
			}
		});
		//Disable drawing focus rectangle
		btn.setFocusPainted(false);
		btn.setBackground(Colors.buttonHover);
		 leftPanel.add(btn);
	}

	public void toggleAA() {
		antia = !antia;
		repaint();
	}

	public Point2D inverse(double x, double y) {
		try {
			return trans.inverseTransform(new Point2D.Double(x, y), null);
		} catch (NoninvertibleTransformException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void zoom(double s, double cx, double cy) {
		pan(-cx, -cy);
		trans.preConcatenate(AffineTransform.getScaleInstance(s, s));
		pan(cx, cy);
	}

	public void pan(double dx, double dy) {
		trans.preConcatenate(AffineTransform.getTranslateInstance(dx, dy));
		model.dirty();
	}

	public void update(Observable obs, Object obj) {
		repaint();
	}

	class Canvas extends JComponent {
		public static final long serialVersionUID = 20160216;

		public void paint(Graphics _g) {
			Graphics2D g = (Graphics2D) _g;
			g.setTransform(trans);
			if (antia) g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setStroke(new BasicStroke(Float.MIN_VALUE));
			for(MapPath mp : model.ways.values()){
				g.setColor(mp.getColor());
                if(mp.isArea()){
                    g.fill(mp.getPath());
                } else {
                    g.draw(mp.getPath());
                }
			}
		}
	}
}
