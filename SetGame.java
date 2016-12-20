// 7/16/14

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.ArrayList;
import javax.imageio.*;
import java.util.Scanner;
import java.io.*;

public class SetGame {
	JFrame frame;
	MainPanel panel;

	public static void main (String [] args) {
		SetGame s = new SetGame();
		s.run();
	}

	public void run() {
		frame = new JFrame("Set");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(575, 620);
		
		panel = new MainPanel();
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		
		frame.setResizable(false);
		frame.setVisible(true);
	}

	class MainPanel extends JPanel {
		private CardLayout cardLayout;
		private IntroPanel introPanel;
		private SetPanel setPanel;
		private SuperPanel superPanel;
		private EndPanel endPanel;
		private int endScore, endTime;
		private boolean playingSuper;

		public MainPanel() {
			cardLayout = new CardLayout();
			this.setLayout(cardLayout);

			introPanel = new IntroPanel();
			this.add(introPanel, "Intro");
			setPanel = new SetPanel();
			this.add(setPanel, "Set");
			superPanel = new SuperPanel();
			this.add(superPanel, "Super");
			endPanel = new EndPanel();
			this.add(endPanel, "End");
		}

		class EndPanel extends JPanel {
			private JButton backButton;
			private Image youWonImage;
			private int highScore;
			private boolean newHigh;

			public EndPanel() {
				backButton = new JButton("Back to menu");
				add(backButton);
				this.setLayout(null);
				backButton.setBounds(138, 425, 300, 100);
				backButton.addActionListener(new BackAction());
				setBackground(Color.green);
				try {
					youWonImage = ImageIO.read(new File("youwon.gif"));
				} catch (IOException e) {
					System.err.println("ERROR: File not found - youwon.gif");
					System.exit(1);
				}
			}

			private class BackAction implements ActionListener {
				public void actionPerformed(ActionEvent e) {
					cardLayout.show(panel, "Intro");
				}
			}

			public void dealWithHighScores() {
				Scanner input = OpenFile.openToRead("highscores.txt");
				int setScore = input.nextInt(), superScore = input.nextInt();
				input.close();

				if (playingSuper)
					highScore = superScore;
				highScore = setScore;
				newHigh = false;
				if (endScore > highScore) {
					newHigh = true;
					highScore = endScore;
					PrintWriter output = OpenFile.openToWrite("highscores.txt");
					if (playingSuper)
						output.print(setScore + " " + highScore);
					else
						output.print(highScore + " " + superScore);
					output.close();
				}
			}

			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				dealWithHighScores();
				g.drawImage(youWonImage, 75, 65, 380, 100, this);
				g.setColor(Color.black);
				g.setFont(new Font("Arial", Font.PLAIN, 40));
				g.drawString("Score :", 150, 250);
				g.drawString("Time :", 150, 330);
				g.setFont(new Font("Arial", Font.PLAIN, 60));
				g.drawString("" + endScore, 300, 258);
				g.drawString("" + endTime/60 + ":" + String.format("%02d", endTime%60), 300, 338);

				g.setFont(new Font("Arial", Font.PLAIN, 25));
				g.drawString("High Score: " + highScore, 195, 415);

				g.setFont(new Font("Arial", Font.PLAIN, 9));
				g.drawString("what perty wordart", 350, 65);
				if (playingSuper) {
					g.drawString("finishing is already an accomplishment. unless your score is -bagillion", 130, 350);
					if (endScore > 1500)
						g.drawString("impressive", 380, 270);
					else if (endScore > 0)
						g.drawString("at least its positive", 350, 270);
					else
						g.drawString("...i have no encouraging words for you", 200, 270);
				}
				else {
					if (endTime/60 < 3)
						g.drawString("go play superset cuz youre obvs too good at this. unless you cheated. then you suck", 150, 350);
					else if (endTime/60 < 5)
						g.drawString("fazt", 380, 350);
					else if (endTime/60 < 8)
						g.drawString("meh", 380, 350);
					else
						g.drawString("u suk", 380, 350);
				}				
				if (newHigh) {
					g.setColor(Color.magenta);
					g.drawString("NEW HIGH SCOREEEEE!!!!!!!! magenta!!!!", 200, 390);
				}
			}
		}

