/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.utils;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Utility class for generating colors. This class provides a method to get a
 * sequence of colors in a round-robin fashion.
 * 
 * @author Veronika Lenková
 */
public class ColorsUtils {

	/**
	 * The index of the next color to be returned. This index is incremented each
	 * time a color is requested, wrapping around when it reaches the end of the
	 * color array.
	 */
	private static int colorIndex = 0;

	/**
	 * An array of RGB colors. These colors are used to create a sequence of colors
	 * that can be cycled through.
	 */
	private static final RGB[] COLORS = { new RGB(255, 0, 0), // red
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

	/**
	 * Returns the next RGB color in the sequence. The colors are cycled through in
	 * a round-robin fashion.
	 * 
	 * @return The next RGB color.
	 */
	public static RGB getNextRGB() {
		RGB color = COLORS[colorIndex];
		colorIndex = (colorIndex + 1) % COLORS.length;
		return color;
	}

	/**
	 * Returns the next Color object in the sequence. The colors are cycled through
	 * in a round-robin fashion.
	 * 
	 * @return The next Color object.
	 */
	public static Color getNextColor() {
		return new Color(Display.getDefault(), getNextRGB());
	}
}
