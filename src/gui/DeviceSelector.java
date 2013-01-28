package gui;

import java.text.Collator;
import java.util.Enumeration;
import java.util.Locale;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Table;
import org.system.DeviceEntry;
import org.system.Devices;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class DeviceSelector extends Dialog {

	protected Object result;
	protected Shell shlDeviceSelector;
	private Table tableDevices;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public DeviceSelector(Shell parent, int style) {
		super(parent, style);
		setText("Device Selector");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		fillTable();
		shlDeviceSelector.open();
		shlDeviceSelector.layout();
		Display display = getParent().getDisplay();
		while (!shlDeviceSelector.isDisposed()) {
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
		shlDeviceSelector = new Shell(getParent(), getStyle());
		shlDeviceSelector.setSize(289, 434);
		shlDeviceSelector.setText("Device Selector");
		shlDeviceSelector.setLayout(new FormLayout());
		
		Button btnCancel = new Button(shlDeviceSelector, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				result = null;
				shlDeviceSelector.dispose();
			}
		});
		FormData fd_btnCancel = new FormData();
		fd_btnCancel.bottom = new FormAttachment(100, -10);
		fd_btnCancel.right = new FormAttachment(100, -10);
		btnCancel.setLayoutData(fd_btnCancel);
		btnCancel.setText("Cancel");
		
		Composite compositeTable = new Composite(shlDeviceSelector, SWT.NONE);
		compositeTable.setLayout(new FillLayout(SWT.HORIZONTAL));
		FormData fd_compositeTable = new FormData();
		fd_compositeTable.bottom = new FormAttachment(btnCancel, -6);
		fd_compositeTable.right = new FormAttachment(btnCancel, 0, SWT.RIGHT);
		fd_compositeTable.top = new FormAttachment(0, 10);
		fd_compositeTable.left = new FormAttachment(0, 10);
		compositeTable.setLayoutData(fd_compositeTable);
		
		tableDevices = new Table(compositeTable, SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.SINGLE);
		TableColumn[] columns = new TableColumn[2];
		columns[0] = new TableColumn(tableDevices, SWT.NONE);
		columns[0].setText("Id");
		columns[1] = new TableColumn(tableDevices, SWT.NONE);
		columns[1].setText("Name");
		tableDevices.setHeaderVisible(true);
		tableDevices.setLinesVisible(true);
		tableDevices.addListener(SWT.DefaultSelection, new Listener() {
		      public void handleEvent(Event e) {
		        TableItem[] selection = tableDevices.getSelection();
		        String string = selection[0].getText(0);
		        result = string;
		        shlDeviceSelector.dispose();
		      }
		    });
		Listener sortListener = new Listener() {  
	         public void handleEvent(Event e) {  
	             TableItem[] items = tableDevices.getItems();  
	             Collator collator = Collator.getInstance(Locale.getDefault());
	             // determine new sort column and direction
	             TableColumn sortColumn = tableDevices.getSortColumn();
	             TableColumn currentColumn = (TableColumn) e.widget;
	             int dir = tableDevices.getSortDirection();
	             if (sortColumn == currentColumn) {
	               dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
	             } else {
	               tableDevices.setSortColumn(currentColumn);
	               dir = SWT.UP;
	             }  
	             int index = currentColumn == tableDevices.getColumn(0) ? 0 : 1;
	             for (int i = 1; i < items.length; i++) {  
	                 String value1 = items[i].getText(index);  
	                 for (int j = 0; j < i; j++){  
	                     String value2 = items[j].getText(index);
	                     if (dir==SWT.UP) {
		                     if (collator.compare(value1, value2) < 0) {  
		                         String[] values = {items[i].getText(0), items[i].getText(1)};  
		                         items[i].dispose();  
		                         TableItem item = new TableItem(tableDevices, SWT.NONE, j);  
		                         item.setText(values);  
		                         items = tableDevices.getItems();
		                         break;  
		                     }
	                     }
	                     else {
		                     if (collator.compare(value1, value2) > 0) {  
		                         String[] values = {items[i].getText(0), items[i].getText(1)};  
		                         items[i].dispose();  
		                         TableItem item = new TableItem(tableDevices, SWT.NONE, j);  
		                         item.setText(values);  
		                         items = tableDevices.getItems();
		                         break;  
		                     }	                    	 
	                     }
	                 }
	             }
	             tableDevices.setSortDirection(dir);
	         }  
	    };
		for (int i = 0, n = tableDevices.getColumnCount(); i < n; i++) {
			  tableDevices.getColumn(i).addListener(SWT.Selection, sortListener);
			  if (i==0) {
				  tableDevices.setSortDirection(SWT.UP);
				  tableDevices.setSortColumn(tableDevices.getColumn(i));  
			  }
		}

		fillTable();
		//tableDevices.clearAll();
	}
	
	public void fillTable() {
		tableDevices.setRedraw(false);
		Enumeration<Object> e = Devices.listDevices(false);
	    while (e.hasMoreElements()) {
	    	TableItem item = new TableItem(tableDevices, SWT.NONE);
	    	DeviceEntry entry = Devices.getDevice((String)e.nextElement());
	    	item.setText(0, entry.getId());
	    	item.setText(1, entry.getName());
	    }
		for (int i = 0, n = tableDevices.getColumnCount(); i < n; i++) {
			  tableDevices.getColumn(i).pack();
		}
		tableDevices.pack();
		tableDevices.setRedraw(true);
	}
}