		class IntroPanel extends JPanel {
			private JButton setButton, superButton;

			public IntroPanel() {
				setLayout(null);
				setBackground(Color.cyan);
				setButton = new JButton("Play SET");
				superButton = new JButton("Play Super SET");
				add(setButton);
				add(superButton);
				setButton.setBounds(138, 150, 300, 100);
				superButton.setBounds(138, 350, 300, 100);
				setButton.addActionListener(new SetAction());
				superButton.addActionListener(new SuperAction());
			}

			private class SetAction implements ActionListener {
				public void actionPerformed(ActionEvent e) {
					cardLayout.show(panel, "Set");
				}
			}

			private class SuperAction implements ActionListener {
				public void actionPerformed(ActionEvent e) {
					cardLayout.show(panel, "Super");
				}
			}
		}

		class SetPanel extends JPanel implements MouseListener {
			private ArrayList<SetCard> deck, selectedCards;
			private SetCard [][] table;
			private JButton dealButton, resetButton, shuffleButton, solveButton,
				hintButton, timerButton, numSetsButton, backButton;
			private int [] xcoord = {50, 130, 210, 290, 370, 450},
				ycoord = {50, 160, 270};
			private int [] clickedX, clickedY;
			private boolean selected3, setValid, noSetFound, triedToFind,
				pressedFind, showTimer, showNumSets, showDealMessage, actuallyShow;
			private int time, messageTime, score, prevNumSets;
			private Timer timer, messageTimer, wrongTimer;
			private boolean[][] chosenCards;

			public SetPanel() {
				addMouseListener(this);
				setBackground(Color.gray);
				setLayout(null);
				reset();
				clickedX = new int[3];
				clickedY = new int[3];
				selectedCards = new ArrayList<SetCard>();;
				setValid = true;
				setUpButtons();
				timer = new Timer(1000, new TimerAction());
				messageTimer = new Timer(50, new MessageAction());
				wrongTimer = new Timer(1000, new WrongAction());
				noSetFound = true;
			}

			public void setUpButtons() {
				dealButton = new JButton("Deal");
				shuffleButton = new JButton("Shuffle cards");
				solveButton = new JButton("Find");
				hintButton = new JButton("Hint");
				resetButton = new JButton("New game");
				timerButton = new JButton("Show timer");
				numSetsButton = new JButton("Show # sets");
				backButton = new JButton("Back");

				add(dealButton);
				add(shuffleButton);
				add(solveButton);
				add(hintButton);
				add(resetButton);
				add(timerButton);
				add(numSetsButton);
				add(backButton);

				dealButton.setBounds(50, 400, 225, 45);
				shuffleButton.setBounds(300, 400, 225, 45);
				solveButton.setBounds(50, 461, 225, 45);
				hintButton.setBounds(300, 461, 225, 45);
				resetButton.setBounds(50, 524, 225, 45);
				timerButton.setBounds(300, 524, 105, 45);
				//timerButton.setBounds(300, 524, 225, 45);
				numSetsButton.setBounds(420, 524, 105, 45);
				backButton.setBounds(9, 9, 100, 30);

				dealButton.addActionListener(new DealAction());
				shuffleButton.addActionListener(new ShuffleAction());
				solveButton.addActionListener(new SolveAction());
				hintButton.addActionListener(new HintAction());
				resetButton.addActionListener(new ResetAction());
				timerButton.addActionListener(new TimerButtonAction());
				numSetsButton.addActionListener(new NumSetsAction());
				backButton.addActionListener(new BackAction());
			}

