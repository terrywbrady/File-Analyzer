package gov.nara.nwts.ftapp.gui;

import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * User interface component that will be generated at the completion of each File Analyzer action
 * @author TBrady
 *
 */
class SummaryPanel extends MyBorderPanel {
	private static final long serialVersionUID = 1L;
	JPanel tp;
	JPanel filterPanel;
	JScrollPane spFilter;
	StatsTable st;
	JTextField note;
	JTextField fnote;
	JTextField hnote;
	DirectoryTable parent;
	SummaryPanel(DirectoryTable dt, String header, String notetxt) {
		parent = dt;
		tp = addBorderPanel("Summary Counts");
		JPanel p = addPanel("", BorderLayout.SOUTH);
		note = new JTextField(notetxt);
		note.setEditable(false);
		note.setBorder(BorderFactory.createEmptyBorder());
		fnote = new JTextField(45);
		fnote.setEditable(false);
		fnote.setBorder(BorderFactory.createEmptyBorder());
		p.add(note);
		p.add(fnote);
		JButton save = new JButton("Export Table");
		save.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					new TableSaver(parent,st.tm,st.jt,"Stats",st.noExport);
				}
			}
		);
		p.add(save);
		JPanel np = addPanel("",BorderLayout.NORTH);
		np.setLayout(new BorderLayout());
		p = new JPanel();
		np.add(p, BorderLayout.NORTH);
		hnote = new JTextField(header,60);
		parent.detailsPanel.jtfRoot.setText(hnote.getText());
		hnote.setEditable(false);
		p.add(hnote);
		JButton b = new JButton("Remove Tab");
		b.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				parent.tabs.remove(SummaryPanel.this);
				parent.checkTabs();
			}});
		p.add(b);
		filterPanel = new JPanel();
		spFilter = new JScrollPane(filterPanel);
		spFilter.setPreferredSize(new Dimension(800, np.getPreferredSize().height + 10));
		np.add(spFilter, BorderLayout.SOUTH);
	}
	
	
	void showStats(StatsItemConfig details,TreeMap<String,Stats> types) {
		st = new StatsTable(details,types, parent);
		tp.removeAll();
		tp.add(new JScrollPane(st.jt), BorderLayout.CENTER);
		filterPanel.removeAll();
		spFilter.setVisible(!st.filters.isEmpty());
		for(Iterator<JComboBox<Object>>i=st.filters.iterator();i.hasNext();) {
			JComboBox<Object> cb = i.next();
			if (cb!=null) filterPanel.add(cb);
		}
	}
	
	public void setFilterNote(int x, int y) {
		fnote.setText("["+FTDriver.nf.format(x)+ " of " + FTDriver.nf.format(y) + " showing]");
	}
}

