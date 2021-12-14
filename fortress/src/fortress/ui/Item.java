package fortress.ui;
import java.awt.Image;

public abstract class Item {
	protected int itemX;
	protected int itemNumber;
	public abstract int getItemNumber();
	public Item(int itemX) {
		this.itemX = itemX;
	}
	public abstract void perform(MyPanel mypanel);
	public abstract Image getImage();
	
	public abstract int getItemX();
	public abstract void setItemX(int itemX); 

}
