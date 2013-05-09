// Class associated to the SELL schema
package fleaMarket.ontology;

import jade.content.AgentAction;

@SuppressWarnings("serial")
public class Sell implements AgentAction {
	private Product item;

	public Product getItem() {
		return item;
	}

	public void setItem(Product item) {
		this.item = item;
	}

}