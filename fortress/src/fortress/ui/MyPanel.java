package fortress.ui;

import java.awt.Color;

import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

/*코드 전체적인 리펙토링 필요
 * 코드 분배라던가 image를 분리한다던가
 * 이중 버퍼링이라던가 변수 하나로 선언해서 묶는다던가
 * */
public class MyPanel extends JPanel {
	// 프로토콜 정리
	public static final int SCREEN_WIDTH = 960;
	public static final int SCREEN_HEIGHT = 640;
	public static final String CHATTING_PROTOCOL = "200";// 채팅
	public static final String EXIT_PROTOCOL = "201";// 게임을 나감
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
	public static final String POWERHIT_PROTOCOL = "907";// 플레이어를 POWERATTACK스킬로 맞춤
	public static final String HPMINUS_PROTOCOL = "901";// 플레이어 HP가 감소됨
	public static final String DEAD_PROTOCOL = "902";// 플레이어가 죽음
	public static final String GAMEOVER_PROTOCOL = "1000";// 게임이 끝남

	Image image = new ImageIcon("src/fortress/ui/forest.jpg").getImage();// 배경
	Image image_tree = new ImageIcon("src/fortress/ui/tree_1.jpg").getImage();// 맵의 다리
	Image image_turn = new ImageIcon("src/fortress/ui/triangle.png").getImage();// nowplayer 화살표
	Image double_shot = new ImageIcon("src/fortress/ui/doubleshoot.png").getImage();// 아이템 추가
	Image power_shot = new ImageIcon("src/fortress/ui/powershoot.png").getImage();// 아이템 추가
	Image heal = new ImageIcon("src/fortress/ui/heal.png").getImage();// 아이템 추가
	Image team1l = new ImageIcon("src/image/player/player1_attack.gif").getImage();
	Image team1r = new ImageIcon("src/image/player/player1r_attack.gif").getImage();
	Image team2l = new ImageIcon("src/image/player/player2_attack.gif").getImage();
	Image team2r = new ImageIcon("src/image/player/player2r_attack.gif").getImage();

	private Image img;
	private Graphics img_g;// 더블버퍼링 변수

	public final int SCREEN_EDGE = FortressUI.SCREEN_WIDTH - image_tree.getWidth(null);
	private Font f = new Font("Arial", Font.BOLD, 30);
	private Font hp = new Font("Arial", Font.BOLD, 12);
	private MyPanel myPanel = this;// 자가자신을 갖음
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private Socket socket; // 연결소켓

	Vector<Item> item_vc = new Vector();

	public int getSkillDouble() {
		return skillDouble;
	}

	public void setSkillDouble(int skillDouble) {
		this.skillDouble = skillDouble;
	}

	public int getSkillPower() {
		return skillPower;
	}

	public void setSkillPower(int skillPower) {
		this.skillPower = skillPower;
	}

	public int getSkillHeal() {
		return skillHeal;
	}

	public void setSkillHeal(int skillHeal) {
		this.skillHeal = skillHeal;
	}

	Vector<Player> playerList = new Vector<Player>();
	Player my_player;
	Player now_player;
	Player new_player;
	int skillDouble = 0;
	int skillPower = 0;
	int skillHeal = 0;
	boolean powerOn = false;// 파워샷여부 여부에따라 크기가달라짐

	public boolean isPowerOn() {
		return powerOn;
	}

	public void setPowerOn(boolean powerOn) {
		this.powerOn = powerOn;
	}

	int turn = 0, camera_x = 500;
	int camera_scale = 0;
	private int field = 300 - image_tree.getHeight(null);
	private int background_x = 0, field_x = 0;
	private Bullet bullet = new Bullet();

	private boolean shot = false;

	public Player getNow_player() {
		return now_player;
	}

	public boolean isShot() {
		return shot;
	}

	public void setShot(boolean shot) {
		this.shot = shot;
	}

	private boolean moving = false;
	private boolean attack = false;

