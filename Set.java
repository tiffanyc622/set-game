// 7/26/14

import java.util.ArrayList;

public class Set {
	private ArrayList<SetCard> cards;

	public Set(ArrayList<SetCard> a) {
		cards = a;
	}

	public boolean equals(Object o) {
		ArrayList<SetCard> other = ((Set)(o)).getCards();
		for (int i = 0; i < cards.size(); i++)
			if (!cards.contains(other.get(i)))
				return false;
		return true;
	}

	public ArrayList<SetCard> getCards() {
		return cards;
	}

	public static boolean isSet(ArrayList<SetCard> test) {
		int [][] a = new int[3][4]; // each row = a card
			// col 0 = color, 1 = shape, 2 = fill, 3 = num
		for (int i = 0; i < test.size(); i++) {
			SetCard current = test.get(i);
			a[i][0] = current.getColor();
			a[i][1] = current.getShape();
			a[i][2] = current.getFill();
			a[i][3] = current.getNum();
		}
		for (int i = 0; i < 4; i++)
			if (!((a[0][i] == a[1][i] && a[0][i] == a[2][i]) ||(a[0][i] != a[1][i]
				&& a[1][i] != a[2][i] && a[0][i] != a[2][i])))
				return false;
		return true;
	}

	public static boolean isSuperSet(ArrayList<SetCard> test) {
		int [][] a = new int[4][4];
		for (int i = 0; i < test.size(); i++) {
			SetCard current = test.get(i);
			a[i][0] = current.getColor();
			a[i][1] = current.getShape();
			a[i][2] = current.getFill();
			a[i][3] = current.getNum();
		}
		return SetSolver.find3rd(test.get(0), test.get(1)).equals(SetSolver.find3rd(test.get(2), test.get(3)));
	}

	public String toString() {
		String s = "";
		for (int i = 0; i < cards.size(); i++)
			s += ", " + cards.get(i);
		return s;
	}
}