/* JNI header file with exported functions */
#include "de_neue_phase_asterisk_ClickDial_widgets_CallWindow.h"
HMODULE hDLL;   /* module */

struct fader_windows faderWindowHead; /* fader data persistence */
HANDLE faderWindowHeadLock;
PSLWA pSetLayeredWindowAttributes; /* SetLayeredWindowAttributes function variableized */

/** ---- Initialisation ----- **/

/*
  * initialize the Module
  * - save the handle
  * - use GetWindowLong to reach extended Window functions
*/
JNIEXPORT void JNICALL Java_de_neue_1phase_asterisk_ClickDial_widgets_CallWindow_initModule
  (JNIEnv *env, jclass clazz, jint handle, jint initialTransparency)
{
	initWindow (handle, initialTransparency);
}

/*
  * used on init time to fill instances
  * - get "SetLayeredWindowAttributes" into pSetLayeredWindowAttributes
  *
*/
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) 
{
	hDLL = LoadLibrary ("user32");
	pSetLayeredWindowAttributes = (PSLWA) GetProcAddress(hDLL,"SetLayeredWindowAttributes");
	faderWindowHeadLock = CreateMutex( NULL,              // default security attributes
	   								   FALSE,             // initially not owned
									   NULL);             // unnamed mutex
	faderWindowHead.data = NULL;
	faderWindowHead.next = NULL;
	faderWindowHead.prev = NULL;
	faderWindowHead.windowID	= -1;
 	return JNI_VERSION_1_4;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved) {
	CloseHandle(faderWindowHeadLock);
	FreeLibrary(hDLL);
}

/** ---- Destruction ----- **/

/*
  * destroy every instance
  * - free the loaded libraries
  * - wait for every thread finished (?)
  *
*/
JNIEXPORT void JNICALL Java_de_neue_1phase_asterisk_ClickDial_widgets_CallWindow_destroyModule
  (JNIEnv *env, jclass clazz, jint handle)
{
	delWindow ( handle );
}

/** ---- worker function ----- **/

JNIEXPORT jint JNICALL Java_de_neue_1phase_asterisk_ClickDial_widgets_CallWindow_fadeIn
  (JNIEnv *env, jclass clazz, jint handle, jint fadeTo, jint stepWidth)
{
	struct fader_windows * cur  = findFaderWindow (handle, 0);

	struct fader_start_data * data = new struct fader_start_data;
	data->new_rgb[0]			= 0; data->new_rgb[1] = 0; data->new_rgb[2] = 0;
	data->new_transparency		= fadeTo;
	data->change				= CHANGE_TRANSPARENCY_FADE_IN;
	data->stepWidth				= stepWidth;
	data->fader					= cur->data;

	cout << "Starting FaderIn Process. Mutex @ "<< &data->fader->ghMutex << endl;
	CreateThread( 
                  NULL,       // default security attributes
                  0,          // default stack size
                  (LPTHREAD_START_ROUTINE) fader_thread, 
                  data,       // no thread function arguments
                  0,          // default creation flags
                  &data->ThreadID); // receive thread identifier

	return 1;
}

JNIEXPORT jint JNICALL Java_de_neue_1phase_asterisk_ClickDial_widgets_CallWindow_fadeOut
  (JNIEnv *env, jclass clazz, jint handle, jint fadeTo, jint stepWidth)
{
	struct fader_windows * cur  = findFaderWindow (handle, 0);

	struct fader_start_data * data = new struct fader_start_data;
	data->new_rgb[0]			= 0; data->new_rgb[1] = 0; data->new_rgb[2] = 0;
	data->new_transparency		= fadeTo;
	data->change				= CHANGE_TRANSPARENCY_FADE_OUT;
	data->stepWidth				= stepWidth;
	data->fader					= cur->data;
	
	cout << "Starting FaderOut Process. Mutex @ "<< &data->fader->ghMutex << endl;
	CreateThread( 
                  NULL,       // default security attributes
                  0,          // default stack size
                  (LPTHREAD_START_ROUTINE) fader_thread, 
                  data,       // no thread function arguments
                  0,          // default creation flags
                  &data->ThreadID); // receive thread identifier

	return 1;
}

JNIEXPORT jint JNICALL Java_de_neue_1phase_asterisk_ClickDial_widgets_CallWindow_getTransparency
  (JNIEnv *env, jclass clazz, jint handle)
{
	int re = 255;
	struct fader_windows * cur = findFaderWindow (handle, 0);

	/* we wait 10 ms */
	if ( WaitForSingleObject( cur->data->ghMutex , 10) != WAIT_OBJECT_0 )
		return -1; // -- something went wrong (waiting too long) - signal that we failed
	
	re = cur->data->transparency;
	ReleaseMutex(cur->data->ghMutex);

	cout << "getTransparency: " << re << endl;
	return re;
}

JNIEXPORT jint JNICALL Java_de_neue_1phase_asterisk_ClickDial_widgets_CallWindow_changeColor
  (JNIEnv *env, jclass clazz, jint R, jint G, jint B, jint stepWidth)
{
	struct fader_start_data data;
	data.new_rgb[0]				= R; data.new_rgb[1] = G; data.new_rgb[2] = B;
	data.new_transparency		= -1;
	data.change					= CHANGE_COLOR;
	data.stepWidth				= stepWidth;
	data.fader					= NULL;

	return 1;
}