	public Bullet getBullet() {
		return bullet;
	}

	public boolean isMoving() {
		return moving;
	}

	public void setMoving(boolean moving) {
		this.moving = moving;
	}

	public boolean isAttack() {
		return attack;
	}

	public void setAttack(boolean attack) {
		this.attack = attack;
	}

	private boolean start = false;
	private boolean game_over = false;
	String result = null;
	JButton exit;

	public static void playSound(String PathName, boolean isLoop) {
		Clip sound;
		try {
			sound = AudioSystem.getClip();
			File audioFile = new File(PathName);
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
			sound.open(audioStream);
			FloatControl gainControl = (FloatControl) sound.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(-10.0f);
			sound.start();
			if (isLoop)
				sound.loop(Clip.LOOP_CONTINUOUSLY);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void SendObject(Object ob) { // 서버로 메세지를 보내는 메소드
		try {
			oos.writeObject(ob);
		} catch (IOException e) {
			// textArea.append("메세지 송신 에러!!\n");
			System.out.println("SendObject Error");
		}
	}

	class ListenNetwork extends Thread {
		ChatMsg cm = null;
		AttackObject ao = null;

		public void run() {
			while (true) {
				try {

					Object obcm = null;
					String msg = null;
					String code = null;
					try {
						obcm = ois.readObject();
					} catch (ClassNotFoundException e) {

						e.printStackTrace();
						break;
					}
					if (obcm == null)
						break;
					if (obcm instanceof ChatMsg) {
						cm = (ChatMsg) obcm;
						msg = String.format("[%s] %s", cm.getId(), cm.getData());
						code = cm.getCode();
					} else if (obcm instanceof AttackObject) {
						ao = (AttackObject) obcm;
						code = ao.getCode();
					} else
						continue;
					switch (code) {
					case CHATTING_PROTOCOL: // 채팅

						System.out.println("200" + cm.getData());

						break;
					case MYPLAYERINIT_PROTOCOL: // 내 자신 초기화
						System.out.println("500" + cm.getData() + " player_num:" + cm.getPlayer_num());
						new_player = new Player(cm.getPlayer_num(), cm.getTeamStatus(), cm.getUserStatus(),
								cm.getData());
						new_player.setMyPanel(myPanel);
						if (playerList.size() == 0)
							now_player = new_player;
						playerList.add(new_player);
						new_player.init(field, cm.getPlayer_x(), cm.getHp());
						if (cm.getPlayer_x() > 500)
							new_player.setPlayer_preX(500);
						System.out.println("요로로로로!!!!" + cm.getPlayer_x());
						break;
					case OTHERPLAYERINIT_PROTOCOL: // 상대 초기화
						System.out.println("501 myplayer" + cm.getData() + " player_num:" + cm.getPlayer_num());
						my_player = new Player(cm.getPlayer_num(), cm.getTeamStatus(), cm.getUserStatus(),
								cm.getData());
						my_player.setMyPanel(myPanel);
						if (playerList.size() == 0)
							now_player = my_player;
						playerList.add(my_player);
						my_player.init(field, cm.getPlayer_x(), cm.getHp());
						if (cm.getPlayer_x() > 500)
							my_player.setPlayer_preX(500);
						System.out.println("요로로로로!!!!" + cm.getPlayer_x());
						break;
					case GAMESTART_PROTOCOL: // 게임 시작
						System.out.println("게임시작했어!!!!");
						start = true;
						break;
					case YOURTURN_PROTOCOL: // 내 턴
						System.out.println("600 is your turn");
						next_turn(my_player);
						mySkill();

						break;
					case OTHERTURN_PROTOCOL: // 다음턴
						System.out.println("601 is other  turn");
						for (Player player : playerList) {
							if (player.getPlayer_num() == cm.getPlayer_num())
								next_turn(player);
						}
						break;
					case PLAYERMOVERIGHT_PROTOCOL: // 다른 플레잉어의 이동신호 오른쪽
						System.out.println("700 is going = " + cm.getData());
						move_right();
						if (now_player.getPlayer_x() != cm.getPlayer_x())
							now_player.setPlayer_x(cm.getPlayer_x());// 만약 자신의 x와 다르면 두개를 맞춤
						break;
					case PLAYERMOVELEFT_PROTOCOL: // 다른 플레잉어의 이동신호 왼쪽
						System.out.println("701 is going = " + cm.getData());
						move_left();
						if (now_player.getPlayer_x() != cm.getPlayer_x())// 만약자신의 x와 다르면 두개를 맞춤
							now_player.setPlayer_x(cm.getPlayer_x());
						break;
					case PLAYERATTACK_PROTOCOL:// 다른 플레잉어의 공격신호 AttackObject를 사용한다.
						System.out.println("705 is attack = " + cm.getData());
						bullet.setPower(ao.getPower());
						bullet.setVeloY(ao.getVeloY());
						Thread shooting = new Thread(new Runnable() {
							@Override
							public void run() {
								synchronized (this) {
									shot = true;
									checkHit();
									player_attack();// 공격신호를 알리자
									shot = false;
									System.out.println("내려옴");
									notify();// notify 즉 접근 가능 신호 보냄

								}
							}

						});
						shooting.setDaemon(true);
						shooting.start();
						new Thread(new Runnable() {
							@Override
							public void run() {
								synchronized (shooting) {
									try {
										shooting.wait();
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

									AttackObject obcm1 = new AttackObject(ATTACK_COMPLETE, 0, 0);
									SendObject(obcm1);
									bullet.setVeloY(1.0);
								}
							}

						}).start();
						;

						break;
					case DOUBLEATTACK_PROTOCOL: // 다른 플레잉어의 스킬 공격신호 AttackObject를 사용한다.
						System.out.println("706 is skill attack = " + cm.getData());
						bullet.setPower(ao.getPower());
						bullet.setVeloY(ao.getVeloY());
						int index = 0;
						Vector<Item> myItemVc = now_player.getItems();
						for (Item item : myItemVc) {
							if (item.getItemNumber() == 0) {
								item.perform(myPanel);
								break;
							}
							index++;
						}
						if (index <= myItemVc.size())
							myItemVc.remove(index);
						// item쓰고 item 삭제

						break;
					case POWERATTACK_PROTOCOL: // 다른 플레잉어의 스킬 공격신호 AttackObject를 사용한다.
						System.out.println("707 is skill attack = " + cm.getData());
						bullet.setPower(ao.getPower());
						bullet.setVeloY(ao.getVeloY());

						int index1 = 0;
						Vector<Item> myItemVc1 = now_player.getItems();
						for (Item item : myItemVc1) {
							if (item.getItemNumber() == 1) {
								item.perform(myPanel);
								break;
							}
							index1++;
						}
						if (index1 <= myItemVc1.size())
							myItemVc1.remove(index1);
						// item쓰고 item 삭
						// item쓰고 item 삭제

						break;
					case CREATESKILL_PROTOCOL:// 스킬생성
						if (cm.getData().equals("double")) {
							System.out.println("double attack!");
							item_vc.add(new DoubleAttack(cm.getPlayer_x()));// 게임 아이템 배치에 추가
						} else if (cm.getData().equals("strong")) {
							System.out.println("strong attack!");
							item_vc.add(new PowerAttack(cm.getPlayer_x()));
						} else if (cm.getData().equals("heal")) {
							System.out.println("heal!");
							item_vc.add(new Heal(cm.getPlayer_x()));
						}

						break;

					case HPMINUS_PROTOCOL:
						for (Player player : playerList) {
							if (player.getPlayer_num() == cm.getPlayer_num())
								player.setPlayer_hp(cm.getHp());
						}
						break;
					case DEAD_PROTOCOL:
						for (Player player : playerList) {
							if (player.getPlayer_num() == cm.getPlayer_num()) {
								playerList.remove(player);
								player.dead();
								break;
							}

						}
						break;
					case HEALING_PROTOCOL:
						 int index2 = 0;
						Vector<Item> myItemVc2 = now_player.getItems();
						for (Item item : myItemVc2) {
							if (item.getItemNumber() == 2) {
								for (Player player : playerList) {// item perform
									if (player.getPlayer_num() == cm.getPlayer_num())
										player.setPlayer_hp(cm.getHp());
								}
								break;
							}
							index2++;
						}
						if (index2 <= myItemVc2.size())
							myItemVc2.remove(index2);
						// item쓰고 item 삭
						// item쓰고 item 삭제
						break;
					case GAMEOVER_PROTOCOL:
						game_over = true;
						result = cm.getData();
						exit = new JButton("게임 종료");
						exit.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								ChatMsg msg = new ChatMsg(null, EXIT_PROTOCOL, "Bye", -10, -10);
								SendObject(msg);
								System.exit(0);

							}

						});
						exit.setBounds(700, 460, 200, 60);
						add(exit);
						break;

					}
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("ois.readObject() error입니다.");
					/*
					 * try { ois.close(); oos.close(); socket.close(); System.exit(0); break; }
					 * catch (Exception ee) { break; }
					 */
				}
			}
		}
	}

