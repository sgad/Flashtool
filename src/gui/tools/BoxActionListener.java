package gui.tools;

import gui.AskBox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BoxActionListener implements ActionListener {
	
	AskBox _box;
	String _result;
	
	public BoxActionListener(AskBox box,String result) {
		_box = box;
		_result = result;
	}
	
	public void actionPerformed(ActionEvent e) {
		_box.setResult(_result);
		_box.dispose();
	}
}