			public void paintComponent(Graphics g) {
				playingSuper = false;
				super.paintComponent(g);
				if (deck.size() == 0 && countNumSets() == 0) {
					endScore = score;
					endTime = time;
					timer.stop();
					reset();
					endScore-=endTime;
					cardLayout.show(panel, "End");
					return;
				}
				g.setFont(new Font("Arial", Font.BOLD, 16));
				if (showDealMessage && actuallyShow) {
					g.setColor(Color.red);
					if (prevNumSets == 1)
						g.drawString("you suck there was " + prevNumSets + " set", 150, 25);
					else
						g.drawString("you suck there were " + prevNumSets + " sets", 150, 25);
					time+=30;
				}
				timer.start();
				g.setColor(Color.green);
				if (noSetFound && triedToFind) {
					g.setColor(Color.red);
					g.drawString("NO SETS ON TABLE", 150, 25);
				}
				else if (triedToFind)
					for (int r = 0; r < chosenCards.length; r++)
						for (int c = 0; c < chosenCards[r].length; c++)
							if (chosenCards[r][c])
								g.fillRect(xcoord[c]-2, ycoord[r]-2, 74, 104);
				g.setColor(Color.blue);
				if (selected3)
					if (setValid)
						setFound();
					else g.setColor(Color.red);
				if (clickedX[0] != 0 && !triedToFind)
					for (int i = 0; i < 3; i++) {
						if (clickedX[i] == 0) break;
						g.fillRect(clickedX[i]-2, clickedY[i]-2, 74, 104);
					}
				int x = 50, y = 50, cardnum = 1;
				for (int r = 0; r < table.length; r++)
					for (int c = 0; c < table[r].length; c++) {
						if (table[r][c] == null) break;
						g.drawImage(table[r][c].getImage(), x, y, 70, 100, this);
						if (cardnum%6 == 0) {
							x = 50;
							y+=110;
						} else
							x+=80;
						cardnum++;
					}
				if (selected3) {
					selected3 = false;
					clickedX = new int[3];
					clickedY = new int[3];
					selectedCards = new ArrayList<SetCard>();
					setValid = true;
				}
				g.setColor(Color.black);
				g.drawString("Score: " + score, 70, 390);
				g.drawString("Cards left: " + deck.size(), 405, 390);
				if (showNumSets)
					g.drawString("Sets on table: " + countNumSets(), 228, 390);
				g.setFont(new Font("Arial", Font.BOLD, 20));
				if (showTimer)
					g.drawString("" + time/60 + ":" + String.format("%02d", time%60), 485, 33);
			}

			private class MessageAction implements ActionListener {
				public void actionPerformed(ActionEvent e) {
					messageTime++;
					if (showDealMessage)
						if (messageTime < 30)
							actuallyShow = messageTime%2 == 0;
						else {
							messageTime = 0;
							messageTimer.stop();
							showDealMessage = actuallyShow = false;
						}
					repaint();
				}
			}

			private class WrongAction implements ActionListener {
				public void actionPerformed(ActionEvent e) {
					selected3 = false;
					wrongTimer.stop();
					repaint();
				}
			}

			private class BackAction implements ActionListener {
				public void actionPerformed(ActionEvent e) {
					timer.stop();
					time = 0;
					cardLayout.show(panel, "Intro");
				}
			}

			private class NumSetsAction implements ActionListener {
				public void actionPerformed(ActionEvent e) {
					if (showNumSets)
						numSetsButton.setText("Show # sets");
					else
						numSetsButton.setText("Hide # sets");
					showNumSets = !showNumSets;
				}
			}

			private class TimerButtonAction implements ActionListener {
				public void actionPerformed(ActionEvent e) {
					if (showTimer)
						timerButton.setText("Show timer");
					else
						timerButton.setText("Hide timer");
					showTimer = !showTimer;
				}
			}

			private class TimerAction implements ActionListener {
				public void actionPerformed(ActionEvent e) {
					time++;
					repaint();
				}
			}

			private class DealAction implements ActionListener {
				public void actionPerformed(ActionEvent e) {
					prevNumSets = countNumSets();
					if (deck.size() != 0 && prevNumSets != 0) {
						score-=50;
						messageTimer.stop();
						messageTimer.start();
						showDealMessage = true;
						messageTime = 0;
					}
					if (table[2][3] == null)
						deal3();
					triedToFind = false;
				}
			}

			private class ShuffleAction implements ActionListener {
				public void actionPerformed(ActionEvent e) {
					ArrayList<SetCard> temp = new ArrayList<SetCard>();
					for (int r = 0; r < table.length; r++)
						for (int c = 0; c < table[r].length; c++)
							if (table[r][c] != null)
								temp.add(table[r][c]);
					table = new SetCard[3][6];
					for (int r = 0; r < table.length && temp.size() > 0; r++)
						for (int c = 0; c < table[r].length && temp.size() > 0; c++) {
							SetCard rand = temp.get((int)(Math.random()*temp.size()));
							table[r][c] = rand;
							temp.remove(rand);
						}
					clickedX = new int[3];
					clickedY = new int[3];
					triedToFind = false;
					repaint();
				}
			}

