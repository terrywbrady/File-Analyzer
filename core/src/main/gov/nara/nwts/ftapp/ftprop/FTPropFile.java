package gov.nara.nwts.ftapp.ftprop;

import java.io.File;

import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.gui.FileSelectChooser;

import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * File Test Property object for string values
 * @author TBrady
 *
 */

public class FTPropFile extends DefaultFTProp {
	FileSelectChooser chooser;
	public FTPropFile(FTDriver ft, String prefix, String name, String shortname, String description, Object def) {
		super(ft, prefix, name, shortname, description, def);
		init();
		
		chooser = new FileSelectChooser(ft.getFrame(), name, this.def.toString());
		chooser.tf.getDocument().addDocumentListener(new DocumentListener(){
			public void changedUpdate(DocumentEvent arg0) {
				if (FTPropFile.this.ft.hasPreferences()) {
					FTPropFile.this.ft.setPreference(getPrefString(), chooser.tf.getText());
				}
			}

			public void insertUpdate(DocumentEvent arg0) {
				if (FTPropFile.this.ft.hasPreferences()) {
					FTPropFile.this.ft.setPreference(getPrefString(), chooser.tf.getText());
				}
			}

			public void removeUpdate(DocumentEvent arg0) {
				if (FTPropFile.this.ft.hasPreferences()) {
					FTPropFile.this.ft.setPreference(getPrefString(), chooser.tf.getText());
				}
			}

		});
	}

	public JComponent getEditor() {
		return chooser;
	}

	public Object getValue() {
		return chooser.tf.getText();
	}
	
	public File getFile() {
		String s = (String)getValue();
		if (s == null) return null;
		if (s.isEmpty()) return null;

		return new File(s);
	}

	public void setValue(Object obj) {
		chooser.tf.setText(obj.toString());
	}
	public Object validate(String s) {
		if (s == null) s = "";
		return getValue();
	}


}
