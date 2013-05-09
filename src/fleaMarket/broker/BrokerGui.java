package fleaMarket.broker;

public interface BrokerGui {
	void setAgent(BrokerAgent a);

	void show();

	void hide();

	void notifyUser(String message);

	void dispose();
}