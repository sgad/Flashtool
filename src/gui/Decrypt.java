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
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.viewers.IStructuredContentProvider;

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
		shlDecruptWizard.setSize(532, 300);
		shlDecruptWizard.setText("Decrypt Wizard");
		shlDecruptWizard.setLayout(new FormLayout());
		
		Label lblNewLabel = new Label(shlDecruptWizard, SWT.NONE);
		FormData fd_lblNewLabel = new FormData();
		fd_lblNewLabel.top = new FormAttachment(0, 15);
		fd_lblNewLabel.left = new FormAttachment(0, 10);
		fd_lblNewLabel.right = new FormAttachment(100, -422);
		lblNewLabel.setLayoutData(fd_lblNewLabel);
		lblNewLabel.setText("Source Folder : ");
		
		Button btnNewButton = new Button(shlDecruptWizard, SWT.NONE);
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
		FormData fd_btnNewButton = new FormData();
		fd_btnNewButton.top = new FormAttachment(0, 10);
		fd_btnNewButton.right = new FormAttachment(100, -10);
		btnNewButton.setLayoutData(fd_btnNewButton);
		btnNewButton.setText("...");
		
		sourceFolder = new Text(shlDecruptWizard, SWT.BORDER);
		FormData fd_sourceFolder = new FormData();
		fd_sourceFolder.right = new FormAttachment(btnNewButton, -6);
		fd_sourceFolder.top = new FormAttachment(0, 12);
		fd_sourceFolder.left = new FormAttachment(lblNewLabel, 6);
		sourceFolder.setLayoutData(fd_sourceFolder);
		
		listViewerFiles = new ListViewer(shlDecruptWizard, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		List list = listViewerFiles.getList();
		FormData fd_list = new FormData();
		fd_list.left = new FormAttachment(0, 10);
		fd_list.bottom = new FormAttachment(100, -49);
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
		fd_list.top = new FormAttachment(lblAvailableFiles, 6);
		FormData fd_lblAvailableFiles = new FormData();
		fd_lblAvailableFiles.right = new FormAttachment(0, 115);
		fd_lblAvailableFiles.top = new FormAttachment(sourceFolder, 20);
		fd_lblAvailableFiles.left = new FormAttachment(0, 10);
		lblAvailableFiles.setLayoutData(fd_lblAvailableFiles);
		lblAvailableFiles.setText("Available files :");
		
		listViewerConvert = new ListViewer(shlDecruptWizard, SWT.BORDER | SWT.V_SCROLL);
		List list_1 = listViewerConvert.getList();
		FormData fd_list_1 = new FormData();
		fd_list_1.bottom = new FormAttachment(100, -49);
		fd_list_1.top = new FormAttachment(btnNewButton, 39);
		fd_list_1.right = new FormAttachment(100, -10);
		fd_list_1.left = new FormAttachment(100, -250);
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
		fd_list.right = new FormAttachment(btnNewButton_1, -6);
		FormData fd_btnNewButton_1 = new FormData();
		fd_btnNewButton_1.right = new FormAttachment(list_1, -6);
		btnNewButton_1.setLayoutData(fd_btnNewButton_1);
		btnNewButton_1.setText("->");
		
		Button btnNewButton_2 = new Button(shlDecruptWizard, SWT.NONE);
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
		fd_btnNewButton_1.bottom = new FormAttachment(100, -144);
		FormData fd_btnNewButton_2 = new FormData();
		fd_btnNewButton_2.top = new FormAttachment(btnNewButton_1, 27);
		fd_btnNewButton_2.right = new FormAttachment(list_1, -6);
		btnNewButton_2.setLayoutData(fd_btnNewButton_2);
		btnNewButton_2.setText("<-");
		
		Label lblNewLabel_1 = new Label(shlDecruptWizard, SWT.NONE);
		FormData fd_lblNewLabel_1 = new FormData();
		fd_lblNewLabel_1.bottom = new FormAttachment(lblAvailableFiles, 0, SWT.BOTTOM);
		fd_lblNewLabel_1.left = new FormAttachment(list_1, 0, SWT.LEFT);
		lblNewLabel_1.setLayoutData(fd_lblNewLabel_1);
		lblNewLabel_1.setText("Files to convert :");
		
		Button btnNewButton_3 = new Button(shlDecruptWizard, SWT.NONE);
		btnNewButton_3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlDecruptWizard.dispose();
			}
		});
		FormData fd_btnNewButton_3 = new FormData();
		fd_btnNewButton_3.bottom = new FormAttachment(100, -10);
		fd_btnNewButton_3.right = new FormAttachment(btnNewButton, 0, SWT.RIGHT);
		btnNewButton_3.setLayoutData(fd_btnNewButton_3);
		btnNewButton_3.setText("Cancel");
		
		Button btnNewButton_4 = new Button(shlDecruptWizard, SWT.NONE);
		btnNewButton_4.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (convert.size()>0) result=convert;
				shlDecruptWizard.dispose();
			}
		});
		FormData fd_btnNewButton_4 = new FormData();
		fd_btnNewButton_4.bottom = new FormAttachment(btnNewButton_3, 0, SWT.BOTTOM);
		fd_btnNewButton_4.right = new FormAttachment(btnNewButton_3, -6);
		btnNewButton_4.setLayoutData(fd_btnNewButton_4);
		btnNewButton_4.setText("Convert");

	}
}
