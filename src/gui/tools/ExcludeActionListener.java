package gui.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import flashsystem.Bundle;
import gui.firmSelect;

public class ExcludeActionListener implements ActionListener {

	JCheckBox _box;
	String _categ;
	firmSelect _frame;
	
	public ExcludeActionListener(firmSelect frame, JCheckBox box, String categ) {
		_box = box;
		_categ = categ;
		_frame = frame;
	}

	public void actionPerformed(ActionEvent arg0) {
		try {
			_frame.setCategEnabled(_categ, !_box.isSelected());
		}
		catch (Exception e) {}
	}

}
