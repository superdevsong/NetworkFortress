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
	public static final int SCREEN_WIDTH = 960;
	public static final int SCREEN_HEIGHT = 640;
	Image image = new ImageIcon("src/fortress/ui/forest.jpg").getImage();// 배경
	Image image_tree = new ImageIcon("src/fortress/ui/tree_1.jpg").getImage();// 맵의 다리
	Image image_turn = new ImageIcon("src/fortress/ui/triangle.png").getImage();// nowplayer 화살표

	public final int SCREEN_EDGE = FortressUI.SCREEN_WIDTH - image_tree.getWidth(null);
	private Font f = new Font("Arial", Font.BOLD, 30);
	private Font hp = new Font("Arial", Font.BOLD, 12);
	private MyPanel myPanel = this;// 자가자신을 갖음
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private Socket socket; // 연결소켓

	Vector<Player> playerList = new Vector<Player>();
	Player my_player;
	Player now_player;
	Player new_player;
	int turn = 0, camera_x = 500;
	int camera_scale = 0;
	private int field = 300 - image_tree.getHeight(null);
	private int background_x = 0, field_x = 0;
	private Bullet bullet = new Bullet();

	private boolean shot = false;
	private boolean moving = false;
	private boolean attack = false;

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

	private void gameEnd() {

	}

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
					case "200": // chat message

						System.out.println("200" + cm.getData());

						break;
					case "500": // chat message
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
					case "501": // chat message
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
					case "502": // chat message
						System.out.println("게임시작했어!!!!");
						start = true;
						break;
					case "600": // chat message
						System.out.println("600 is your turn");
						next_turn(my_player);

						break;
					case "601": // 다음턴
						System.out.println("601 is other  turn");
						for (Player player : playerList) {
							if (player.getPlayer_num() == cm.getPlayer_num())
								next_turn(player);
						}
						break;
					case "700": // 다른 플레잉어의 이동신호 오른쪽
						System.out.println("700 is going = " + cm.getData());
						move_right();
						if (now_player.getPlayer_x() != cm.getPlayer_x())
							now_player.setPlayer_x(cm.getPlayer_x());// 만약 자신의 x와 다르면 두개를 맞춤
						break;
					case "701": // 다른 플레잉어의 이동신호 왼쪽
						System.out.println("701 is going = " + cm.getData());
						move_left();
						if (now_player.getPlayer_x() != cm.getPlayer_x())// 만약자신의 x와 다르면 두개를 맞춤
							now_player.setPlayer_x(cm.getPlayer_x());
						break;
					case "705": // 다른 플레잉어의 공격신호
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

									AttackObject obcm1 = new AttackObject("710", 0, 0);
									SendObject(obcm1);
									bullet.setVeloY(1.0);
								}
							}

						}).start();
						;

						break;
					case "706": // 다른 플레잉어의 공격신호 AttackObject를 사용한다.
						System.out.println("706 is skill attack = " + cm.getData());
						bullet.setPower(ao.getPower());
						bullet.setVeloY(ao.getVeloY());
						Thread skill_shooting = new Thread(new Runnable() {
							@Override
							public void run() {
								synchronized (this) {
									shot = true;
									checkHit();
									player_attack();// 공격신호를 알리자
									bullet.setPower(ao.getPower());
									bullet.setVeloY(ao.getVeloY());
									player_attack();
									shot = false;
									System.out.println("내려옴");
									notify();// notify 즉 접근 가능 신호 보냄

								}
							}

						});
						skill_shooting.setDaemon(true);
						skill_shooting.start();

						new Thread(new Runnable() {
							@Override
							public void run() {
								synchronized (skill_shooting) {
									try {
										skill_shooting.wait();
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

									ChatMsg obcm1 = new ChatMsg(now_player.getUser_name(), "710", "attack complete",
											-10, -10);
									SendObject(obcm1);
									bullet.setVeloY(1.0);
								}
							}

						}).start();
						;

						break;
					case "750":
						if(cm.getData().equals("double"))
							System.out.println("double attack!");
						else if(cm.getData().equals("strong"))
							System.out.println("strong attack!");
						else if(cm.getData().equals("shield"))
							System.out.println("shield!");
						break;
					case "760":
						if(cm.getData().equals("rightWind"))
							System.out.println("right Wind");
						else if(cm.getData().equals("leftWind"))
							System.out.println("left Wind");
						else if(cm.getData().equals("noWind"))
							System.out.println("no Wind");
						break;
					case "901":
						for (Player player : playerList) {
							if (player.getPlayer_num() == cm.getPlayer_num())
								player.setPlayer_hp(cm.getHp());
						}
						break;
					case "902":
						for (Player player : playerList) {
							if (player.getPlayer_num() == cm.getPlayer_num()) {
								playerList.remove(player);
								player.dead();
								break;
							}

						}
						break;
					case "1000":
						game_over = true;
						result = cm.getData();
						exit = new JButton("게임 종료");
						exit.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								ChatMsg msg = new ChatMsg(null, "201", "Bye", -10, -10);
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

	public MyPanel(ObjectInputStream new_ois, ObjectOutputStream new_oos, Socket new_socket) {// 연결소켓

		this.socket = new_socket;
		this.oos = new_oos;
		this.ois = new_ois;
		System.out.println(ois + "존재");
		System.out.println(oos + "존재");

		// SendMessage("/login " + UserName);
		ChatMsg obcm = new ChatMsg(null, "500", "gamejoin", -10, -10);
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
						ChatMsg obcm = new ChatMsg(now_player.getUser_name(), "701", "move left",
								now_player.getPlayer_x(), now_player.getPlayer_y());
						SendObject(obcm);// 이동 내용을 보냄
					}
					if (e.getKeyCode() == 39) {
						move_right();
						ChatMsg obcm = new ChatMsg(now_player.getUser_name(), "700", "move right",
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
					if (e.getKeyCode() == KeyEvent.VK_K) {
						if (bullet.getPower() < 30.0)
							bullet.setPower(bullet.getPower() + 1.0);

					}

				}
			}

			@Override
			public void keyReleased(KeyEvent e) {

				if (e.getKeyCode() == 32) {
					if (attack && start && !game_over) {

						AttackObject obcm = new AttackObject("705", bullet.getVeloY(), bullet.getPower());
						obcm.setPower(bullet.getPower());
						obcm.setVeloY(bullet.getVeloY());
						SendObject(obcm);

					}
				}
				if (e.getKeyCode() == KeyEvent.VK_K) {
					if (attack == true) {

						AttackObject obcm = new AttackObject("706", bullet.getVeloY(), bullet.getPower());
						obcm.setPower(bullet.getPower());
						obcm.setVeloY(bullet.getVeloY());
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
	}

	public void move_left() {// 왼쪽으로 움직임
		// background가 이미지크기에 도달할때까지 옆으로 화면이 흐름
		// player_x -= 2;

		if (background_x != 0) {// 맵의 중앙에서 배경이 흐를수 있을때
			now_player.moveNowPlayer_left(false);// 왼쪽으로 이동

			if (now_player.getPlayer_x() <= camera_x) {// 맵에 오른쪽에 있을수 있으므로 그럴때는 이렇게 camera의 위치를 고려하여 이동
				background_x += 2;
				field_x += 2;
				for (Player player : playerList) {
					if (player != now_player)
						player.movePlayer_right();
				}
			}
		} else if (background_x == 0)// 맵의 중앙에서 배경이 끝에 도달해 더이상 흐르지 못할때
			now_player.moveNowPlayer_left(true);// 왼쪽으로 이동

	}

	public void move_right() {// 오른쪽으로 움직임
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
			}
		} else if (SCREEN_EDGE >= background_x) {// 백그라운드 이미지가 더이상
			// 오른쪽으로 흐를 크기가
			// 없을때
			now_player.movePlayer_right(camera_x, true);// player가 화면끝까지 이동할수있도록이동
		}

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
					for (Player player : playerList) {
						if (player != now_player)
							player.movePlayer_left();
					}
				} else
					break;
			} else if (result == 2) {
				background_x += result;
				field_x += result;
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
				boolean hit = false;
				while (true) {
					for (Player player : playerList) {
						int player_x = player.getPlayer_x();
						int player_y = player.getPlayer_y();
						if (bullet.getBullet_x() < player_x + player.getImage_l().getWidth(null) + 10
								&& bullet.getBullet_x() >= player_x - 10 && bullet.getBullet_y() >= player_y) {
							if (player == now_player)
								continue;
							if (player == my_player) {// 모두가 보내면 요청이겹치므로
								ChatMsg obcm = new ChatMsg(player.getUser_name(), "900", "hit", -10, -10);
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

		g.drawImage(image, background_x, 0, image.getWidth(null), image.getHeight(null), null);
		g.drawImage(image_tree, field_x, field, null);
		if (start && !game_over) {

			for (Player player : playerList) {
				if (player.getUserStatus().equals("O")) {

					if (player.isDirection())
						g.drawImage(player.getImage_r(), player.getPlayer_x(), player.getPlayer_y(), null);
					else {
						g.drawImage(player.getImage_l(), player.getPlayer_x(), player.getPlayer_y(), null);
					}
					g.setColor(Color.GREEN);
					g.fillRect(player.getPlayer_x(), player.getPlayer_y() - 20, (int) (player.getPlayer_hp() / 2), 10);
					g.setColor(Color.BLUE);
					g.fillRect(player.getPlayer_x(), player.getPlayer_y() - 40, (int) (player.getMoveGauge() / 3), 10);
					g.setColor(Color.RED);
					g.setFont(hp);
					g.drawString("HP", player.getPlayer_x() - 15, player.getPlayer_y() - 10);
					g.drawString("Move", player.getPlayer_x() - 25, player.getPlayer_y() - 20);
					{
						if (player.getTeamStatus().equals("team1"))
							g.setColor(Color.BLUE);
						else if (player.getTeamStatus().equals("team2"))
							g.setColor(Color.PINK);
						g.setFont(f);
						g.drawString(player.getUser_name(), player.getPlayer_x() + 5, player.getPlayer_y() + 80);
					}

				}
			}
			g.drawImage(image_turn, now_player.getPlayer_x() + 12, now_player.getPlayer_y() - 100, 30, 30, null);
			g.setColor(Color.YELLOW);
			g.fillRect(now_player.getPlayer_x(), now_player.getPlayer_y() - 60, (int) (bullet.getPower()), 10);// 플레이어
																												// 가르키는
																												// 화살표
		}

		if (result != null) {
			g.setColor(Color.RED);
			g.setFont(new Font("궁서", Font.BOLD, 100));
			g.drawString("GAME OVER", 180, 100);
			g.setFont(new Font("궁서", Font.BOLD, 60));
			g.drawString(my_player.getTeamStatus() + " " + my_player.getUser_name() + " " + result, 240, 180);
		}

		if (bullet.isShot())
			g.drawImage(bullet.getImage_bullet(), bullet.getBullet_x(), bullet.getBullet_y(), null);

	}

}
