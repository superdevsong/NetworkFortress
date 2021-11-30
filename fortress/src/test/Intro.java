/*
 * package test;
 * 
 * import java.awt.Color; import java.awt.Graphics; import java.awt.Image;
 * import java.awt.event.ActionEvent; import java.awt.event.ActionListener;
 * import java.awt.event.MouseAdapter; import java.awt.event.MouseEvent; import
 * java.awt.event.MouseMotionAdapter;
 * 
 * import javax.swing.ImageIcon; import javax.swing.JButton; import
 * javax.swing.JFrame; import javax.swing.JLabel;
 * 
 * public class Intro extends JFrame { private Image screenImage; private
 * Graphics screenGraphic;
 * 
 * private Image mainImage = new ImageIcon("src/image/Title.jpg").getImage();
 * private JLabel menuBar = new JLabel(new ImageIcon("src/image/menuBar.png"));
 * 
 * private JButton exitButton = new JButton(new
 * ImageIcon("src/image/exitButton.jpg")); private JButton startButton = new
 * JButton(new ImageIcon("src/image/startButton.jpg")); private JButton
 * quitButton = new JButton(new ImageIcon("src/image/quitButton.jpg"));
 * 
 * private int mouseX, mouseY;
 * 
 * public Intro() { setUndecorated(true); setTitle("Battle Fortress");
 * setSize(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT); setResizable(false);
 * setLocationRelativeTo(null); setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 * setVisible(true); setBackground(new Color(0, 0, 0, 0)); setLayout(null);
 * 
 * exitButton.setBounds(930, 0, 30, 30); exitButton.setBorderPainted(false);
 * exitButton.setContentAreaFilled(false); exitButton.setFocusPainted(false);
 * exitButton.addMouseListener(new MouseAdapter() {
 * 
 * @Override public void mousePressed(MouseEvent e) { System.exit(0); } });
 * add(exitButton);
 * 
 * startButton.setBounds(15, 200, 400, 100);
 * startButton.setBorderPainted(false); startButton.setContentAreaFilled(false);
 * startButton.setFocusPainted(false); startButton.addMouseListener(new
 * MouseAdapter() {
 * 
 * @Override public void mousePressed(MouseEvent e) { gameStart(); } });
 * add(startButton);
 * 
 * quitButton.setBounds(15, 330, 400, 100); quitButton.setBorderPainted(false);
 * quitButton.setContentAreaFilled(false); quitButton.setFocusPainted(false);
 * quitButton.addMouseListener(new MouseAdapter() {
 * 
 * @Override public void mousePressed(MouseEvent e) { System.exit(0); } });
 * add(quitButton);
 * 
 * menuBar.setBounds(0, 0, 960, 30); menuBar.addMouseListener(new MouseAdapter()
 * {
 * 
 * @Override public void mousePressed(MouseEvent e) { mouseX = e.getX(); mouseY
 * = e.getY(); } }); menuBar.addMouseMotionListener(new MouseMotionAdapter() {
 * 
 * @Override public void mouseDragged(MouseEvent e) { int x = e.getXOnScreen();
 * int y = e.getYOnScreen(); setLocation(x - mouseX,y - mouseY); } });
 * add(menuBar);
 * 
 * 
 * 
 * add(quitButton); add(startButton);
 * 
 * }
 * 
 * public void paint(Graphics g) { screenImage = createImage(Main.SCREEN_WIDTH,
 * Main.SCREEN_HEIGHT); screenGraphic = screenImage.getGraphics();
 * screenDraw(screenGraphic); g.drawImage(screenImage, 0, 0, null); }
 * 
 * public void screenDraw(Graphics g) { g.drawImage(mainImage, 0, 0, null);
 * paintComponents(g); this.repaint(); }
 * 
 * private void gameStart() {
 * 
 * } }
 */