			private class SolveAction implements ActionListener {
				public void actionPerformed(ActionEvent e) {
					pressedFind = true;
					if (countNumSets() != 0)
						score-=100;
					clickedX = new int[3];
					clickedY = new int[3];
					triedToFind = false;
					selectedCards = new ArrayList<SetCard>();
					showSet(3);
				}
			}

			private class HintAction implements ActionListener {
				public void actionPerformed(ActionEvent e) {
					if (countNumSets() != 0)
						score-=30;
					clickedX = new int[3];
					clickedY = new int[3];
					triedToFind = false;
					selectedCards = new ArrayList<SetCard>();
					showSet(1);
				}
			}

			private class ResetAction implements ActionListener {
				public void actionPerformed(ActionEvent e) {
					reset();
					time = 0;
				}
			}

			public int countNumSets() {
				SetSolver solver = new SetSolver(table);
				return solver.countSets();
			}

			public void setFound() {
				for (int i = 0; i < 3; i++)
					removeFromTable(selectedCards.get(i));
				if (table[1][3] == null && deck.size() != 0)
					deal3();
				if (!pressedFind)
					score+=100;
				else
					pressedFind = false;
				repaint();
			}

			public void removeFromTable(SetCard card) {
				ArrayList<SetCard> temp = new ArrayList<SetCard>();
				for (int r = 0; r < table.length; r++)
					for (int c = 0; c < table[r].length; c++)
						if (!card.equals(table[r][c]))
							temp.add(table[r][c]);
				int i = 0;
				table = new SetCard [3][6];
				for (int r = 0; r < table.length && i < temp.size(); r++)
					for (int c = 0; c < table[r].length && i < temp.size(); c++) {
						table[r][c] = temp.get(i);
						i++;
					}
				repaint();
			}

			public void deal3() {
				int rowEmpty = -1, colEmpty = -1;
				for (int r = 0; r < table.length; r++)
					for (int c = 0; c < table[r].length; c++)
						if (table[r][c] == null && rowEmpty == -1) {
							rowEmpty = r;
							colEmpty = c;
						}
				if (rowEmpty == -1) return;
				for (int i = 0; i < 3; i++) {
					SetCard rand = deck.get((int)(Math.random()*deck.size()));
					//SetCard rand = deck.get(0);
					table[rowEmpty][colEmpty + i] = rand;
					deck.remove(rand);
				}
				repaint();
			}

			public void reset() {
				deck = new ArrayList<SetCard>();
				for (int color = 0; color < 3; color++)
					for (int shape = 0; shape < 3; shape++)
						for (int fill = 0; fill < 3; fill++)
							for (int num = 0; num < 3; num++)
								deck.add(new SetCard(color, shape, fill, num));
				table = new SetCard[3][6];
				for (int r = 0; r < 2; r++)
					for (int c = 0; c < 6; c++) {
						SetCard rand = deck.get((int)(Math.random()*deck.size()));
						//SetCard rand = deck.get(0);
						table[r][c] = rand;
						deck.remove(rand);
					}
				clickedX = new int[3];
				clickedY = new int[3];
				triedToFind = false;
				time = 0;
				score = 0;
				repaint();
			}

			public void showSet(int n) {
				triedToFind = true;
				SetSolver solver = new SetSolver(table);
				ArrayList<SetCard> arrlist = solver.findSet(0);
				if (arrlist == null) {
					noSetFound = true;
					repaint();
					return;
				}
				noSetFound = false;
				chosenCards = new boolean [3][6];
				int num = 0;
				for (int i = 0; i < arrlist.size(); i++)
					for (int r = 0; r < table.length; r++)
						for (int c = 0; c < table[r].length; c++)
							if (arrlist.size() > 0 && arrlist.get(i).equals(table[r][c]) && num < n) {
								chosenCards[r][c] = true;
								arrlist.remove(i);
								num++;
							}
				repaint();
			}

