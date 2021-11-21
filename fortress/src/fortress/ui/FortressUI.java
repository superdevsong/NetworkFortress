package fortress.ui;

import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JTable;
import java.awt.SystemColor;
import java.awt.Color;
import net.miginfocom.swing.MigLayout;
import javax.swing.JTextArea;
import javax.swing.plaf.basic.BasicOptionPaneUI.ButtonActionListener;

import org.w3c.dom.events.MouseEvent;

import javax.swing.JFormattedTextField;
import javax.swing.JEditorPane;
import java.awt.Canvas;
import javax.swing.JScrollPane;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class FortressUI {
	public static final int SCREEN_WIDTH = 960;
	public static final int SCREEN_HEIGHT = 640;

	private JFrame frame;
	int x=100,y=200;
	int direction=1;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FortressUI window = new FortressUI();
					window.frame.setVisible(true);
					window.frame.setResizable(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public FortressUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		frame = new JFrame();
		frame.setBounds(100, 100, SCREEN_WIDTH, SCREEN_HEIGHT);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel panel = new MyPanel();
		
		
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);
		panel.setFocusable(true);
		panel.setVisible(true);

	}

}
