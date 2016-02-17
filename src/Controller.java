import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.util.function.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.*;

public class Controller implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
	Model model;
	View view;
	int mx, my;

	public void mouseMoved(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}

	public void mousePressed(MouseEvent e) {
		mx = e.getX();
		my = e.getY();
	}

	public void mouseReleased(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseWheelMoved(MouseWheelEvent e) {
		view.zoom(Math.pow(1.1, e.getWheelRotation()), e.getX(), e.getY());
	}

	public void mouseDragged(MouseEvent e) {
		view.pan(e.getX() - mx, e.getY() - my);
		mx = e.getX();
		my = e.getY();
	}

	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {
		switch (e.getKeyChar()) {
			case 'x': view.toggleAA();
					  break;
			default:
					  break;
		}
	}

	public Controller(Model m, View v) {
		model = m;
		view = v;
		v.addKeyListener(this);
		v.canvas.addMouseListener(this);
		v.canvas.addMouseMotionListener(this);
		v.canvas.addMouseWheelListener(this);
	}
}
