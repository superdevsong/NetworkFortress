package fortress.ui;

import java.awt.Image;

import javax.swing.ImageIcon;

public class Background {
	Image image = new ImageIcon("src/fortress/ui/tree.jpg").getImage();
	Image image_t = new ImageIcon("src/fortress/ui/tree_1.jpg").getImage();
	private int field = 300 - image_t.getHeight(null);
	private int background_x = 0, field_x = 0;
}
