package fortress.ui;

import java.awt.Image;

import javax.swing.ImageIcon;

public class DoubleAttack extends Item {
	
	Image skillIcon = new ImageIcon("src/fortress/ui/doubleitem.png").getImage();
	public DoubleAttack(int itemX) {
		super(itemX);
		itemNumber=0;
	}
	@Override
	public void perform(MyPanel mypanel) {
		Bullet bullet = mypanel.getBullet();
		double bulletPower = bullet.getPower();
		double bulletVeloY = bullet.getVeloY();
		Thread skill_shooting = new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (this) {
					mypanel.setShot(true);
					mypanel.checkHit();
					mypanel.player_attack();// 공격신호를 알리자
					bullet.setPower(bulletPower);
					bullet.setVeloY(bulletVeloY);
					mypanel.checkHit();
					mypanel.player_attack();
					mypanel.setShot(false);
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

					AttackObject obcm1 = new AttackObject("710", 0, 0);
					mypanel.SendObject(obcm1);
					bullet.setVeloY(1.0);
				}
			}

		}).start();
	}
	@Override
	public Image getImage() {
		return skillIcon;
	}
	
	@Override
	public int getItemX() {
		return itemX;
	}
	@Override
	public void setItemX(int itemX) {
		this.itemX = itemX;
	}
	@Override
	public int getItemNumber() {
		return itemNumber;
	}

}
