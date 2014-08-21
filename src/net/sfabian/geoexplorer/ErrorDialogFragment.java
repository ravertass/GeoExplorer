package net.sfabian.geoexplorer;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * This is used to display an error dialog fragment if the Google
 * Play Services API will not connect.
 * 
 * @author sfabian
 */

public class ErrorDialogFragment extends DialogFragment {

	private Dialog dialog;
	
	public ErrorDialogFragment() {
		super();
		dialog = null;
	}
	
	public void setDialog(Dialog dialog) {
		this.dialog = dialog;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return dialog;
	}
}
