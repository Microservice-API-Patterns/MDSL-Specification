package io.mdsl.ui.quickfix.dialogs;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Based on the Eclipse Public License 2.0 licensed samples from 
 * https://www.vogella.com/tutorials/EclipseDialogs/article.html#copyright-license-and-source-code 
 */
public class NamePromptDialog extends TitleAreaDialog {

	private Text nameText;
	private String name;
	private boolean identifierRequested;
	private final String title;
	private final String infoMessage;

	public NamePromptDialog(Shell parentShell, String title, String infoMessage, boolean identifierRequested) {
		super(parentShell);
		this.title = title;
		this.infoMessage = infoMessage;
		this.identifierRequested = identifierRequested;
	}	

	@Override
	public void create() {
		super.create();
		setTitle(title);
		setMessage(infoMessage, IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(2, false);
		container.setLayout(layout);

		createNameText(container);

		return area;
	}

	private void createNameText(Composite container) {
		Label lbtFirstName = new Label(container, SWT.NONE);
		lbtFirstName.setText("Name");

		GridData dataFirstName = new GridData();
		dataFirstName.grabExcessHorizontalSpace = true;
		dataFirstName.horizontalAlignment = GridData.FILL;

		nameText = new Text(container, SWT.BORDER);
		nameText.setLayoutData(dataFirstName);
		if(identifierRequested) {
			nameText.addModifyListener(listener -> {
				if(nameText.getText().matches("^[a-z].*")) {
					// Reset to the initial message
					setMessage(infoMessage, IMessageProvider.INFORMATION);
				} else {
					setMessage("Identifier name should start with a lowercase character", IMessageProvider.WARNING);
				}
			}); 
		}
		else {
			// must be type
			nameText.addModifyListener(listener -> {
				if(nameText.getText().matches("^[A-Z].*")) {
					// Reset to the initial message
					setMessage(infoMessage, IMessageProvider.INFORMATION);
				} else {
					setMessage("Identifier name should start with an uppercase character", IMessageProvider.WARNING);
				}
			}); 
		}
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	// save content of the Text fields because they get disposed
	// as soon as the Dialog closes
	private void saveInput() {
		name = nameText.getText();
	}

	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}

	public String getFirstName() {
		return name;
	}
}