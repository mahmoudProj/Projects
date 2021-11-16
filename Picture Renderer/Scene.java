import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.PrintWriter;

import javax.swing.JPanel;

public class Scene {
	private static Triangle.Key minY = new Triangle.Key() {
		public double apply(Triangle t) {
			return t.getMinY();
		}		
	};

	private static Triangle.Key minX = new Triangle.Key() {
		public double apply(Triangle t) {
			return t.getMinX();
		}
	};

	private static class ZKey implements Triangle.Key {
		private double scanX;
		public double apply(Triangle t) {
			return t.getZ(scanX);
		}
		public void setScanX(double x) {
			scanX = x;
		}
	}

	private ZKey keyZ = new ZKey();

	private Triangle.Group all = new Triangle.Group(minY);

	public void add(Triangle t) {
		all.add(t);
	}

	public static final Color BACKGROUND = Color.BLACK; // new Color(128,128,128);

	public void paint(Graphics g, Dimension d) {
		all.sort(); // Sort by minimum y:
		//Sort "all" the triangles (by minimum y)

		// We create a bunch of temporary groups.
		// Each triangle will be in only one at a time
		Triangle.Group input = all;
		all = new Triangle.Group(minY);
		Triangle.Group line = new Triangle.Group(minX);
		Triangle.Group waiting = new Triangle.Group(minX);
		Triangle.Group current = new Triangle.Group(keyZ);

		// input: the triangles waiting to be added as the scan line reaches them.
		// line: the triangles in the scan line that are handled already
		// waiting: the triangles in the scan line whose minX hasn't been reached yet
		// current: the triangles in the scan line which may be visible.
		//     The first of the current group is the one currently visible.
		//     The others are behind it.
		// The "waiting" and "current" groups are only used while
		// drawing pixels on the scan line.
		// When we're all done with a triangle, it's put back in the "all" group.

		// start the scan:
		for (int y=0; y < d.height; ++y) {
			// add any new polygons to "line" from "input" which the scan line has reached
			// Use getMinY to see if the triangle starts at this y or before.
			// Since the triangles in "input" are sorted by y,
			// then as soon as the first triangle hasn't been reached, we can stop looking.
			// TODO:
			// DONE! for you
			while (!input.isEmpty() && input.getFirst().getMinY() <= y) {
				line.add(input.removeFirst());
			}

			// consume "line" (remove all its triangles) and for each triangle
			// if it still intersects the scan line (use Triangle.setScan)
			// add it to waiting, or to "all" (if not).
			// ("all" is used for triangles we aren't interested in any more).
			// TODO
			while (!line.isEmpty()) {
				if(line.getFirst().setScan(y) == true) {
					waiting.add(line.removeFirst());
				}else {
					all.add(line.removeFirst());
				}

			}
			// Sort the waiting triangles

			waiting.sort();

			for (int x=0; x < d.width; ++x) { // x is modified in the loop
				// Remove any "waiting" triangles (from the front of the list) whose
				// start (minX) is at or before pixel x and add them to "current."
				// As before, as soon as we find one that isn't ready, we can stop.
				// TODO
				while (!waiting.isEmpty() && waiting.getFirst().getMinX() <= x) {
					current.add(waiting.removeFirst());

				}
				// Record what the "x" value is and then sort the current triangles
				keyZ.setScanX(x);
				// TODO: we did the first part for you already.  Just sort the "current" triangles.
				current.sort();
				// Repeatedly remove the first "current" triangle if it ended (MaxX) 
				// before the current x value.
				// Place them back in "line" as being done with for now.
				// As before, because of sorting, as soon as we find one to keep, we can stop.
				// TODO
				while (!current.isEmpty() && current.getFirst().getMaxX() <= x) {
					line.add(current.removeFirst());


				}
				// draw some pixels: either a lot of black ones (if no current) or
				// one pixel from the current triangle.
				if (current.isEmpty()) {
					int supx = d.width;
					if (!waiting.isEmpty() && waiting.getFirst().getMinX() < supx) {
						supx = (int)waiting.getFirst().getMinX();
					}
					g.setColor(BACKGROUND);
					g.drawLine(x, y, supx, y);
					x = supx;
				} else {
					g.setColor(current.getFirst().getColor(x, Scene.BACKGROUND));
					g.drawLine(x, y, x+1, y);
				}
			}

			// Remove all current and waiting triangles to the "line" group
			while (!current.isEmpty()) {
				line.add(current.removeFirst());
			}
			while (!waiting.isEmpty()) {
				line.add(waiting.removeFirst());
			}	


		}



		// Remove all active (line) triangles or remaining input triangles
		// back to "all".

		while (!line.isEmpty()) {
			all.add(line.removeFirst());
		}


		while (!input.isEmpty()) {
			all.add(input.removeFirst());
		}



	}

	/**
	 * Print all the triangles in the scene.
	 * @param pw
	 */
	public void print(PrintWriter pw) {
		Triangle.Group old = all;
		all = new Triangle.Group(minY);
		while (!old.isEmpty()) {
			Triangle tri = old.removeFirst();
			pw.println(tri);
			all.add(tri);
		}
		pw.flush();
	}

	@SuppressWarnings("serial")
	public JPanel getPanel() {
		return new JPanel() {

			@Override
			protected void paintComponent(Graphics g) {
				Scene.this.paint(g,getSize());
			}

		};
	}
}
