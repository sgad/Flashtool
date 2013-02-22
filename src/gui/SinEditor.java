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

public class SinEditor extends Dialog {

	protected Object result;
	protected Shell shlSinEditor;
	private Text sourceFile;
	private Button btnDumpHeader;
	private Button btnDumpData;
	private Button btnNewButton_1;
	private Button btnClose;

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
		shlSinEditor.setSize(481, 175);
		shlSinEditor.setText("Sin Editor");
		
		Label lblNewLabel = new Label(shlSinEditor, SWT.NONE);
		lblNewLabel.setBounds(10, 10, 55, 15);
		lblNewLabel.setText("Sin file :");
		
		sourceFile = new Text(shlSinEditor, SWT.BORDER);
		sourceFile.setEditable(false);
		sourceFile.setBounds(10, 33, 397, 21);
		
		Button btnNewButton = new Button(shlSinEditor, SWT.NONE);
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
		btnNewButton.setBounds(413, 30, 47, 26);
		btnNewButton.setText("...");
		
		btnDumpHeader = new Button(shlSinEditor, SWT.NONE);
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
		btnDumpHeader.setBounds(10, 76, 112, 25);
		btnDumpHeader.setText("Dump header");
		btnDumpHeader.setEnabled(false);
		
		btnDumpData = new Button(shlSinEditor, SWT.NONE);
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
		btnDumpData.setBounds(231, 76, 112, 25);
		btnDumpData.setText("Extract data");
		btnDumpData.setEnabled(false);
		
		btnNewButton_1 = new Button(shlSinEditor, SWT.NONE);
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
		btnNewButton_1.setBounds(128, 76, 97, 25);
		btnNewButton_1.setText("Dump raw");
		btnNewButton_1.setEnabled(false);
		
		btnClose = new Button(shlSinEditor, SWT.NONE);
		btnClose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlSinEditor.dispose();
			}
		});
		btnClose.setBounds(385, 111, 75, 25);
		btnClose.setText("Close");
	}
}
