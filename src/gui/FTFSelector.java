package gui;

import flashsystem.Bundle;
import flashsystem.BundleEntry;

import java.io.File;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.JCheckBox;

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
import gui.tools.WidgetsTool;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;

public class FTFSelector extends Dialog {

	protected Bundle result;
	protected Shell shlFirmwareSelector;
	private Properties hasCmd25 = new Properties();
	private String filename="";
	private Button btnOK=null;
	private TableViewer tableFirmwareViewer;
	private TableViewer tableContentViewer;
	private Table tableFirmware;
	private Table tableContent;
	private Text sourceFolder;
	private Button btnCheckCmd25;
	private Composite compositeWipe;
	private Composite compositeExclude;
	private Composite compositeContent;
	private Properties btnHandles = new Properties();

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
		WidgetsTool.setSize(shlFirmwareSelector);
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
		shlFirmwareSelector.setSize(695, 486);
		shlFirmwareSelector.setText("Firmware Selector");
		shlFirmwareSelector.addListener(SWT.Close, new Listener() {
		      public void handleEvent(Event event) {
					result = null;
					shlFirmwareSelector.dispose();
		      }
		    });
		shlFirmwareSelector.setLayout(new FormLayout());
		Button btnCancel = new Button(shlFirmwareSelector, SWT.NONE);
		FormData fd_btnCancel = new FormData();
		fd_btnCancel.right = new FormAttachment(100, -11);
		fd_btnCancel.bottom = new FormAttachment(100, -10);
		btnCancel.setLayoutData(fd_btnCancel);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				result = null;
				shlFirmwareSelector.dispose();
			}
		});
		btnCancel.setText("Cancel");
		
		Composite compositeFirmware = new Composite(shlFirmwareSelector, SWT.NONE);
		FormData fd_compositeFirmware = new FormData();
		fd_compositeFirmware.bottom = new FormAttachment(0, 408);
		fd_compositeFirmware.right = new FormAttachment(0, 341);
		fd_compositeFirmware.top = new FormAttachment(0, 73);
		fd_compositeFirmware.left = new FormAttachment(0, 10);
		compositeFirmware.setLayoutData(fd_compositeFirmware);
		compositeFirmware.setLayout(new FillLayout(SWT.HORIZONTAL));
		
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
		        shlFirmwareSelector.dispose();
		      }
		    });
		tableFirmware.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent event) {
		    	  IStructuredSelection sel = (IStructuredSelection) tableFirmwareViewer.getSelection();
		    	  Firmware firm = (Firmware)sel.getFirstElement();
		    	  tableContentViewer.setInput(firm);
		    	  tableContentViewer.refresh();
		    	  result = firm.getBundle();
		    	  btnCheckCmd25.setSelection(result.hasCmd25());
		    	  updateCheckBoxes();
		      }
		    });
		
		compositeContent = new Composite(shlFirmwareSelector, SWT.NONE);
		FormData fd_compositeContent = new FormData();
		fd_compositeContent.bottom = new FormAttachment(100, -49);
		fd_compositeContent.right = new FormAttachment(compositeFirmware, 164, SWT.RIGHT);
		fd_compositeContent.left = new FormAttachment(compositeFirmware, 6);
		compositeContent.setLayoutData(fd_compositeContent);

		tableContentViewer = new TableViewer(compositeContent, SWT.NONE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		tableContentViewer.setContentProvider(new ContentContentProvider());
		tableContentViewer.setLabelProvider(new ContentLabelProvider());
		
		tableContent = tableContentViewer.getTable();
		tableContent.setEnabled(false);
		tableContent.setBounds(0, 0, 158, 335);
		TableColumn[] columnsContent = new TableColumn[1];
		columnsContent[0] = new TableColumn(tableContent, SWT.NONE);
		columnsContent[0].setText("Filename");
	    for (int i = 0, n = tableContent.getColumnCount(); i < n; i++) {
	    	tableContent.getColumn(i).pack();
	    	tableContent.getColumn(i).setWidth(153);
	      }
		tableContent.setHeaderVisible(true);
		tableContent.setLinesVisible(true);
		
		Label lblNewLabel = new Label(shlFirmwareSelector, SWT.NONE);
		FormData fd_lblNewLabel = new FormData();
		fd_lblNewLabel.right = new FormAttachment(0, 109);
		fd_lblNewLabel.top = new FormAttachment(0, 53);
		fd_lblNewLabel.left = new FormAttachment(0, 10);
		lblNewLabel.setLayoutData(fd_lblNewLabel);
		lblNewLabel.setText("Firmware :");
		
		Label lblContent = new Label(shlFirmwareSelector, SWT.NONE);
		fd_compositeContent.top = new FormAttachment(0, 73);
		FormData fd_lblContent = new FormData();
		fd_lblContent.top = new FormAttachment(lblNewLabel, 0, SWT.TOP);
		fd_lblContent.left = new FormAttachment(compositeContent, 0, SWT.LEFT);
		fd_lblContent.right = new FormAttachment(0, 428);
		lblContent.setLayoutData(fd_lblContent);
		lblContent.setText("Content :");
				
		Label lblMisc = new Label(shlFirmwareSelector, SWT.NONE);
		FormData fd_lblMisc = new FormData();
		fd_lblMisc.left = new FormAttachment(compositeContent, 6);
		fd_lblMisc.right = new FormAttachment(0, 570);
		lblMisc.setLayoutData(fd_lblMisc);
		lblMisc.setText("Misc : ");
		
		
		Composite compositeMisc = new Composite(shlFirmwareSelector, SWT.BORDER);
		fd_lblMisc.bottom = new FormAttachment(compositeMisc, -2);
		compositeMisc.setLayout(new RowLayout(SWT.HORIZONTAL));
		FormData fd_compositeMisc = new FormData();
		fd_compositeMisc.left = new FormAttachment(compositeContent, 6);
		fd_compositeMisc.bottom = new FormAttachment(0, 408);
		fd_compositeMisc.right = new FormAttachment(0, 679);
		fd_compositeMisc.top = new FormAttachment(0, 323);
		compositeMisc.setLayoutData(fd_compositeMisc);
		
		
		btnCheckCmd25 = new Button(compositeMisc, SWT.CHECK);
		btnCheckCmd25.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				result.setCmd25(btnCheckCmd25.getSelection()?"true":"false");
			}
		});
		btnCheckCmd25.setToolTipText("");
		btnCheckCmd25.setText("No final verification");
		
		Composite composite_1 = new Composite(shlFirmwareSelector, SWT.NONE);
		composite_1.setLayout(new GridLayout(3, false));
		FormData fd_composite_1 = new FormData();
		fd_composite_1.right = new FormAttachment(btnCancel, 0, SWT.RIGHT);
		fd_composite_1.left = new FormAttachment(0, 10);
		fd_composite_1.bottom = new FormAttachment(lblNewLabel, -6);
		fd_composite_1.top = new FormAttachment(0, 10);
		composite_1.setLayoutData(fd_composite_1);
		
				Label lblSourceFolder = new Label(composite_1, SWT.NONE);
				GridData gd_lblSourceFolder = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
				gd_lblSourceFolder.widthHint = 93;
				lblSourceFolder.setLayoutData(gd_lblSourceFolder);
				lblSourceFolder.setText("Source folder :");
				
				sourceFolder = new Text(composite_1, SWT.BORDER);
				GridData gd_sourceFolder = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
				gd_sourceFolder.widthHint = 498;
				sourceFolder.setLayoutData(gd_sourceFolder);
				sourceFolder.setEditable(false);
				if (pathname.length()==0) {
					sourceFolder.setText(OS.getWorkDir()+File.separator+"firmwares");
				}
				else sourceFolder.setText(pathname);

				Button btnNewButton = new Button(composite_1, SWT.NONE);
				GridData gd_btnNewButton = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
				gd_btnNewButton.widthHint = 49;
				btnNewButton.setLayoutData(gd_btnNewButton);
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
				btnNewButton.setText("...");
				updateTables();
	}

	public void updateTables() {
		FirmwaresModel firms = new FirmwaresModel(sourceFolder.getText());
		tableFirmwareViewer.setInput(firms.firmwares);
		tableFirmwareViewer.refresh();
		tableContentViewer.setInput(firms.getFirstFirmware());
		tableContentViewer.refresh();
		tableFirmware.select(0);
	    if (tableFirmware.getSelection().length>0) {
	    	IStructuredSelection sel = (IStructuredSelection) tableFirmwareViewer.getSelection();
	    	Firmware firm = (Firmware)sel.getFirstElement();
	    	result = firm.getBundle();
	    	btnCheckCmd25.setSelection(((Bundle)result).hasCmd25());
	    }
	    updateCheckBoxes();
	}
	
	public void updateCheckBoxes() {
		if (compositeWipe!=null) compositeWipe.dispose();
		if (compositeExclude!=null) compositeExclude.dispose();

		Label lblWipe = new Label(shlFirmwareSelector, SWT.NONE);
		FormData fd_lblWipe = new FormData();
		fd_lblWipe.left = new FormAttachment(compositeContent, 6);
		fd_lblWipe.right = new FormAttachment(0, 592);
		fd_lblWipe.top = new FormAttachment(0, 65);
		lblWipe.setLayoutData(fd_lblWipe);
		lblWipe.setText("Wipe :");
		compositeWipe = new Composite(shlFirmwareSelector, SWT.BORDER|SWT.V_SCROLL);
		compositeWipe.setLayout(new RowLayout(SWT.HORIZONTAL));
		fd_lblWipe.bottom = new FormAttachment(compositeWipe, -2);
		FormData fd_compositeWipe = new FormData();
		fd_compositeWipe.left = new FormAttachment(compositeContent, 6);
		fd_compositeWipe.right = new FormAttachment(0, 679);
		fd_compositeWipe.bottom = new FormAttachment(0, 165);
		fd_compositeWipe.top = new FormAttachment(0, 82);
		compositeWipe.setLayoutData(fd_compositeWipe);

		Label lblExclude = new Label(shlFirmwareSelector, SWT.NONE);
		FormData fd_lblExclude = new FormData();
		fd_lblExclude.left = new FormAttachment(compositeContent, 6);
		fd_lblExclude.right = new FormAttachment(0, 602);
		fd_lblExclude.top = new FormAttachment(0, 171);
		lblExclude.setLayoutData(fd_lblExclude);
		lblExclude.setText("Exclude :");
		compositeExclude = new Composite(shlFirmwareSelector, SWT.BORDER|SWT.V_SCROLL);
		fd_lblExclude.bottom = new FormAttachment(compositeExclude, -2);
		compositeExclude.setLayout(new RowLayout(SWT.HORIZONTAL));
		FormData fd_compositeExclude = new FormData();
		fd_compositeExclude.left = new FormAttachment(compositeContent, 6);
		fd_compositeExclude.right = new FormAttachment(0, 679);
		fd_compositeExclude.top = new FormAttachment(0, 191);
		fd_compositeExclude.bottom = new FormAttachment(0, 301);
		compositeExclude.setLayoutData(fd_compositeExclude);

		btnHandles.clear();
		if (!tableFirmwareViewer.getSelection().isEmpty()) {
		IStructuredSelection sel = (IStructuredSelection) tableFirmwareViewer.getSelection();
    	Firmware firm = (Firmware)sel.getFirstElement();
		
		Enumeration<String> exclude = result.getMeta().getExclude();
    	while (exclude.hasMoreElements()) {
			String categ = exclude.nextElement();
			Button btnExclude = new Button(compositeExclude, SWT.CHECK);
			btnExclude.setText(result.getMeta().getExcludeLabel(categ));
			btnExclude.setSelection(!firm.getBundle().getMeta().isCategEnabled(categ));
			btnExclude.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
			    	IStructuredSelection sel = (IStructuredSelection) tableFirmwareViewer.getSelection();
			    	Firmware firm = (Firmware)sel.getFirstElement();
					Button b = (Button)e.widget;
					String categ = (String)btnHandles.get(b.handle);
			    	if (b.getSelection()) firm.disableCateg(categ);
			    	else firm.enableCateg(categ);
					tableContentViewer.setInput(firm);
					tableContentViewer.refresh();
				}
			});
			btnHandles.put(btnExclude.handle, categ);
    	}
    	Enumeration<String> wipe = result.getMeta().getWipe();
    	while (wipe.hasMoreElements()) {
			String categ = wipe.nextElement();
			Button btnWipe = new Button(compositeWipe, SWT.CHECK);
			btnWipe.setText(result.getMeta().getWipeLabel(categ));
			btnWipe.setSelection(firm.getBundle().getMeta().isCategEnabled(categ));
			btnWipe.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Button b = (Button)e.widget;
					String categ = (String)btnHandles.get(b.handle);
			    	IStructuredSelection sel = (IStructuredSelection) tableFirmwareViewer.getSelection();
			    	Firmware firm = (Firmware)sel.getFirstElement();
			    	if (b.getSelection()) firm.enableCateg(categ);
			    	else firm.disableCateg(categ);
					tableContentViewer.setInput(firm);
					tableContentViewer.refresh();
				}
			});
			btnHandles.put(btnWipe.handle, categ);
    	}
		}
    	shlFirmwareSelector.layout();
	}

}