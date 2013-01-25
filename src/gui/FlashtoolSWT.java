package gui;

import java.awt.Toolkit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class FlashtoolSWT extends Shell {

	/**
	 * Launch the application.
	 * @param args
	 */
	public FlashtoolSWT() {
		super(Display.getDefault(), SWT.SHELL_TRIM);
		setImage(SWTResourceManager.getImage(FlashtoolSWT.class, "/gui/ressources/icons/flash_32.png"));
		
		ToolBar toolBar = new ToolBar(this, SWT.FLAT | SWT.RIGHT);
		toolBar.setBounds(10, 10, 148, 38);
		
		ToolItem tltmNewItem = new ToolItem(toolBar, SWT.NONE);
		tltmNewItem.setImage(SWTResourceManager.getImage(FlashtoolSWT.class, "/gui/ressources/icons/flash_32.png"));
		
		ToolItem tltmNewItem_1 = new ToolItem(toolBar, SWT.NONE);
		tltmNewItem_1.setImage(SWTResourceManager.getImage(FlashtoolSWT.class, "/gui/ressources/icons/root_32.png"));
		try {
			createContents();
			open();
			layout();
			while (!isDisposed()) {
				if (!this.getDisplay().readAndDispatch()) {
					this.getDisplay().sleep();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	/**
	 * Create contents of the shell.
	 */
	protected void createContents() {
		setText("SonyEricsson X10 Flasher by Bin4ry & Androxyde");
		setSize(845, 480);

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
