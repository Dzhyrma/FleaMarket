package fleaMarket.seller;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.proto.ContractNetResponder;
import jade.content.*;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.*;
import jade.content.onto.basic.*;

import java.util.*;

import fleaMarket.ontology.*;

@SuppressWarnings("serial")
public class SellerAgent extends Agent {
	// AID of registered broker
	private AID myBroker = null;

	// AIDs of all online borkers
	private Vector<AID> Brokers = new Vector<AID>();

	// The catalogue of productss available for sale
	private HashMap<String, PriceManager> catalogue = new HashMap<String, PriceManager>();

	// The GUI to interact with the user
	private SellerGui myGui;

	private Codec codec = new SLCodec();
	private Ontology ontology = TradingOntology.getInstance();

	/**
	 * Agent initializations
	 **/
	protected void setup() {
		// Printout a welcome message
		System.out.println(String.format(Consts.WELCOME_MESSAGE_FORMAT, Consts.SELLER, getAID().getName()));

		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);

		// Create and show the GUI
		myGui = new SellerGuiImpl();
		myGui.setAgent(this);
		myGui.show();

		// Add the behaviour serving calls for price from buyer agents
		addBehaviour(new CallForOfferServer());

		// Register the Product-selling service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType(Consts.PRODUCT_SELLING);
		sd.setName(getLocalName() + '-' + Consts.PRODUCT_SELLING);
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		addBehaviour(new TickerBehaviour(this, 5000) {
			protected void onTick() {
				try {
					ACLMessage msg = receive();
					if (msg != null) {
						if (msg.getPerformative() == ACLMessage.CONFIRM) {
							try {
								myBroker = msg.getSender();
								myGui.notifyUser(Consts.RECEIVED_REGISTRATION_CONFIRMATION);
								myGui.setEnable(true);
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else if (msg.getPerformative() == ACLMessage.DISCONFIRM) {
							try {
								myGui.notifyUser(Consts.RECEIVED_REGISTRATION_DISCONFIRMATION);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				} catch (Exception fe) {
					fe.printStackTrace();
				}
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType(Consts.REGISTRATION);
				template.addServices(sd);
				try {
					int borkersCount = Brokers.size();
					DFAgentDescription[] result = DFService.search(myAgent, template);
					Brokers.clear();
					for (int i = 0; i < result.length; ++i) {
						Brokers.addElement(result[i].getName());
					}
					if (Brokers.size() > borkersCount) {
						myGui.notifyUser(Consts.NEW_BROKER_FOUND);
						myGui.notifyUser(String.format(Consts.AMOUNT_OF_BROKERS_FORMAT, Brokers.size()));
					} else if (Brokers.size() < borkersCount) {
						myGui.notifyUser(Consts.BROKER_WENT_OFFLINE);
						myGui.notifyUser(String.format(Consts.AMOUNT_OF_BROKERS_FORMAT, Brokers.size()));
					}
					if (myBroker != null && !Brokers.contains(myBroker) && myGui.getEnable()) {
						myGui.notifyUser(Consts.YOUR_BROKER_WENT_OFFLINE);
						myGui.setEnable(false);
					}
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}
			}
		});
	}

	/**
	 * Agent clean-up
	 **/
	protected void takeDown() {
		// Dispose the GUI if it is there
		if (myGui != null) {
			myGui.dispose();
		}

		// Printout a dismissal message
		System.out.println(String.format(Consts.DISMISSAL_MESSAGE_FORMAT, Consts.SELLER, getAID().getName()));

		// Deregister from the yellow pages
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}

	/**
	 * This method is called by the GUI when the user try to register
	 * 
	 * @param name
	 *            Name of the user
	 **/
	public void register(String name) {
		ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
		if (Brokers.size() > 0) {
			msg.addReceiver(Brokers.get(0));
			msg.setLanguage(Consts.LANGUAGE_EN);
			msg.setOntology(Consts.ONTOLOGY_REGISTER_NAME);
			msg.setContent(name);
			send(msg);
			myGui.notifyUser(Consts.REGISTER_PROPOSAL_SENT_TO + Brokers.get(0));
		} else
			myGui.notifyUser(Consts.NO_BROKERS_ONLINE);
	}

	/**
	 * This method is called by the GUI when the user inserts a new product for
	 * sale
	 * 
	 * @param title
	 *            The title of the product for sale
	 * @param initialPrice
	 *            The initial price
	 * @param minPrice
	 *            The minimum price
	 * @param deadline
	 *            The deadline by which to sell the product
	 **/
	public void putForSale(String title, int initPrice, int minPrice, Date deadline) {
		addBehaviour(new PriceManager(this, title, initPrice, minPrice, deadline));
	}

	private class PriceManager extends TickerBehaviour {
		private String title;
		private int currentPrice, initPrice, deltaP;
		private long initTime, deadline, deltaT;

		private PriceManager(Agent a, String t, int ip, int mp, Date d) {
			super(a, 10000); // tick every minute
			title = t;
			initPrice = ip;
			currentPrice = initPrice;
			deltaP = initPrice - mp;
			deadline = d.getTime();
			initTime = System.currentTimeMillis();
			deltaT = ((deadline - initTime) > 0 ? (deadline - initTime) : 60000);
		}

		public void onStart() {
			// Insert the product in the catalogue of products available for
			// sale
			catalogue.put(title, this);
			super.onStart();
		}

		public void onTick() {
			long currentTime = System.currentTimeMillis();
			if (currentTime > deadline) {
				// Deadline expired
				myGui.notifyUser(String.format(Consts.CANNOT_SELL_PRODUCT_FORMAT, title));
				catalogue.remove(title);
				stop();
			} else {
				// Compute the current price
				long elapsedTime = currentTime - initTime;
				// System.out.println("initPrice"+initPrice+"deltaP"+deltaP+"elapsedTime"+elapsedTime+"deltaT"+deltaT+"currentPrice"+currentPrice+"");
				currentPrice = (int) Math.round(initPrice - 1.0 * deltaP * (1.0 * elapsedTime / deltaT));
			}
		}

		public int getCurrentPrice() {
			return currentPrice;
		}

		public int getInitialPrice() {
			return initPrice;
		}
	}

	private class CallForOfferServer extends ContractNetResponder {

		int price, initialPrice;

		CallForOfferServer() {
			super(SellerAgent.this, MessageTemplate.and(MessageTemplate.MatchOntology(ontology.getName()), MessageTemplate.MatchPerformative(ACLMessage.CFP)));
		}

		protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
			// CFP Message received. Process it
			ACLMessage reply = cfp.createReply();
			{
				try {
					ContentManager cm = myAgent.getContentManager();
					Action act = (Action) cm.extractContent(cfp);
					Sell sellAction = (Sell) act.getAction();
					Product product = sellAction.getItem();
					myGui.notifyUser(String.format(Consts.RECEIVED_PROPOSAL_TO_BUY_FORMAT, product.getTitle()));
					PriceManager pm = (PriceManager) catalogue.get(product.getTitle());
					if (pm != null) {
						// The requested product is available for sale
						reply.setPerformative(ACLMessage.PROPOSE);
						ContentElementList cel = new ContentElementList();
						cel.add(act);
						Costs costs = new Costs();
						costs.setItem(product);
						price = pm.getCurrentPrice();
						initialPrice = pm.getInitialPrice();
						costs.setPrice(price);
						cel.add(costs);
						cm.fillContent(reply, cel);
					} else {
						// The requested product is NOT available for sale.
						reply.setPerformative(ACLMessage.REFUSE);
					}
				} catch (OntologyException oe) {
					oe.printStackTrace();
					reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
				} catch (CodecException ce) {
					ce.printStackTrace();
					reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
				} catch (Exception e) {
					e.printStackTrace();
					reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
				}
			}
			// System.out.println(myAgent.getLocalName()+"RX"+cfp+"\nTX"+reply+"\n\n");
			myGui.notifyUser(reply.getPerformative() == ACLMessage.PROPOSE ? String.format(Consts.SENT_PROPOSAL_TO_SELL_FORMAT, price)
					: Consts.REFUSED_PROPOSAL);
			return reply;
		}

		protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
			catalogue.remove(accept.getContent());
			ACLMessage inform = accept.createReply();
			inform.setPerformative(ACLMessage.INFORM);
			inform.setContent(Integer.toString(price));
			myGui.notifyUser(String.format(Consts.SENT_INFORM_AT_PRICE_FORMAT, price));
			@SuppressWarnings("deprecation")
			ACLMessage giveMoney = new ACLMessage();
			if (myBroker != null) giveMoney.addReceiver(myBroker);
			giveMoney.setPerformative(ACLMessage.INFORM);
			giveMoney.setContent(Integer.toString(Math.abs(initialPrice - price)/4));
			send(giveMoney);
			return inform;
		}

	}

}