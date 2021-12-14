package fortress.ui;

import java.io.Serializable;

import javax.swing.ImageIcon;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@AllArgsConstructor
public class AttackObject implements Serializable{
	private static final long serialVersionUID = 2L;
	private String code;
	private double veloY;
	private double power;
}
