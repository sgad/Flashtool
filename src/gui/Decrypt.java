package gui;

import gui.models.Firmware;
import gui.models.Firmwares;
import gui.tools.FtfFilter;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;
import java.util.jar.JarFile;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.List;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

public class Decrypt extends Dialog {

	protected Shell shlDecruptWizard;
	private Text sourceFolder;
	ListViewer listViewerFiles;
	ListViewer listViewerConvert;
	Vector files = new Vector();
	Vector convert = new Vector();
	Vector result = null;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public Decrypt(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Vector open() {
		createContents();
		shlDecruptWizard.open();
		shlDecruptWizard.layout();
		Display display = getParent().getDisplay();
		while (!shlDecruptWizard.isDisposed()) {
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
		shlDecruptWizard = new Shell(getParent(), getStyle());
		shlDecruptWizard.setSize(539, 300);
		shlDecruptWizard.setText("Decrypt Wizard");
		shlDecruptWizard.setLayout(new FormLayout());
		
		listViewerFiles = new ListViewer(shlDecruptWizard, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		List list = listViewerFiles.getList();
		FormData fd_list = new FormData();
		fd_list.bottom = new FormAttachment(0, 229);
		fd_list.right = new FormAttachment(0, 223);
		fd_list.top = new FormAttachment(0, 71);
		fd_list.left = new FormAttachment(0, 10);
		list.setLayoutData(fd_list);

	    listViewerFiles.setContentProvider(new IStructuredContentProvider() {
	        public Object[] getElements(Object inputElement) {
	          Vector v = (Vector)inputElement;
	          return v.toArray();
	        }
	        
	        public void dispose() {
	        }
	   
	        public void inputChanged(
	          Viewer viewer,
	          Object oldInput,
	          Object newInput) {
	        }
	      });
	    listViewerFiles.setLabelProvider(new LabelProvider() {
	        public Image getImage(Object element) {
	          return null;
	        }
	   
	        public String getText(Object element) {
	          return ((File)element).getName();
	        }
	      });

		Label lblAvailableFiles = new Label(shlDecruptWizard, SWT.NONE);
		FormData fd_lblAvailableFiles = new FormData();
		fd_lblAvailableFiles.right = new FormAttachment(0, 115);
		fd_lblAvailableFiles.top = new FormAttachment(0, 51);
		fd_lblAvailableFiles.left = new FormAttachment(0, 10);
		lblAvailableFiles.setLayoutData(fd_lblAvailableFiles);
		lblAvailableFiles.setText("Available files :");
		
		listViewerConvert = new ListViewer(shlDecruptWizard, SWT.BORDER | SWT.V_SCROLL);
		List list_1 = listViewerConvert.getList();
		FormData fd_list_1 = new FormData();
		fd_list_1.bottom = new FormAttachment(list, 0, SWT.BOTTOM);
		fd_list_1.top = new FormAttachment(list, 0, SWT.TOP);
		fd_list_1.right = new FormAttachment(0, 522);
		fd_list_1.left = new FormAttachment(0, 282);
		list_1.setLayoutData(fd_list_1);

		listViewerConvert.setContentProvider(new IStructuredContentProvider() {
	        public Object[] getElements(Object inputElement) {
	          Vector v = (Vector)inputElement;
	          return v.toArray();
	        }
	        
	        public void dispose() {
	        }
	   
	        public void inputChanged(
	          Viewer viewer,
	          Object oldInput,
	          Object newInput) {
	        }
	      });
	    
		listViewerConvert.setLabelProvider(new LabelProvider() {
	        public Image getImage(Object element) {
	          return null;
	        }
	   
	        public String getText(Object element) {
	          return ((File)element).getName();
	        }
	      });
		
		Button btnNewButton_1 = new Button(shlDecruptWizard, SWT.NONE);
		FormData fd_btnNewButton_1 = new FormData();
		fd_btnNewButton_1.left = new FormAttachment(list, 16);
		btnNewButton_1.setLayoutData(fd_btnNewButton_1);
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)listViewerFiles.getSelection();
				Iterator i = selection.iterator();
				while (i.hasNext()) {
					File f = (File)i.next();
					files.remove(f);
					convert.add(f);
					listViewerFiles.refresh();
					listViewerConvert.refresh();
				}
			}
		});
		btnNewButton_1.setText("->");
		
