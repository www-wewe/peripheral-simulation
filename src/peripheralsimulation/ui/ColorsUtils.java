package peripheralsimulation.ui;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorsUtils {

	private static int colorIndex = 0;

	private static final RGB[] COLORS = {
			new RGB(255, 0, 0), // red
			new RGB(0, 255, 0), // green
			new RGB(0, 0, 255), // blue
			new RGB(255, 128, 0), // orange
			new RGB(128, 0, 255), // purple
			new RGB(0, 255, 255), // cyan
			new RGB(255, 0, 255), // magenta
			new RGB(128, 128, 0), // olive
			new RGB(0, 128, 128), // teal
			new RGB(128, 128, 128), // gray
			new RGB(255, 192, 203), // pink
			new RGB(255, 255, 0), // yellow
	};

	public static RGB getNextRGB() {
		RGB color = COLORS[colorIndex];
		colorIndex = (colorIndex + 1) % COLORS.length;
		return color;
	}

	public static Color getNextColor() {
		return new Color(Display.getDefault(), getNextRGB());
	}
}
