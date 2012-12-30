package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import flashsystem.Bundle;
import org.lang.Language;
import org.logger.MyLogger;
import org.system.DeviceEntry;
import org.system.Devices;
import org.system.OS;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Enumeration;
import java.util.Properties;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;



public class deviceSelectGui extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private String result="";
	private Bundle _bundle;
	private JTable tableDevices;
	private DefaultTableModel modelDevices;

	public void fillTable() {
    	modelDevices = new DefaultTableModel();
    	modelDevices.addColumn("Id");
    	modelDevices.addColumn("Name");
    	tableDevices.setModel(modelDevices);
    	tableDevices.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    	tableDevices.getColumn("Id").setPreferredWidth(50);
    	tableDevices.getColumn("Name").setPreferredWidth(170);
    	Enumeration<Object> e = Devices.listDevices(false);
    	while (e.hasMoreElements()) {
    		DeviceEntry entry = Devices.getDevice((String)e.nextElement());
    		modelDevices.addRow(new String[]{entry.getId(),entry.getName()});
    		tableDevices.setRowSelectionInterval(0, 0);
    	}
    	tableDevices.getRowSorter().toggleSortOrder(0);
	}

	public void fillTable(Properties list) {
    	modelDevices = new DefaultTableModel();
    	modelDevices.addColumn("Id");
    	modelDevices.addColumn("Name");
    	tableDevices.setModel(modelDevices);
    	tableDevices.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    	tableDevices.getColumn("Id").setPreferredWidth(50);
    	tableDevices.getColumn("Name").setPreferredWidth(170);
    	Enumeration<Object> e = list.keys();
    	while (e.hasMoreElements()) {
    		DeviceEntry entry = Devices.getDevice((String)e.nextElement());
    		modelDevices.addRow(new String[]{entry.getId(),entry.getName()});
    		tableDevices.setRowSelectionInterval(0, 0);
    	}
    	tableDevices.getRowSorter().toggleSortOrder(0);
	}

	/**
	 * Create the dialog.
	 */
	public deviceSelectGui(Bundle bundle) {
		_bundle = bundle;
		setName("deviceSelectGui");
		setModal(true);
		setTitle("Device Selection");
		if (OS.getName().startsWith("mac"))
			setBounds(100, 100, 294, 322);
		else if (OS.getName().startsWith("linux"))
			setBounds(100, 100, 294, 322);
		else
			setBounds(100, 100, 240, 322);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("max(131dlu;default):grow"),},
			new RowSpec[] {
				RowSpec.decode("default:grow"),}));
		{
			JScrollPane scrollPane = new JScrollPane();
			contentPanel.add(scrollPane, "1, 1, fill, fill");
			{
				tableDevices = new JTable() {
					public boolean getScrollableTracksViewportWidth()
		            {
		                return getPreferredSize().width < getParent().getWidth();
		            }
				};
				tableDevices.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
				tableDevices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				tableDevices.setAutoCreateRowSorter(true);
				scrollPane.setViewportView(tableDevices);
			}
		}

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				result="";
				dispose();
			}
		});

		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						result=(String)modelDevices.getValueAt(getSelectedRow(tableDevices), 0);
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setName("cancelButton");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						result ="";
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		setLanguage();
	}

	public boolean isSelected() {
		fillTable();
		setVisible(!_bundle.hasLoader());
		if (!_bundle.hasLoader() && (result.length()>0)) {
			MyLogger.getLogger().debug("Choosed loader from device selection :"+result);
			if (Devices.getDevice(result).hasUnlockedLoader()) {
				LoaderSelectGUI sel = new LoaderSelectGUI();
				String resultL = sel.getResult();
				if (resultL.equals("L")) 
					_bundle.setLoader(new File(Devices.getDevice(result).getLoader()));
				else 
					_bundle.setLoader(new File(Devices.getDevice(result).getLoaderUnlocked()));
			}
			else
				_bundle.setLoader(new File(Devices.getDevice(result).getLoader()));
		}
		return ((result.length()>0) || _bundle.hasLoader());
	}

	public String getDevice() {
		fillTable();
		setVisible(true);
		return result;
	}

	public String getDevice(Properties list) {
		if (list.size()>1) {
			fillTable(list);
			MyLogger.getLogger().warn("Your device has been matched with more than one device");
		}
		else
			fillTable();
		setVisible(true);
		return result;
	}

	public String getDeviceFromList(Properties list) {
		if (list==null) fillTable();
		else {
			if (list.size()>0) {
				fillTable(list);
			}
			else
				fillTable();
		}
		setVisible(true);
		return result;
	}

	public void setLanguage() {
		Language.translate(this);
	}

	private int getSelectedRow(JTable table) {
		return table.getRowSorter().convertRowIndexToModel(table.getSelectedRow());
	}

}
