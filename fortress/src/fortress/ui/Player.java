package fortress.ui;

import java.awt.Image;
import java.nio.file.Files;
import java.util.Vector;

import javax.swing.ImageIcon;

public class Player {
	
	boolean direction = true;
	
	Image image_r = new ImageIcon("src/image/player/player2r_attack.gif").getImage();
	Image image_l = new ImageIcon("src/image/player/player2_attack.gif").getImage();
	Vector<Item> items = new Vector();
	MyPanel myPanel;
	int range_x,range_y;
	private int player_x, player_y;
	public Vector<Item> getItems() {
		return items;
	}
	private int player_preX;
	private int moveGauge = 0;
	private String user_name;
	private String UserStatus;
	
	public Player(int player_num,String TeamStatus,String UserStatus,String UserName) {
		this.player_num = player_num;
		this.TeamStatus = TeamStatus;
		this.UserStatus = UserStatus;
		this.user_name = UserName;
	
	}
	public void newItem(Item item) {//아이템 추가
		this.items.add(item);
		if(item.getItemNumber()==0) {//더블공격 스킬을 먹었을때
			myPanel.setSkillDouble(myPanel.getSkillDouble()+1);
		} else if(item.getItemNumber()==1) {//power스킬을 먹었을때
			myPanel.setSkillPower(myPanel.getSkillPower()+1);
		} else if(item.getItemNumber()==2) {//힐 스킬을 먹었을때
			myPanel.setSkillHeal(myPanel.getSkillHeal()+1);
		}
	}
	public void setMyPanel(MyPanel myPanel) {
		this.myPanel = myPanel;
	}

	public void dead() {
		image_r = null;
		image_l = null;
		UserStatus="D";
	}
	public String getUserStatus() {
		return UserStatus;
	}

	public void setUserStatus(String userStatus) {
		UserStatus = userStatus;
	}
	private String TeamStatus;
	public String getTeamStatus() {
		return TeamStatus;
	}

	public void setTeamStatus(String teamStatus) {
		TeamStatus = teamStatus;
	}
	private int player_num;
	private int player_hp;
	
	
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
		if(moveGauge>0) {
			System.out.println("왼쪽 게이지 "+moveGauge);
			moveGauge -=1;
		direction=false;
		if(player_x>=500)
		player_x-=2;
		if(edge && player_x>=0)
			player_x-=2;
		System.out.println("?????");
		}
		//무빙금지
	}
	public void init(int field, int x,int hp) {
		player_y=field-image_r.getHeight(null);;
		player_x =x; /*일단 0으로 주어짐 (int)(Math.random()*500); */
		player_preX = player_x;
		player_hp = hp;
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
		if(moveGauge>0) {
			System.out.println("오른쪽 게이지 "+moveGauge);
			moveGauge -=1;
		direction=true;
		if(player_x< camera_x-image_r.getWidth(null))
		player_x+=2;
		if(edge&&player_x< FortressUI.SCREEN_WIDTH-image_r.getWidth(null))//오른쪽으로 어느정도 왔을때 끝까지 이동할수있게 설정
			player_x+=2;
		}
		//무빙금지
	}
	public int getMoveGauge() {
		return moveGauge;
	}
	public void setMoveGauge(int moveGauge) {
		this.moveGauge = moveGauge;
	}
	public int getPlayer_hp() {
		return player_hp;
	}
	public void setPlayer_hp(int player_hp) {
		this.player_hp = player_hp;
	}
	public boolean isDirection() {//isdiretion true right false left 
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
