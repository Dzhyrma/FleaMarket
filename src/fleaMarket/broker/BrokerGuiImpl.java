package fleaMarket.broker;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

@SuppressWarnings("serial")
public class BrokerGuiImpl extends JFrame implements BrokerGui {
	private BrokerAgent myAgent;

	private JButton exitB;
	private JTextArea logTA;

	public void setAgent(BrokerAgent a) {
		myAgent = a;
		setTitle(myAgent.getName());
	}

	public BrokerGuiImpl() {
		super();

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				myAgent.doDelete();
			}
		});

		JPanel rootPanel = new JPanel();
		rootPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		getContentPane().add(rootPanel, BorderLayout.NORTH);

		logTA = new JTextArea();
		logTA.setEnabled(false);
		JScrollPane jsp = new JScrollPane(logTA);
		jsp.setMinimumSize(new Dimension(300, 180));
		jsp.setPreferredSize(new Dimension(300, 180));
		JPanel p = new JPanel();
		p.setBorder(new BevelBorder(BevelBorder.LOWERED));
		p.add(jsp);
		getContentPane().add(p, BorderLayout.CENTER);

		p = new JPanel();
		exitB = new JButton("Exit");
		exitB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				myAgent.doDelete();
			}
		});

		p.add(exitB);

		p.setBorder(new BevelBorder(BevelBorder.LOWERED));
		getContentPane().add(p, BorderLayout.SOUTH);

		pack();

		setResizable(false);
	}

	public void notifyUser(String message) {
		logTA.append(message + "\n");
	}
}