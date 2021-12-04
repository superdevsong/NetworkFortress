package fortress.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

public class Room extends JPanel {
	private JTextField txtInput;
	private String UserName;
	private JButton btnSend;
	private Socket socket; // 연결소켓

	JPanel user[] = new JPanel[4];
	JButton start, ready, exit;
	JButton u1, u2, u3, u4;
	JTable table;
	JTextField user1, user2, user3, user4;
	ImageIcon team1 = new ImageIcon("src/fortress/ui/cannon.png");
	ImageIcon team2 = new ImageIcon("src/fortress/ui/team2.png");
	String TeamStatus;

	DefaultTableModel model;

	private DataInputStream dis;
	private DataOutputStream dos;

	private ObjectInputStream ois;
	private ObjectOutputStream oos;

	private JLabel lblUserName;
	// private JTextArea textArea;
	private JTextPane textArea;
	private JButton readyBtn;
	private JFrame frame;

	class ListenNetwork extends Thread {
		private boolean stop = false;

		public void run() {
			while (!stop) {
				try {
					// String msg = dis.readUTF();
//					byte[] b = new byte[BUF_LEN];
//					int ret;
//					ret = dis.read(b);
//					if (ret < 0) {
//						AppendText("dis.read() < 0 error");
//						try {
//							dos.close();
//							dis.close();
//							socket.close();
//							break;
//						} catch (Exception ee) {
//							break;
//						}// catch문 끝
//					}
//					String	msg = new String(b, "euc-kr");
//					msg = msg.trim(); // 앞뒤 blank NULL, \n 모두 제거

					Object obcm = null;
					String msg = null;
					ChatMsg cm;
					try {
						obcm = ois.readObject();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}
					if (obcm == null)
						break;
					if (obcm instanceof ChatMsg) {
						cm = (ChatMsg) obcm;
						msg = String.format("[%s] %s", cm.getId(), cm.getData());
					} else
						continue;
					switch (cm.getCode()) {
					case "200": // player join
						AppendText(msg);
						break;
					case "300": // Image 첨부
						AppendText("[" + cm.getId() + "]");
						break;
					case "499": // 게임시
						System.out.println(ois + "존");
						System.out.println(oos + "존");
						AppendText("게임을 시작합니다.");
						try {
							sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						JPanel panel = new MyPanel(ois, oos, socket);
						frame.getContentPane().removeAll();// 프레임을 비우고
						frame.setLayout(new BorderLayout());
						frame.getContentPane().add(panel, BorderLayout.CENTER);// 새로운 패널을 추가한뒤
						frame.revalidate();// 재확인한다. 그럼 패널이 바뀌어있음

						panel.setLayout(null);
						panel.setFocusable(true);
						panel.setVisible(true);
						panel.grabFocus();// 패널이 포커스를 가져온다.
						stop = true;
						break;
					case "801":
						TeamStatus="team1";
						user1.setText(cm.getId()+"(Team1)");
						
						break;
					case "802":
						TeamStatus="team1";
						user2.setText(cm.getId()+"(Team1)");
					
						break;
					case "803":
						TeamStatus="team2";
						user3.setText(cm.getId()+"(Team2)");
						break;
					case "804":
						TeamStatus="team2";
						user4.setText(cm.getId()+"(Team2)");
						break;

					}

				} catch (IOException e) {
					AppendText("ois.readObject() error");
					try {
//						dos.close();
//						dis.close();
						ois.close();
						oos.close();
						socket.close();

						break;
					} catch (Exception ee) {
						break;
					} // catch문 끝
				} // 바깥 catch문끝

			}
		}
	}

public Room(String username, String ip_addr, String port_no,JFrame frame) {
		
		setLayout(null);
		

		
		JPanel jp=new JPanel();
		JPanel jp2=new JPanel();
		JPanel jp3=new JPanel();
		JPanel jp4=new JPanel();

		user1 = new JTextField("Team1");
		user2 = new JTextField("Team1");
		user3 = new JTextField("Team2");
		user4 = new JTextField("Team2");
	
		
		
		jp.setBounds(500,10,185,300);
		jp2.setBounds(700,10,185,150);
		jp3.setBounds(500,205,185,150);
		jp4.setBounds(700,205,185,150);
		
		user1.setBounds(500,165,185,30);
		user2.setBounds(700,165,185,30);
		user3.setBounds(500,360,185,30);
		user4.setBounds(700,360,185,30);
		
	
		
		JPanel button = new JPanel();
		start=new JButton("시작");
		ready=new JButton("준비");
		exit=new JButton("나가기");
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ChatMsg msg = new ChatMsg(UserName, "201", "Bye", -10, -10);
				SendObject(msg);
				System.exit(0);
			}
		});
		button.setLayout(new GridLayout(3,1));
		button.add(start);
		button.add(ready);
		button.add(exit);
		button.setBounds(570,400,280,140);
		add(jp);
		add(jp2);
		add(jp3);
		add(jp4);
		
		add(user1);
		add(user2);
		add(user3);
		add(user4);
		add(button);
		
		u1 = new  JButton(team1);
		u1.setBorderPainted(false); // 버튼 테두리 설정해제
		u1.setPreferredSize(new Dimension(185, 150));// 버튼 크기 지정
		jp.add(u1); // 패널에 버튼을 붙여준다
		u1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ChatMsg msg = new ChatMsg(UserName, "801", "Team1", -10, -10);
				SendObject(msg);
			}
		});
		
		
		
		u2 = new JButton(team1);
		u2.setBorderPainted(false); // 버튼 테두리 설정해제
		u2.setPreferredSize(new Dimension(185, 150)); // 버튼 크기 지정
		jp2.add(u2); // 패널에 버튼을 붙여준다
		u2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ChatMsg msg = new ChatMsg(UserName, "802", "Team1", -10, -10);
				SendObject(msg);
			
			}
		});
		
		u3 = new JButton(team2);
		u3.setBorderPainted(false); // 버튼 테두리 설정해제
		u3.setPreferredSize(new Dimension(185, 150)); // 버튼 크기 지정
		jp3.add(u3); // 패널에 버튼을 붙여준다
		u3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ChatMsg msg = new ChatMsg(UserName, "803", "Team2", -10, -10);
				SendObject(msg);
			
			}
		});
		
		u4 = new JButton(team2);
		u4.setBorderPainted(false); // 버튼 테두리 설정해제
		u4.setPreferredSize(new Dimension(185, 150)); // 버튼 크기 지정
		jp4.add(u4); // 패널에 버튼을 붙여준다
		u4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ChatMsg msg = new ChatMsg(UserName, "804", "Team2", -10, -10);
				SendObject(msg);
			}
		});
		
		
		
		
		
		setSize(800,600);
		setVisible(true);
		
		
		this.frame = frame;
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(null);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 352, 471);
		add(scrollPane);
		textArea = new JTextPane();
		textArea.setEditable(true);
		textArea.setFont(new Font("굴림체", Font.PLAIN, 14));
		scrollPane.setViewportView(textArea);
		txtInput = new JTextField();
		txtInput.setBounds(74, 489, 209, 40);
		add(txtInput);
		txtInput.setColumns(10);

		btnSend = new JButton("Send");
		btnSend.setFont(new Font("굴림", Font.PLAIN, 14));
		btnSend.setBounds(295, 489, 69, 40);
		add(btnSend);

		lblUserName = new JLabel("Name");
		lblUserName.setBorder(new LineBorder(new Color(0, 0, 0)));
		lblUserName.setBackground(Color.WHITE);
		lblUserName.setFont(new Font("굴림", Font.BOLD, 14));
		lblUserName.setHorizontalAlignment(SwingConstants.CENTER);
		lblUserName.setBounds(12, 539, 62, 40);
		add(lblUserName);
		setVisible(true);

		AppendText("User " + username + " connecting " + ip_addr + " " + port_no);
		UserName = username;
		lblUserName.setText(username);
	

		try {
			socket = new Socket(ip_addr, Integer.parseInt(port_no));
//			is = socket.getInputStream();
//			dis = new DataInputStream(is);
//			os = socket.getOutputStream();
//			dos = new DataOutputStream(os);

			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(socket.getInputStream());

			// SendMessage("/login " + UserName);
			ChatMsg obcm = new ChatMsg(UserName, "100", "Join", -10, -10);
			SendObject(obcm);

			ListenNetwork net = new ListenNetwork();
			net.start();
			TextSendAction action = new TextSendAction();
			btnSend.addActionListener(action);
			txtInput.addActionListener(action);
			txtInput.requestFocus();
			ReadySendAction action2 = new ReadySendAction();
			ready.addActionListener(action2);

		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			AppendText("connect error");
		}
	}

	class TextSendAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Send button을 누르거나 메시지 입력하고 Enter key 치면
			if (e.getSource() == btnSend || e.getSource() == txtInput) {
				String msg = null;
				// msg = String.format("[%s] %s\n", UserName, txtInput.getText());
				msg = txtInput.getText();
				SendMessage(msg);
				txtInput.setText(""); // 메세지를 보내고 나면 메세지 쓰는창을 비운다.
				txtInput.requestFocus(); // 메세지를 보내고 커서를 다시 텍스트 필드로 위치시킨다

			}
		}
	}

	class ReadySendAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Send button을 누르거나 메시지 입력하고 Enter key 치면
			if (e.getSource() == ready) {
				ChatMsg obcm = new ChatMsg(UserName, "400", "ready", -10, -10);
				SendObject(obcm);
				//System.out.println(fd.getDirectory() + fd.getFile());
				
				
			}
		}
	}

	public void SendObject(Object ob) { // 서버로 메세지를 보내는 메소드
		try {
			oos.writeObject(ob);
		} catch (IOException e) {
			// textArea.append("메세지 송신 에러!!\n");
			AppendText("SendObject Error");
		}
	}

	public void SendMessage(String msg) {
		try {

			ChatMsg obcm = new ChatMsg(UserName, "200", msg, -10, -10);
			oos.writeObject(obcm);
		} catch (IOException e) {
			// AppendText("dos.write() error");
			AppendText("oos.writeObject() error");
			try {
//				dos.close();
//				dis.close();
				ois.close();
				oos.close();
				socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.exit(0);
			}
		}
	}
	

	public void AppendText(String msg) {
		// textArea.append(msg + "\n");
		// AppendIcon(icon1);
		msg = msg.trim(); // 앞뒤 blank와 \n을 제거한다.
		int len = textArea.getDocument().getLength();
		// 끝으로 이동
		textArea.setCaretPosition(len);
		textArea.replaceSelection(msg + "\n");
	}

}
