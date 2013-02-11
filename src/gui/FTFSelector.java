package gui;

import flashsystem.Bundle;
import java.io.File;
import java.util.Properties;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.system.OS;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import gui.models.ContentContentProvider;
import gui.models.ContentLabelProvider;
import gui.models.Firmware;
import gui.models.FirmwareContentProvider;
import gui.models.FirmwareLabelProvider;
import gui.models.FirmwaresModel;

public class FTFSelector extends Dialog {

	protected Object result;
	protected Shell shlFirmwareSelector;
	private Properties hasCmd25 = new Properties();
	private String filename="";
	private Button btnOK=null;
	private TableViewer tableFirmwareViewer;
	private TableViewer tableContentViewer;
	private Table tableFirmware;
	private Table tableContent;
	private Text sourceFolder;
	private Bundle selected=null;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public FTFSelector(Shell parent, int style) {
		super(parent, style);
		setText("Firmware selector");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open(String pathname, String ftfname) {
		filename=ftfname;
		createContents(pathname, ftfname);
		shlFirmwareSelector.open();
		shlFirmwareSelector.layout();
		Display display = getParent().getDisplay();
		while (!shlFirmwareSelector.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents(String pathname, String ftfname) {
		shlFirmwareSelector = new Shell(getParent(), getStyle());
		shlFirmwareSelector.setSize(688, 486);
		shlFirmwareSelector.setText("Firmware Selector");
		shlFirmwareSelector.setLayout(new FormLayout());
		shlFirmwareSelector.addListener(SWT.Close, new Listener() {
		      public void handleEvent(Event event) {
					result = null;
					shlFirmwareSelector.dispose();
		      }
		    });
		Button btnCancel = new Button(shlFirmwareSelector, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				result = null;
				shlFirmwareSelector.dispose();
			}
		});
		FormData fd_btnCancel = new FormData();
		fd_btnCancel.bottom = new FormAttachment(100, -10);
		fd_btnCancel.right = new FormAttachment(100, -10);
		btnCancel.setLayoutData(fd_btnCancel);
		btnCancel.setText("Cancel");
		
		Composite compositeFirmware = new Composite(shlFirmwareSelector, SWT.NONE);
		compositeFirmware.setLayout(new FillLayout(SWT.HORIZONTAL));
		FormData fd_compositeFirmware = new FormData();
		fd_compositeFirmware.left = new FormAttachment(0, 10);
		fd_compositeFirmware.bottom = new FormAttachment(btnCancel, -18);
		compositeFirmware.setLayoutData(fd_compositeFirmware);
		
		tableFirmwareViewer = new TableViewer(compositeFirmware,SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.SINGLE);
		tableFirmwareViewer.setContentProvider(new FirmwareContentProvider());
		tableFirmwareViewer.setLabelProvider(new FirmwareLabelProvider());

		
		tableFirmware = tableFirmwareViewer.getTable();
		TableColumn[] columns = new TableColumn[4];
		columns[0] = new TableColumn(tableFirmware, SWT.NONE);
		columns[0].setText("Filename");
		columns[1] = new TableColumn(tableFirmware, SWT.NONE);
		columns[1].setText("Device");
		columns[2] = new TableColumn(tableFirmware, SWT.NONE);
		columns[2].setText("Version");
		columns[3] = new TableColumn(tableFirmware, SWT.NONE);
		columns[3].setText("Branding");
	    for (int i = 0, n = tableFirmware.getColumnCount(); i < n; i++) {
	    	tableFirmware.getColumn(i).pack();
	    	if (i==0) {
	    		tableFirmware.getColumn(i).setWidth(0);tableFirmware.getColumn(i).setResizable(false);
	    	}
	    	if (i==1)
	    		tableFirmware.getColumn(i).setWidth(60);
		    if (i==2)
		    	tableFirmware.getColumn(i).setWidth(100);
		    if (i==3)
		    	tableFirmware.getColumn(i).setWidth(180);
	    }
		tableFirmware.setHeaderVisible(true);
		tableFirmware.setLinesVisible(true);
		tableFirmware.addListener(SWT.DefaultSelection, new Listener() {
		      public void handleEvent(Event e) {
		        TableItem[] selection = tableFirmware.getSelection();
		        String string = selection[0].getText(0);
		        result = new Bundle(sourceFolder.getText()+File.separator+string,Bundle.JARTYPE);
		        shlFirmwareSelector.dispose();
		      }
		    });
		tableFirmware.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent event) {
		    	  IStructuredSelection sel = (IStructuredSelection) tableFirmwareViewer.getSelection();
		    	  Firmware firm = (Firmware)sel.getFirstElement();
		    	  tableContentViewer.setInput(firm);
		    	  tableContentViewer.refresh();
		      }
		    });

		Label lblSourceFolder = new Label(shlFirmwareSelector, SWT.NONE);
		FormData fd_lblSourceFolder = new FormData();
		fd_lblSourceFolder.top = new FormAttachment(0, 10);
		fd_lblSourceFolder.left = new FormAttachment(0, 10);
		lblSourceFolder.setLayoutData(fd_lblSourceFolder);
		lblSourceFolder.setText("Source folder :");
		
		Button btnNewButton = new Button(shlFirmwareSelector, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(shlFirmwareSelector);

		        // Set the initial filter path according
		        // to anything they've selected or typed in
		        dlg.setFilterPath(sourceFolder.getText());

		        // Change the title bar text
		        dlg.setText("Directory chooser");

		        // Customizable message displayed in the dialog
		        dlg.setMessage("Select a directory");

		        // Calling open() will open and run the dialog.
		        // It will return the selected directory, or
		        // null if user cancels
		        String dir = dlg.open();
		        if (dir != null) {
		          // Set the text box to the new selection
		        	if (!sourceFolder.getText().equals(dir)) {
		        		sourceFolder.setText(dir);
		        		updateTables();
		        	}
		        }
			}
		});
		FormData fd_btnNewButton = new FormData();
		fd_btnNewButton.top = new FormAttachment(0, 5);
		fd_btnNewButton.right = new FormAttachment(btnCancel, 0, SWT.RIGHT);
		btnNewButton.setLayoutData(fd_btnNewButton);
		btnNewButton.setText("...");
		
		Composite compositeContent = new Composite(shlFirmwareSelector, SWT.NONE);
		fd_compositeFirmware.right = new FormAttachment(100, -325);
		FormData fd_compositeContent = new FormData();
		fd_compositeContent.bottom = new FormAttachment(100, -51);
		fd_compositeContent.left = new FormAttachment(compositeFirmware, 6);
		fd_compositeContent.right = new FormAttachment(100, -161);
		compositeContent.setLayoutData(fd_compositeContent);

		tableContentViewer = new TableViewer(compositeContent, SWT.NONE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		tableContentViewer.setContentProvider(new ContentContentProvider());
		tableContentViewer.setLabelProvider(new ContentLabelProvider());
		
		tableContent = tableContentViewer.getTable();
		tableContent.setEnabled(false);
		tableContent.setBounds(0, 0, 158, 340);
		TableColumn[] columnsContent = new TableColumn[1];
		columnsContent[0] = new TableColumn(tableContent, SWT.NONE);
		columnsContent[0].setText("Filename");
	    for (int i = 0, n = tableContent.getColumnCount(); i < n; i++) {
	    	tableContent.getColumn(i).pack();
	      }
		tableContent.setHeaderVisible(true);
		tableContent.setLinesVisible(true);
		
		Label lblNewLabel = new Label(shlFirmwareSelector, SWT.NONE);
		fd_lblSourceFolder.right = new FormAttachment(lblNewLabel, 0, SWT.RIGHT);
		fd_compositeFirmware.top = new FormAttachment(lblNewLabel, 6);
		FormData fd_lblNewLabel = new FormData();
		fd_lblNewLabel.right = new FormAttachment(100, -579);
		fd_lblNewLabel.left = new FormAttachment(0, 10);
		fd_lblNewLabel.bottom = new FormAttachment(100, -397);
		lblNewLabel.setLayoutData(fd_lblNewLabel);
		lblNewLabel.setText("Firmware :");
		
		Label lblContent = new Label(shlFirmwareSelector, SWT.NONE);
		fd_compositeContent.top = new FormAttachment(lblContent, 6);
		FormData fd_lblContent = new FormData();
		fd_lblContent.left = new FormAttachment(lblNewLabel, 260);
		fd_lblContent.right = new FormAttachment(100, -238);
		fd_lblContent.bottom = new FormAttachment(100, -397);
		lblContent.setLayoutData(fd_lblContent);
		lblContent.setText("Content :");
		
		sourceFolder = new Text(shlFirmwareSelector, SWT.BORDER);
		sourceFolder.setEditable(false);
		if (pathname.length()==0) {
			sourceFolder.setText(OS.getWorkDir()+File.separator+"firmwares");
		}
		else sourceFolder.setText(pathname);
		FormData fd_sourceFolder = new FormData();
		fd_sourceFolder.top = new FormAttachment(lblSourceFolder, -3, SWT.TOP);
		fd_sourceFolder.right = new FormAttachment(btnNewButton, -6);
		fd_sourceFolder.left = new FormAttachment(lblSourceFolder, 6);
		sourceFolder.setLayoutData(fd_sourceFolder);
		
		Label lblWipe = new Label(shlFirmwareSelector, SWT.NONE);
		FormData fd_lblWipe = new FormData();
		fd_lblWipe.right = new FormAttachment(compositeContent, 65, SWT.RIGHT);
		fd_lblWipe.top = new FormAttachment(sourceFolder, 39);
		fd_lblWipe.left = new FormAttachment(compositeContent, 6);
		lblWipe.setLayoutData(fd_lblWipe);
		lblWipe.setText("wipe :");
		
		Label lblExclude = new Label(shlFirmwareSelector, SWT.NONE);
		FormData fd_lblExclude = new FormData();
		fd_lblExclude.top = new FormAttachment(lblWipe, 92);
		fd_lblExclude.right = new FormAttachment(compositeContent, 75, SWT.RIGHT);
		fd_lblExclude.left = new FormAttachment(compositeContent, 6);
		lblExclude.setLayoutData(fd_lblExclude);
		lblExclude.setText("Exclude :");
		
		Label lblMisc = new Label(shlFirmwareSelector, SWT.NONE);
		FormData fd_lblMisc = new FormData();
		fd_lblMisc.top = new FormAttachment(lblExclude, 122);
		fd_lblMisc.right = new FormAttachment(lblWipe, 0, SWT.RIGHT);
		fd_lblMisc.left = new FormAttachment(compositeContent, 6);
		lblMisc.setLayoutData(fd_lblMisc);
		lblMisc.setText("Misc : ");
		
		Composite compositeExclude = new Composite(shlFirmwareSelector, SWT.BORDER);
		FormData fd_compositeExclude = new FormData();
		fd_compositeExclude.top = new FormAttachment(lblExclude, 6);
		fd_compositeExclude.bottom = new FormAttachment(lblMisc, -6);
		fd_compositeExclude.right = new FormAttachment(btnCancel, 0, SWT.RIGHT);
		fd_compositeExclude.left = new FormAttachment(compositeContent, 6);
		compositeExclude.setLayoutData(fd_compositeExclude);
		
		Composite compositeMisc = new Composite(shlFirmwareSelector, SWT.BORDER);
		FormData fd_compositeMisc = new FormData();
		fd_compositeMisc.bottom = new FormAttachment(compositeFirmware, 0, SWT.BOTTOM);
		fd_compositeMisc.right = new FormAttachment(btnCancel, 0, SWT.RIGHT);
		fd_compositeMisc.top = new FormAttachment(lblMisc, 2);
		fd_compositeMisc.left = new FormAttachment(compositeContent, 6);
		compositeMisc.setLayoutData(fd_compositeMisc);
		
		Composite composite = new Composite(shlFirmwareSelector, SWT.BORDER);
		FormData fd_composite = new FormData();
		fd_composite.top = new FormAttachment(lblWipe, 3);
		fd_composite.bottom = new FormAttachment(lblExclude, -6);
		fd_composite.right = new FormAttachment(btnCancel, 0, SWT.RIGHT);
		fd_composite.left = new FormAttachment(compositeContent, 6);
		composite.setLayoutData(fd_composite);
		updateTables();		
	}

	public void updateTables() {
		FirmwaresModel firms = new FirmwaresModel(sourceFolder.getText());
		tableFirmwareViewer.setInput(firms.firmwares);
		tableFirmwareViewer.refresh();
		tableContentViewer.setInput(firms.getFirstFirmware());
		tableContentViewer.refresh();
		tableFirmware.select(0);
	    TableItem[] selection = tableFirmware.getSelection();
	    if (selection.length>0) {
	    	String string = selection[0].getText(0);
	    	result = new Bundle(sourceFolder.getText()+File.separator+string,Bundle.JARTYPE);
	    }
	}
}