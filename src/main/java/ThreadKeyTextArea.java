import java.awt.event.KeyListener;
import java.util.Collections;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.scene.input.KeyCode;

public class ThreadKeyTextArea implements Runnable {

	// main thread
	public static void main(String[] args) {

		// create new thread
		new Thread(new ThreadKeyTextArea()).start();
	}

	@Override
	public void run() {
		// single game that will be shared between threads
		var game = new Game();
		// GUI thread
		new Thread(new GUI(game)).start();
		// enemy thread
		new Thread(new Enemy(game)).start();
	}

	public class GUI implements Runnable {

		protected Game game;
		protected JTextArea textarea;

		public GUI(Game game) {
			this.game = game;
			// print field on change
			this.game.addListener(new Runnable() {
				@Override
				public void run() {
					System.out.println("field updated!");
					printField();
				}
			});
			// init textarea
			this.textarea = new JTextArea();
			this.textarea.setFocusable(false);
		}

		public void printField() {
			var field = this.game.getField().get();
			var output = new StringBuilder();
			for (var y = 0; y < field.size(); y++) {
				for (var x = 0; x < field.get(y).size(); x++) {
					output.append(field.get(y).get(x) + " ");
				}
				output.append("\n");
			}
			// print to textarea
			this.textarea.setText(output.toString());
		}

		@Override
		public void run() {
			// panel
			var panel = new JPanel();
			// add textarea
			panel.add(textarea);
			panel.setFocusable(true);
			panel.addKeyListener(new KeyListener() {
				@Override
				public void keyReleased(java.awt.event.KeyEvent e) {}

				@Override
				public void keyTyped(java.awt.event.KeyEvent e) {}

				@Override
				public void keyPressed(java.awt.event.KeyEvent e) {
					// print list on key pressed
					System.out.println("key pressed: " + e.getKeyChar());
					printField();
					// exit on key Q
					if (e.getKeyCode() == KeyCode.Q.getCode()) {
						System.out.println("bye!");
						System.exit(0);
					}
				}
			});
			// frame
			var frame = new JFrame();
			frame.setContentPane(panel);
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			frame.pack();
			frame.setVisible(true);
		}
	}

	public class Enemy implements Runnable {

		protected Game game;

		public Enemy(Game game) {
			this.game = game;
		}

		@Override
		public void run() {
			try {
				while (true) {
					// randomly set numbers in field
					var x = (int) (Math.random() * Game.SIZE_X);
					var y = (int) (Math.random() * Game.SIZE_Y);
					var value = (int) (Math.random() * 10);
					this.game.set(x, y, value);
					// wait 1 second
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public class Game {

		public static final int SIZE_X = 16;
		public static final int SIZE_Y = 9;

		// list inside list with integer values
		private ListProperty<ListProperty<Integer>> field;

		public Game() {
			// fill list with zeroes
			this.field = new SimpleListProperty<>(FXCollections.observableArrayList());
			for (var y = 0; y < SIZE_Y; y++) {
				var listZeroes = Collections.nCopies(SIZE_X, 0);
				this.field.add(y, new SimpleListProperty<>(FXCollections.observableArrayList(listZeroes)));
			}
		}

		public synchronized ListProperty<ListProperty<Integer>> getField() {
			return this.field;
		}

		public synchronized void addListener(Runnable changed) {
			// add listener for field -> when field[y] changed
			this.field.addListener(new ListChangeListener<ListProperty<Integer>>() {
				@Override
				public void onChanged(Change<? extends ListProperty<Integer>> c) {
					changed.run();
				}
			});
			// add listener for all field[y] -> when field[y][x] changed
			for (var list : this.field) {
				list.addListener(new ListChangeListener<Integer>() {
					@Override
					public void onChanged(Change<? extends Integer> c) {
						changed.run();
					}
				});
			}
		}

		public synchronized void set(int x, int y, int value) {
			if (0 < x && x < SIZE_X && 0 < y && y < SIZE_Y) {
				// field[y][x] = value -> trigger change listener
				this.field.get(y).set(x, value);
			}
		}
	}
}
