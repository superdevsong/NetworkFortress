package fortress.ui;

import java.awt.Color;

import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
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
import javax.swing.JPanel;

/*코드 전체적인 리펙토링 필요
 * 코드 분배라던가 image를 분리한다던가
 * 이중 버퍼링이라던가 변수 하나로 선언해서 묶는다던가
 * */
public class MyPanel extends JPanel {

	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private Socket socket; // 연결소켓
	Image image = new ImageIcon("src/fortress/ui/forest.jpg").getImage();
	Image image_t = new ImageIcon("src/fortress/ui/tree_1.jpg").getImage();
	Vector<Player> playerList = new Vector<Player>();
	Player my_player;
	Player now_player;
	Player new_player;
	int turn = 0, camera_x = 500;
	int camera_scale = 0;
	private int field = 300 - image_t.getHeight(null);
	private int background_x = 0, field_x = 0;
	private Bullet bullet = new Bullet();
	boolean shot = false;
	boolean moving = false;
	boolean attack = false;
	String result = null;

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
		public void run() {
			while (true) {
				try {

					Object obcm = null;
					String msg = null;
					ChatMsg cm;
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
					} else
						continue;
					switch (cm.getCode()) {
					case "200": // chat message

						System.out.println("200" + cm.getData());

						break;
					case "500": // chat message
						System.out.println("500" + cm.getData() + " player_num:" + cm.getPlayer_num());
						new_player = new Player(cm.getPlayer_num(), cm.getTeamStatus(), cm.getUserStatus(),
								cm.getData());
						if (playerList.size() == 0)
							now_player = new_player;
						playerList.add(new_player);
						new_player.init(field, cm.getPlayer_x(), cm.getHp());
						break;
					case "501": // chat message
						System.out.println("501 myplayer" + cm.getData() + " player_num:" + cm.getPlayer_num());
						my_player = new Player(cm.getPlayer_num(), cm.getTeamStatus(), cm.getUserStatus(),
								cm.getData());
						if (playerList.size() == 0)
							now_player = my_player;
						playerList.add(my_player);
						my_player.init(field, cm.getPlayer_x(), cm.getHp());
						break;
					case "600": // chat message
						System.out.println("600 is your turn");
						next_turn(my_player);

						break;
					case "601": // 다음턴
						System.out.println("601 is next  turn");
						for (Player player : playerList) {
							if (player.getPlayer_num() == cm.getPlayer_num())
								next_turn(player);
						}
						break;
					case "700": // 다른 플레잉어의 이동신호 오른쪽
						System.out.println("700 is going = " + cm.getData());
						move_right();
						break;
					case "701": // 다른 플레잉어의 이동신호 왼쪽
						System.out.println("701 is going = " + cm.getData());
						move_left();
						break;
					case "705": // 다른 플레잉어의 공격신호
						System.out.println("705 is attack = " + cm.getData());
						bullet.setPower(cm.getPower());
						bullet.setVeloY(cm.getVeloY());
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

									ChatMsg obcm1 = new ChatMsg(now_player.getUser_name(), "710", "attack complete",
											-10, -10);
									SendObject(obcm1);
									bullet.setVeloY(1.0);
								}
							}

						}).start();
						;

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
								break;
							}

						}
						break;
					case "1000":
						System.out.println("왔어왔어 내가왔어");
						result = "Lose";
						break;
					case "1001":
						result = "Win";
						System.out.println("왔어왔어 내가왔어");
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
		net.start();

		playSound("src/music/music.wav", true);
		Thread nt = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
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

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (moving) {

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
				}

			}

			@Override
			public void keyReleased(KeyEvent e) {

				if (e.getKeyCode() == 32) {
					if (attack == true) {

						ChatMsg obcm = new ChatMsg(now_player.getUser_name(), "705", "attck!!",
								now_player.getPlayer_x(), now_player.getPlayer_y());
						obcm.setPower(bullet.getPower());
						obcm.setVeloY(bullet.getVeloY());
						SendObject(obcm);

					}
				}
			}
		});

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
		if (FortressUI.SCREEN_WIDTH - image_t.getWidth(null) < background_x) {// 백그라운드 이미지 크기만큼 이동이 가능하다
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
		} else if (FortressUI.SCREEN_WIDTH - image_t.getWidth(null) >= background_x) {// 백그라운드 이미지가 더이상
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
		now_player.setPlayer_preX(now_player.getPlayer_x());

		now_player = next_player;
		int result;
		while (true) {
			result = now_player.getPlayerLocation();
			if (result == 0)
				break;
			else if (result == -2) {// 이전 플레이어 위치가 더높을때 즉 배경이 왼쪽으로 흘렀으므로 오른쪽으로 이동시키고 상대플레이어도
									// 오른쪽으로 이동시켜야됨
				background_x += result;
				field_x += result;
				for (Player player : playerList) {
					if (player != now_player)
						player.movePlayer_left();
				}
			} else if (result == 2) {
				background_x += result;
				field_x += result;
				for (Player player : playerList) {
					if (player != now_player)
						player.movePlayer_right();
				}
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
		if (now_player == my_player) {
			moving = true;// 이동가능
			attack = true;// 공격가능
		}
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
		Font f = new Font("Arial", Font.BOLD, 30);
		try {
			BufferedImage bi = ImageIO.read(new File("src/fortress/ui/missile.jpg"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		g.drawImage(image, background_x, 0, image.getWidth(null), image.getHeight(null), null);
		g.drawImage(image_t, field_x, field, null);
		for (Player player : playerList) {
			

				if (player.isDirection())
					g.drawImage(player.getImage_r(), player.getPlayer_x(), player.getPlayer_y(), null);
				else {
					g.drawImage(player.getImage_l(), player.getPlayer_x(), player.getPlayer_y(), null);
				}
				g.setColor(Color.GREEN);
				g.fillRect(player.getPlayer_x(), player.getPlayer_y() - 20, (int) (player.getPlayer_hp() / 2), 10);
				g.fillRect(now_player.getPlayer_x(), now_player.getPlayer_y() - 40, (int) (bullet.getPower()), 10);
				{
					g.setColor(Color.BLUE);
					g.setFont(f);
					g.drawString(player.getUser_name(), player.getPlayer_x() + 5, player.getPlayer_y() + 80);
				}
			
		}
		
		if (result != null) {
			g.setColor(Color.RED);
			g.setFont(new Font("궁서", Font.BOLD, 100));
			g.drawString("GAME OVER", 180, 100);
			g.setFont(new Font("궁서", Font.BOLD, 60));
			g.drawString(my_player.getTeamStatus() + " "+ result, 320, 180);
		}

		if (bullet.isShot())
			g.drawImage(bullet.getImage_bullet(), bullet.getBullet_x(), bullet.getBullet_y(), null);
		

	}

}
