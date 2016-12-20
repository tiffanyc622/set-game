// 7/16/14

import java.awt.*;
import java.io.*;
import javax.imageio.*;

public class SetCard {
	private int color, shape, fill, num;
	private Image image;

	/*color
		0 = red
		1 = green
		2 = purple
	shape
		0 = oval
		1 = diamond
		2 = squiggly
	fill
		0 = empty
		1 = striped
		2 = filled
	number
		0 = one
		1 = two
		2 = three*/

	public SetCard(int c, int s, int f, int n) {
		color = c;
		shape = s;
		fill = f;
		num = n;
		try {
			image = ImageIO.read(new File("images/"+c+s+f+n+".gif"));
		} catch (IOException e) {
			System.err.println("ERROR: File not found - " + c+s+f+n);
			System.exit(1);
		}
	}

	public int getColor() {
		return color;
	}

	public int getShape() {
		return shape;
	}

	public int getFill() {
		return fill;
	}

	public int getNum() {
		return num;
	}

	public boolean equals(Object o) {
		SetCard card = (SetCard)o;
		return card != null && card.getColor() == getColor() && card.getShape() == getShape()
			&& card.getFill() == getFill() && card.getNum() == getNum();
	}

	public String toString() {
		return "" + color + shape + fill + num;
	}

	public Image getImage() {
		return image;
	}
}