package gui;

import java.io.File;
import java.util.Vector;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.List;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;

public class BundleCreator extends Dialog {

	protected Object result;
	protected Shell shlBundler;
	private Text sourceFolder;
	private Text text_1;
	private Text text_2;
	private Text text_3;
	Vector files = new Vector();
	ListViewer listViewerFiles;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public BundleCreator(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlBundler.open();
		shlBundler.layout();
		Display display = getParent().getDisplay();
		while (!shlBundler.isDisposed()) {
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
		shlBundler = new Shell(getParent(), getStyle());
		shlBundler.setSize(632, 393);
		shlBundler.setText("Bundler");
		shlBundler.setLayout(new FormLayout());
		
		Label lblSelectSourceFolder = new Label(shlBundler, SWT.NONE);
		FormData fd_lblSelectSourceFolder = new FormData();
		fd_lblSelectSourceFolder.right = new FormAttachment(0, 140);
		fd_lblSelectSourceFolder.top = new FormAttachment(0, 10);
		fd_lblSelectSourceFolder.left = new FormAttachment(0, 10);
		lblSelectSourceFolder.setLayoutData(fd_lblSelectSourceFolder);
		lblSelectSourceFolder.setText("Select source folder :");
		
		sourceFolder = new Text(shlBundler, SWT.BORDER);
		FormData fd_sourceFolder = new FormData();
		fd_sourceFolder.bottom = new FormAttachment(0, 68);
		fd_sourceFolder.right = new FormAttachment(0, 321);
		fd_sourceFolder.top = new FormAttachment(0, 43);
		fd_sourceFolder.left = new FormAttachment(0, 10);
		sourceFolder.setLayoutData(fd_sourceFolder);
		
		Button btnNewButton = new Button(shlBundler, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(shlBundler);

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
		    			File srcdir = new File(sourceFolder.getText());
		    			File[] chld = srcdir.listFiles();
		    			for(int i = 0; i < chld.length; i++) {
		    				if (chld[i].getName().toUpperCase().endsWith("SIN"))
		    					files.add(chld[i]);
		    			}
		    			listViewerFiles.setInput(files);
		        	}
		        }
			}
		});
		FormData fd_btnNewButton = new FormData();
		fd_btnNewButton.right = new FormAttachment(0, 356);
		fd_btnNewButton.top = new FormAttachment(0, 43);
		fd_btnNewButton.left = new FormAttachment(0, 327);
		btnNewButton.setLayoutData(fd_btnNewButton);
		btnNewButton.setText("...");
		
		Label lblNewLabel = new Label(shlBundler, SWT.NONE);
		FormData fd_lblNewLabel = new FormData();
		fd_lblNewLabel.right = new FormAttachment(0, 443);
		fd_lblNewLabel.top = new FormAttachment(0, 10);
		fd_lblNewLabel.left = new FormAttachment(0, 376);
		lblNewLabel.setLayoutData(fd_lblNewLabel);
		lblNewLabel.setText("Device :");
		
		Label lblNewLabel_1 = new Label(shlBundler, SWT.NONE);
		FormData fd_lblNewLabel_1 = new FormData();
		fd_lblNewLabel_1.right = new FormAttachment(0, 443);
		fd_lblNewLabel_1.top = new FormAttachment(0, 46);
		fd_lblNewLabel_1.left = new FormAttachment(0, 376);
		lblNewLabel_1.setLayoutData(fd_lblNewLabel_1);
		lblNewLabel_1.setText("Version :");
		
		Label lblNewLabel_2 = new Label(shlBundler, SWT.NONE);
		FormData fd_lblNewLabel_2 = new FormData();
		fd_lblNewLabel_2.right = new FormAttachment(0, 443);
		fd_lblNewLabel_2.top = new FormAttachment(0, 84);
		fd_lblNewLabel_2.left = new FormAttachment(0, 376);
		lblNewLabel_2.setLayoutData(fd_lblNewLabel_2);
		lblNewLabel_2.setText("Branding :");
		
		text_1 = new Text(shlBundler, SWT.BORDER);
		FormData fd_text_1 = new FormData();
		fd_text_1.bottom = new FormAttachment(0, 32);
		fd_text_1.right = new FormAttachment(0, 616);
		fd_text_1.top = new FormAttachment(0, 7);
		fd_text_1.left = new FormAttachment(0, 450);
		text_1.setLayoutData(fd_text_1);
		
		text_2 = new Text(shlBundler, SWT.BORDER);
		FormData fd_text_2 = new FormData();
		fd_text_2.bottom = new FormAttachment(0, 106);
		fd_text_2.right = new FormAttachment(0, 616);
		fd_text_2.top = new FormAttachment(0, 81);
		fd_text_2.left = new FormAttachment(0, 449);
		text_2.setLayoutData(fd_text_2);
		
		text_3 = new Text(shlBundler, SWT.BORDER);
		FormData fd_text_3 = new FormData();
		fd_text_3.bottom = new FormAttachment(0, 68);
		fd_text_3.right = new FormAttachment(0, 616);
		fd_text_3.top = new FormAttachment(0, 43);
		fd_text_3.left = new FormAttachment(0, 450);
		text_3.setLayoutData(fd_text_3);
		
		Button btnNoFinalVerification = new Button(shlBundler, SWT.CHECK);
		FormData fd_btnNoFinalVerification = new FormData();
		fd_btnNoFinalVerification.right = new FormAttachment(0, 616);
		fd_btnNoFinalVerification.top = new FormAttachment(0, 112);
		fd_btnNoFinalVerification.left = new FormAttachment(0, 450);
		btnNoFinalVerification.setLayoutData(fd_btnNoFinalVerification);
		btnNoFinalVerification.setText("No final verification");
		
		listViewerFiles = new ListViewer(shlBundler, SWT.BORDER | SWT.V_SCROLL);
		List list = listViewerFiles.getList();
		FormData fd_list = new FormData();
		fd_list.right = new FormAttachment(btnNewButton, 0, SWT.RIGHT);
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

		Label lblNewLabel_3 = new Label(shlBundler, SWT.NONE);
		fd_list.bottom = new FormAttachment(lblNewLabel_3, 163, SWT.BOTTOM);
		fd_list.top = new FormAttachment(lblNewLabel_3, 6);
		FormData fd_lblNewLabel_3 = new FormData();
		fd_lblNewLabel_3.right = new FormAttachment(0, 98);
		fd_lblNewLabel_3.top = new FormAttachment(0, 146);
		fd_lblNewLabel_3.left = new FormAttachment(0, 10);
		lblNewLabel_3.setLayoutData(fd_lblNewLabel_3);
		lblNewLabel_3.setText("folder list :");
		
		Composite composite = new Composite(shlBundler, SWT.NONE);
		composite.setLayout(new TreeColumnLayout());
		FormData fd_composite = new FormData();
		fd_composite.right = new FormAttachment(text_1, 0, SWT.RIGHT);
		fd_composite.top = new FormAttachment(list, 0, SWT.TOP);
		composite.setLayoutData(fd_composite);
		
		TreeViewer treeViewer = new TreeViewer(composite, SWT.BORDER);
		Tree tree = treeViewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		
		Button btnCancel = new Button(shlBundler, SWT.NONE);
		fd_composite.bottom = new FormAttachment(btnCancel, -5);
		FormData fd_btnCancel = new FormData();
		fd_btnCancel.bottom = new FormAttachment(100, -10);
		fd_btnCancel.right = new FormAttachment(text_1, 0, SWT.RIGHT);
		btnCancel.setLayoutData(fd_btnCancel);
		btnCancel.setText("Cancel");
		
		Button btnCreate = new Button(shlBundler, SWT.NONE);
		FormData fd_btnCreate = new FormData();
		fd_btnCreate.bottom = new FormAttachment(btnCancel, 0, SWT.BOTTOM);
		fd_btnCreate.right = new FormAttachment(btnCancel, -6);
		btnCreate.setLayoutData(fd_btnCreate);
		btnCreate.setText("Create");
		
		Button btnNewButton_1 = new Button(shlBundler, SWT.NONE);
		fd_composite.left = new FormAttachment(btnNewButton_1, 6);
		FormData fd_btnNewButton_1 = new FormData();
		fd_btnNewButton_1.right = new FormAttachment(list, 35, SWT.RIGHT);
		fd_btnNewButton_1.top = new FormAttachment(lblNewLabel_2, 99);
		fd_btnNewButton_1.left = new FormAttachment(list, 6);
		btnNewButton_1.setLayoutData(fd_btnNewButton_1);
		btnNewButton_1.setText("->");
		
		Button btnNewButton_2 = new Button(shlBundler, SWT.NONE);
		FormData fd_btnNewButton_2 = new FormData();
		fd_btnNewButton_2.right = new FormAttachment(btnNewButton_1, 0, SWT.RIGHT);
		fd_btnNewButton_2.top = new FormAttachment(btnNewButton_1, 33);
		fd_btnNewButton_2.left = new FormAttachment(list, 6);
		btnNewButton_2.setLayoutData(fd_btnNewButton_2);
		btnNewButton_2.setText("<-");
		
		Label lblNewLabel_4 = new Label(shlBundler, SWT.NONE);
		FormData fd_lblNewLabel_4 = new FormData();
		fd_lblNewLabel_4.top = new FormAttachment(lblNewLabel_3, 0, SWT.TOP);
		fd_lblNewLabel_4.left = new FormAttachment(composite, 0, SWT.LEFT);
		fd_lblNewLabel_4.right = new FormAttachment(100, -130);
		lblNewLabel_4.setLayoutData(fd_lblNewLabel_4);
		lblNewLabel_4.setText("Firmware content :");

	}
}
