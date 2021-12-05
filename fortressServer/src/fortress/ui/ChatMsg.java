package fortress.ui;

import java.io.Serializable;
import javax.swing.ImageIcon;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter

class ChatMsg implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	private String code; // 100:로그인, 400:로그아웃, 200:채팅메시지, 300:Image 700이동 600번 당신의턴 601 턴변경
	private String data;
	private int player_x, player_y;
	private int player_num;
	private double power;
	public ImageIcon img;
	
	public ChatMsg(String id, String code, String msg,int player_x,int player_y) {
		this.id = id;
		this.code = code;
		this.data = msg;
		this.player_x = player_x;
		this.player_y = player_y;
	}


	
}