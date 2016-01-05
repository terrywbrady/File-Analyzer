package gov.nara.nwts.ftapp.ftprop;

import java.text.NumberFormat;

import javax.swing.JFormattedTextField;

import gov.nara.nwts.ftapp.FTDriver;

public class FTPropInt extends FTPropString {
	public NumberFormat nf;
	public FTPropInt(FTDriver ft, String prefix, String name,
			String shortname, String description, Object def) {
		super(ft, prefix, name, shortname, description, def);
	}

	public void createTextField() {
		nf = NumberFormat.getIntegerInstance();
		nf.setGroupingUsed(false);
		tf = new JFormattedTextField(nf);
		tf.setText(this.def.toString());
	}
	
	public NumberFormat getNumberFormat() {
		return nf;
	}
	
	public Integer getIntValue(int def) {
	    try {
            return Integer.parseInt(getValue());
        } catch (NumberFormatException e) {
            return def;
        }
	}
}
