package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

import flashsystem.BytesUtil;
import flashsystem.HexDump;
import flashsystem.io.USBFlash;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.system.Device;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class CustomFlashUI extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextPane textPaneReply;
	private JTextPane textPaneCommand;

	/**
	 * Create the dialog.
	 */
	public CustomFlashUI() {
		setTitle("Raw I/O Module");
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(35dlu;default)"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("max(61dlu;default)"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),}));
		{
			JButton btnNewButton = new JButton("Open Device");
			btnNewButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					try {
						USBFlash.open(Device.getLastConnected(false).getPid());
					}
					catch (IOException ioe) {
					}
				}
			});
			contentPanel.add(btnNewButton, "2, 2, left, center");
		}
		{
			JLabel lblNewLabel = new JLabel("Array to send :");
			contentPanel.add(lblNewLabel, "2, 4, default, center");
		}
		{
			JButton btnNewButton_1 = new JButton("Send");
			btnNewButton_1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String packet = textPaneCommand.getText().trim();
					if (packet.contains(" ")) packet.replaceAll(" ", "");
					Byte[] b1 = BytesUtil.getBytes(textPaneCommand.getText());
					byte[] b2 = new byte[b1.length];
					for (int i=0;i<b1.length;i++)
						b2[i] = b1[i];
					try {
						USBFlash.write(b2);
					}
					catch (Exception e1) {
					}
				}
			});
			contentPanel.add(btnNewButton_1, "4, 4");
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			contentPanel.add(scrollPane, "2, 6, 7, 1, fill, fill");
			{
				textPaneCommand = new JTextPane();
				scrollPane.setViewportView(textPaneCommand);
			}
		}
		{
			JButton btnReadReply = new JButton("Read Reply");
			btnReadReply.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						USBFlash.readReply();
						textPaneReply.setText(HexDump.toHex(USBFlash.getLastReply()));
					}
					catch (Exception e1) {
					}
					
				}
			});
			contentPanel.add(btnReadReply, "2, 8");
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			contentPanel.add(scrollPane, "2, 10, 7, 1, fill, fill");
			{
				textPaneReply = new JTextPane();
				scrollPane.setViewportView(textPaneReply);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Close");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
	}

}
