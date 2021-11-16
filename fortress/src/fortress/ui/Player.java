package fortress.ui;

import java.awt.Image;

import javax.swing.ImageIcon;

public class Player {
	boolean direction = true;
	Image image_r = new ImageIcon("src/fortress/ui/cannon.png").getImage();
	Image image_l = new ImageIcon("src/fortress/ui/cannon_reverse.png").getImage();
	int range_x,range_y;
	private int player_x, player_y;
	private int player_hp = 100;
	public void movePlayer_left() {
		if(player_x>0)
		player_x-=2;
		direction=false;
	}
	public int getRange_x() {
		return range_x;
	}
	public void setRange_x(int range_x) {
		this.range_x = range_x;
	}
	public int getRange_y() {
		return range_y;
	}
	public void setRange_y(int range_y) {
		this.range_y = range_y;
	}
	public void movePlayer_right() {
		if(player_x<650-image_r.getWidth(null))
		player_x+=2;
		direction=true;
	}
	public int getPlayer_hp() {
		return player_hp;
	}
	public void setPlayer_hp(int player_hp) {
		this.player_hp = player_hp;
	}
	public boolean isDirection() {
		return direction;
	}
	public void setDirection(boolean direction) {
		this.direction = direction;
	}
	public Image getImage_l() {
		return image_l;
	}
	
	
	public int getPlayer_x() {
		return player_x;
	}
	public void setPlayer_x(int player_x) {
		this.player_x = player_x;
	}
	public int getPlayer_y() {
		return player_y;
	}
	public Image getImage_r() {
		return image_r;
	}
	public void setPlayer_y(int field) {
		this.player_y = field - image_r.getHeight(null);
	}
	
	
}