	public void mySkill() {// 내턴일때 스킬계산 아이템 추가
		new Thread(new Runnable() {

			@Override
			public void run() {

				for (Item item : my_player.getItems()) {
					if (item.getItemNumber() == 0)
						skillDouble++;
					if (item.getItemNumber() == 1)
						skillPower++;
					if (item.getItemNumber() == 2)
						skillHeal++;
					/*
					 * int skillStrong = 0; int skillHill = 0;
					 */
				}

			}
		}).start();
	};

	public MyPanel(ObjectInputStream new_ois, ObjectOutputStream new_oos, Socket new_socket) {// 연결소켓

		this.socket = new_socket;
		this.oos = new_oos;
		this.ois = new_ois;
		System.out.println(ois + "존재");
		System.out.println(oos + "존재");

		// SendMessage("/login " + UserName);
		ChatMsg obcm = new ChatMsg(null, MYPLAYERINIT_PROTOCOL, "gamejoin", -10, -10);// 플레이어가 참여했다고 INIT요청을함
		// 로그인 사실을 알림와 동시에 초기 위치 알림
		SendObject(obcm);
		ListenNetwork net = new ListenNetwork();
		net.setDaemon(true);
		net.start();

		playSound("src/music/music.wav", true);

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (moving && start && !game_over) {

					System.out.println(e.getKeyCode());
					if (e.getKeyCode() == 37) {
						move_left();
						ChatMsg obcm = new ChatMsg(now_player.getUser_name(), PLAYERMOVELEFT_PROTOCOL, "move left",
								now_player.getPlayer_x(), now_player.getPlayer_y());
						SendObject(obcm);// 이동 내용을 보냄
					}
					if (e.getKeyCode() == 39) {
						move_right();
						ChatMsg obcm = new ChatMsg(now_player.getUser_name(), PLAYERMOVERIGHT_PROTOCOL, "move right",
								now_player.getPlayer_x(), now_player.getPlayer_y());
						SendObject(obcm);
					}
				}
				if (attack && start && !game_over) {
					if (e.getKeyCode() == 38) {// 포 각도 조절 위로 조절

						bullet.setVeloY(bullet.getVeloY() + 1.0);
					}
					if (e.getKeyCode() == 40) {// 포 각도 아래로 조절
						if (bullet.getVeloY() > 1.0)
							bullet.setVeloY(bullet.getVeloY() - 1.0);
					}
					if (e.getKeyCode() == 32) {
						if (bullet.getPower() < 30.0)
							bullet.setPower(bullet.getPower() + 1.0);

					}
					if (e.getKeyCode() == KeyEvent.VK_Q) {
						if (bullet.getPower() < 30.0 && skillDouble > 0)
							bullet.setPower(bullet.getPower() + 1.0);

					}
					if (e.getKeyCode() == KeyEvent.VK_W) {
						if (bullet.getPower() < 30.0 && skillPower > 0)
							bullet.setPower(bullet.getPower() + 1.0);

					}
					if (e.getKeyCode() == KeyEvent.VK_E) {
						if (skillHeal > 0) {
							skillHeal -= 1;
							AttackObject obcm = new AttackObject(HEALING_PROTOCOL, -10, -10);
							SendObject(obcm);
						}

					}

				}
			}

