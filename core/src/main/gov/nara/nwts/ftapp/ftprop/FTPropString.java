package gov.nara.nwts.ftapp.ftprop;

import gov.nara.nwts.ftapp.FTDriver;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * File Test Property object for string values
 * @author TBrady
 *
 */

public class FTPropString extends DefaultFTProp {
	JTextField tf;
	public FTPropString(FTDriver ft, String prefix, String name, String shortname, String description, Object def) {
		super(ft, prefix, name, shortname, description, def);
		init();
		createTextField();
		tf.getDocument().addDocumentListener(new DocumentListener(){
			public void changedUpdate(DocumentEvent arg0) {
				if (FTPropString.this.ft.hasPreferences()) {
					FTPropString.this.ft.setPreference(getPrefString(), tf.getText());
				}
			}

			public void insertUpdate(DocumentEvent arg0) {
				if (FTPropString.this.ft.hasPreferences()) {
					FTPropString.this.ft.setPreference(getPrefString(), tf.getText());
				}
			}

			public void removeUpdate(DocumentEvent arg0) {
				if (FTPropString.this.ft.hasPreferences()) {
					FTPropString.this.ft.setPreference(getPrefString(), tf.getText());
				}
			}
		});
	}

	public void createTextField() {
		tf = new JTextField(this.def.toString());		
	}
	
	public JComponent getEditor() {
		return tf;
	}

	public String getValue() {
		return tf.getText();
	}

	public void setValue(Object obj) {
		tf.setText(obj.toString());
	}
	public Object validate(String s) {
		if (s == null) s = "";
		return getValue();
	}


}
