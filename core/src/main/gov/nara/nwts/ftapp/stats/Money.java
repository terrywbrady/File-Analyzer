package gov.nara.nwts.ftapp.stats;

import gov.nara.nwts.ftapp.gui.DirectoryTable;

import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Money implements Comparable<Money> {
    static Pattern pm = Pattern.compile("^(-?\\d+)\\.(\\d{0,2})\\d*$");
    static NumberFormat csvndurf = NumberFormat.getNumberInstance();
    static NumberFormat csvndurft = NumberFormat.getNumberInstance();
    
    static {
        csvndurf.setMinimumFractionDigits(2);
        csvndurf.setGroupingUsed(false);
        csvndurft.setMinimumFractionDigits(1);
        csvndurft.setGroupingUsed(false);
    }

    final Long cents;
    public Money() {
        this.cents = null;
    }
    
    public Money(double fcents) {
        this.cents = (long)(fcents * 100);
    }
    
    public Money(long cents) {
        this.cents = cents;
    }
    
    public Money(String val) throws NumberFormatException {
        if (val == null) {
            cents = null;
            return;
        }
        val = val.replaceAll("[\\$,\\s]","");
        Matcher m = pm.matcher(val);
        String s = "0";
        if (m.matches()) {
            s = m.group(1) + (m.group(2) + "00").substring(0,2);
        } else {
            s = val + "00";
        }
        Long tcents = null;
        try {
			tcents = Long.parseLong(s);
		} catch (Exception e) {
        } finally {
            cents = tcents;
        }
    }
    
    public String toString() {
        if (cents == null) return "N/A";
        return "$" + DirectoryTable.ndurf.format(cents / 100.0);
    }

    public String csvString() {
        if (cents == null) return "";
        return csvndurf.format(cents / 100.0);
    }

    public String csvStringTrim() {
        if (cents == null) return "";
        return csvndurft.format(cents / 100.0);
    }

    public int compareTo(Money m) {
        if (m == null) return 1;
        if (m.cents == null && this.cents == null) return 0;
        if (m.cents == null) return 1;
        if (this.cents == null) return -1;
        return this.cents.compareTo(m.cents);
    }
    
    public boolean isNullValue() {
        return this.cents == null;
    }
    
    public Money moneySum(Money m) {
        if (m.cents == null) return m;
        if (cents == null) {
            return new Money(m.cents);
        }
        return new Money(cents + m.cents);
    }
    
    public Money moneySum(int v) {
        if (cents == null) {
            return new Money(v);
        }
        return new Money(cents + v);
    }

    public Money moneySum(long v) {
        if (cents == null) {
            return new Money(v);
        }
        return new Money(cents + v);
    }
    
    public boolean equals(Money m) {
        if (m == null) return false;
        if (this.cents == null || m.cents == null) return false;
        return this.cents.equals(m.cents);
    }
    
    public void test() {
        System.out.printf("%15d\t%15s\t%15s\t", this.cents, this.toString(), this.csvString());
    }
    
    public static void test(String s) {
        new Money(s).test();
        System.out.println("\t\t"+s);
    }

    public static void test(long s) {
        new Money(s).test();
        System.out.println("\t\t"+s);
    }

    public static void test(double s) {
        new Money(s).test();
        System.out.println("\t\t"+s);
    }
    
    public static final void main(String[] args) {
        new Money().test();
        System.out.println();
        test(-1);
        test(-1.01);
        test(1);
        test(1.01);
        test(-101);
        test(101);
        test("1.");
        test("1.0");
        test("1.00");
        test("1.01");
        test("1.001");
        test("-1.01");
        test("123,456.789");
        test("$123,456.789");
        test("-$123,456.789");
        test("123456.789");
        test("123456");
    }
}