			@Override
			public void keyReleased(KeyEvent e) {

				if (e.getKeyCode() == 32) {
					if (attack && start && !game_over) {

						AttackObject obcm = new AttackObject(PLAYERATTACK_PROTOCOL, bullet.getVeloY(),
								bullet.getPower());
						SendObject(obcm);

					}
				}
				if (e.getKeyCode() == KeyEvent.VK_Q) {

					if (attack == true && skillDouble > 0) {
						skillDouble -= 1;
						AttackObject obcm = new AttackObject(DOUBLEATTACK_PROTOCOL, bullet.getVeloY(),
								bullet.getPower());
						SendObject(obcm);

					}
				}
				if (e.getKeyCode() == KeyEvent.VK_W) {

					if (attack == true && skillPower > 0) {
						skillPower -= 1;
						AttackObject obcm = new AttackObject(POWERATTACK_PROTOCOL, bullet.getVeloY(),
								bullet.getPower());
						SendObject(obcm);

					}
				}

			}
		});
		Thread nt = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					if (start)
						repaint();
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}

		});
		nt.setDaemon(true);// main종료할때 같이 종료
		nt.start();
		Thread itemCheck = new Thread(new Runnable() {// item을 먹었는지 확인

			@Override
			public void run() {
				boolean get = false;
				int index = 0;
				while (true) {
					if (start) {
						for (Item item : item_vc) {// 아이템추가
							for (Player player : playerList) {
								int player_x = player.getPlayer_x();
								if (player_x <= item.getItemX() + 20 && player.getPlayer_x()
										+ player.getImage_l().getWidth(null) >= item.getItemX()) {// 아이템의 x크기+20보다작고 x보다
																									// 커야됨
									player.newItem(item);
									System.out.println("돌아가뇽?");
									get = true;
									break;
								}

							}
							
							if (get) {
								index = item_vc.indexOf(item);
								break;
							}
							index++;
						}
						if (get) {

							get = false;
							item_vc.remove(index);
						}
						index = 0;
					}
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}

		});
		itemCheck.setDaemon(true);// main종료할때 같이 종료
		itemCheck.start();
	}

	public void move_left() {// 왼쪽으로 움직임
		// background가 이미지크기에 도달할때까지 옆으로 화면이 흐름
		// player_x -= 2;
		if (now_player.getMoveGauge() > 0) {
			if (background_x != 0) {// 맵의 중앙에서 배경이 흐를수 있을때
				now_player.moveNowPlayer_left(false);// 왼쪽으로 이동

				if (now_player.getPlayer_x() <= camera_x) {// 맵에 오른쪽에 있을수 있으므로 그럴때는 이렇게 camera의 위치를 고려하여 이동
					background_x += 2;
					field_x += 2;
					for (Player player : playerList) {
						if (player != now_player)
							player.movePlayer_right();
					}
					for (Item item : item_vc)
						item.setItemX(item.getItemX() + 2);
				}
			} else if (background_x == 0)// 맵의 중앙에서 배경이 끝에 도달해 더이상 흐르지 못할때
				now_player.moveNowPlayer_left(true);// 왼쪽으로 이동
		} else
			now_player.setDirection(false);

	}

	public void move_right() {// 오른쪽으로 움직임
		if (now_player.getMoveGauge() > 0) {
			if (SCREEN_EDGE < background_x) {// 백그라운드 이미지 크기만큼 이동이 가능하다
				// 만약에 백글아운드를 초과하면 else
				// if로 넘어가 중앙에서 끝까지 이동함
				now_player.movePlayer_right(camera_x, false);// 플레이어의 카메라영역을 끝으로 지정하고 이동
				if (now_player.getPlayer_x() >= camera_x - now_player.getImage_r().getWidth(null)) {// 맵의끝에
					// 갔을때
					background_x -= 2;
					field_x -= 2;
					for (Player player : playerList) {
						if (player != now_player)
							player.movePlayer_left();
					}
					for (Item item : item_vc)
						item.setItemX(item.getItemX() - 2);
				}
			} else if (SCREEN_EDGE >= background_x) {// 백그라운드 이미지가 더이상
				// 오른쪽으로 흐를 크기가
				// 없을때
				now_player.movePlayer_right(camera_x, true);// player가 화면끝까지 이동할수있도록이동
			}
		} else
			now_player.setDirection(true);

	}

	public void player_attack() {// 포탄쏘기
		attack = false;
		moving = false;
		playSound("src/music/shooting.wav", false);
		bullet.shotBullet(now_player, playerList, field);

	}

	public void next_turn(Player next_player) {// 포탄쏘기
		if (turn != 0)
			now_player.setPlayer_preX(now_player.getPlayer_x());
		skillDouble = 0;
		skillHeal = 0;
		skillPower = 0;
		now_player = next_player;
		int result;
		while (true) {
			result = now_player.getPlayerLocation();
			if (result == 0)
				break;
			else if (result == -2) {// 이전 플레이어 위치가 더높을때 즉 배경이 왼쪽으로 흘렀으므로 오른쪽으로 이동시키고 상대플레이어도
				// 오른쪽으로 이동시켜야됨
				if (SCREEN_EDGE < background_x) {
					background_x += result;
					field_x += result;
					for (Item item : item_vc) {
						item.setItemX(item.getItemX() + result);
						System.out.println("현재 위치는" + item.getItemX());
					}
					for (Player player : playerList) {
						if (player != now_player)
							player.movePlayer_left();
					}
				} else
					break;
			} else if (result == 2) {
				background_x += result;
				field_x += result;
				for (Item item : item_vc) {
					item.setItemX(item.getItemX() + result);
					System.out.println("현재 위치는" + item.getItemX());
				}
				for (Player player : playerList) {
					if (player != now_player)
						player.movePlayer_right();
				}
			} else
				break;
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

		now_player.setMoveGauge(130);// 이동 게이지 조정
		if (now_player == my_player) {
			moving = true;// 이동가능
			attack = true;// 공격가능
		}
		turn++;
	}

	void checkHit() {// bullet에 맞는지 확인
		new Thread(new Runnable() {
			@Override
			public void run() {
				ChatMsg obcm = null;
				boolean hit = false;
				int bulletWidth = 10;
				int bulletHeight = 0;
				while (true) {
					if (powerOn) {
						bulletWidth = 25;
						bulletHeight = 20;
					}
					for (Player player : playerList) {
						int player_x = player.getPlayer_x();
						int player_y = player.getPlayer_y();
						if (bullet.getBullet_x() < player_x + player.getImage_l().getWidth(null) + bulletWidth
								&& bullet.getBullet_x() >= player_x - bulletWidth
								&& bullet.getBullet_y() + bulletHeight >= player_y) {
							if (player == now_player)
								continue;
							if (player == my_player) {// 모두가 보내면 요청이겹치므로
								if (powerOn)
									obcm = new ChatMsg(player.getUser_name(), POWERHIT_PROTOCOL, "power hit", -10, -10);
								else
									obcm = new ChatMsg(player.getUser_name(), HIT_PROTOCOL, "hit", -10, -10);
								obcm.setPlayer_num(player.getPlayer_num());// 데미지입힘
								obcm.setTeamStatus(now_player.getTeamStatus());// 쏘는놈 팀정보
								SendObject(obcm);
							}
							hit = true;

						}
					}
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (!shot || hit) {
						if (hit)
							bullet.setShot(false);
						break;
					}
				}

			}

		}).start();
	}

	@Override
	public void paintComponent(Graphics g) {
		img = createImage(980, 640);
		img_g = img.getGraphics(); // Graphics
		paintComponents(img_g);

		img_g.drawImage(image, background_x, 0, image.getWidth(null), image.getHeight(null), null);
		img_g.drawImage(image_tree, field_x, field, null);
		if (start) {

			for (Player player : playerList) {
				if (player.getUserStatus().equals("O")) {

					if (player.getTeamStatus().equals("team2")) {
						if (player.isDirection()) {
							img_g.drawImage(team2r, player.getPlayer_x(), player.getPlayer_y(), null);
						} else {
							img_g.drawImage(team2l, player.getPlayer_x(), player.getPlayer_y(), null);
						}
					} else {
						if (player.isDirection()) {
							img_g.drawImage(team1r, player.getPlayer_x(), player.getPlayer_y(), null);
						} else {
							img_g.drawImage(team1l, player.getPlayer_x(), player.getPlayer_y(), null);
						}
					}
					img_g.setColor(Color.GREEN);
					img_g.fillRect(player.getPlayer_x(), player.getPlayer_y() - 20, (int) (player.getPlayer_hp() / 2),
							10);
					img_g.setColor(Color.BLUE);
					img_g.fillRect(player.getPlayer_x(), player.getPlayer_y() - 40, (int) (player.getMoveGauge() / 3),
							10);
					img_g.setColor(Color.RED);
					img_g.setFont(hp);
					img_g.drawString("HP", player.getPlayer_x() - 15, player.getPlayer_y() - 10);
					img_g.drawString("Move", player.getPlayer_x() - 30, player.getPlayer_y() - 30);
					img_g.drawString("Power", player.getPlayer_x() - 35, player.getPlayer_y() - 50);
					{
						if (player.getTeamStatus().equals("team1"))
							img_g.setColor(Color.BLUE);
						else if (player.getTeamStatus().equals("team2"))
							img_g.setColor(Color.PINK);
						img_g.setFont(f);
						img_g.drawString(player.getUser_name(), player.getPlayer_x() + 5, player.getPlayer_y() + 80);
					}

				}
			}
			img_g.drawImage(image_turn, now_player.getPlayer_x() + 12, now_player.getPlayer_y() - 100, 30, 30, null);
			img_g.setColor(Color.YELLOW);
			img_g.fillRect(now_player.getPlayer_x(), now_player.getPlayer_y() - 60, (int) (bullet.getPower() * 1.5),
					10);// 플레이어 가르키는 화살표

			for (Item item : item_vc) {
				img_g.drawImage(item.getImage(), item.getItemX(), field - 20, 20, 20, null);
			}
			if (now_player == my_player) {// item 추가
				if (skillDouble > 0)
					img_g.drawImage(double_shot, 0, 500, 160, 100, null);
				if (skillPower > 0)
					img_g.drawImage(power_shot, 170, 500, 160, 100, null);
				if (skillHeal > 0)
					img_g.drawImage(heal, 340, 500, 160, 100, null);
			}

		}

		if (result != null) {
			img_g.setColor(Color.RED);
			img_g.setFont(new Font("궁서", Font.BOLD, 100));
			img_g.drawString("GAME OVER", 180, 100);
			img_g.setFont(new Font("궁서", Font.BOLD, 60));
			img_g.drawString(my_player.getTeamStatus() + " you are " + result, 240, 180);
		}

		if (bullet.isShot()) {
			if (powerOn)
				img_g.drawImage(bullet.getImage_bullet(), bullet.getBullet_x(), bullet.getBullet_y(), 40, 40, null);
			else
				img_g.drawImage(bullet.getImage_bullet(), bullet.getBullet_x(), bullet.getBullet_y(), null);
		}
		g.drawImage(img, 0, 0, null);
	}

}
