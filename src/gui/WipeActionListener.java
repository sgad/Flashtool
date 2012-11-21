package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import flashsystem.Bundle;

public class WipeActionListener implements ActionListener {

	JCheckBox _box;
	String _categ;
	firmSelect _frame;
	
	public WipeActionListener(firmSelect frame, JCheckBox box, String categ) {
		_box = box;
		_categ = categ;
		_frame = frame;
	}

	public void actionPerformed(ActionEvent arg0) {
		try {
			_frame.setCategEnabled(_categ, _box.isSelected());
		}
		catch (Exception e) {}
	}

}
