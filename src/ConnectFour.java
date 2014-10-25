import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EmptyStackException;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/*
 * HW11
 * "Connect Four pt 1"
 * Jonathan Cole
 */


public class ConnectFour implements ActionListener {
	
	private JFrame frame = new JFrame("ConnectFour!");
	private JPanel mainPanel = new JPanel(new BorderLayout());
	private JPanel boardPanel = new JPanel(new GridLayout(6,7));
	private JPanel buttonPanel = new JPanel(new GridLayout(1,7));
	private JPanel bottomPanel = new JPanel(new FlowLayout());
	private JTextField status = new JTextField();
	private JButton undo = new JButton("Undo");
	private Graphics graphics = frame.getGraphics();
	
	private final int EMPTY = 0;
	private final int PLAYER = 1;
	private final int COMP = 2;
	private final int TREE_LEVELS = 4;
	
	private JButton[] buttonArr = new JButton[7];
	
	private JTextArea[][] textAreaArr = new JTextArea[6][7];
	private int[][] values = new int[6][7];
	
	private Stack<Move> moves = new Stack<Move>();

	public ConnectFour(){
		
		frame.setContentPane(mainPanel);
		frame.setResizable(false);
		
		status.setColumns(30);
		bottomPanel.add(status);
		bottomPanel.add(undo);
		
		undo.setActionCommand("undo");
		undo.addActionListener(this);
		
		mainPanel.add(boardPanel);
		mainPanel.add(buttonPanel,BorderLayout.NORTH);
		mainPanel.add(bottomPanel,BorderLayout.SOUTH);

		
		for (int i=0; i<7; i++){
			buttonArr[i] = new JButton("Col "+ i);
			buttonPanel.add(buttonArr[i]);
			buttonArr[i].setActionCommand("button"+i);
			buttonArr[i].addActionListener(this);
		}
		
		for (int r=5; r>-1; r--){  // Have to put the rows in backwards so row 0 is on the bottom
			for (int c=0; c<7; c++){
				textAreaArr[r][c] = new JTextArea();
				textAreaArr[r][c].setEnabled(false);
				textAreaArr[r][c].setColumns(5);
				textAreaArr[r][c].setRows(3);
				textAreaArr[r][c].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
				textAreaArr[r][c].setBackground(Color.YELLOW);
				boardPanel.add(textAreaArr[r][c]);
				values[r][c] = EMPTY;
			}
		}
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().length() >= 6 && e.getActionCommand().substring(0,6).equals("button")){
			if (chooseSquare(PLAYER,new Integer(e.getActionCommand().substring(6)),values,true)){
				compMove(false);
			}
			status.setText(""+boardScore(values));
		} else {
			undo();
		}
	}
	
	private boolean chooseSquare(int who, int col, int[][] board, boolean push){
		int row = 0;
		while (row < 6 && board[row][col] != EMPTY){
			row++;
		}
		if (row == 6)
			return false;
		else {
			
			board[row][col] = who;
			if (push){
				fillSquare(who, row, col);
				moves.push(new Move(who,row,col));
			}
			return true;
		}
	}
	
	private void fillSquare (int who, int row, int col){
		Graphics g = textAreaArr[row][col].getGraphics();
		if (who == PLAYER)
			g.setColor(Color.BLACK);
		else
			g.setColor(Color.RED);
		g.fillOval(0,0,textAreaArr[row][col].getWidth(),textAreaArr[row][col].getHeight());
	}
	
	private void undo(){
		try {
			Move move1 = moves.pop();
			Move move2 = moves.pop();
			
			values[move1.getRow()][move1.getCol()] = EMPTY;
			values[move2.getRow()][move2.getCol()] = EMPTY;
			
			textAreaArr[move1.getRow()][move1.getCol()].repaint();
			textAreaArr[move2.getRow()][move2.getCol()].repaint();
		} catch (EmptyStackException e){
			return;
		}
	}
	
	private void compMove(boolean rand){
		if (rand){
			boolean b;
			do {
				 b = chooseSquare(COMP,(int)(Math.random()*7), values, true);
			} while (!b);
		} else {
		
			int[][] valuesClone = new int[6][7];
			
			for (int row=0; row<6; row++){
				for (int col=0; col<7; col++){
					valuesClone[row][col] = values[row][col];
				}
			}
			
			
			MoveTree tree = new MoveTree(valuesClone);
			if (tree.getBestCol() > -1)
				chooseSquare(COMP,tree.getBestCol(),values,true);
			else {
				System.out.println("crap");
				System.exit(1);
			}
			
		}
	}
	
	private int boardScore(int[][] board){
		int result = 0;
		
		// Horizontals
		for (int row=0; row<6; row++){
			for (int col=0; col<4; col++){
				result += fourScore(board[row][col], board[row][col+1], board[row][col+2], board[row][col+3]);
			}
		}
		
		// Verticals
		for (int col=0; col<7; col++){
			for (int row=0; row<3; row++){
				result += fourScore(board[row][col], board[row+1][col], board[row+2][col], board[row+3][col]);
			}
		}
		
		// Diagonals like /  (NB: 0,0 in bottom left corner)
		for (int row=0; row<3; row++){
			for (int col=0; col<4; col++){
				result += fourScore(board[row][col], board[row+1][col+1], board[row+2][col+2], board[row+3][col+3]);
			}
		}
		
		// Diagonals like \
		for (int row=0; row<3; row++){
			for (int col=3; col<7; col++){
				result += fourScore(board[row][col], board[row+1][col-1], board[row+2][col-2], board[row+3][col-3]);
			}
		}
		return result;
	}
	
	private int fourScore(int a, int b, int c, int d){
		int result = 0;
		
		if (a == COMP && b == COMP && c == COMP && d == COMP){
			result = 1000000;
		} else if (a == PLAYER && b == PLAYER && c == PLAYER && d == PLAYER){
			result = -1000000;
		} else if ((a == COMP || b == COMP || c == COMP || d == COMP)&&(a == PLAYER || b == PLAYER || c == PLAYER || d == PLAYER)){
			result = 0;
		} else if (a != COMP && b != COMP && c != COMP && d != COMP){
			result = ((a == PLAYER) ? -1 : 0) + ((b == PLAYER) ? -1 : 0) + ((c == PLAYER) ? -1 : 0) + ((d == PLAYER) ? -1 : 0); 
		} else if (a != PLAYER && b != PLAYER && c != PLAYER && d != PLAYER){
			result = ((a == COMP) ? 1 : 0) + ((b == COMP) ? 1 : 0) + ((c == COMP) ? 1 : 0) + ((d == COMP) ? 1 : 0); 
		} else {
			System.out.println("Fourscore Failure");
			System.exit(1);
		}
		
		return result;
	}
	
	private class MoveTree {
		private Node root;
		private int bestCol;
		
		public MoveTree(int[][] board){
			root = new Node(board,PLAYER,null,0);
	//		System.out.println("root: "+root.describe());
			root.setCol0(populateNode(root,1,0));
			root.setCol1(populateNode(root,1,1));
			root.setCol2(populateNode(root,1,2));
			root.setCol3(populateNode(root,1,3));
			root.setCol4(populateNode(root,1,4));
			root.setCol5(populateNode(root,1,5));
			root.setCol6(populateNode(root,1,6));
			
			Integer score = null;
			int col = -1;
			if (score == null || (root.hasCol0() && scoreByLevel(root.getCol0()) > score)){
				score = scoreByLevel(root.getCol0());
				col = 0;
			}
			if (score == null || (root.hasCol1() && scoreByLevel(root.getCol1()) > score)){
				score = scoreByLevel(root.getCol1());
				col = 1;
			}
			if (score == null || (root.hasCol2() && scoreByLevel(root.getCol2()) > score)){
				score = scoreByLevel(root.getCol2());
				col = 2;
			}
			if (score == null || (root.hasCol3() && scoreByLevel(root.getCol3()) > score)){
				score = scoreByLevel(root.getCol3());
				col = 3;
			}
			if (score == null || (root.hasCol4() && scoreByLevel(root.getCol4()) > score)){
				score = scoreByLevel(root.getCol4());
				col = 4;
			}
			if (score == null || (root.hasCol5() && scoreByLevel(root.getCol5()) > score)){
				score = scoreByLevel(root.getCol5());
				col = 5;
			}
			if (score == null || (root.hasCol6() && scoreByLevel(root.getCol6()) > score)){
				score = scoreByLevel(root.getCol6());
				col = 6;
			}
			System.out.println("scores-- 0:"+scoreByLevel(root.getCol0()) + " 1:"+scoreByLevel(root.getCol1()) + " 2:"+scoreByLevel(root.getCol2()) + " 3:"+scoreByLevel(root.getCol3()) + " 4:"+scoreByLevel(root.getCol4()) + " 5:"+scoreByLevel(root.getCol5()) + " 6:"+scoreByLevel(root.getCol6()));
			bestCol = col;
		}
		
		private Integer scoreByLevel(Node arg){
			if (arg != null){
				if (arg.getScore() != null){
					return arg.getScore();
				}else{
					Integer result = null;
					if (arg.getLevel() % 2 == 1){ // minimize
						result = (arg.hasCol0() && scoreByLevel(arg.getCol0()) != null && (result == null || scoreByLevel(arg.getCol0()) < result)) ? scoreByLevel(arg.getCol0()) : result;
						result = (arg.hasCol1() && scoreByLevel(arg.getCol1()) != null && (result == null || scoreByLevel(arg.getCol1()) < result)) ? scoreByLevel(arg.getCol1()) : result;
						result = (arg.hasCol2() && scoreByLevel(arg.getCol2()) != null && (result == null || scoreByLevel(arg.getCol2()) < result)) ? scoreByLevel(arg.getCol2()) : result;
						result = (arg.hasCol3() && scoreByLevel(arg.getCol3()) != null && (result == null || scoreByLevel(arg.getCol3()) < result)) ? scoreByLevel(arg.getCol3()) : result;
						result = (arg.hasCol4() && scoreByLevel(arg.getCol4()) != null && (result == null || scoreByLevel(arg.getCol4()) < result)) ? scoreByLevel(arg.getCol4()) : result;
						result = (arg.hasCol5() && scoreByLevel(arg.getCol5()) != null && (result == null || scoreByLevel(arg.getCol5()) < result)) ? scoreByLevel(arg.getCol5()) : result;
						result = (arg.hasCol6() && scoreByLevel(arg.getCol6()) != null && (result == null || scoreByLevel(arg.getCol6()) < result)) ? scoreByLevel(arg.getCol6()) : result;
					} else{
						result = (arg.hasCol0() && scoreByLevel(arg.getCol0()) != null && (result == null || scoreByLevel(arg.getCol0()) > result)) ? scoreByLevel(arg.getCol0()) : result;
						result = (arg.hasCol1() && scoreByLevel(arg.getCol1()) != null && (result == null || scoreByLevel(arg.getCol1()) > result)) ? scoreByLevel(arg.getCol1()) : result;
						result = (arg.hasCol2() && scoreByLevel(arg.getCol2()) != null && (result == null || scoreByLevel(arg.getCol2()) > result)) ? scoreByLevel(arg.getCol2()) : result;
						result = (arg.hasCol3() && scoreByLevel(arg.getCol3()) != null && (result == null || scoreByLevel(arg.getCol3()) > result)) ? scoreByLevel(arg.getCol3()) : result;
						result = (arg.hasCol4() && scoreByLevel(arg.getCol4()) != null && (result == null || scoreByLevel(arg.getCol4()) > result)) ? scoreByLevel(arg.getCol4()) : result;
						result = (arg.hasCol5() && scoreByLevel(arg.getCol5()) != null && (result == null || scoreByLevel(arg.getCol5()) > result)) ? scoreByLevel(arg.getCol5()) : result;
						result = (arg.hasCol6() && scoreByLevel(arg.getCol6()) != null && (result == null || scoreByLevel(arg.getCol6()) > result)) ? scoreByLevel(arg.getCol6()) : result;
					}
					arg.setScore(result);
					return result;
				}
			}else{
			//	System.out.println("weak");
			//	System.exit(1);
				return null;
			}
		}
		
		private Node populateNode(Node start, int lvl, int col){
			int who = (lvl % 2 == 1) ? COMP : PLAYER;
			
			int[][] newBoard = new int[6][7];
			
			for (int row1=0; row1<6; row1++){
				for (int col1=0; col1<7; col1++){
					newBoard[row1][col1] = start.getBoard()[row1][col1];
				}
			}
			
			Node result = new Node(newBoard,who,start,lvl);
		//	System.out.println("lvl " + lvl + ": " + result.describe());
			if (chooseSquare(who, col, result.getBoard(),false)){
				if (lvl < TREE_LEVELS){
					result.setCol0(populateNode(result,lvl+1,0));
					result.setCol1(populateNode(result,lvl+1,1));
					result.setCol2(populateNode(result,lvl+1,2));
					result.setCol3(populateNode(result,lvl+1,3));
					result.setCol4(populateNode(result,lvl+1,4));
					result.setCol5(populateNode(result,lvl+1,5));
					result.setCol6(populateNode(result,lvl+1,6));
				} else {
					result.setScore(boardScore(result.getBoard()));
				}
				return result;
			} else {
				//System.out.println("null node here");
				//System.exit(1);
				return null;
			}
		}
		
		public int getBestCol(){return bestCol;}
		
		private class Node {
			private int[][] board = new int[6][7];
			private Node parent, col0, col1, col2, col3, col4, col5, col6 = null;
			private int whosLast; // player who made the most recent move in this board
			private Integer score = null;
			private int level;
			
			public Node(int[][] board, int who, Node parent, int level){
				this.board = board;
				whosLast = who;
				this.parent = parent;
				this.level = level;
			}
			
			public int[][] getBoard(){return board;}
			public int getWho(){return whosLast;}
			public Node getCol0(){return col0;}
			public Node getCol1(){return col1;}
			public Node getCol2(){return col2;}
			public Node getCol3(){return col3;}
			public Node getCol4(){return col4;}
			public Node getCol5(){return col5;}
			public Node getCol6(){return col6;}
			public Integer getScore(){return score;}
			public int getLevel(){return level;}
			
			public void setParent(Node arg){parent = arg;}
			public void setBoard(int[][] arg){board = arg;}
			public void setCol0(Node arg){col0 = arg; if (arg != null) arg.setParent(this);}
			public void setCol1(Node arg){col1 = arg; if (arg != null) arg.setParent(this);}
			public void setCol2(Node arg){col2 = arg; if (arg != null) arg.setParent(this);}
			public void setCol3(Node arg){col3 = arg; if (arg != null) arg.setParent(this);}
			public void setCol4(Node arg){col4 = arg; if (arg != null) arg.setParent(this);}
			public void setCol5(Node arg){col5 = arg; if (arg != null) arg.setParent(this);}
			public void setCol6(Node arg){col6 = arg; if (arg != null) arg.setParent(this);}
			public void setScore(int arg){score = arg;}
			
			public boolean hasCol0(){return (col0 == null) ? false : true;}
			public boolean hasCol1(){return (col1 == null) ? false : true;}
			public boolean hasCol2(){return (col2 == null) ? false : true;}
			public boolean hasCol3(){return (col3 == null) ? false : true;}
			public boolean hasCol4(){return (col4 == null) ? false : true;}
			public boolean hasCol5(){return (col5 == null) ? false : true;}
			public boolean hasCol6(){return (col6 == null) ? false : true;}
			
			public String describe(){
				String result = this.toString();
				if (parent != null){
					result = result +" parent: " + parent.toString();
				}
				result = result + " score: " + score;
				return result;
			}
			
		}
	}
	
	private class Move {
		private int who, row, col;
		public Move(int who, int row, int col){
			this.who = who;
			this.row = row;
			this.col = col;
		}
		public int getWho(){return who;}
		public int getRow(){return row;}
		public int getCol(){return col;}
	}
	

	public static void main(String[] args) {
		@SuppressWarnings("unused")
		ConnectFour c4 = new ConnectFour();
	//	c4.compMove(true);
		//System.out.println(new Integer(4)>null);
	}

	

}
