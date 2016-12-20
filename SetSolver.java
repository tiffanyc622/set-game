// 7/22/14

import java.util.ArrayList;

public class SetSolver {
	private SetCard [][] cards;

	public SetSolver(SetCard [][] table) {
		cards = table;
	}

	public static SetCard find3rd(SetCard card1, SetCard card2) {
		if (card1 == null || card2 == null)
			return null;
		int [] newCard = new int[4];
		int [][] a = new int[2][4];
		a[0][0] = card1.getColor();
		a[0][1] = card1.getShape();
		a[0][2] = card1.getFill();
		a[0][3] = card1.getNum();
		a[1][0] = card2.getColor();
		a[1][1] = card2.getShape();
		a[1][2] = card2.getFill();
		a[1][3] = card2.getNum();
		for (int i = 0; i < 4; i++) {
			if (a[0][i] == a[1][i])
				newCard[i] = a[0][i];
			else if ((a[0][i] == 0 && a[1][i] == 1) || (a[1][i] == 0 && a[0][i] == 1))
				newCard[i] = 2;
			else if ((a[0][i] == 0 && a[1][i] == 2) || (a[1][i] == 0 && a[0][i] == 2))
				newCard[i] = 1;
			else if ((a[0][i] == 1 && a[1][i] == 2) || (a[1][i] == 1 && a[0][i] == 2))
				newCard[i] = 0;
		}
		return new SetCard(newCard[0], newCard[1], newCard[2], newCard[3]);
	}

	public ArrayList<SetCard> findSet(int startIndex) {
		ArrayList<SetCard> newList = new ArrayList<SetCard>();
		for (int r = 0; r < cards.length; r++)
			for (int c = 0; c < cards[r].length; c++)
				newList.add(cards[r][c]);
		for (int i = startIndex; i < newList.size()-1; i++)
			for (int j = i+1; j < newList.size(); j++) {
				SetCard temp = find3rd(newList.get(i), newList.get(j));
				if (temp != null && newList.contains(temp)) {
					ArrayList<SetCard> returning = new ArrayList<SetCard>();
					returning.add(newList.get(i));
					returning.add(newList.get(j));
					returning.add(temp);
					return returning;
				}
			}
		return null;
	}

	public ArrayList<SetCard> findSuperSet(int startIndex) {
		ArrayList<SetCard> newList = new ArrayList<SetCard>();
		for (int r = 0; r < cards.length; r++)
			for (int c = 0; c < cards[r].length; c++)
				if (cards[r][c] != null)
					newList.add(cards[r][c]);
		ArrayList<Set> allpairs = new ArrayList<Set>();
		ArrayList<SetCard> all3rds = new ArrayList<SetCard>();
		for (int i = 0; i < newList.size()-1; i++)
			for (int j = i+1; j < newList.size(); j++) {
				ArrayList<SetCard> currentSet = new ArrayList<SetCard>();
				currentSet.add(newList.get(i));
				currentSet.add(newList.get(j));
				currentSet.add(find3rd(newList.get(i), newList.get(j)));
				allpairs.add(new Set(currentSet));
				all3rds.add(allpairs.get(allpairs.size()-1).getCards().get(2));
			}
		for (int index = 0; index < all3rds.size()-1; index++) {
			for (int i = index+1; i < all3rds.size(); i++) {
				if (all3rds.get(index).equals(all3rds.get(i))) {
					ArrayList<SetCard> returning = new ArrayList<SetCard>();
					ArrayList<SetCard> pair1 = allpairs.get(i).getCards();
					ArrayList<SetCard> pair2 = allpairs.get(index).getCards();
					returning.add(pair1.get(0));
					returning.add(pair1.get(1));
					returning.add(pair2.get(0));
					returning.add(pair2.get(1));
					return returning;
				}
			}
			index++;
		}
		return null;
	}

	public boolean contains(SetCard temp) {
		for (int r = 0; r < cards.length; r++)
			for (int c = 0; c < cards[r].length; c++)
				if (temp.equals(cards[r][c]))
					return true;
		return false;
	}

	public int countSets() {
		ArrayList<SetCard> newList = new ArrayList<SetCard>();
		for (int r = 0; r < cards.length; r++)
			for (int c = 0; c < cards[r].length; c++)
				newList.add(cards[r][c]);
		ArrayList<Set> sets = new ArrayList<Set>();
		for (int i = 0; i < newList.size(); i++) {
			ArrayList<SetCard> list = findSet(i);
			Set foundSet = new Set(list);
			if (list != null && !sets.contains(foundSet))
				sets.add(foundSet);
		}
		return sets.size();
	}
}