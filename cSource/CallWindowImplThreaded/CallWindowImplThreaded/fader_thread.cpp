#include "de_neue_phase_asterisk_ClickDial_widgets_CallWindow.h"

/** ---- thread function ----- **/

/**
  * the starting point for all fader threads 
*/
DWORD WINAPI fader_thread ( LPVOID lpParam ) {
	struct fader_start_data*  fader_data = (struct fader_start_data*) lpParam;
	cout << "fader_thread: InitTrans is : " << fader_data->fader->transparency << "  Mutex is at: " << & (fader_data->fader->ghMutex) << endl;
	DWORD wait = WaitForSingleObject( fader_data->fader->ghMutex , 3000);

	if ( wait != WAIT_OBJECT_0 )
	{
		cout << "Fader Thread gave up waiting for the Mutex! wait was " << wait << endl;
		if (wait == WAIT_FAILED) {
			cout << "LAST Error: " << GetLastError() << endl;
		}

		
		return 0;
	}

	cout << "Starting fader thread - type: " << fader_data->change << endl;

	switch (fader_data->change) 
	{
		case CHANGE_COLOR:	fader_color (fader_data);
											break;
		case CHANGE_TRANSPARENCY_FADE_IN: fader_transparency_in (fader_data);
											break;
		case CHANGE_TRANSPARENCY_FADE_OUT: fader_transparency_out (fader_data);
											break;
	}

	ReleaseMutex(fader_data->fader->ghMutex);
	delete lpParam;
	return 1;
}

static void fader_transparency_in (struct fader_start_data*  fader_data) {
	extern PSLWA pSetLayeredWindowAttributes;

	int i = fader_data->fader->transparency;

	for (; i < fader_data->new_transparency; i += fader_data->stepWidth ) {
		cout << "Trans IN: before pSetLayeredWindowAttributes" << endl;
		pSetLayeredWindowAttributes (fader_data->fader->hWnd, RGB(255,255,255), 
														i, LWA_ALPHA );
		cout << "Trans IN: after pSetLayeredWindowAttributes" << endl;
		if (i + fader_data->stepWidth > fader_data->new_transparency) {
			fader_data->stepWidth = fader_data->new_transparency - i; // - don't overwrap!
		}
		cout << "Trans IN: " << i << " StepWidth: " << fader_data->stepWidth << endl;
		Sleep ( 30 );
		cout << "Trans IN: after sleep" << endl;
	}
	fader_data->fader->transparency = i;
}

static void fader_transparency_out (struct fader_start_data*  fader_data) {
	extern PSLWA pSetLayeredWindowAttributes;

	int i = fader_data->fader->transparency;

	for (; i > fader_data->new_transparency; i -= fader_data->stepWidth ) {
		   pSetLayeredWindowAttributes (fader_data->fader->hWnd, RGB(255,255,255),
														i, LWA_ALPHA );

		if (i - fader_data->stepWidth < fader_data->new_transparency)
			fader_data->stepWidth = i - fader_data->new_transparency; // - don't overwrap!

		cout << "Trans OUT: " << i << " StepWidth: " << fader_data->new_transparency << endl;
		Sleep ( 30 );
	}

	fader_data->fader->transparency = i;
}

static void fader_color (struct fader_start_data*  fader_data) {
}