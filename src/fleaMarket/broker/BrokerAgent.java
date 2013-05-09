package fleaMarket.broker;

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
public class BrokerAgent extends Agent {
	// The list of known seller agents
	private Vector<AID> sellers = new Vector<AID>();

	// The list of known registered agents
	private Map<AID, String> registeredAgents = new HashMap<AID, String>();

	// The GUI to interact with the user
	private BrokerGui myGui;

	private Codec codec = new SLCodec();
	private Ontology ontology = TradingOntology.getInstance();

	/**
	 * Agent initializations
	 **/
	protected void setup() {
		// Printout a welcome message
		System.out.println(String.format(Consts.WELCOME_MESSAGE_FORMAT, Consts.BROKER, getAID().getName()));

		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);

		// Create and show the GUI
		myGui = new BrokerGuiImpl();
		myGui.setAgent(this);
		myGui.show();

		// Add the behaviour serving calls for price from buyer agents
		addBehaviour(new CallForOfferServer());

		// Register the Product-selling service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType(Consts.REGISTRATION);
		sd.setName(getLocalName() + '-' + Consts.REGISTRATION);
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		addBehaviour(new TickerBehaviour(this, 5000) {
			protected void onTick() {

				DFAgentDescription template1 = new DFAgentDescription();
				DFAgentDescription template2 = new DFAgentDescription();
				ServiceDescription sd1 = new ServiceDescription();
				sd1.setType(Consts.PRODUCT_SELLING);
				ServiceDescription sd2 = new ServiceDescription();
				sd2.setType(Consts.PRODUCT_BUYING);
				template1.addServices(sd1);
				template2.addServices(sd2);
				try {
					DFAgentDescription[] result1 = DFService.search(myAgent, template1);
					DFAgentDescription[] result2 = DFService.search(myAgent, template2);
					List<AID> resultList = new ArrayList<AID>();
					for (DFAgentDescription element : result1)
						resultList.add(element.getName());
					for (DFAgentDescription element : result2)
						resultList.add(element.getName());
					Vector<AID> removableIds = new Vector<AID>();
					for (AID aid : registeredAgents.keySet()) {
						if (!resultList.contains(aid)) {
							myGui.notifyUser(String.format(Consts.USER_WENT_OFFLINE, registeredAgents.get(aid)));
							removableIds.add(aid);
						}
					}
					for (AID aid:removableIds)
						registeredAgents.remove(aid);
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}
				try {
					ACLMessage msg;
					while ((msg = receive()) != null) {
						if (msg.getPerformative() == ACLMessage.PROPOSE) {
							try {
								String name = msg.getContent();
								myGui.notifyUser(String.format(Consts.RECEIVED_REGISTRATION_REQUEST_FORMAT, name));
								ACLMessage rpl = msg.createReply();
								boolean nameExists = registeredAgents.containsValue(name);
								rpl.setPerformative(nameExists ? ACLMessage.DISCONFIRM : ACLMessage.CONFIRM);
								myGui.notifyUser(nameExists ? String.format(Consts.SENT_REGISTER_DISCONFIRMATION_FORMAT, name) : String.format(
										Consts.SENT_REGISTER_CONFIRMATION_FORMAT, name));
								if (!nameExists) 
									registeredAgents.put(msg.getSender(), name);
								send(rpl);
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else if (msg.getPerformative() == ACLMessage.INFORM) {
							try {
								String money = msg.getContent();
								myGui.notifyUser(String.format(Consts.RECEIVED_MONEY_FROM_USER_FORMAT, registeredAgents.get(msg.getSender()), money));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				} catch (Exception fe) {
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
		System.out.println(String.format(Consts.DISMISSAL_MESSAGE_FORMAT, Consts.BROKER, getAID().getName()));

		// Deregister from the yellow pages
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}

	private class CallForOfferServer extends ContractNetResponder {

		int price;

		CallForOfferServer() {
			super(BrokerAgent.this, MessageTemplate.and(MessageTemplate.MatchOntology(ontology.getName()), MessageTemplate.MatchPerformative(ACLMessage.CFP)));
		}

		protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
			// CFP Message received. Process it

			try {
				ContentManager cm = myAgent.getContentManager();
				Action act = (Action) cm.extractContent(cfp);
				Sell sellAction = (Sell) act.getAction();
				Product product = sellAction.getItem();
				myGui.notifyUser(String.format(Consts.RECEIVED_PROPOSAL_TO_BUY_FORMAT, product.getTitle()));
				cfp.clearAllReceiver();
				cfp.clearAllReplyTo();
				cfp.addReplyTo(cfp.getSender());
				for (int i = 0; sellers.size() > i; i++) {
					cfp.addReceiver((AID) sellers.get(i));
				}
			} catch (OntologyException oe) {
				oe.printStackTrace();
				cfp = cfp.createReply();
				cfp.setPerformative(ACLMessage.NOT_UNDERSTOOD);
			} catch (CodecException ce) {
				ce.printStackTrace();
				cfp = cfp.createReply();
				cfp.setPerformative(ACLMessage.NOT_UNDERSTOOD);
			} catch (Exception e) {
				e.printStackTrace();
				cfp = cfp.createReply();
				cfp.setPerformative(ACLMessage.NOT_UNDERSTOOD);
			}
			return cfp;
		}

		protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
			ACLMessage inform = accept.createReply();
			inform.setPerformative(ACLMessage.INFORM);
			inform.setContent(Integer.toString(price));
			myGui.notifyUser(String.format(Consts.SENT_INFORM_AT_PRICE_FORMAT, price));
			return inform;
		}

	}

}