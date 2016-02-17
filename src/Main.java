
import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.util.function.*;

public class Main {
	void run(String[] args) {
		Model m;
		long time = System.nanoTime();
		if (args[0].matches(".*obj")) {
			m = Model.load(args[0]);
			time = System.nanoTime() - time;
		} else {
			m = new Model(args[0]);
			time = System.nanoTime() - time;
			m.save(args[0] + ".obj");
		}
		System.out.printf("Model load: %d ms\n", time/1000000);
		new Controller(m,new View(m));
	}

	public static void main (String[] args) {
		new Main().run(args);
	}
}
