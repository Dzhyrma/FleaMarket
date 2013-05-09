package fleaMarket.ontology;

import jade.content.onto.*;
import jade.content.schema.*;

@SuppressWarnings("serial")
public class TradingOntology extends Ontology implements TradingVocabulary {
	// The name identifying this ontology
	public static final String ONTOLOGY_NAME = "Trading-ontology";

	// The singleton instance of this ontology
	private static Ontology theInstance = new TradingOntology();

	// Retrieve the singleton Book-trading ontology instance
	public static Ontology getInstance() {
		return theInstance;
	}

	// Private constructor
	private TradingOntology() {
		// The trading ontology extends the basic ontology
		super(ONTOLOGY_NAME, BasicOntology.getInstance());
		try {
			add(new ConceptSchema(PRODUCT), Product.class);
			add(new PredicateSchema(COSTS), Costs.class);
			add(new AgentActionSchema(SELL), Sell.class);

			// Structure of the schema for the product concept
			ConceptSchema cs = (ConceptSchema) getSchema(PRODUCT);
			cs.add(PRODUCT_TITLE, (PrimitiveSchema) getSchema(BasicOntology.STRING));

			// Structure of the schema for the Costs predicate
			PredicateSchema ps = (PredicateSchema) getSchema(COSTS);
			ps.add(COSTS_ITEM, (ConceptSchema) cs);
			ps.add(COSTS_PRICE, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));

			// Structure of the schema for the Sell agent action
			AgentActionSchema as = (AgentActionSchema) getSchema(SELL);
			as.add(SELL_ITEM, (ConceptSchema) getSchema(PRODUCT));

		} catch (OntologyException oe) {
			oe.printStackTrace();
		}
	}
}