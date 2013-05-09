// Class associated to the PRODUCT schema
package fleaMarket.ontology;

import jade.content.Concept;

@SuppressWarnings("serial")
public class Product implements Concept {
	private String title;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