			public void mousePressed(MouseEvent e) {
				int x = e.getX(), y = e.getY(), row = -1, col = -1;
				if (y > 370) return;
				if (x >= 50 && x <= 120) col = 0;
				if (x >= 130 && x <= 200) col = 1;
				if (x >= 210 && x <= 280) col = 2;
				if (x >= 290 && x <= 360) col = 3;
				if (x >= 370 && x <= 440) col = 4;
				if (x >= 450 && x <= 520) col = 5;

				if (y >= 75 && y <= 175) row = 0;
				if (y >= 185 && y <= 285) row = 1;
				if (table[2][0] != null && y >= 295 && y <= 395) row = 2;
				
				if (row == -1 || col == -1) return;
				if (!selectedCards.contains(table[row][col]))
					for (int i = 0; i < 3; i++) {
						if (clickedX[i] == 0) {
							clickedX[i] = xcoord[col];
							clickedY[i] = ycoord[row];
							selectedCards.add(table[row][col]);
							if (i == 2) {
								selected3 = true;
								setValid = Set.isSet(selectedCards);
								if (!setValid)
									wrongTimer.start();
							}
							break;
						}
					}
				else {
					selectedCards.remove(table[row][col]);
					int index = 0;
					for (int i = 0; i < clickedX.length; i++)
						if (clickedX[i] == xcoord[col])
							index = i;
					for (int i = index; i < clickedX.length-1; i++) {
						clickedX[i] = clickedX[i+1];
						clickedY[i] = clickedY[i+1];
					}
				}
				repaint();
				triedToFind = false;
			}
			
			public void mouseReleased(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
		}

		class SuperPanel extends JPanel implements MouseListener {
			private ArrayList<SetCard> deck, selectedCards;
			private SetCard [][] table;
			private JButton resetButton, shuffleButton, solveButton, hintButton,
				timerButton, backButton;
			private int [] xcoord = {50, 130, 210, 290, 370, 450},
				ycoord = {50, 160, 270};
			private int [] clickedX, clickedY;
			private boolean selected4, setValid, noSetFound, triedToFind,
				pressedFind, showTimer;
			private int time, score;
			private Timer timer;
			private boolean[][] chosenCards;

			public SuperPanel() {
				addMouseListener(this);
				setBackground(Color.gray);
				setLayout(null);
				reset();
				clickedX = new int[4];
				clickedY = new int[4];
				selectedCards = new ArrayList<SetCard>();;
				setValid = true;
				setUpButtons();
				timer = new Timer(1000, new TimerAction());
				noSetFound = true;
			}

			public void setUpButtons() {
				shuffleButton = new JButton("Shuffle cards");
				solveButton = new JButton("Find");
				hintButton = new JButton("Hint");
				resetButton = new JButton("New game");
				timerButton = new JButton("Show timer");
				backButton = new JButton("Back");

				add(shuffleButton);
				add(solveButton);
				add(hintButton);
				add(resetButton);
				add(timerButton);
				add(backButton);

				shuffleButton.setBounds(50, 400, 475, 45);
				solveButton.setBounds(50, 461, 225, 45);
				hintButton.setBounds(300, 461, 225, 45);
				resetButton.setBounds(50, 524, 225, 45);
				timerButton.setBounds(300, 524, 225, 45);
				backButton.setBounds(9, 9, 100, 30);

				shuffleButton.addActionListener(new ShuffleAction());
				solveButton.addActionListener(new SolveAction());
				hintButton.addActionListener(new HintAction());
				resetButton.addActionListener(new ResetAction());
				timerButton.addActionListener(new TimerButtonAction());
				backButton.addActionListener(new BackAction());
			}

