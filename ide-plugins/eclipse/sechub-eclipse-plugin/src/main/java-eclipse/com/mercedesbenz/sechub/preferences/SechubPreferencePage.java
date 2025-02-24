package com.mercedesbenz.sechub.preferences;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Objects;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class SechubPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage  {
	
	public static final String PAGE_ID = "sechub.preference.page";
	
	private StringFieldEditor serverUrlField;
	private StringFieldEditor usernameField;
	private StringFieldEditor apiTokenField;
	
	public static final String PREFERENCE_PAGE_ID = "sechub.preference.page";
	
	private SecureStorageAccess secureStorageAccess;


	public SechubPreferencePage() {
		super(GRID);
	}
	
	@Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, PAGE_ID));
        
        secureStorageAccess = new SecureStorageAccess();
	}
    
	@Override
    public void createFieldEditors() {
    	serverUrlField = new StringFieldEditor(PreferenceIdConstants.SERVER, "Server URL:", getFieldEditorParent());
    	
    	
    	usernameField = new StringFieldEditor(PreferenceIdConstants.USER_ID, "User id:", getFieldEditorParent());
    	usernameField.getTextControl(getFieldEditorParent()).setEchoChar('*');
        apiTokenField = new StringFieldEditor(PreferenceIdConstants.APITOKEN, "API Token:", getFieldEditorParent());
        apiTokenField.getTextControl(getFieldEditorParent()).setEchoChar('*');
        
        
        addField(serverUrlField);
        addField(usernameField);
        addField(apiTokenField);
        
     }
	
	@Override
	public void initialize() {
		super.initialize();
		
		 try {
			 String username = secureStorageAccess.getUserId();
			 String apitoken = secureStorageAccess.getApiToken();
			 
			 usernameField.setStringValue(username);
			 apiTokenField.setStringValue(apitoken);
			 
		 }catch (StorageException e) {
			 usernameField.setStringValue("");
			 apiTokenField.setStringValue("");
		 }
	}

	 @Override
	 public boolean performOk() {
		 // ATTENTION: NEVER do super.performOk here ! Reason: The current implementation
		 // uses userNameField and apiTokenField which hold the credentials inside UI
		 // if we would do a super.performOk, the fields would store the sensitive data
		 // plain to normal preferences.
		 
		 // TODO: Think about using extra SWT components here instead of preference fields
		 try {
			 validateServerURL();
		 }catch (URISyntaxException e) {
			 serverUrlField.setFocus();
			 serverUrlField.setErrorMessage("Please enter a valid URI");
			 serverUrlField.showErrorMessage();
			 return false;
		 }
		 addHttpsProtocol();
		 
		 serverUrlField.store(); // triggers change event when field has changed
		 
		 boolean credentialsChanged = false;
		 String userId = usernameField.getStringValue();
		 String apitoken = apiTokenField.getStringValue();
		 
		 try {
			 credentialsChanged = credentialsChanged ||! Objects.equals(userId, secureStorageAccess.getUserId());
			 credentialsChanged = credentialsChanged ||! Objects.equals(apitoken, secureStorageAccess.getApiToken());
				 
			 secureStorageAccess.storeSecureStorage(userId, apitoken);
		 }catch (StorageException e) {
			 return false;
		 }
		 if (credentialsChanged) {
			 // we store here an artificial value in peference store - this is recognized in server view which leads to an update
			 SecHubPreferences.get().getScopedPreferenceStore().setValue(PreferenceIdConstants.CREDENTIALS_CHANGED, LocalDateTime.now().toString());
		 }
		 
		 return true;
	 }
	 
	 private void validateServerURL() throws URISyntaxException {
		new URI(serverUrlField.getStringValue());
	 }
	 
	 private void addHttpsProtocol() {
		String url = serverUrlField.getStringValue();
		if (url.startsWith("http://") || url.startsWith("https://") || url.isBlank()) {
			return;
		}
		url = "https://" + url;
		serverUrlField.setStringValue(url);
	 }
}
	
