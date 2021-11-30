package fortress.ui;

import java.awt.EventQueue;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JTable;
import java.awt.SystemColor;
import java.awt.Color;
import net.miginfocom.swing.MigLayout;
import test.Main;

import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicOptionPaneUI.ButtonActionListener;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JFormattedTextField;
import javax.swing.JEditorPane;
import java.awt.Canvas;
import javax.swing.JScrollPane;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;

public class FortressUI {
	public static final int SCREEN_WIDTH = 960;
	public static final int SCREEN_HEIGHT = 640;
	JTextField txtIpAddress;
	JTextField txtPortNumber;
	JTextField txtUserName;
	private JFrame frame;
	int x=100,y=200;
	int alpha=0;
	int direction=1;
	

	private Image mainImage = new ImageIcon("src/image/Title.jpg").getImage();
	
	private JButton startButton = new JButton(new ImageIcon("src/image/startButton.jpg"));
	private JButton quitButton = new JButton(new ImageIcon("src/image/quitButton.jpg"));
	private JPanel intro_panel;

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
		frame.setTitle("Battle Fortress");
		frame.setResizable(false);
		frame.setBounds(100, 100, SCREEN_WIDTH, SCREEN_HEIGHT);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		intro_panel = new JPanel()
		{
			public JPanel intro() {
			
			
			
		
			setLayout(null);
			
			
			
			
			
			startButton.setBounds(15, 200, 400, 100);
			startButton.setBorderPainted(false);
			startButton.setContentAreaFilled(false);
			startButton.setFocusPainted(false);
			startButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					gameStart();
				}
			});
			
			
			
			quitButton.setBounds(15, 330, 400, 100);
			quitButton.setBorderPainted(false);
			quitButton.setContentAreaFilled(false);
			quitButton.setFocusPainted(false);
			quitButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					System.exit(0);
				}
			});
			
			
			
			
			

			

	
			add(startButton);
			add(quitButton);
			return this;

		}
		
		public void paint(Graphics g) {
			g.drawImage(mainImage, 0, 0, null);
			paintComponents(g);
			if(alpha==255)
				alpha=254;
			g.setColor(new Color(0,0,0,alpha));
			g.fillRect(0,0,SCREEN_WIDTH,SCREEN_HEIGHT);
				 
			
		}

		

		private void gameStart() {
			remove(quitButton);
			remove(startButton);
			JLabel lblNewLabel = new JLabel("User Name");
			lblNewLabel.setBounds(12, 39, 82, 33);
			add(lblNewLabel);
			
			txtUserName = new JTextField();
			txtUserName.setHorizontalAlignment(SwingConstants.CENTER);
			txtUserName.setBounds(101, 39, 116, 33);
			add(txtUserName);
			txtUserName.setColumns(10);
			
			JLabel lblIpAddress = new JLabel("IP Address");
			lblIpAddress.setBounds(12, 100, 82, 33);
			add(lblIpAddress);
			
			txtIpAddress = new JTextField();
			txtIpAddress.setHorizontalAlignment(SwingConstants.CENTER);
			txtIpAddress.setText("127.0.0.1");
			txtIpAddress.setColumns(10);
			txtIpAddress.setBounds(101, 100, 116, 33);
			add(txtIpAddress);
			
			JLabel lblPortNumber = new JLabel("Port Number");
			lblPortNumber.setBounds(12, 163, 82, 33);
			add(lblPortNumber);
			
			txtPortNumber = new JTextField();
			txtPortNumber.setText("30000");
			txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
			txtPortNumber.setColumns(10);
			txtPortNumber.setBounds(101, 163, 116, 33);
			add(txtPortNumber);
			
			JButton btnConnect = new JButton("Connect");
			btnConnect.setBounds(12, 223, 205, 38);
			add(btnConnect);
			Myaction action = new Myaction();
			btnConnect.addActionListener(action);
			txtUserName.addActionListener(action);
			txtIpAddress.addActionListener(action);
			txtPortNumber.addActionListener(action);
			repaint();
	
		}
		}.intro();
		
		frame.getContentPane().add(intro_panel, BorderLayout.CENTER);
		
		
		

	}
	class Myaction implements ActionListener // 내부클래스로 액션 이벤트 처리 클래스
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			String username = txtUserName.getText().trim();
			String ip_addr = txtIpAddress.getText().trim();
			String port_no = txtPortNumber.getText().trim();
			Thread start = new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					synchronized(this) {
					while(alpha<254) {
						alpha +=5;
						try {
							intro_panel.repaint();
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					notify();
					}
					
					}});
			start.setDaemon(true);
			start.start();
			
		new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					synchronized(start) {
						try {
							start.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					
						frame.getContentPane().removeAll();
						
							JPanel panel = new MyPanel(username, ip_addr, port_no);
							
							frame.setLayout( new BorderLayout());
							frame.getContentPane().add(panel, BorderLayout.CENTER);
							frame.revalidate();
							
							panel.setLayout(null);
							panel.setFocusable(true);
							panel.setVisible(true);
							panel.grabFocus();
							
							
					
					}
					}}).start();
			
		}
	}

}
