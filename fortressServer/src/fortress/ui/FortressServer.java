package fortress.ui;

//JavaObjServer.java ObjectStream 기반 채팅 Server

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Vector;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;

public class FortressServer extends JFrame {
	private static final long serialVersionUID = 1L;
	
	
	public static final String CHATTING_PROTOCOL = "200";// 채팅
	public static final String EXIT_PROTOCOL = "201";// 게임을 나감
	public static final String READY_PROTOCOL = "400";// 룸의 게임시작을 알림
	public static final String ROOMGAMESTART_PROTOCOL = "499";// 룸의 게임시작을 알림
	public static final String MYPLAYERINIT_PROTOCOL = "500";// 내 플레이어 초기화
	public static final String OTHERPLAYERINIT_PROTOCOL = "501";// 상대 플레이어 초기화
	public static final String GAMESTART_PROTOCOL = "502";// 게임시작 알림
	public static final String YOURTURN_PROTOCOL = "600";// 나의 턴을 알림
	public static final String OTHERTURN_PROTOCOL = "601";// 상대 턴을 알림
	public static final String PLAYERMOVERIGHT_PROTOCOL = "700";// 오른쪽으로 이동을 알림
	public static final String PLAYERMOVELEFT_PROTOCOL = "701";// 왼쪽으로 이동을 알림
	public static final String PLAYERATTACK_PROTOCOL = "705";// 플레이어가 공격을함
	public static final String DOUBLEATTACK_PROTOCOL = "706";// 플레이어가 두번공격 스킬로 공격을함
	public static final String POWERATTACK_PROTOCOL = "707";// 플레이어가 강한 공격 스킬로 공격을함
	public static final String HEALING_PROTOCOL = "708";// 체력회복 스킬 사용
	public static final String ATTACK_COMPLETE = "710";//// 공격이 성공함
	public static final String CREATESKILL_PROTOCOL = "750";// 스킬생성
	public static final String HIT_PROTOCOL = "900";// 플레이어 맞춤
	public static final String HPMINUS_PROTOCOL = "901";// 플레이어 HP가 감소됨
	public static final String DEAD_PROTOCOL = "902";// 플레이어가 죽음
	public static final String POWERHIT_PROTOCOL = "907";// 플레이어를 POWERATTACK스킬로 맞춤
	public static final String GAMEOVER_PROTOCOL = "1000";// 게임이 끝남
	
	
	
	private JPanel contentPane;
	JTextArea textArea;
	private JTextField txtPortNumber;

