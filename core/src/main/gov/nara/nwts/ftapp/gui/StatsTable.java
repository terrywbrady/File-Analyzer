package gov.nara.nwts.ftapp.gui;

import gov.nara.nwts.ftapp.stats.Money;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * Helper class that displays the results of a File Test action within a table; the generated table will be presented with custom column headers and custom column filters.
 * @author TBrady
 *
 */
class StatsTable {
	JTable jt;
	MyStatsTableModel tm;
	StatsItemConfig details;
	Pattern patt;
	Vector<JComboBox<Object>> filters;
	TableRowSorter<TableModel> sorter;
	ArrayList<String>noExport;
	DirectoryTable dt;

	class MyStatsTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public int getColumnCount() {
			return details.size();
		}

		public Class<?> getColumnClass(int col) {
			return (Class<?>) details.get(col).myclass;
		}
		
	};

	StatsTable(StatsItemConfig details, TreeMap<String, Stats> mystats, DirectoryTable dt) {
		patt = Pattern.compile("\\s\\s+");
		tm = new MyStatsTableModel();
		this.details = details;
		this.dt = dt;
		noExport = new ArrayList<String>();
		filters = new Vector<JComboBox<Object>>();
		for (Iterator<String> i = mystats.keySet().iterator(); i.hasNext();) {
			String s = i.next();
			Vector<Object> v = new Vector<Object>();
			v.add(s);
			for (Iterator<Object> j = mystats.get(s).getVals().iterator(); j
					.hasNext();) {
				v.add(j.next());
			}
			tm.addRow(v);
		}
		jt = new JTable(tm);

		sorter = new TableRowSorter<TableModel>(tm);
		jt.setRowSorter(sorter);
		jt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		setColumns();

		jt.setPreferredScrollableViewportSize(new Dimension(500, 300));
		for (StatsItem o : details) {
			if (o.values != null) {
				ArrayList<Object> list = new ArrayList<Object>();
				list.add(o.header);
				for(Object obj: o.values) {
					list.add(obj);
				}
				JComboBox<Object> cb = new JComboBox<Object>(list.toArray());
				cb.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						RowFilter<TableModel, Integer> rf = new RowFilter<TableModel, Integer>() {
							public boolean include(
									Entry<? extends TableModel, ? extends Integer> arg0) {
								for (int i = 0; i < arg0.getValueCount(); i++) {
									Object o = arg0.getValue(i);
									if (o == null)
										continue;
									JComboBox<Object> cb = filters.get(i);
									if (cb == null)
										continue;
									if (cb.getSelectedIndex() == 0)
										continue;
									if (!cb.getSelectedItem().equals(o)) {
										if (!cb.getSelectedItem()
												.toString().equals(
														o.toString())) {
											return false;
										}
									}
								}
								return true;
							}
						};
						StatsTable.this.sorter.setRowFilter(rf);
						StatsTable.this.dt.summaryPanel.setFilterNote(jt.getRowCount(), tm.getRowCount());
					}
				});
				filters.add(cb);
			} else {
				filters.add(null);
			}
			
			if (!o.export) {
				noExport.add((String)o.header);				
			}
		}
	}

	void setColumns() {
		JTableHeader jth = jt.getTableHeader();
		jth.setReorderingAllowed(true);
		TableColumnModel tcm = jt.getColumnModel();
		TableColumn tc;
		for (int i = 0; i < details.size(); i++) {
			tc = tcm.getColumn(i);
			int cw = (Integer) details.get(i).width;
			tc.setPreferredWidth(cw);
			tc.setHeaderValue(details.get(i).header);

			if (i != 0) {
				tc.setCellRenderer(new DefaultTableCellRenderer() {
					private static final long serialVersionUID = 1L;

					public void setValue(Object value) {
						if (value == null)
							return;
						if (value instanceof Long) {
							long v = (Long) value;
							setText(DirectoryTable.nf.format(v));
							setHorizontalAlignment(JLabel.RIGHT);
						} else if (value instanceof Integer) {
							int v = (Integer) value;
							setText(DirectoryTable.nf.format(v));
							setHorizontalAlignment(JLabel.RIGHT);
                        } else if (value instanceof Float) {
                            float v = (Float) value;
                            setText(DirectoryTable.ndurf.format(v));
                            setHorizontalAlignment(JLabel.RIGHT);
                        } else if (value instanceof Money) {
                            Money v = (Money) value;
                            setText(v.toString());
                            setHorizontalAlignment(JLabel.RIGHT);
                        } else if (value instanceof Double) {
                            double v = (Double) value;
                            setText(DirectoryTable.ndurf.format(v));
                            setHorizontalAlignment(JLabel.RIGHT);
						} else {
							String s = value.toString();
							if (s.contains("\n")) {
								s = "<html><body>" + s + "</body></html>";
								s = patt.matcher(s).replaceAll("<br/>");
								s = s.replaceAll("\n", "<br/>");
								this.setToolTipText(s);
							}else if (s.contains("<br/>")) {
								s = "<html><body>" + s + "</body></html>";
								this.setToolTipText(s);
							}else {
								this.setToolTipText(null);
							}
							setText(value.toString());
						}
					}

				});

			}
		}
	}

}
