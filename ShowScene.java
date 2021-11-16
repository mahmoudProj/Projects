import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import Point3D;
import Scene;
import Triangle;

public class ShowScene {
	static final int WIDTH = 300;
	static final int HEIGHT = 200;

	public static void main(final String[] args) throws IOException {
		final Scene scene = new Scene();
		if (args.length > 0) {
			for (String arg : args) {
				BufferedReader br = new BufferedReader(new FileReader(arg));
				String ts;
				while ((ts = br.readLine()) != null) {
					Triangle tri = Triangle.fromString(ts);
					scene.add(tri);
				}
				br.close();
			}
		} else {
			scene.add(
					new Triangle(new Point3D(100,10,0), 
							new Point3D(200,100,0.2), 
							new Point3D(175,25,0.8), 
							Color.YELLOW));
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame(args.length > 0 ? args[0] : "Test Scene");
				frame.setSize(600,400);
				frame.setContentPane(scene.getPanel());
				frame.setVisible(true);
				frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			}
		});
	}

}
