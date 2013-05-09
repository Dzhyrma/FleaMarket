// Class associated to the COSTS schema
package fleaMarket.ontology;

import jade.content.Predicate;

@SuppressWarnings("serial")
public class Costs implements Predicate {
	private Product item;
	private int price;

	public Product getItem() {
		return item;
	}

	public void setItem(Product item) {
		this.item = item;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}
}