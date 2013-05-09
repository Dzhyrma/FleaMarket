package fleaMarket.seller;

public interface SellerGui {

	void setAgent(SellerAgent a);

	void show();

	void hide();

	void notifyUser(String message);

	void dispose();

	void setEnable(boolean flag);

	boolean getEnable();
}