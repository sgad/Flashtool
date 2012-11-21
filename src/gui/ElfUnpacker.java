package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

import flashsystem.HexDump;
import flashsystem.SinFile;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.logger.MyLogger;
import org.system.Elf;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;

public class ElfUnpacker extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField textElf;
	private JTextField textParts;
	private JButton btnDumpData;
	private Elf elfobj;


	/**
	 * Create the dialog.
	 */
	public ElfUnpacker() {
		setTitle("ELF Unpacker");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 220);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		{
			JLabel lblElfFile = new JLabel("Elf File :");
			contentPanel.add(lblElfFile, "2, 2, 3, 1");
		}
		{
			textElf = new JTextField();
			contentPanel.add(textElf, "2, 4, 3, 1, fill, default");
			textElf.setColumns(10);
		}
		{
			JButton button = new JButton("...");
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					String file=chooseElf();
					if (!file.equals("ERROR")) {
						try {
							if (file.toLowerCase().endsWith("sin")) {
								SinFile sin = new SinFile(file);
								if (sin.getDataType().equals("elf")) {
									sin.dumpImage();
									file = sin.getImageFileName();
									textElf.setText(file);
									elfobj = new Elf(new File(file));
									textParts.setText(Integer.toString(elfobj.getNumPrograms()));
									btnDumpData.setEnabled(true);
									MyLogger.getLogger().info("You can now press the Unpack button to get the elf data content");
								}
								else {
									textElf.setText("");
									textParts.setText("");
									btnDumpData.setEnabled(false);
									MyLogger.getLogger().error(sin.getShortFileName()+" does not contain elf data");
								}
							}
							else {
								textElf.setText(file);
								elfobj = new Elf(new File(file));
								textParts.setText(Integer.toString(elfobj.getNumPrograms()));
								btnDumpData.setEnabled(true);
							}
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			});
			contentPanel.add(button, "6, 4");
		}
		{
			JLabel lblPartitionInfo = new JLabel("Number of parts :");
			contentPanel.add(lblPartitionInfo, "2, 6, 3, 1");
		}
		{
			textParts = new JTextField();
			contentPanel.add(textParts, "2, 8, 3, 1, fill, default");
			textParts.setColumns(10);
		}
		{
			btnDumpData = new JButton("Unpack");
			btnDumpData.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					try {
						elfobj.unpack();
					}
					catch (Exception e) {
						MyLogger.getLogger().error(e.getMessage());
					}
				}
			});
			btnDumpData.setEnabled(false);
			contentPanel.add(btnDumpData, "2, 10, center, center");
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Close");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
	}

	public String chooseElf() {
		JFileChooser chooser = new JFileChooser(new java.io.File(".")); 

		FileFilter ff = new FileFilter(){
			public boolean accept(File f){
				if(f.isDirectory()) return true;
				else if(f.getName().endsWith(".elf")) return true;
				else if(f.getName().endsWith(".sin")) return true;
				else return false;
			}
			public String getDescription(){
				return "*.elf *.sin";
			}
		};
		 
		chooser.removeChoosableFileFilter(chooser.getAcceptAllFileFilter());
		chooser.setFileFilter(ff);
		
	    chooser.setDialogTitle("Choose sin file)");
	    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    //chooser.setFileFilter(newkernelimgFileFilter);
	    //
	    // disable the "All files" option.
	    //
	    chooser.setAcceptAllFileFilterUsed(false);
	    //    
	    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
	    	return chooser.getSelectedFile().getAbsolutePath();
	    }
	    return "ERROR";
	}

}
