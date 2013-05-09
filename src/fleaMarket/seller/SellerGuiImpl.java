package fleaMarket.seller;

import jade.gui.TimeChooser;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import fleaMarket.ontology.*;

import java.util.Date;

@SuppressWarnings("serial")
public class SellerGuiImpl extends JFrame implements SellerGui {
	private SellerAgent myAgent;

	private JTextField titleTF, desiredPriceTF, minPriceTF, deadlineTF, nameTF;
	private JButton setDeadlineB, registerB;
	private JButton sellB, resetB, exitB;
	private JTextArea logTA;

	private Date deadline;

	public void setAgent(SellerAgent a) {
		myAgent = a;
		setTitle(myAgent.getName());
	}

	public SellerGuiImpl() {
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
					JOptionPane.showMessageDialog(SellerGuiImpl.this, "Choose name", Consts.WARNING, JOptionPane.WARNING_MESSAGE);
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
		l = new JLabel("Product to sell:");
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
		l = new JLabel("Best price:");
		l.setHorizontalAlignment(SwingConstants.LEFT);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new java.awt.Insets(5, 3, 0, 3);
		rootPanel.add(l, gridBagConstraints);

		desiredPriceTF = new JTextField(64);
		desiredPriceTF.setMinimumSize(new Dimension(70, 20));
		desiredPriceTF.setPreferredSize(new Dimension(70, 20));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new Insets(5, 3, 0, 3);
		rootPanel.add(desiredPriceTF, gridBagConstraints);

		l = new JLabel("Min price:");
		l.setHorizontalAlignment(SwingConstants.LEFT);
		l.setMinimumSize(new Dimension(70, 20));
		l.setPreferredSize(new Dimension(70, 20));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new java.awt.Insets(5, 3, 0, 3);
		rootPanel.add(l, gridBagConstraints);

		minPriceTF = new JTextField(64);
		minPriceTF.setMinimumSize(new Dimension(80, 20));
		minPriceTF.setPreferredSize(new Dimension(80, 20));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new Insets(5, 3, 0, 3);
		rootPanel.add(minPriceTF, gridBagConstraints);

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
				if (tc.showEditTimeDlg(SellerGuiImpl.this) == TimeChooser.OK) {
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
		sellB = new JButton("Sell");
		sellB.setEnabled(false);
		sellB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String title = titleTF.getText();
				int desiredPrice = -1;
				int minPrice = -1;
				if (title != null && title.length() > 0) {
					if (deadline != null && deadline.getTime() > System.currentTimeMillis()) {
						try {
							desiredPrice = Integer.parseInt(desiredPriceTF.getText());
							try {
								minPrice = Integer.parseInt(minPriceTF.getText());
								if (minPrice <= desiredPrice) {
									myAgent.putForSale(title, desiredPrice, minPrice, deadline);
									notifyUser("PUT FOR SALE: " + title + " between " + desiredPrice + " and " + minPrice + " by " + deadline);
								} else {
									// minPrice > desiredPrice
									JOptionPane.showMessageDialog(SellerGuiImpl.this, "Min price must be cheaper than best price", Consts.WARNING,
											JOptionPane.WARNING_MESSAGE);
								}
							} catch (Exception ex1) {
								// Invalid max cost
								JOptionPane.showMessageDialog(SellerGuiImpl.this, "Invalid min price", Consts.WARNING, JOptionPane.WARNING_MESSAGE);
							}
						} catch (Exception ex2) {
							// Invalid desired cost
							JOptionPane.showMessageDialog(SellerGuiImpl.this, "Invalid best price", Consts.WARNING, JOptionPane.WARNING_MESSAGE);
						}
					} else {
						// No deadline specified
						JOptionPane.showMessageDialog(SellerGuiImpl.this, "Invalid deadline", Consts.WARNING, JOptionPane.WARNING_MESSAGE);
					}
				} else {
					// No product title specified
					JOptionPane.showMessageDialog(SellerGuiImpl.this, "No product title specified", Consts.WARNING, JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		resetB = new JButton("Reset");
		resetB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				titleTF.setText(Consts.EMPTY_STRING);
				nameTF.setText(Consts.EMPTY_STRING);
				desiredPriceTF.setText(Consts.EMPTY_STRING);
				minPriceTF.setText(Consts.EMPTY_STRING);
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

		sellB.setPreferredSize(resetB.getPreferredSize());
		exitB.setPreferredSize(resetB.getPreferredSize());

		p.add(sellB);
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
		if (desiredPriceTF != null)
			desiredPriceTF.setEnabled(flag);
		if (minPriceTF != null)
			minPriceTF.setEnabled(flag);
		if (setDeadlineB != null)
			setDeadlineB.setEnabled(flag);
		if (resetB != null)
			resetB.setEnabled(flag);
		if (sellB != null)
			sellB.setEnabled(flag);
	}

	public void notifyUser(String message) {
		logTA.append(message + Consts.NEW_LINE_STRING);
	}

	public boolean getEnable() {
		return (registerB != null) ? !registerB.isEnabled() : false;
	}
}