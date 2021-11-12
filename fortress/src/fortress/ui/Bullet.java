package fortress.ui;

import java.awt.Image;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.swing.ImageIcon;

public class Bullet {
	Image image_bullet = new ImageIcon("src/fortress/ui/bullet.png").getImage();
	private double power =0;
	private double veloX = 1.0;
	private double veloY = 1.0;
	private int bullet_x;
	private int bullet_y;
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
	public double getRadian() {
		return Math.atan(veloY/veloX);
	}
	public Bullet() {
		
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
	
}