	private ServerSocket socket; // 서버소켓
	private Socket client_socket; // accept() 에서 생성된 client 소켓
	private Vector<UserService> UserVec = new Vector(); // 연결된 사용자를 저장할 벡터
	private Vector<UserService> UserTeam1 = new Vector(); // 연결된 사용자를 저장할 벡터
	private Vector<UserService> UserTeam2 = new Vector(); // 연결된 사용자를 저장할 벡터
	private Vector<String> rooms = new Vector<>(Arrays.asList("801", "802", "803", "804"));
	private static final int BUF_LEN = 128; // Windows 처럼 BUF_LEN 을 정의
	private int turn = 0, turns = 0;
	private int now_player_num = 1000;// 맨처음 들어오는 플레이어가 첫턴 플레이어임
	private GameService gameService = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FortressServer frame = new FortressServer();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public FortressServer() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 338, 440);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 300, 298);
		contentPane.add(scrollPane);

		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);

		JLabel lblNewLabel = new JLabel("Port Number");
		lblNewLabel.setBounds(13, 318, 87, 26);
		contentPane.add(lblNewLabel);

		txtPortNumber = new JTextField();
		txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
		txtPortNumber.setText("30000");
		txtPortNumber.setBounds(112, 318, 199, 26);
		contentPane.add(txtPortNumber);
		txtPortNumber.setColumns(10);

		JButton btnServerStart = new JButton("Server Start");
		btnServerStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					System.out.println(e);
					socket = new ServerSocket(Integer.parseInt(txtPortNumber.getText()));
				} catch (NumberFormatException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				AppendText("Chat Server Running..");
				btnServerStart.setText("Chat Server Running..");
				btnServerStart.setEnabled(false); // 서버를 더이상 실행시키지 못 하게 막는다
				txtPortNumber.setEnabled(false); // 더이상 포트번호 수정못 하게 막는다
				AcceptServer accept_server = new AcceptServer();
				accept_server.start();
			}
		});
		btnServerStart.setBounds(12, 356, 300, 35);
		contentPane.add(btnServerStart);
	}

	// 새로운 참가자 accept() 하고 user thread를 새로 생성한다.
	class AcceptServer extends Thread {
		@SuppressWarnings("unchecked")
		public void run() {

			int player_num = 1000;
			while (true) { // 사용자 접속을 계속해서 받기 위해 while문
				try {
					Integer a = 10;
					AppendText("" + a.SIZE);
					AppendText("Waiting new clients ...");
					client_socket = socket.accept(); // accept가 일어나기 전까지는 무한 대기중
					AppendText("새로운 참가자 from " + client_socket);
					// User 당 하나씩 Thread 생성
					UserService new_user = new UserService(client_socket, player_num++);
					UserVec.add(new_user); // 새로운 참가자 배열에 추가

					new_user.start(); // 만든 객체의 스레드 실행
					AppendText("현재 참가자 수 " + UserVec.size());
				} catch (IOException e) {
					AppendText("accept() error");
					// System.exit(0);
				}
			}
		}
	}

	public void AppendText(String str) {
		// textArea.append("사용자로부터 들어온 메세지 : " + str+"\n");
		textArea.append(str + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}

	public void AppendObject(ChatMsg msg) {
		// textArea.append("사용자로부터 들어온 object : " + str+"\n");
		textArea.append("code = " + msg.getCode() + "\n");
		textArea.append("id = " + msg.getId() + "\n");
		textArea.append("data = " + msg.getData() + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}

	class GameService {// 게임의 전반적인 시스템
		private Vector<UserService> user_vc;

		public GameService() {
			this.user_vc = UserVec;
			gameService = this;
			Thread checkStart = new Thread(new Runnable() {

				@Override
				public void run() {
					int i = 0;
					while (true) {
						for (i = 0; i < user_vc.size(); i++) {
							if (user_vc.get(i).game_ready == false)
								break;
						}
						if (i == user_vc.size()) {
							checkHP();
							checkGame();
							gameStart();
							break;
						}
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

				}
			});
			checkStart.setDaemon(true);
			checkStart.start();
		}

		public void checkHP() {// 플레이어들의 hp를 확인
			Thread checkHP = new Thread(new Runnable() {

				@Override
				public void run() {
					while (true) {
						for (UserService user : user_vc) {
							if (user.hp <= 0 && user.UserStatus == "O") {
								user.UserStatus = "D";
								ChatMsg obcm = new ChatMsg("SERVER", DEAD_PROTOCOL, "player die", -10, -10);// 플레이어가 죽으면
																											// 죽은 신호를
								// 보냄
								obcm.setPlayer_num(user.user_player_num);
								obcm.setUserStatus(user.UserStatus);// 플레이어가죽으면 죽었따는 표시
								user.WriteAllObject(obcm);
							}
						}
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
			checkHP.setDaemon(true);
			checkHP.start();

		}

		public void checkGame() {// 플레이어들의 hp를 확인
			Thread checkGame = new Thread(new Runnable() {

				@Override
				public void run() {

					int i = 0, t = 0;
					UserService Team1 = null;
					UserService Team2 = null;

					while (true) {
						for (i = 0; i < UserTeam1.size(); i++) {
							Team1 = UserTeam1.elementAt(i);

							if (Team1.UserStatus.equals("O")) {// 살아있다면 break;
								break;
							}
						}

						for (t = 0; t < UserTeam2.size(); t++) {
							Team2 = UserTeam2.elementAt(t);
							if (Team2.UserStatus.equals("O")) {// 살아있다면 break;
								break;
							}
						}
						if (i == UserTeam1.size() && t == UserTeam2.size()) {
							ChatMsg obcm = new ChatMsg("SERVER", GAMEOVER_PROTOCOL, "Draw", -10, -10);// 패배
							Team1.WriteAllObject(obcm);
							break;
						} else if (i == UserTeam1.size() && t < UserTeam2.size()) {
							ChatMsg obcm = new ChatMsg("SERVER", GAMEOVER_PROTOCOL, "Lose", -10, -10);// 패배
							for (UserService user : UserTeam1) {
								user.WriteOneObject(obcm);
							}
							obcm = new ChatMsg("SERVER", GAMEOVER_PROTOCOL, "Win", -10, -10);
							for (UserService user : UserTeam2) {
								user.WriteOneObject(obcm);
							}
							break;
						} else if (t == UserTeam2.size() && i < UserTeam1.size()) {
							ChatMsg obcm = new ChatMsg("SERVER", GAMEOVER_PROTOCOL, "Lose", -10, -10);// 패배
							for (UserService user : UserTeam2) {
								user.WriteOneObject(obcm);
							}
							obcm = new ChatMsg("SERVER", GAMEOVER_PROTOCOL, "Win", -10, -10);
							for (UserService user : UserTeam1) {
								user.WriteOneObject(obcm);
							}
							break;
						}

						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
			checkGame.setDaemon(true);
			checkGame.start();

		}

		public void gameStart() {// 게임시작을 알림
			ChatMsg obcm = new ChatMsg("SERVER", GAMESTART_PROTOCOL, "Start", -10, -10);// 게임 시작하라고 알림

			now_player_num = user_vc.get(0).user_player_num;// 첫번째 놈을 항상 먼저 보냄

			for (UserService user : user_vc) {
				user.WriteListMe();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (UserService user : user_vc) {
				user.WriteOneObject(obcm);
			}

			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (UserService user : user_vc) {
				user.isTurn(now_player_num);

			}

		}

		public void isAttack() {// 공격이 다완료되었는지 확인하고 다완료되면 다음턴으로 넘김 게임서비스쪽으로 옮길 필요가있어보임
			new Thread(new Runnable() {

				public void run() {
					int i;
					while (true) {
						for (i = 0; i < user_vc.size(); i++) {
							UserService user = (UserService) user_vc.elementAt(i);
							if (user.attack == false)
								break;
						}
						if (i == user_vc.size()) {
							for (UserService user : user_vc)
								user.attack = false;
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							while (true) {
								System.out.println("여기옴?");
								turn++;
								if (user_vc.size() <= turn)// 살아있는 user의 size를 검사
									turn = 0;
								UserService user = user_vc.get(turn);
								if (user.UserStatus.equals("O")) {// 플레이어가 살아있을때만
									now_player_num = user.user_player_num;
									user.Turns();
									System.out.println("실행종료");
									break;
								} else if (user.UserStatus.equals("D"))// 플레이어 죽어있으면 안됨
									continue;

							}
							break;
						}
					}
					itemCreation();

				}
			}).start();

		}

		public void itemCreation() {// item을 확률적으로 생성
			int itemNumber = (int) (Math.random() * 100);
			int itemX = (int) (Math.random() * 900);
			System.out.println("몇이냐" + itemNumber);
			if (itemNumber >= 0 && itemNumber < 20) {
				ChatMsg obcm = new ChatMsg("SERVER", CREATESKILL_PROTOCOL, "double", itemX, -10);// 더블샷
				for (UserService user : user_vc) {
					user.WriteOneObject(obcm);
					;
				}
			} else if (itemNumber >= 20 && itemNumber < 40) {
				ChatMsg obcm = new ChatMsg("SERVER", CREATESKILL_PROTOCOL, "strong", itemX, -10);// 좀강한샷
				for (UserService user : user_vc) {
					user.WriteOneObject(obcm);
					;
				}
			} else if (itemNumber >= 40 && itemNumber < 60) {
				ChatMsg obcm = new ChatMsg("SERVER", CREATESKILL_PROTOCOL, "heal", itemX, -10);// 힐
				for (UserService user : user_vc) {
					user.WriteOneObject(obcm);

				}
			}
		}

	}

	// User 당 생성되는 Thread
	// Read One 에서 대기 -> Write All
	class UserService extends Thread {
		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;

		private ObjectInputStream ois;
		private ObjectOutputStream oos;

		private Socket client_socket;
		private Vector<UserService> user_vc;
		public String UserName = "";
		public String UserStatus;
		public String TeamStatus;
		private String RoomNumber = null;
		private int player_x, player_y;
		private int user_player_num;
		private boolean ready = false;
		private boolean attack = false;
		private boolean game_ready = false;
		private int hp = 100;// 플레이어 hp

		public UserService(Socket client_socket, int player_num) {
			// TODO Auto-generated constructor stub
			// 매개변수로 넘어온 자료 저장
			user_player_num = player_num;
			this.client_socket = client_socket;
			this.user_vc = UserVec;
			try {
//				

				oos = new ObjectOutputStream(client_socket.getOutputStream());
				oos.flush();
				ois = new ObjectInputStream(client_socket.getInputStream());

			} catch (Exception e) {
				AppendText("userService error");
			}

		}

		public void GameJoin() {
			AppendText("새로운 참가자 " + UserName + " 입장.");
			/*
			 * WriteOne("Welcome to Java chat server\n"); WriteOne(UserName + "님 환영합니다.\n");
			 * // 연결된 사용자에게 정상접속을 알림
			 */

			String msg = "[" + UserName + "]님이 입장 하였습니다.\n";
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				if (user != this && user.UserStatus == "O") {
					ChatMsg obcm = new ChatMsg("SERVER", CHATTING_PROTOCOL, msg, -10, -10);

					user.WriteOneObject(obcm);
				}

			} // 아직 user_vc에 새로 입장한 user는 포함되지 않았다.
		}

		public void RoomJoin() {
			AppendText("새로운 참가자 " + UserName + "대기방 입장.");
			WriteOne("Welcome to Java Fortress\n");
			WriteOne(UserName + "님 환영합니다.\n");
			// 연결된 사용자에게 정상접속을 알림
			String msg = "[" + UserName + "]님이 입장 하였습니다.\n";
			WriteOthers(msg); // 아직 user_vc에 새로 입장한 user는 포함되지 않았다.
			for (UserService user : user_vc) {
				ChatMsg obcm = null;
				if (user.RoomNumber != null) {
					if (user.RoomNumber.equals("801")) {
						obcm = new ChatMsg(user.UserName, "801", null, -10, -10);
					} else if (user.RoomNumber.equals("802")) {
						obcm = new ChatMsg(user.UserName, "802", null, -10, -10);
					} else if (user.RoomNumber.equals("803")) {
						obcm = new ChatMsg(user.UserName, "803", null, -10, -10);
					} else if (user.RoomNumber.equals("804")) {
						obcm = new ChatMsg(user.UserName, "804", null, -10, -10);
					}
					if (obcm != null)
						WriteOneObject(obcm);
				}
			}

		}

		public void Logout() {
			String msg = "[" + UserName + "]님이 퇴장 하였습니다.\n";
			UserVec.removeElement(this); // Logout한 현재 객체를 벡터에서 지운다
			WriteAll(msg); // 나를 제외한 다른 User들에게 전송
			AppendText("사용자 " + "[" + UserName + "] 퇴장. 현재 참가자 수 " + UserVec.size());
		}

		// 모든 User들에게 방송. 각각의 UserService Thread의 WriteONe() 을 호출한다.
		public void WriteAll(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				if (user.UserStatus == "O")
					user.WriteOne(str);
			}
		}

		// 모든 User들에게 Object를 방송. 채팅 message와 image object를 보낼 수 있다
		public void WriteAllObject(Object ob) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				user.WriteOneObject(ob);
			}
		}

		public void WriteAllGameStart(Object ob) {// 전체한테 게임시작 메시지 보내는김에
			for (int i = 0; i < user_vc.size(); i++) {
				try {

					sleep(10);// 이렇게 처리안하면R 왠지 모르게 ois 오류가 뜸
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				UserService user = (UserService) user_vc.elementAt(i);
				if (user.UserStatus == "O")
					user.WriteOneObject(ob);
			}
		}

		// 모든 User들에게 Object를 방송. 채팅 message와 image object를 보낼 수 있다
		public void WriteOthersObject(Object ob) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				if (user != this )
					user.WriteOneObject(ob);
			}
		}

		// 나를 제외한 User들에게 방송. 각각의 UserService Thread의 WriteONe() 을 호출한다.
		public void WriteOthers(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				if (user != this && user.UserStatus == "O")
					user.WriteOne(str);
			}
		}

		// User들의 목록을 방송. 게임시작할때 인원을 받아서 화면에 처리하기위함
		public void WriteListMe() {

			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				if (user != this && user.UserStatus == "O") {// 다른플레이어일때
					ChatMsg obcm = new ChatMsg("user", MYPLAYERINIT_PROTOCOL, user.UserName, user.player_x,
							user.player_y);
					obcm.setPlayer_num(user.user_player_num);// player_num를 통해 클라이언트에서 player를 찾을것이므로 정보가 필요함
					obcm.setHp(user.hp);// 플레이어 hp 설정
					obcm.setTeamStatus(user.TeamStatus);
					obcm.setUserStatus(UserStatus);
					WriteOneObject(obcm);
				} else if (user == this) {// 자신이 맞으면 myplayer넣어야 되므로 이렇게 보냄
					ChatMsg obcm = new ChatMsg("SERVER", OTHERPLAYERINIT_PROTOCOL, user.UserName, player_x, player_y);
					obcm.setPlayer_num(user_player_num);// player_num를 통해 클라이언트에서 player를 찾을것이므로 정보가 필요함
					obcm.setHp(user.hp);// 플레이어 hp 설정
					obcm.setTeamStatus(user.TeamStatus);
					obcm.setUserStatus(UserStatus);
					WriteOneObject(obcm);
				}
			}
		}

		public void ReadyCheck() {// 준비가 되었는지 확인 다되어있으면 시작
			ready = true;
			if (user_vc.size() == 1)
				return;
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				if (user != this && user.UserStatus == "O") {
					if (user.ready == false)
						return;
				}
			}
			GameStart();

		}

		public void GameStart() {
			gameService = new GameService();
			ChatMsg obcm = new ChatMsg("SERVER",ROOMGAMESTART_PROTOCOL, "GameStart" + " : ", -10, -10);

			WriteAllGameStart(obcm);

		}

	
		

		// UserService Thread가 담당하는 Client 에게 1:1 전송
		public void WriteOne(String msg) {
			try {
				
				ChatMsg obcm = new ChatMsg("SERVER", CHATTING_PROTOCOL, msg, player_x, player_y);
				oos.writeObject(obcm);
			} catch (IOException e) {
				AppendText("dos.writeObject() error");
				try {
//					dos.close();
//					dis.close();
					ois.close();
					oos.close();
					client_socket.close();
					client_socket = null;
					ois = null;
					oos = null;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Logout(); // 에러가난 현재 객체를 벡터에서 지운다
			}
		}

		

		public void WriteOneObject(Object ob) {
			try {
				oos.writeObject(ob);
			} catch (IOException e) {
				AppendText("oos.writeObject(ob) error");
				try {
					ois.close();
					oos.close();
					client_socket.close();
					client_socket = null;
					ois = null;
					oos = null;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Logout();
			}
		}

		public void isTurn(int turn) {// 자신의 턴이 맞으면 자신에게 자신의턴이라고 아니면 다른사람 턴이라고 알림
			
			if (turn == user_player_num) {
				ChatMsg obcm = new ChatMsg("SERVER", YOURTURN_PROTOCOL, "youtr turn", player_x, player_y);
				obcm.setPlayer_num(turn);
				WriteOneObject(obcm);
			} else if (turn != user_player_num) {
				ChatMsg obcm = new ChatMsg("SERVER", OTHERTURN_PROTOCOL, "other turn", player_x, player_y);
				obcm.setPlayer_num(turn);// 플레이어가 누구의 턴인지 확인할수있게 turn정보를 보냄
				WriteOneObject(obcm);
			}

		}

		public void Turns() {// 모두 자신의 턴이 맞는지 확인
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				user.isTurn(now_player_num);
			}
		}

		public void teamInit() {
			if (TeamStatus.equals("team1")) {
				UserTeam1.add(this);
				player_x = (int) (Math.random() * (960 - 760)) + 760;// 플레이어 위치 지정
				System.out.println("누가먼저왔나1 " + player_x);
			}
			if (TeamStatus.equals("team2")) {
				UserTeam2.add(this);
				player_x = (int) (Math.random() * 200);
				System.out.println("누가먼저왔나2 " + player_x);
			}
		}

		public void TeamChange1() {// 정리필요;

			for (UserService user : user_vc) {
				if (user.RoomNumber != null) {
					if (user.RoomNumber.equals("801")) {
						String msg = "이미 자리가 있습니다.\n";
						WriteOne(msg);
						return;
					}
				}
			}
			if (RoomNumber != null) {
				ChatMsg obcm = new ChatMsg(null, RoomNumber, "TeamChange4", -10, -10);
				WriteAllObject(obcm);
			}
			RoomNumber = "801";
			String msg = "[" + UserName + "]님은 Team1입니다.\n";
			ChatMsg obcm = new ChatMsg(UserName, "801", "TeamChange1", -10, -10);
			TeamStatus = "team1";
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
			}
			WriteAll(msg);
			WriteAllObject(obcm);

		}

		public void TeamChange2() {
			for (UserService user : user_vc) {
				if (user.RoomNumber != null) {
					if (user.RoomNumber.equals("802")) {
						String msg = "이미 자리가 있습니다.\n";
						WriteOne(msg);
						return;
					}
				}
			}
			if (RoomNumber != null) {
				ChatMsg obcm = new ChatMsg(null, RoomNumber, "TeamChange4", -10, -10);
				WriteAllObject(obcm);
			}
			RoomNumber = "802";
			String msg = "[" + UserName + "]님은 Team1입니다.\n";
			ChatMsg obcm = new ChatMsg(UserName, "802", "TeamChange2", -10, -10);
			TeamStatus = "team1";
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
			}
			WriteAll(msg);
			WriteAllObject(obcm);
		}

		public void TeamChange3() {
			for (UserService user : user_vc) {
				if (user.RoomNumber != null) {
					if (user.RoomNumber.equals("803")) {
						String msg = "이미 자리가 있습니다.\n";
						WriteOne(msg);
						return;
					}
				}
			}
			if (RoomNumber != null) {
				ChatMsg obcm = new ChatMsg(null, RoomNumber, "TeamChange4", -10, -10);
				WriteAllObject(obcm);
			}
			RoomNumber = "803";
			String msg = "[" + UserName + "]님은 Team2입니다.\n";
			ChatMsg obcm = new ChatMsg(UserName, "803", "TeamChange3", -10, -10);
			TeamStatus = "team2";
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
			}
			WriteAll(msg);
			WriteAllObject(obcm);
		}

		public void TeamChange4() {
			for (UserService user : user_vc) {
				if (user.RoomNumber != null) {
					if (user.RoomNumber.equals("804")) {
						String msg = "이미 자리가 있습니다.\n";
						WriteOne(msg);
						return;
					}
				}
			}
			if (RoomNumber != null) {
				ChatMsg obcm = new ChatMsg(null, RoomNumber, "TeamChange4", -10, -10);
				WriteAllObject(obcm);
			}
			RoomNumber = "804";
			String msg = "[" + UserName + "]님은 Team2입니다.\n";
			ChatMsg obcm = new ChatMsg(UserName, "804", "TeamChange4", -10, -10);
			TeamStatus = "team2";
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
			}
			WriteAll(msg);
			WriteAllObject(obcm);
		}

		public void playerHit(int player_num, String TeamStatus, int hp) {// 플레이어가 맞으면 모두에게 이사실을 전함
			for (UserService user : user_vc) {

				if (user.user_player_num == player_num && !user.TeamStatus.equals(TeamStatus)) {
					System.out.println(user.TeamStatus + "fda");
					System.out.println(TeamStatus + "vz");
					user.hp -= hp;
					ChatMsg obcm = new ChatMsg("SERVER", HPMINUS_PROTOCOL, "hit player_num : " + user.user_player_num,
							-10, -10);
					obcm.setHp(user.hp);
					obcm.setPlayer_num(user.user_player_num);
					WriteAllObject(obcm);
					break;
				}

			}

		}

		public void playerHeal() {// 플레이어가 맞으면 모두에게 이사실을 전함\
			hp += 20;
			ChatMsg obcm = new ChatMsg("SERVER", HEALING_PROTOCOL, "hit player_num : " + user_player_num, -10, -10);
			obcm.setHp(hp);
			obcm.setPlayer_num(user_player_num);
			WriteAllObject(obcm);

		}

		public void run() {
			while (true) { // 사용자 접속을 계속해서 받기 위해 while문
				try {

					Object obcm = null;
					String msg = null;
					ChatMsg cm = null;
					AttackObject ao = null;
					String code = null;
					if (socket == null)
						break;
					try {
						obcm = ois.readObject();

					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
					if (obcm == null)
						break;
					if (obcm instanceof ChatMsg) {
						cm = (ChatMsg) obcm;
						AppendObject(cm);
						code = cm.getCode();
					} else if (obcm instanceof AttackObject) {
						ao = (AttackObject) obcm;
						code = ao.getCode();
					} else
						continue;
					if (code.matches("100")) {
						UserName = cm.getId();
						UserStatus = "O"; // Online 상태
						RoomJoin();
					} else if (code.matches(MYPLAYERINIT_PROTOCOL)) {// 플레이어 게임 입장처리
						teamInit();
						GameJoin();

						System.out.println("이제 여기왔어요");
						game_ready = true;
					} else if (code.matches(CHATTING_PROTOCOL)) {
						msg = String.format("[%s] %s", cm.getId(), cm.getData());
						AppendText(msg); // server 화면에 출력
						String[] args = msg.split(" "); // 단어들을 분리한다.
						// 일반 채팅 메시지
						UserStatus = "O";
						// WriteAll(msg + "\n"); // Write All
						WriteAllObject(cm);

					} else if (code.matches(CHATTING_PROTOCOL)) { // logout message 처리
						Logout();
						break;
					} else if (code.matches("300")) {

					} else if (code.matches(READY_PROTOCOL)) {// 준비
						ReadyCheck();
					} else if (code.matches(OTHERTURN_PROTOCOL)) {

					} else if (code.matches(PLAYERMOVERIGHT_PROTOCOL)) {// 플레이어 오른쪽이동
						player_x = cm.getPlayer_x();
						player_y = cm.getPlayer_y();
						cm.setPlayer_num(user_player_num);
						WriteOthersObject(cm);
						AppendText(cm.getData());
					} else if (code.matches(PLAYERMOVELEFT_PROTOCOL)) {// 플레이어 왼쪽이동
						player_x = cm.getPlayer_x();
						player_y = cm.getPlayer_y();
						cm.setPlayer_num(user_player_num);
						WriteOthersObject(cm);
						AppendText(cm.getData());
					} else if (code.matches(PLAYERATTACK_PROTOCOL)) {// attack object
						turns++;// 공격하면 종합턴이 올라감
						WriteAllObject(ao);
						gameService.isAttack();
						AppendText(String.format("[%f] %f 입니다.", ao.getVeloY(), ao.getPower()));
					} else if (code.matches(DOUBLEATTACK_PROTOCOL)) {// attack object
						turns++;// 공격하면 종합턴이 올라감
						WriteAllObject(ao);
						gameService.isAttack();
						AppendText(String.format("[%f] %f 입니다.", ao.getVeloY(), ao.getPower()));
					} else if (code.matches(POWERATTACK_PROTOCOL)) {// attack object
						turns++;// 공격하면 종합턴이 올라감
						WriteAllObject(ao);
						gameService.isAttack();
						AppendText(String.format("[%f] %f 입니다.", ao.getVeloY(), ao.getPower()));

					} else if (code.matches(HEALING_PROTOCOL)) {// attack object
						playerHeal();

					} else if (code.matches(ATTACK_COMPLETE)) {// attack object
						attack = true;
						AppendText(String.format("[%f] %f 입니다.", ao.getVeloY(), ao.getPower()));
					} else if (code.matches("801")) {// 수정필요
						TeamChange1();
					} else if (code.matches("802")) {
						TeamChange2();
					} else if (code.matches("803")) {
						TeamChange3();
					} else if (code.matches("804")) {
						TeamChange4();
					} else if (code.matches(HIT_PROTOCOL)) {// 수정필요
						playerHit(cm.getPlayer_num(), cm.getTeamStatus(), 20);
					} else if (code.matches(POWERHIT_PROTOCOL)) {// 수정필요
						playerHit(cm.getPlayer_num(), cm.getTeamStatus(), 30);
					}
				} catch (IOException e) {
					AppendText("ois.readObject() error");
					try {
//					
						ois.close();
						oos.close();
						client_socket.close();
						Logout(); // 에러가난 현재 객체를 벡터에서 지운다
						break;
					} catch (Exception ee) {
						break;
					} // catch문 끝
				} // 바깥 catch문끝
			} // while
		} // run
	}

}