			public void paintComponent(Graphics g) {
				playingSuper = true;
				super.paintComponent(g);
				SetSolver solver = new SetSolver(table);
				if (deck.size() == 0 && solver.findSuperSet(0) == null) {
					endScore = score;
					endTime = time;
					timer.stop();
					reset();
					cardLayout.show(panel, "End");
					return;
				}
				timer.start();
				g.setFont(new Font("Arial", Font.BOLD, 16));
				g.setColor(Color.green);
				if (noSetFound && triedToFind) {
					g.setColor(Color.red);
					g.drawString("NO SUPER SETS ON TABLE", 150, 25);
				}
				else if (triedToFind)
					for (int r = 0; r < chosenCards.length; r++)
						for (int c = 0; c < chosenCards[r].length; c++)
							if (chosenCards[r][c])
								g.fillRect(xcoord[c]-2, ycoord[r]-2, 74, 104);
				g.setColor(Color.blue);
				if (selected4)
					if (setValid)
						setFound();
					else g.setColor(Color.red);
				if (clickedX[0] != 0 && !triedToFind)
					for (int i = 0; i < 4; i++) {
						if (clickedX[i] == 0) break;
						g.fillRect(clickedX[i]-2, clickedY[i]-2, 74, 104);
					}
				int x = 50, y = 50, cardnum = 1;
				for (int r = 0; r < table.length; r++)
					for (int c = 0; c < table[r].length; c++) {
						if (table[r][c] == null) break;
						g.drawImage(table[r][c].getImage(), x, y, 70, 100, this);
						if (cardnum%6 == 0) {
							x = 50;
							y+=110;
						} else
							x+=80;
						cardnum++;
					}
				if (selected4) {
					selected4 = false;
					clickedX = new int[4];
					clickedY = new int[4];
					selectedCards = new ArrayList<SetCard>();
					setValid = true;
				}
				g.setColor(Color.black);
				g.drawString("Score: " + score, 70, 390);
				g.drawString("Cards left: " + deck.size(), 405, 390);
				g.setFont(new Font("Arial", Font.BOLD, 20));
				if (showTimer)
					g.drawString("" + time/60 + ":" + String.format("%02d", time%60), 485, 33);
			}

			private class BackAction implements ActionListener {
				public void actionPerformed(ActionEvent e) {
					timer.stop();
					time = 0;
					cardLayout.show(panel, "Intro");
				}
			}

			private class TimerButtonAction implements ActionListener {
				public void actionPerformed(ActionEvent e) {
					if (showTimer)
						timerButton.setText("Show timer");
					else
						timerButton.setText("Hide timer");
					showTimer = !showTimer;
					repaint();
				}
			}

			private class TimerAction implements ActionListener {
				public void actionPerformed(ActionEvent e) {
					time++;
					repaint();
				}
			}

			private class ShuffleAction implements ActionListener {
				public void actionPerformed(ActionEvent e) {
					ArrayList<SetCard> temp = new ArrayList<SetCard>();
					for (int r = 0; r < table.length; r++)
						for (int c = 0; c < table[r].length; c++)
							if (table[r][c] != null)
								temp.add(table[r][c]);
					table = new SetCard[3][6];
					for (int r = 0; r < table.length && temp.size() > 0; r++)
						for (int c = 0; c < table[r].length && temp.size() > 0; c++) {
							SetCard rand = temp.get((int)(Math.random()*temp.size()));
							table[r][c] = rand;
							temp.remove(rand);
						}
					clickedX = new int[4];
					clickedY = new int[4];
					triedToFind = false;
					repaint();
				}
			}

			private class SolveAction implements ActionListener {
				public void actionPerformed(ActionEvent e) {
					pressedFind = true;
					score-=100;
					clickedX = new int[4];
					clickedY = new int[4];
					triedToFind = false;
					selectedCards = new ArrayList<SetCard>();
					showSet(4);
				}
			}

			private class HintAction implements ActionListener {
				public void actionPerformed(ActionEvent e) {
					score-=30;
					clickedX = new int[4];
					clickedY = new int[4];
					triedToFind = false;
					selectedCards = new ArrayList<SetCard>();
					showSet(2);
				}
			}

			private class ResetAction implements ActionListener {
				public void actionPerformed(ActionEvent e) {
					reset();
					time = 0;
				}
			}

			public void setFound() {
				for (int i = 0; i < 4; i++)
					removeFromTable(selectedCards.get(i));
				if (table[1][0] == null && deck.size() != 0)
					deal(4);
				if (!pressedFind)
					score+=100;
				else
					pressedFind = false;
				repaint();
			}

			public void removeFromTable(SetCard card) {
				ArrayList<SetCard> temp = new ArrayList<SetCard>();
				for (int r = 0; r < table.length; r++)
					for (int c = 0; c < table[r].length; c++)
						if (!card.equals(table[r][c]))
							temp.add(table[r][c]);
				int i = 0;
				table = new SetCard [3][6];
				for (int r = 0; r < table.length && i < temp.size(); r++)
					for (int c = 0; c < table[r].length && i < temp.size(); c++) {
						table[r][c] = temp.get(i);
						i++;
					}
				repaint();
			}