		Button btnNewButton_2 = new Button(shlDecruptWizard, SWT.NONE);
		fd_btnNewButton_1.bottom = new FormAttachment(100, -139);
		FormData fd_btnNewButton_2 = new FormData();
		fd_btnNewButton_2.top = new FormAttachment(btnNewButton_1, 30);
		fd_btnNewButton_2.right = new FormAttachment(btnNewButton_1, 0, SWT.RIGHT);
		btnNewButton_2.setLayoutData(fd_btnNewButton_2);
		btnNewButton_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)listViewerConvert.getSelection();
				Iterator i = selection.iterator();
				while (i.hasNext()) {
					File f = (File)i.next();
					convert.remove(f);
					files.add(f);
					listViewerFiles.refresh();
					listViewerConvert.refresh();
				}
			}
		});
		btnNewButton_2.setText("<-");
		
		Label lblNewLabel_1 = new Label(shlDecruptWizard, SWT.NONE);
		FormData fd_lblNewLabel_1 = new FormData();
		fd_lblNewLabel_1.top = new FormAttachment(0, 51);
		fd_lblNewLabel_1.left = new FormAttachment(0, 282);
		lblNewLabel_1.setLayoutData(fd_lblNewLabel_1);
		lblNewLabel_1.setText("Files to convert :");
		
		Button btnNewButton_3 = new Button(shlDecruptWizard, SWT.NONE);
		FormData fd_btnNewButton_3 = new FormData();
		fd_btnNewButton_3.right = new FormAttachment(100, -11);
		btnNewButton_3.setLayoutData(fd_btnNewButton_3);
		btnNewButton_3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlDecruptWizard.dispose();
			}
		});
		btnNewButton_3.setText("Cancel");
		
		Button btnNewButton_4 = new Button(shlDecruptWizard, SWT.NONE);
		fd_btnNewButton_3.top = new FormAttachment(btnNewButton_4, 0, SWT.TOP);
		FormData fd_btnNewButton_4 = new FormData();
		fd_btnNewButton_4.right = new FormAttachment(100, -71);
		fd_btnNewButton_4.top = new FormAttachment(list_1, 7);
		btnNewButton_4.setLayoutData(fd_btnNewButton_4);
		btnNewButton_4.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (convert.size()>0) result=convert;
				shlDecruptWizard.dispose();
			}
		});
		btnNewButton_4.setText("Convert");
		
		Composite composite = new Composite(shlDecruptWizard, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		FormData fd_composite = new FormData();
		fd_composite.bottom = new FormAttachment(lblAvailableFiles, -6);
		fd_composite.left = new FormAttachment(list, 0, SWT.LEFT);
		fd_composite.right = new FormAttachment(list_1, 0, SWT.RIGHT);
		fd_composite.top = new FormAttachment(0, 10);
		composite.setLayoutData(fd_composite);
		
		Label lblNewLabel = new Label(composite, SWT.NONE);
		GridData gd_lblNewLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblNewLabel.widthHint = 92;
		lblNewLabel.setLayoutData(gd_lblNewLabel);
		lblNewLabel.setText("Source Folder : ");
		lblNewLabel.setFont(new Font(Display.getCurrent(),"Arial",11,SWT.NONE));
		sourceFolder = new Text(composite, SWT.BORDER);
		GridData gd_sourceFolder = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_sourceFolder.widthHint = 359;
		sourceFolder.setLayoutData(gd_sourceFolder);
		
		Button btnNewButton = new Button(composite, SWT.NONE);
		GridData gd_btnNewButton = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_btnNewButton.widthHint = 32;
		btnNewButton.setLayoutData(gd_btnNewButton);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(shlDecruptWizard);

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
		        		files = new Vector();
		        		convert = new Vector();
		    			File srcdir = new File(sourceFolder.getText());
		    			File[] chld = srcdir.listFiles();
		    			for(int i = 0; i < chld.length; i++) {
		    				if (chld[i].getName().toUpperCase().startsWith("FILE") && chld[i].length()>20000)
		    					files.add(chld[i]);
		    			}
		    			listViewerFiles.setInput(files);
		    			listViewerConvert.setInput(convert);
		        	}
		        }
			}
		});
		btnNewButton.setText("...");
		btnNewButton.setFont(new Font(Display.getCurrent(),"Arial",11,SWT.NONE));

	}
}
