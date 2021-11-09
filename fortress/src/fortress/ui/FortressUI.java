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

	private JFrame frame;
	int x=100,y=100;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FortressUI window = new FortressUI();
					window.frame.setVisible(true);
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
		frame.setBounds(100, 100, 528, 426);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel panel = new JPanel() {
			Image image = new ImageIcon("C:\\dev\\fortress\\fortress\\src\\fortress\\ui\\tree.jpg").getImage();
			Image image_c = new ImageIcon("C:\\dev\\fortress\\fortress\\src\\fortress\\ui\\cannon.png").getImage();
			@Override
			public void paintComponent(Graphics g) {
				g.drawImage(image, 0,0,null);
				g.drawImage(image_c,x,y,null);
			}
			
			
		};
		
		panel.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				System.out.println(e.getKeyCode());
				 if( e.getKeyCode() == 37 ) {
					 x-=1;
					 panel.repaint();
				 }
	             if( e.getKeyCode() == 39 ) {
	            	 x+=1;
					 panel.repaint();
	             }
			}
		});
		
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);
		panel.setFocusable(true);
		JLabel lblNewLabel = new JLabel(new ImageIcon("C:\\dev\\fortress\\fortress\\src\\fortress\\ui\\cannon.png"));
		lblNewLabel.setBounds(100, 100, 300, 200);
		panel.add(lblNewLabel);
		panel.setVisible(true);

	}

}