			public void deal(int num) {
				int rowEmpty = -1, colEmpty = -1;
				for (int r = 0; r < table.length; r++)
					for (int c = 0; c < table[r].length; c++)
						if (table[r][c] == null && rowEmpty == -1) {
							rowEmpty = r;
							colEmpty = c;
						}
				if (rowEmpty == -1) return;
				int numInNewRow = -1;
				for (int i = 0; i < num; i++) {
					SetCard rand = deck.get((int)(Math.random()*deck.size()));
					//SetCard rand = deck.get(0);
					if (colEmpty+i <= 5)
						table[rowEmpty][colEmpty + i] = rand;
					else {
						if (numInNewRow == -1)
							numInNewRow = 4-i;
						table[rowEmpty+1][numInNewRow-i] = rand;
					}
					deck.remove(rand);
				}
				repaint();
			}

			public void reset() {
				deck = new ArrayList<SetCard>();
				for (int color = 0; color < 3; color++)
					for (int shape = 0; shape < 3; shape++)
						for (int fill = 0; fill < 3; fill++)
							for (int num = 0; num < 3; num++)
								deck.add(new SetCard(color, shape, fill, num));
				table = new SetCard[3][6];
				for (int r = 0; r < 2; r++)
					for (int c = 0; c < 6; c++) {
						if (!(r == 1 && c > 2)) {
							SetCard rand = deck.get((int)(Math.random()*deck.size()));
							//SetCard rand = deck.get(0);
							table[r][c] = rand;
							deck.remove(rand);
						}
					}
				clickedX = new int[4];
				clickedY = new int[4];
				triedToFind = false;
				time = 0;
				score = 0;
				repaint();
			}

			public void showSet(int n) {
				triedToFind = true;
				SetSolver solver = new SetSolver(table);
				ArrayList<SetCard> arrlist = solver.findSuperSet(0);
				if (arrlist == null) {
					noSetFound = true;
					repaint();
					return;
				}
				noSetFound = false;
				chosenCards = new boolean [3][6];
				int num = 0;
				for (int i = 0; i < arrlist.size(); i++)
					for (int r = 0; r < table.length; r++)
						for (int c = 0; c < table[r].length; c++)
							if (arrlist.get(i).equals(table[r][c]) && num < n) {
								chosenCards[r][c] = true;
								num++;
							}
				repaint();
			}

			public void mousePressed(MouseEvent e) {
				int x = e.getX(), y = e.getY(), row = -1, col = -1;
				if (y >= 75 && y <= 175) row = 0;
				if (y >= 185 && y <= 285) row = 1;
				if (table[2][0] != null && y >= 295 && y <= 395) row = 2;

				if (y > 370) return;
				if (x >= 50 && x <= 120) col = 0;
				if (x >= 130 && x <= 200) col = 1;
				if (x >= 210 && x <= 280) col = 2;
				if (table[1][3] != null || row == 0) {
					if (x >= 290 && x <= 360) col = 3;
					if (x >= 370 && x <= 440) col = 4;
					if (x >= 450 && x <= 520) col = 5;
				}
				
				if (row == -1 || col == -1) return;
				if (!selectedCards.contains(table[row][col]))
					for (int i = 0; i < 4; i++) {
						if (clickedX[i] == 0) {
							clickedX[i] = xcoord[col];
							clickedY[i] = ycoord[row];
							selectedCards.add(table[row][col]);
							if (i == 3) {
								selected4 = true;
								setValid = Set.isSuperSet(selectedCards);
							}
							break;
						}
					}
				else {
					selectedCards.remove(table[row][col]);
					int index = 0;
					for (int i = 0; i < clickedX.length; i++)
						if (clickedX[i] == xcoord[col])
							index = i;
					for (int i = index; i < clickedX.length-1; i++) {
						clickedX[i] = clickedX[i+1];
						clickedY[i] = clickedY[i+1];
					}
				}
				repaint();
				triedToFind = false;
			}
			
			public void mouseReleased(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
		}
	}
}