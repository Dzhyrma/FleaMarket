package fleaMarket.buyer;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;

import jade.proto.ContractNetInitiator;

import java.util.Vector;
import java.util.Date;

import jade.content.*;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.*;
import jade.content.onto.basic.*;

import fleaMarket.ontology.*;

@SuppressWarnings("serial")
public class BuyerAgent extends Agent {
	// AID of registered broker
	private AID myBroker = null;

	// The list of known brokers
	private Vector<AID> Brokers = new Vector<AID>();

	// The GUI to interact with the user
	private BuyerGui myGui;

	private Codec codec = new SLCodec();
	private Ontology ontology = TradingOntology.getInstance();

	/**
	 * Agent initializations
	 **/
	protected void setup() {

		// Enable O2A Communication
		setEnabledO2ACommunication(true, 0);
		// Add the behaviour serving notifications from the external system
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {
				ProductInfo info = (ProductInfo) myAgent.getO2AObject();
				if (info != null) {
					purchase(info.getTitle(), info.getMaxPrice(), info.getDeadline());
				} else {
					block();
				}
			}
		});

		// Printout a welcome message
		System.out.println(String.format(Consts.WELCOME_MESSAGE_FORMAT, Consts.BUYER, getAID().getName()));

		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);

		// Show the GUI to interact with the user
		myGui = new BuyerGuiImpl();
		myGui.setAgent(this);
		myGui.show();

		// Register the Product-buying service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType(Consts.PRODUCT_BUYING);
		sd.setName(getLocalName() + '-' + Consts.PRODUCT_BUYING);
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// Update the list of seller agents every minute
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
		System.out.println(String.format(Consts.DISMISSAL_MESSAGE_FORMAT, Consts.BUYER, getAID().getName()));

		// Deregister from the yellow pages
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}

	/**
	 * This method is called by the GUI when the user inserts a new product to
	 * buy
	 * 
	 * @param title
	 *            The title of the product to buy
	 * @param maxPrice
	 *            The maximum acceptable price to buy the product
	 * @param deadline
	 *            The deadline by which to buy the product
	 **/
	public void purchase(String title, int maxPrice, Date deadline) {
		addBehaviour(new PurchaseManager(this, title, maxPrice, deadline));
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

	private class PurchaseManager extends TickerBehaviour {
		private String title;
		private int maxPrice;
		private long deadline, initTime, deltaT;

		private PurchaseManager(Agent a, String t, int mp, Date d) {
			super(a, 60000); // tick every minute
			title = t;
			maxPrice = mp;
			deadline = d.getTime();
			initTime = System.currentTimeMillis();
			deltaT = deadline - initTime;
		}

		public void onTick() {
			long currentTime = System.currentTimeMillis();
			if (currentTime > deadline) {
				// Deadline expired
				myGui.notifyUser(String.format(Consts.CANNOT_BUY_PRODUCT_FORMAT, title));
				stop();
			} else {
				// Compute the currently acceptable price and start a
				// negotiation
				long elapsedTime = currentTime - initTime;
				int acceptablePrice = (int) Math.round(1.0 * maxPrice * (1.0 * elapsedTime / deltaT));
				myAgent.addBehaviour(new Negotiator(title, acceptablePrice, this));
			}
		}
	}

	public ACLMessage cfp = new ACLMessage(ACLMessage.CFP);

	public class Negotiator extends ContractNetInitiator {
		private String title;
		private int maxPrice;
		private PurchaseManager manager;

		public Negotiator(String t, int p, PurchaseManager m) {
			super(BuyerAgent.this, cfp);
			title = t;
			maxPrice = p;
			manager = m;
			Product product = new Product();
			product.setTitle(title);
			Sell sellAction = new Sell();
			sellAction.setItem(product);
			Action act = new Action(BuyerAgent.this.getAID(), sellAction);
			try {
				cfp.setLanguage(codec.getName());
				cfp.setOntology(ontology.getName());
				BuyerAgent.this.getContentManager().fillContent(cfp, act);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		protected Vector<ACLMessage> prepareCfps(ACLMessage cfp) {
			cfp.clearAllReceiver();
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType(Consts.PRODUCT_SELLING);
			template.addServices(sd);
			DFAgentDescription[] result = null;
			try {
				result = DFService.search(myAgent, template);
				for (int i = 0; i < result.length; ++i) {
					cfp.addReceiver(result[i].getName());
				}
			} catch (FIPAException fe) {
				fe.printStackTrace();
			}
			Vector<ACLMessage> v = new Vector<ACLMessage>();
			v.add(cfp);
			if (result != null && result.length > 0)
				myGui.notifyUser(String.format(Consts.CALL_FOR_PROPOSAL_FORMAT, result.length));
			return v;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		protected void handleAllResponses(Vector responses, Vector acceptances) {
			ACLMessage bestOffer = null;
			int bestPrice = -1;
			for (int i = 0; i < responses.size(); i++) {
				ACLMessage rsp = (ACLMessage) responses.get(i);
				if (rsp.getPerformative() == ACLMessage.PROPOSE) {
					try {
						ContentElementList cel = (ContentElementList) myAgent.getContentManager().extractContent(rsp);
						int price = ((Costs) cel.get(1)).getPrice();
						myGui.notifyUser(String.format(Consts.RECEIVED_PROPOSAL_FORMAT, price, maxPrice));
						if (bestOffer == null || price < bestPrice) {
							bestOffer = rsp;
							bestPrice = price;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			for (int i = 0; i < responses.size(); i++) {
				ACLMessage rsp = (ACLMessage) responses.get(i);
				ACLMessage accept = rsp.createReply();

				if (rsp == bestOffer) {
					boolean acceptedProposal = (bestPrice <= maxPrice);
					accept.setPerformative(acceptedProposal ? ACLMessage.ACCEPT_PROPOSAL : ACLMessage.REJECT_PROPOSAL);
					accept.setContent(title);
					myGui.notifyUser(acceptedProposal ? Consts.SENT_ACCEPT_PROPOSAL : Consts.SENT_REJECT_PROPOSAL);
				} else {
					accept.setPerformative(ACLMessage.REJECT_PROPOSAL);
				}
				// System.out.println(myAgent.getLocalName()+" handleAllResponses.acceptances.add "+accept);
				acceptances.add(accept);
			}
		}

		protected void handleInform(ACLMessage inform) {
			// Product successfully purchased
			int price = Integer.parseInt(inform.getContent());
			myGui.notifyUser(String.format(Consts.PRODUCT_PURCHASED_FORMAT, title, price));
			manager.stop();
		}

	}
}
