package gov.nara.nwts.ftapp.stats;

public class StatsItem {
	public Class<?> myclass;
	public String header;
	public int width;
	public Object[] values;
	public boolean export = true;
	public Object initVal;
	private int index;
	private boolean makeFilter;
		
	public StatsItem(Class<?> myclass, String header, int width, Object[] values, boolean export, Object initVal) {
		this.myclass = myclass;
		this.header = header;
		this.width = width;
		this.values = values;
		this.export = export;
		this.initVal = initVal;
	}
	
	public int getIndex() {return index;}
	void setIndex(int index) {this.index = index;}
	
	public static StatsItem makeStatsItem(Class<?> myclass, String header, int width) {
		return new StatsItem(myclass, header, width, null, true, null);
	}
	
	public static StatsItem makeStringStatsItem(String header, int width) {
		return new StatsItem(String.class, header, width, null, true, "");
	}
	
	public static StatsItem makeStringStatsItem(String header) {
		return new StatsItem(String.class, header, 100, null, true, "");
	}
	
	public static StatsItem makeLongStatsItem(String header) {
		return new StatsItem(Long.class, header, 100, null, true, (long)0);
	}

	public static StatsItem makeIntStatsItem(String header) {
		return new StatsItem(Integer.class, header, 100, null, true, (int)0);
	}

	public static StatsItem makeFloatStatsItem(String header) {
		return new StatsItem(Float.class, header, 100, null, true, (float)0);
	}

    public static StatsItem makeMoneyStatsItem(String header) {
        return new StatsItem(Money.class, header, 100, null, true, new Money());
    }

	public StatsItem setWidth(int width) {
		this.width = width;
		return this;
	}
	
	public StatsItem setExport(boolean export) {
		this.export = export;
		return this;
	}
	
	public StatsItem makeFilter(boolean b) {
		this.makeFilter = b;
		return this;
	}
	
	public boolean getFilter() {return makeFilter;}
	
	public StatsItem setValues(Object[] values) {
		this.values = values;
		return this;
	}

	public StatsItem setClass(Class<?> myclass) {
		this.myclass = myclass;
		return this;
	}
	
	public StatsItem setHeader(String header) {
		this.header = header;
		return this;
	}
	
	public StatsItem setInitVal(Object initVal) {
		this.initVal = initVal;
		return this;
	}

	
	public static <T extends Enum<T>> StatsItem makeEnumStatsItem(Class<T> eclass, String name) {
		return new StatsItem(eclass, name, 100, eclass.getEnumConstants(), true, eclass.getEnumConstants()[0]);
	}

	public static <T extends Enum<T>> StatsItem makeEnumStatsItem(Class<T> eclass, String name,  Enum<T> initItem) {
		return new StatsItem(eclass, name, 100, eclass.getEnumConstants(), true, initItem);
	}

	public static <T extends Enum<T>> StatsItem makeEnumStatsItem(Class<T> eclass) {
		return makeEnumStatsItem(eclass, eclass.getName());
	}

	public Object[] array() {
		Object[] obj = new Object[5];
		obj[0] = myclass;
		obj[1] = header;
		obj[2] = width;
		obj[3] = values;
		obj[4] = export;
		return obj;
	}

}
