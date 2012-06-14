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
import java.io.IOException;


public class SinCreatorUI extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField textSin;
	private JTextField textPartition;
	private JTextField textSpare;
	private SinFile sin;
	private JTextField textData;


	/**
	 * Create the dialog.
	 */
	public SinCreatorUI(String sinfile, String part, String spare) {
		setTitle("Sin Creator");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 318);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
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
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		{
			JLabel lblSinFil = new JLabel("Sin FileName :");
			contentPanel.add(lblSinFil, "2, 2, 5, 1");
		}
		{
			textSin = new JTextField();
			contentPanel.add(textSin, "2, 4, 5, 1, fill, default");
			textSin.setColumns(10);
		}
		{
			JLabel lblPartitionInfo = new JLabel("Partition Info :");
			contentPanel.add(lblPartitionInfo, "2, 6, 5, 1");
		}
		{
			textPartition = new JTextField();
			contentPanel.add(textPartition, "2, 8, 5, 1, fill, default");
			textPartition.setColumns(10);
		}
		{
			JLabel lblSpareInfo = new JLabel("Spare Info :");
			contentPanel.add(lblSpareInfo, "2, 10, 5, 1");
		}
		{
			textSpare = new JTextField();
			contentPanel.add(textSpare, "2, 12, 5, 1, fill, default");
			textSpare.setColumns(10);
		}
		{
			JButton btnCreateSin = new JButton("Create sin");
			btnCreateSin.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					try {
						sin.dumpImage();
					}
					catch (Exception e) {
						MyLogger.getLogger().error(e.getMessage());
					}
				}
			});
			{
				JLabel lblContentType = new JLabel("Data File");
				contentPanel.add(lblContentType, "2, 14");
			}
			{
				textData = new JTextField();
				contentPanel.add(textData, "2, 16, 5, 1, fill, default");
				textData.setColumns(10);
			}
			contentPanel.add(btnCreateSin, "2, 18, center, center");
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
		textSin.setText(sinfile);
		textPartition.setText(part);
		textSpare.setText(spare);
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
