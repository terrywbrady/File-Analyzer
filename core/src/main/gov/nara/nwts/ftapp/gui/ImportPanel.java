package gov.nara.nwts.ftapp.gui;

import gov.nara.nwts.ftapp.ftprop.FTProp;
import gov.nara.nwts.ftapp.ftprop.InitializationStatus;
import gov.nara.nwts.ftapp.importer.Importer;
import gov.nara.nwts.ftapp.stats.Stats;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

/**
 * User interface component presenting file import options and auto-sequencing options.
 * @author TBrady
 *
 */
class ImportPanel extends MyPanel {
	
	private static final long serialVersionUID = 1L;
	JTextField prefix;
	JTextField suffix;
	JFormattedTextField start;
	JFormattedTextField end;
	JComboBox<Object> pad;
	JComboBox<Importer> importers;
	JTextArea importerDesc;
	JCheckBox forceKey;
	DirectoryTable parent;
	FileSelectChooser fsc;
	JPanel propPanel;
	JTabbedPane dptab;
	
	JPanel ipGen;
	
	void updateDesc() {
		Importer i = (Importer)importers.getSelectedItem();
		importerDesc.setText(i.getDescription());
		forceKey.setEnabled(i.allowForceKey());
		
		propPanel.removeAll();
		Box b = new Box(BoxLayout.Y_AXIS);
		b.add(Box.createHorizontalStrut(500));
		propPanel.add(b);
		List<FTProp> myprops = i.getPropertyList();
		dptab.setEnabledAt(1, false);
		for(FTProp myprop: myprops) {
			b.add(DirectoryTable.getPropPanel(propPanel, myprop));
			dptab.setEnabledAt(1, true);
		}
	}
	
