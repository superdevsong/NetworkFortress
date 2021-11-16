package fortress.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/*코드 전체적인 리펙토링 필요
 * 코드 분배라던가 image를 분리한다던가
 * 이중 버퍼링이라던가 변수 하나로 선언해서 묶는다던가
 * */
public class MyPanel extends JPanel {
	

	Image image = new ImageIcon("src/fortress/ui/tree.jpg").getImage();
	Image image_t = new ImageIcon("src/fortress/ui/tree_1.jpg").getImage();
	Vector<Player> playerList = new Vector<Player>();
	Player now_player;
	Player player2 = new Player();
	Player player3 = new Player();
	int turn = 0;
	private int field = 300 - image_t.getHeight(null),removeIndex;
	private int background_x = 0, field_x = 0;
	private Bullet bullet = new Bullet();
	

	public static void playSound(String PathName, boolean isLoop) {
		Clip sound;
		try {
			sound = AudioSystem.getClip();
			File audioFile = new File(PathName);
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
			sound.open(audioStream);
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

	public MyPanel() {
		playerList.add(player2);
		playerList.add(player3);
		now_player = playerList.get(turn++);
		for(Player player :playerList) {
			player.setPlayer_y(field);
			player.setPlayer_x((int)(Math.random()*500));
		}
		
		playSound("src/music/music.wav",true);
		new Thread(new Runnable() {

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
		}).start();
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				System.out.println(e.getKeyCode());
				if (e.getKeyCode() == 37) {
					// background가 이미지크기에 도달할때까지 옆으로 화면이 흐름
					//player_x -= 2;
					now_player.movePlayer_left();// 맵의끝에 갔을때
					if (background_x != 0 && now_player.getPlayer_x()==0) {
						background_x += 2;
						field_x += 2;
					}
					
				}
				if (e.getKeyCode() == 39) {
					now_player.movePlayer_right();// 오른쪽으로 감
					if (650 - image_t.getWidth(null) < background_x) {// background가 이미지크기에 도달할때까지 옆으로 화면이 흐름
						if (now_player.getPlayer_x() >= 650 - now_player.getImage_r().getWidth(null)) {// 맵의끝에 갔을때
							background_x -= 2;
							field_x -= 2;
						}
					}
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

			@Override
			public void keyReleased(KeyEvent e) {

				if (e.getKeyCode() == 32) {
					
					new Thread(new Runnable() {
						@Override
						public void run() {
							playSound("src/music/shooting.wav",false);
							bullet.setShot(true);
							bullet.setBullet_x(now_player.getPlayer_x());
							bullet.setBullet_y(now_player.getPlayer_y());
							new Thread(new Runnable() {
								

								@Override
								public void run() {
									boolean hit=false;
									while(true) {
											for(Player player :playerList) {
											
											if(bullet.getBullet_x()<=player.getPlayer_x()+player.getImage_l().getWidth(null)+10&&
													bullet.getBullet_x()>=player.getPlayer_x()-10&&
													bullet.getBullet_y()>=player.getPlayer_y()) {
												if(player==now_player)
													continue;
												player.setPlayer_hp(player.getPlayer_hp()-20);
												hit = true;
											}
											}
									try {
										Thread.sleep(10);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									if(!bullet.isShot()||hit)
										break;
									}
									
								}
								
							}).start();
							int jump_y;
							int set;
							int jump_x;
							if (!now_player.isDirection()) {// 왼쪽일때
								bullet.setVeloX(-1.0);
								jump_y = (int) (bullet.getPower() * Math.sin(Math.PI + bullet.getRadian()));
								set = (int) (bullet.getPower() * Math.sin(Math.PI + bullet.getRadian()));
								jump_x = (int) (bullet.getPower() * Math.cos(Math.PI + bullet.getRadian()));
							} else {// 오른쪽일때
								bullet.setVeloX(1.0);
								jump_y = (int) (bullet.getPower() * Math.sin(bullet.getRadian()));
								set = (int) (bullet.getPower() * Math.sin(bullet.getRadian()));
								jump_x = (int) (bullet.getPower() * Math.cos(bullet.getRadian()));
							}

							
							long t1 = bullet.getTime();
							long t2;

							System.out.println((bullet.getPower() * Math.sin(Math.PI + bullet.getRadian())) + " "
									+ (bullet.getPower() * Math.cos(Math.PI + bullet.getRadian())));
							while (jump_y > 0) {
								t2 = bullet.getTime() - t1;
								jump_y = set - (int) (t2 / 40);
								bullet.setBullet_x((int) (bullet.getBullet_x() + jump_x));
								bullet.setBullet_y((int) (bullet.getBullet_y() - jump_y));
								
								try {
									Thread.sleep(45);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							
							while (bullet.getBullet_y() < field - bullet.getImage_bullet().getHeight(null)) {
								t2 = bullet.getTime() - t1;
								jump_y = set - (int) (t2 / 40);
								bullet.setBullet_x((int) (bullet.getBullet_x() + jump_x));
								bullet.setBullet_y((int) (bullet.getBullet_y() - jump_y));
								
								try {
									Thread.sleep(45);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							
							if (bullet.getBullet_y() > field - bullet.getImage_bullet().getHeight(null)) {
								bullet.setBullet_y(field - bullet.getImage_bullet().getHeight(null));
							}
							bullet.setBullet_x(now_player.getPlayer_x());
							bullet.setBullet_y(now_player.getPlayer_y());
							bullet.setPower(0);
							bullet.setShot(false);
							for(Player player :playerList) {
								if(player.getPlayer_hp()==0)
									removeIndex=playerList.indexOf(player);
							}
							if(playerList.get(removeIndex).getPlayer_hp()==0)
							playerList.remove(removeIndex);
							if(turn>=playerList.size())
								turn=0;
							now_player = playerList.get(turn++);
						}
					}).start();
				}
			}
		});
	}

	@Override
	public void paintComponent(Graphics g) {
		try {
			BufferedImage bi = ImageIO.read(new File("src/fortress/ui/missile.jpg"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		g.setColor(Color.GREEN);
		g.drawImage(image, background_x, 0, image_t.getWidth(null), 500, null);
		g.drawImage(image_t, field_x, field, null);
		for(Player player :playerList) {
			if (player.isDirection())
				g.drawImage(player.getImage_r(), player.getPlayer_x(), player.getPlayer_y(), null);
			else
				g.drawImage(player.getImage_l(), player.getPlayer_x(), player.getPlayer_y(), null);
			g.fillRect(player.getPlayer_x(), player.getPlayer_y() - 20, (int) (player.getPlayer_hp() / 2), 10);
		}
		
		if (bullet.isShot())
			g.drawImage(bullet.getImage_bullet(), bullet.getBullet_x(), bullet.getBullet_y(), null);
		

	}

}
