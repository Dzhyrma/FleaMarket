package fleaMarket.buyer;

import jade.gui.TimeChooser;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import fleaMarket.ontology.*;

import java.util.Date;

@SuppressWarnings("serial")
public class BuyerGuiImpl extends JFrame implements BuyerGui {
	private BuyerAgent myAgent;

	private JTextField titleTF, desiredCostTF, maxCostTF, deadlineTF, nameTF;
	private JButton setDeadlineB, registerB;
	private JButton setCCB, buyB, resetB, exitB;
	private JTextArea logTA;

	private Date deadline;

	public BuyerGuiImpl() {
		super();

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				myAgent.doDelete();
			}
		});

		JPanel rootPanel = new JPanel();
		rootPanel.setLayout(new GridBagLayout());
		rootPanel.setMinimumSize(new Dimension(330, 140));
		rootPanel.setPreferredSize(new Dimension(330, 140));

		// /////////////
		// Register Line
		// /////////////
		JLabel l = new JLabel("Your name:");
		l.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new java.awt.Insets(5, 3, 0, 3);
		rootPanel.add(l, gridBagConstraints);

		nameTF = new JTextField(64);
		nameTF.setMinimumSize(new Dimension(146, 20));
		nameTF.setPreferredSize(new Dimension(146, 20));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new Insets(5, 3, 0, 3);
		rootPanel.add(nameTF, gridBagConstraints);

		registerB = new JButton("Register");
		registerB.setMinimumSize(new Dimension(80, 20));
		registerB.setPreferredSize(new Dimension(80, 20));
		registerB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String name = nameTF.getText();
				if (name.isEmpty())
					JOptionPane.showMessageDialog(BuyerGuiImpl.this, "Choose name", Consts.WARNING, JOptionPane.WARNING_MESSAGE);
				else
					myAgent.register(name);
			}
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new Insets(5, 3, 0, 3);
		rootPanel.add(registerB, gridBagConstraints);

		// /////////
		// Line 0
		// /////////
		l = new JLabel("Product to buy:");
		l.setHorizontalAlignment(SwingConstants.LEFT);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new java.awt.Insets(5, 3, 0, 3);
		rootPanel.add(l, gridBagConstraints);

		titleTF = new JTextField(64);
		titleTF.setMinimumSize(new Dimension(232, 20));
		titleTF.setPreferredSize(new Dimension(232, 20));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new Insets(5, 3, 0, 3);
		rootPanel.add(titleTF, gridBagConstraints);

		// /////////
		// Line 1
		// /////////
		l = new JLabel("Max cost:");
		l.setHorizontalAlignment(SwingConstants.LEFT);
		l.setMinimumSize(new Dimension(70, 20));
		l.setPreferredSize(new Dimension(70, 20));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new java.awt.Insets(5, 3, 0, 3);
		rootPanel.add(l, gridBagConstraints);

		maxCostTF = new JTextField(64);
		maxCostTF.setMinimumSize(new Dimension(232, 20));
		maxCostTF.setPreferredSize(new Dimension(232, 20));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new Insets(5, 3, 0, 3);
		rootPanel.add(maxCostTF, gridBagConstraints);

		// /////////
		// Line 2
		// /////////
		l = new JLabel("Deadline:");
		l.setHorizontalAlignment(SwingConstants.LEFT);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new java.awt.Insets(5, 3, 0, 3);
		rootPanel.add(l, gridBagConstraints);

		deadlineTF = new JTextField(64);
		deadlineTF.setMinimumSize(new Dimension(146, 20));
		deadlineTF.setPreferredSize(new Dimension(146, 20));
		deadlineTF.setEnabled(false);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new Insets(5, 3, 0, 3);
		rootPanel.add(deadlineTF, gridBagConstraints);

		setDeadlineB = new JButton("Set");
		setDeadlineB.setMinimumSize(new Dimension(80, 20));
		setDeadlineB.setPreferredSize(new Dimension(80, 20));
		setDeadlineB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Date d = deadline;
				if (d == null) {
					d = new Date();
				}
				TimeChooser tc = new TimeChooser(d);
				if (tc.showEditTimeDlg(BuyerGuiImpl.this) == TimeChooser.OK) {
					deadline = tc.getDate();
					deadlineTF.setText(deadline.toString());
				}
			}
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new Insets(5, 3, 0, 3);
		rootPanel.add(setDeadlineB, gridBagConstraints);

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
		buyB = new JButton("Buy");
		buyB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String title = titleTF.getText();
				// int desiredCost = -1;
				int maxCost = -1;
				if (title != null && title.length() > 0) {
					if (deadline != null && deadline.getTime() > System.currentTimeMillis()) {
						try {
							try {
								maxCost = Integer.parseInt(maxCostTF.getText());
								myAgent.purchase(title, maxCost, deadline);
								notifyUser("PUT FOR BUY: " + title + " at max " + maxCost + " by " + deadline);
							} catch (Exception ex1) {
								// Invalid max cost
								JOptionPane.showMessageDialog(BuyerGuiImpl.this, "Invalid max cost", Consts.WARNING, JOptionPane.WARNING_MESSAGE);
							}
						} catch (Exception ex2) {
							// Invalid desired cost
							JOptionPane.showMessageDialog(BuyerGuiImpl.this, "Invalid best cost", Consts.WARNING, JOptionPane.WARNING_MESSAGE);
						}
					} else {
						// No deadline specified
						JOptionPane.showMessageDialog(BuyerGuiImpl.this, "Invalid deadline", Consts.WARNING, JOptionPane.WARNING_MESSAGE);
					}
				} else {
					// No product title specified
					JOptionPane.showMessageDialog(BuyerGuiImpl.this, "No product title specified", Consts.WARNING, JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		resetB = new JButton("Reset");
		resetB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				titleTF.setText(Consts.EMPTY_STRING);
				nameTF.setText(Consts.EMPTY_STRING);
				desiredCostTF.setText(Consts.EMPTY_STRING);
				maxCostTF.setText(Consts.EMPTY_STRING);
				deadlineTF.setText(Consts.EMPTY_STRING);
				deadline = null;
			}
		});
		exitB = new JButton("Exit");
		exitB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				myAgent.doDelete();
			}
		});

		buyB.setPreferredSize(resetB.getPreferredSize());
		exitB.setPreferredSize(resetB.getPreferredSize());

		p.add(buyB);
		p.add(resetB);
		p.add(exitB);

		p.setBorder(new BevelBorder(BevelBorder.LOWERED));
		getContentPane().add(p, BorderLayout.SOUTH);

		pack();

		setResizable(false);

		setEnable(false);
	}

	public void setEnable(boolean flag) {
		if (registerB != null)
			registerB.setEnabled(!flag);
		if (titleTF != null)
			titleTF.setEnabled(flag);
		if (maxCostTF != null)
			maxCostTF.setEnabled(flag);
		if (setDeadlineB != null)
			setDeadlineB.setEnabled(flag);
		if (setCCB != null)
			setCCB.setEnabled(flag);
		if (resetB != null)
			resetB.setEnabled(flag);
		if (buyB != null)
			buyB.setEnabled(flag);
	}

	public void setAgent(BuyerAgent a) {
		myAgent = a;
		setTitle(myAgent.getName());
	}

	public void notifyUser(String message) {
		logTA.append(message + Consts.NEW_LINE_STRING);
	}

	public boolean getEnable() {
		return (registerB != null) ? !registerB.isEnabled() : false;
	}
}