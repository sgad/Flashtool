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

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;

public class SinEditorUI extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField textSin;
	private JTextField textPartition;
	private JTextField textSpare;
	private SinFile sin;


	/**
	 * Create the dialog.
	 */
	public SinEditorUI() {
		setTitle("Sin Editor");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 259);
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
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		{
			JLabel lblSinFil = new JLabel("Sin File :");
			contentPanel.add(lblSinFil, "2, 2, 3, 1");
		}
		{
			textSin = new JTextField();
			contentPanel.add(textSin, "2, 4, 3, 1, fill, default");
			textSin.setColumns(10);
		}
		{
			JButton button = new JButton("...");
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					String file=chooseSin();
					if (!file.equals("ERROR")) {
						try {
						sin = new SinFile(file);
						System.out.println(sin.getIdent());
						textSin.setText(file);
						String p = HexDump.toHex(sin.getPartitionInfo()).replaceAll(", ", "");
						textPartition.setText(p.substring(1, p.length()-1));
						textSpare.setText(HexDump.toHex(sin.getSpare()));
						}
						catch (Exception e) {}
					}
				}
			});
			contentPanel.add(button, "6, 4");
		}
		{
			JLabel lblPartitionInfo = new JLabel("Partition Info :");
			contentPanel.add(lblPartitionInfo, "2, 6, 3, 1");
		}
		{
			textPartition = new JTextField();
			contentPanel.add(textPartition, "2, 8, 3, 1, fill, default");
			textPartition.setColumns(10);
		}
		{
			JLabel lblSpareInfo = new JLabel("Spare Info :");
			contentPanel.add(lblSpareInfo, "2, 10, 3, 1");
		}
		{
			textSpare = new JTextField();
			contentPanel.add(textSpare, "2, 12, 3, 1, fill, default");
			textSpare.setColumns(10);
		}
		{
			JButton btnDumpData = new JButton("Dump data");
			btnDumpData.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					try {
						sin.dumpImage();
					}
					catch (Exception e) {
						MyLogger.getLogger().error(e.getMessage());
					}
				}
			});
			contentPanel.add(btnDumpData, "2, 14, center, center");
		}
		{
			JButton btnNewButton = new JButton("Unpack data");
			contentPanel.add(btnNewButton, "4, 14, center, default");
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

	public String chooseSin() {
		JFileChooser chooser = new JFileChooser(new java.io.File(".")); 

		FileFilter ff = new FileFilter(){
			public boolean accept(File f){
				if(f.isDirectory()) return true;
				else if(f.getName().endsWith(".sin")) return true;
				else return false;
			}
			public String getDescription(){
				return "*.sin";
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
