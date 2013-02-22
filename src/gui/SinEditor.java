package gui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import flashsystem.SinFile;
import gui.tools.ExtractSinDataJob;
import gui.tools.WidgetsTool;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.GridData;

public class SinEditor extends Dialog {

	protected Object result;
	protected Shell shlSinEditor;
	private Text sourceFile;
	private Button btnDumpHeader;
	private Button btnDumpData;
	private Button btnNewButton_1;
	private Button btnClose;
	private Label lblNewLabel;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public SinEditor(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		WidgetsTool.setSize(shlSinEditor);
		
		Composite composite = new Composite(shlSinEditor, SWT.NONE);
		FormData fd_composite = new FormData();
		fd_composite.top = new FormAttachment(lblNewLabel, 6);
		fd_composite.right = new FormAttachment(btnClose, 0, SWT.RIGHT);
		fd_composite.bottom = new FormAttachment(0, 66);
		fd_composite.left = new FormAttachment(0, 10);
		composite.setLayoutData(fd_composite);
		composite.setLayout(new GridLayout(2, false));
		
		sourceFile = new Text(composite, SWT.BORDER);
		GridData gd_sourceFile = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_sourceFile.widthHint = 384;
		sourceFile.setLayoutData(gd_sourceFile);
		sourceFile.setEditable(false);
		
		Button btnNewButton = new Button(composite, SWT.NONE);
		GridData gd_btnNewButton = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnNewButton.widthHint = 41;
		btnNewButton.setLayoutData(gd_btnNewButton);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(shlSinEditor);

		        // Set the initial filter path according
		        // to anything they've selected or typed in
		        dlg.setFilterPath(sourceFile.getText());
		        dlg.setFilterExtensions(new String[]{"*.sin"});

		        // Change the title bar text
		        dlg.setText("SIN File Chooser");
		        // Calling open() will open and run the dialog.
		        // It will return the selected directory, or
		        // null if user cancels
		        String dir = dlg.open();
		        if (dir != null) {
		          // Set the text box to the new selection
		        	if (!sourceFile.getText().equals(dir)) {
		        		sourceFile.setText(dir);
		        		btnDumpHeader.setEnabled(true);
		        		btnDumpData.setEnabled(true);
		        		btnNewButton_1.setEnabled(true);
		        	}
		        }
			}
		});
		btnNewButton.setText("...");
		shlSinEditor.open();
		shlSinEditor.layout();
		Display display = getParent().getDisplay();
		while (!shlSinEditor.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlSinEditor = new Shell(getParent(), getStyle());
		shlSinEditor.setSize(481, 174);
		shlSinEditor.setText("Sin Editor");
		shlSinEditor.setLayout(new FormLayout());
		
		lblNewLabel = new Label(shlSinEditor, SWT.NONE);
		FormData fd_lblNewLabel = new FormData();
		fd_lblNewLabel.right = new FormAttachment(0, 65);
		fd_lblNewLabel.top = new FormAttachment(0, 10);
		fd_lblNewLabel.left = new FormAttachment(0, 10);
		lblNewLabel.setLayoutData(fd_lblNewLabel);
		lblNewLabel.setText("Sin file :");
		
		btnDumpHeader = new Button(shlSinEditor, SWT.NONE);
		FormData fd_btnDumpHeader = new FormData();
		fd_btnDumpHeader.right = new FormAttachment(0, 122);
		fd_btnDumpHeader.top = new FormAttachment(0, 76);
		fd_btnDumpHeader.left = new FormAttachment(0, 10);
		btnDumpHeader.setLayoutData(fd_btnDumpHeader);
		btnDumpHeader.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					SinFile sinf = new SinFile(sourceFile.getText());
					sinf.dumpHeader();
				}
				catch (Exception ex) {
				}
			}
		});
		btnDumpHeader.setText("Dump header");
		btnDumpHeader.setEnabled(false);
		
		btnDumpData = new Button(shlSinEditor, SWT.NONE);
		FormData fd_btnDumpData = new FormData();
		fd_btnDumpData.right = new FormAttachment(0, 343);
		fd_btnDumpData.top = new FormAttachment(0, 76);
		fd_btnDumpData.left = new FormAttachment(0, 231);
		btnDumpData.setLayoutData(fd_btnDumpData);
		btnDumpData.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					SinFile sinf = new SinFile(sourceFile.getText());
					ExtractSinDataJob ej = new ExtractSinDataJob("Sin dump job");
					ej.setSin(sinf);
					ej.setMode("data");
					ej.schedule();
				}
				catch (Exception ex) {
				}
			}
		});
		btnDumpData.setText("Extract data");
		btnDumpData.setEnabled(false);
		
		btnNewButton_1 = new Button(shlSinEditor, SWT.NONE);
		FormData fd_btnNewButton_1 = new FormData();
		fd_btnNewButton_1.right = new FormAttachment(0, 225);
		fd_btnNewButton_1.top = new FormAttachment(0, 76);
		fd_btnNewButton_1.left = new FormAttachment(0, 128);
		btnNewButton_1.setLayoutData(fd_btnNewButton_1);
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					SinFile sinf = new SinFile(sourceFile.getText());
					ExtractSinDataJob ej = new ExtractSinDataJob("Sin dump job");
					ej.setSin(sinf);
					ej.setMode("raw");
					ej.schedule();
				}
				catch (Exception ex) {
				}
			}
		});
		btnNewButton_1.setText("Dump raw");
		btnNewButton_1.setEnabled(false);
		
		btnClose = new Button(shlSinEditor, SWT.NONE);
		FormData fd_btnClose = new FormData();
		fd_btnClose.right = new FormAttachment(0, 460);
		fd_btnClose.top = new FormAttachment(0, 111);
		fd_btnClose.left = new FormAttachment(0, 385);
		btnClose.setLayoutData(fd_btnClose);
		btnClose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlSinEditor.dispose();
			}
		});
		btnClose.setText("Close");
	}
}
