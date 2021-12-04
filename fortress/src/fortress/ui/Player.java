package fortress.ui;

import java.awt.Image;

import javax.swing.ImageIcon;

public class Player {
	
	boolean direction = true;
	
	Image image_r = new ImageIcon("src/image/player/player2r_attack.gif").getImage();
	Image image_l = new ImageIcon("src/image/player/player2_attack.gif").getImage();
	int range_x,range_y;
	private int player_x, player_y;
	private int player_preX;
	private String user_name;
	private int player_num;
	private int player_hp = 100;
	public Player(int player_num) {
		this.player_num = player_num;
	}
	
	public String getUser_name() {
		return user_name;
	}
	public int getPlayer_num() {
		return player_num;
	}

	public void setPlayer_num(int player_num) {
		this.player_num = player_num;
	}

	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}
	public void setPlayer_preX(int player_preX) {
		this.player_preX = player_preX;
	}
	public int getPlayer_preX() {
		return player_preX;
	}
	public void movePlayer_left() {
		player_x-=2;
	}
	public void movePlayer_right() {
		player_x+=2;
	}
	public void moveNowPlayer_left(boolean edge) {
		direction=false;
		if(player_x>=500)
		player_x-=2;
		if(edge && player_x>=0)
			player_x-=2;
	}
	public void init(int field, int x) {
		player_y=field-image_r.getHeight(null);;
		player_x =x; /*일단 0으로 주어짐 (int)(Math.random()*500); */
		player_preX = player_x;
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
	public void movePlayer_right(int camera_x,boolean edge) {
		direction=true;
		if(player_x< camera_x-image_r.getWidth(null))
		player_x+=2;
		if(edge&&player_x< FortressUI.SCREEN_WIDTH-image_r.getWidth(null))//오른쪽으로 어느정도 왔을때 끝까지 이동할수있게 설정
			player_x+=2;
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
	public int getPlayerLocation() {
		if(player_x == player_preX)
			return 0;
		else if(player_x<player_preX) {
			player_x+=2;
			return 2;
		}
		else {
			player_x-=2;
			return -2;
		}
		
	}
	
	
}
