package io.mdsl.ui.handler.wizard.page;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import io.mdsl.ui.handler.wizard.components.FreemarkerFileChooser;

/**
 * Dialog page to select Freemarker template and target file name.
 */
public class TextFileGenerationWizardPage extends MDSLWizardPage {

	private Composite container;
	private FreemarkerFileChooser fileChooser;
	private Text targetFileNameTextBox;
	private IFile selectedFile;
	private String initialFilename;

	public TextFileGenerationWizardPage() {
		super("Generate Text File Using Freemarker");
	}

	@Override
	public String getTitle() {
		return "Text File Generator (Freemarker)";
	}

	@Override
	public String getDescription() {
		return "Generate Text File with Freemarker Templating Engine";
	}

	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 10;
		container.setLayout(layout);

		// Freemarker template selection
		Label fileSelectionLabel = new Label(container, SWT.NONE);
		fileSelectionLabel.setText("Freemarker template: ");
		fileChooser = new FreemarkerFileChooser(container);
		if (selectedFile != null)
			fileChooser.setFile(selectedFile);
		fileChooser.setFileSelectedFunction(f -> {
			selectedFile = f;
			setPageComplete(isPageComplete());
		});

		// target filename
		Label targetFileNameLabel = new Label(container, SWT.NONE);
		targetFileNameLabel.setText("Target file name: ");
		targetFileNameTextBox = new Text(container, SWT.BORDER | SWT.SINGLE);
		targetFileNameTextBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		targetFileNameTextBox.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				setPageComplete(isPageComplete());
			}
		});
		if (initialFilename != null)
			targetFileNameTextBox.setText(initialFilename);

		setControl(container);
		setPageComplete(false);
	}

	public IFile getFile() {
		return selectedFile;
	}

	public String getTargetFileName() {
		return targetFileNameTextBox.getText();
	}

	public void setInitialTemplateFile(IFile initialTemplateFile) {
		this.selectedFile = initialTemplateFile;
	}

	public void setInitialFilename(String initialFilename) {
		this.initialFilename = initialFilename;
	}

	@Override
	public boolean isPageComplete() {
		return selectedFile != null && fileChooser.getFile().exists() && !"".equals(targetFileNameTextBox.getText());
	}

}
