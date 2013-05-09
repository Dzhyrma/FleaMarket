package fleaMarket.buyer;

public interface BuyerGui {
	void setAgent(BuyerAgent a);

	void show();

	void hide();

	void notifyUser(String message);

	void dispose();
	
	void setEnable(boolean flag);
	
	boolean getEnable();
}