	ImportPanel(DirectoryTable dt) {
		parent = dt;
		JPanel ip = addBorderPanel("Import Type");
		JTabbedPane tabs = new JTabbedPane();
		ip.add(tabs, BorderLayout.CENTER);
		
		JPanel ipFile = new JPanel(new BorderLayout());
		Box fileBox = new Box(BoxLayout.Y_AXIS);
		ipFile.add(fileBox, BorderLayout.CENTER);
		tabs.add(ipFile, "File Import");
		
		ipGen = new JPanel(new BorderLayout());
		JPanel genp = new JPanel(new GridLayout(0, 2));
		ipGen.add(genp, BorderLayout.NORTH);
		tabs.add(ipGen, "Auto-Generate Unique Keys");
		
		JPanel ipWeb = new JPanel();
		tabs.add(ipWeb, "Web Harvest");
		tabs.setEnabledAt(2, false);
		
		JPanel ttp = new JPanel();
		fileBox.add(ttp);
		fsc = new FileSelectChooser(parent.frame, "Select file to Import", parent.preferences, "IMPORT", "");
		ttp.setBorder(BorderFactory.createTitledBorder("File to Import"));
		ttp.add(fsc);
		JButton jb = new JButton("Recent");
		jb.addActionListener(
				new ActionListener(){
					@SuppressWarnings("unchecked")
					public void actionPerformed(ActionEvent arg0) {
						ArrayList<File> recentrev = (ArrayList<File>)parent.recentImport.clone();
						Collections.reverse(recentrev);
						File o = (File)JOptionPane.showInputDialog(
							parent.frame,
							"Select a recently imported file",
							"Recent Imports",
							JOptionPane.INFORMATION_MESSAGE,
							null,
							recentrev.toArray(),
							null
						);
						if (o != null) {
							parent.importPanel.fsc.tf.setText(o.getAbsolutePath());
						}
					}
				}
			);
		ttp.add(jb);
		JPanel p1 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		fileBox.add(p1);
		p1 = new JPanel();
		p1.setBorder(BorderFactory.createTitledBorder("File Import Action"));
		String importrule = parent.preferences.get("import-rule", "");
		importers = new JComboBox<Importer>(parent.importerRegistry);
		for(int i=0; i<importers.getItemCount(); i++){
			Importer im = (Importer)importers.getItemAt(i);
			if (im.toString().equals(importrule)) {
				importers.setSelectedIndex(i);
				break;
			}
		}
		importers.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				updateDesc();
			}
		});
		p1.add(importers);
		fileBox.add(p1, BorderLayout.SOUTH);
		JPanel dp = new JPanel();
		fileBox.add(dp, BorderLayout.SOUTH);
		dptab = new JTabbedPane();
		dp.add(dptab);
		importerDesc = new JTextArea(9,60);
		importerDesc.setMargin(new Insets(10,10,10,10));
		importerDesc.setLineWrap(true);
		importerDesc.setWrapStyleWord(true);
		importerDesc.setBackground(this.getBackground());
		importerDesc.setEditable(false);
		dptab.add(importerDesc, "Importer Description");
		propPanel = new JPanel();
		JScrollPane sp = new JScrollPane(propPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sp.setPreferredSize(new Dimension(parent.criteriaPanel.propFilter.getWidth()-20, 220));
		dptab.add(sp,"Importer Properties");
		JPanel dpadv = new JPanel();
		dptab.add(dpadv, "Import Options");
		forceKey = new JCheckBox("Force Unique Keys");
		dpadv.add(forceKey);
		jb = new JButton("Import File");
		jb.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					File f = new File(fsc.tf.getText());
					if (f.exists()) {
						Importer imp = (Importer)parent.importPanel.importers.getSelectedItem();
						try {
						    InitializationStatus iStat = imp.initValidate(f);
						    if (iStat.hasMessage()) {
						        if (iStat.hasFailTest()) {
	                                JOptionPane.showMessageDialog(parent.frame, iStat.getMessage(), "Property Error - Cannot Run Task", JOptionPane.ERROR_MESSAGE);						            
						        } else {
                                    JOptionPane.showMessageDialog(parent.frame, iStat.getMessage(), "Property Warning", JOptionPane.WARNING_MESSAGE);                                  
						        }
						    }
						    if (!iStat.hasFailTest()) {
	                            parent.importFile(imp, f);						        
						    }
						} catch (IOException e) {
							JOptionPane.showMessageDialog(parent.frame, e.getMessage()+": "+f.getAbsolutePath());
						}
						parent.recentImport.remove(f);
						parent.recentImport.add(f);
						for(int i=0; ((i<parent.recentImport.size())&&(i<20));i++){
							parent.setPreference("recentImport"+i,parent.recentImport.get(parent.recentImport.size()-i-1).getAbsolutePath());
						}	
						parent.setPreference("import-rule", imp.toString());
					}
				}
			}
		);
		fileBox.add(jb);
		updateDesc();
		
		NumberFormat nf = NumberFormat.getIntegerInstance();
		nf.setGroupingUsed(false);
		genp.add(new JLabel("Prefix"));
		prefix = new JTextField(25);
		genp.add(prefix);
		genp.add(new JLabel("Start Number"));
		start = new JFormattedTextField(nf);
		start.setColumns(8);
		genp.add(start);
		genp.add(new JLabel("End Number"));
		end = new JFormattedTextField(nf);
		end.setColumns(8);
		genp.add(end);
		genp.add(new JLabel("Num Digits"));
		Object[] objs = {"No Padding",2,3,4,5,6,7,8};
		pad = new JComboBox<Object>(objs);
		genp.add(pad);
		genp.add(new JLabel("Suffix"));
		suffix = new JTextField(25);
		genp.add(suffix);
		genp.add(new JLabel(""));
		jb = new JButton("Auto-Generate Keys for Analysis");
		genp.add(jb);
		jb.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				TreeMap<String,Stats> types = new TreeMap<String,Stats>();
				Long startval = (Long)start.getValue();
				Long endval = (Long)end.getValue();
				
				if ((startval == null)||(endval==null)) return;
				
				for(long i=startval.intValue(); i<=endval.intValue(); i++){
					NumberFormat nf = NumberFormat.getIntegerInstance();
					nf.setGroupingUsed(false);
					if (pad.getSelectedItem() instanceof Integer) {
					  nf.setMinimumIntegerDigits((Integer)pad.getSelectedItem());
					}
					String key = prefix.getText() + nf.format(i) + suffix.getText();
					Stats stats = Stats.Generator.INSTANCE.create(Stats.getDefaultStatsConfig(), key);
					types.put(key, stats);
				}
				String s = "Generated "+(++parent.summaryCount);
				parent.showSummary(s,"Generated Sequence", Stats.getDefaultStatsConfig(), types, true, "");
			}
		});
	}
}
