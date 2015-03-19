package gov.nara.nwts.ftapp.stats;

import java.io.File;
import java.util.Vector;

import gov.nara.nwts.ftapp.filetest.FileTest;

/**
 * Base class for Stats objects containing a key and a variable lenght list of values.
 * @author TBrady
 *
 */
public class Stats {
	public static enum StatsItems implements StatsItemEnum {
		Type(StatsItem.makeStringStatsItem("Type"));
		
		StatsItem si;
		StatsItems(StatsItem si) {this.si=si;}
		public StatsItem si() {return si;}
	}

	public static enum Generator implements StatsGenerator {
		INSTANCE;
		public Stats create(StatsItemConfig config, String key) {return new Stats(config, key);}
		public Stats create(String key) {return create(Stats.details, key);}
	}
	
	//private to prevent use by subclass inner classes
	private static StatsItemConfig details = StatsItemConfig.create(StatsItems.class);
	
	public static StatsItemConfig getDefaultStatsConfig() {
		return details;
	}
	
	public StatsItem header;
	private Vector<Object> vals;
	public String key;
	
	public Stats(StatsItemConfig config, String key) {
		this.key = key;
		vals = new Vector<Object>();
		init(config);
	}

	public void setKeyVal(StatsItem si, Object val) {
		if (si == null) return;
		int index = si.getIndex();
		if ((index >= 0) && (vals.size() > index)) {
			vals.set(index,val);
		}		
	}
	
	public Object getKeyVal(StatsItem si, Object val) {
		if (si == null) return val;
		int index = si.getIndex();
		if ((index >= 0) && (vals.size() > index)) {
			return vals.get(index);
		}		
		return val;
	}
	public void setVal(StatsItemEnum eitem, Object val) {
		int index = eitem.si().getIndex();
		if ((index >= 0) && (vals.size() > index)) {
			vals.set(index,val);
		}
	}
	 
	public void sumVal(StatsItemEnum eitem, int val) {
		int index = eitem.si().getIndex();
		if ((index >= 0) && (vals.size() > index)) {
			Object obj = vals.get(index);
			if (obj == null) {
				vals.set(index,val);				
			} else if (obj instanceof Integer) {
				int n = (Integer)obj;
				n += val;
				vals.set(index, n);
			} else if (obj instanceof Float) {
				float n = (Float)obj;
				n += val;
				vals.set(index, n);
			} else if (obj instanceof Long) {
				long n = (Long)obj;
				n += val;
				vals.set(index, n);
            } else if (obj instanceof Money) {
                vals.set(index, ((Money)obj).moneySum(val));
			}
		}
	}
	 
	public void sumVal(StatsItemEnum eitem, float val) {
		int index = eitem.si().getIndex();
		if ((index >= 0) && (vals.size() > index)) {
			Object obj = vals.get(index);
			if (obj == null) {
				vals.set(index,val);				
			} else if (obj instanceof Float) {
				float n = (Float)obj;
				n += val;
				vals.set(index, n);
			}
		}
	}
	 
	public void sumVal(StatsItemEnum eitem, long val) {
		int index = eitem.si().getIndex();
		if ((index >= 0) && (vals.size() > index)) {
			Object obj = vals.get(index);
			if (obj == null) {
				vals.set(index,val);				
			} else if (obj instanceof Long) {
				long n = (Long)obj;
				n += val;
				vals.set(index, n);
			}
		}
	}
	 
    public void sumVal(StatsItemEnum eitem, Money m) {
        int index = eitem.si().getIndex();
        if ((index >= 0) && (vals.size() > index)) {
            Object obj = vals.get(index);
            if (obj == null) {
                vals.set(index,m);                
            } else if (obj instanceof Money) {
                vals.set(index, ((Money)obj).moneySum(m));
            }
        }
    }

    public void appendVal(StatsItemEnum eitem, String val) {
		appendVal(eitem, val, "");
	}
	
	public void appendVal(StatsItemEnum eitem, String val, String sep) {
		int index = eitem.si().getIndex();
		if ((index >= 0) && (vals.size() > index)) {
			Object obj = vals.get(index);
			if (obj == null) {
				vals.set(index,val);				
			} else if (obj instanceof String) {
				String s = (String)obj;
				if (!s.isEmpty() && !val.isEmpty()) s += sep;
				s += val;
				vals.set(index, s);
			}
		}
	}

	public void appendKeyVal(StatsItem si, Object val) {
		if (si == null) return;
		int index = si.getIndex();
		if ((index >= 0) && (vals.size() > index)) {
			Object obj = vals.get(index);
			if (obj == null) {
				vals.set(index,val);				
			} else if (obj instanceof String) {
				String s = (String)obj;
				s += val;
				vals.set(index, s);
			}
		}
	}

	
	public Vector<Object> getVals() {
		return vals;
	}
	
	public Object getVal(StatsItemEnum eitem) {
		return getVal(eitem, null);
	}
	
	public Long getLongVal(StatsItemEnum eitem) {
		return (Long)getVal(eitem, null);
	}

    public Money getMoneyVal(StatsItemEnum eitem) {
        return (Money)getVal(eitem, new Money());
    }

	public Integer getIntVal(StatsItemEnum eitem) {
		return (Integer)getVal(eitem, null);
	}

	public String getStringVal(StatsItemEnum eitem) {
		return getStringVal(eitem, "");
	}
	public String getStringVal(StatsItemEnum eitem, String def) {
		int index = eitem.si().getIndex();
		if ((index >= 0) && (vals.size() > index)) {
			if (vals.get(index) == null) return def;
			return vals.get(index).toString();
		}
		return def;
	}

	public Object getVal(StatsItemEnum eitem, Object def) {
		int index = eitem.si().getIndex();
		if ((index >= 0) && (vals.size() > index)) {
			return vals.get(index);
		}
		return def;
	}
	 
	public void init(StatsItemConfig config) {
		boolean first = true;
		vals.clear();
		for(StatsItem item: config) {
			if (first) {
				first = false;
			} else {
				vals.add(item.initVal);
			}
		}
	}
	
	public Object compute(File f, FileTest fileTest) {
		Object o = fileTest.fileTest(f);
		return o;
	}
}
