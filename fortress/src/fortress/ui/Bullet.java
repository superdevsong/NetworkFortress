package fortress.ui;

import java.awt.Image;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Vector;

import javax.swing.ImageIcon;

public class Bullet {
	Image image_bullet = new ImageIcon("src/fortress/ui/bullet.png").getImage();
	private int bullet_width ;
	private int bullet_height;
	private double power =0;
	private double veloX = 1.0;
	private double veloY = 1.0;
	private double radian;
	private int bullet_x;
	private int bullet_y;
	public Bullet() {
		bullet_width = image_bullet.getWidth(null);
		bullet_height = image_bullet.getHeight(null);
	}
	long getTime() {
		return Timestamp.valueOf(LocalDateTime.now()).getTime();
	}
	public int getBullet_x() {
		return bullet_x;
	}
	public void setBullet_x(int bullet_x) {
		this.bullet_x = bullet_x;
	}
	public int getBullet_y() {
		return bullet_y;
	}
	public void setBullet_y(int bullet_y) {
		this.bullet_y = bullet_y;
	}
	private boolean shot=false;
	
	public boolean isShot() {
		return shot;
	}
	public void setShot(boolean shot) {
		this.shot = shot;
	}
	public Image getImage_bullet() {
		return image_bullet;
	}
	public void setRadian() {
		radian = Math.atan(veloY/veloX);
	}
	
	public double getPower() {
		return power;
	}
	public void setPower(double power) {
		this.power = power;
	}
	public double getVeloX() {
		return veloX;
	}
	public void setVeloX(double veloX) {
		this.veloX = veloX;
	}
	public double getVeloY() {
		return veloY;
	}
	public void setVeloY(double veloY) {
		this.veloY = veloY;
	}
	public void shotBullet(Player now_player,Vector<Player> playerList,int field) {
		
				shot=true;
				int removeIndex = 0;
				int player_x = now_player.getPlayer_x();
				int player_y = now_player.getPlayer_y();
				bullet_x = player_x;//bullet이 나올곳을 플레이어의 위치로해둠
				bullet_y = player_y;
				checkHit(now_player,playerList);//플레이어가 총에 맞는지 확인
				int jump_y;
				int set;
				int jump_x;
				
				if (!now_player.isDirection()) {// 왼쪽일때
					System.out.println();
					veloX=-1.0;
					setRadian();
					jump_y = (int) (power * Math.sin(Math.PI + radian));//y 속도 
					set = (int) (power * Math.sin(Math.PI + radian));//기본 속도
					jump_x = (int) (power * Math.cos(Math.PI +radian));//x속도
				} else {// 오른쪽일때
					veloX = 1.0;
					setRadian();
					jump_y = (int) (power * Math.sin(radian));
					set = (int) (power * Math.sin(radian));
					jump_x = (int) (power * Math.cos(radian));
				}
				System.out.println("y: "+jump_y+" x:"+jump_x);
				
				long t1 = getTime();
				long t2;

				/*
				 * System.out.println((bullet.getPower() * Math.sin(Math.PI +
				 * bullet.getRadian())) + " " + (bullet.getPower() * Math.cos(Math.PI +
				 * bullet.getRadian())));
				 */
				while (jump_y > 0) {//속도가 중력가속도를 빼면서 +일떄 상승할때
					t2 = getTime() - t1;
					jump_y = set - (int) (t2 / 40);//원래속도에서 중력가속도를 빼서 감속시켜준다.
					bullet_x=bullet_x + jump_x;
					bullet_y= bullet_y - jump_y;
					
					try {
						Thread.sleep(45);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				while (bullet_y < field - bullet_height) {//속도가 중력가속도를 빼면서 -일떄 즉반대로 낙하할때
					t2 = getTime() - t1;
					jump_y = set - (int) (t2 / 40);//원래속도에서 중력가속도를 빼서 이제는 낙하속도가 점점올라감.
					bullet_x = bullet_x+jump_x;
					bullet_y = bullet_y-jump_y;
					
					
					try {
						Thread.sleep(45);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				if (bullet_y > field - bullet_height) {
					bullet_y = field - bullet_height;
				}
				bullet_x = player_x;
				bullet_y = player_y;
				power = 0;
				shot = false;
				for(Player player :playerList) {
					if(player.getPlayer_hp()==0)
						removeIndex=playerList.indexOf(player);
				}
				if(playerList.get(removeIndex).getPlayer_hp()==0)
				playerList.remove(removeIndex);
				
			}
	void checkHit(Player now_player,Vector<Player> playerList) {//bullet에 맞는지 확인
		new Thread(new Runnable() {
			

			@Override
			public void run() {
				boolean hit=false;
				while(true) {
						for(Player player :playerList) {
							int player_x = player.getPlayer_x();
							int player_y = player.getPlayer_y();
						if(bullet_x<player_x+player.getImage_l().getWidth(null)+10&&
								bullet_x>=player_x-10&&
										bullet_y>= player_y) {
							if(player==now_player)
								continue;
							player.setPlayer_hp(player.getPlayer_hp()-20);
								hit = true;
						}
						}
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(!shot||hit) {
					if(hit)
						shot=false;
					break;
				}
				}
				
			}
			
		}).start();
	}
	
}
