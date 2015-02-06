#include "de_neue_phase_asterisk_ClickDial_widgets_CallWindow.h"

struct fader_windows * findFaderWindow (int handle, short createIfNotFound) {
	extern struct fader_windows faderWindowHead;
	extern HANDLE faderWindowHeadLock;

	cout << "findFaderWindow: try to find the window " << handle << " (if not found create: " << createIfNotFound << endl;

	struct fader_windows * cur		= &faderWindowHead;
	struct fader_windows * found	= NULL;

	WaitForSingleObject( faderWindowHeadLock , INFINITE);

	do {
		cout << "findFaderWindow: cur = " << cur->windowID << endl;
		if (cur->windowID == handle) 
		{
			found = cur;
			break;
		}
	} while ( cur->next != NULL && (cur = cur->next) != NULL);

	cout << "findFaderWindow: found = " << found  << " cur = " << cur->windowID << endl;

	if (createIfNotFound == 1 && found == NULL)
		found = createNewFaderWindows(handle, cur);

	ReleaseMutex ( faderWindowHeadLock );
	return found;

}

struct fader_windows * createNewFaderWindows (const int handle, struct fader_windows *prev) {
	struct fader_windows * window	= new struct fader_windows;	
	window->windowID	= handle;
	window->next		= NULL;
	window->prev		= prev;
	window->data		= new struct fader_data;
	prev->next			= window;
	cout << "createNewFaderWindows: window @ " << window << " with data @ " << window->data << endl;
	return window;
}

void initWindow (int handle, int transparency) {
	struct fader_windows * cur = findFaderWindow (handle, 1);
	cout << "initWindow: found window " << handle << " @ " << cur << endl; 
	cur->data->ghMutex		= CreateMutex(  NULL,              // default security attributes
	   				    				   FALSE,             // initially not owned
										   NULL);             // unnamed mutex
	cur->data->hWnd			= (HWND) handle;
	cur->data->transparency = transparency;
	SetWindowLong (cur->data->hWnd , (-20) , GetWindowLong (cur->data->hWnd , (-20) ) | 0x00080000 );
}

void delWindow (int handle) {
	extern HANDLE faderWindowHeadLock;

	struct fader_windows * cur = findFaderWindow (handle, 0);
	struct fader_windows * next = cur->next;
	struct fader_windows * prev = cur->prev;

	if (cur == NULL)
	{
		cout << "delWindow: did not found window " << handle << endl;
		return;
	}
	/* aquire Lock and del */
	cout << "delWindow: linking next/prev new before lock" << endl;
	WaitForSingleObject( faderWindowHeadLock , INFINITE);

	if (next != NULL) {
		prev->next = next;
		next->prev = prev;
	}
	else
		prev->next = NULL;

	ReleaseMutex ( faderWindowHeadLock );

	cout << "delWindow: closing ghMutex handle ..." << endl;
	CloseHandle(cur->data->ghMutex);

	cout << "delWindow: delete ' data ' object ..." << endl;
	delete cur->data;

	cout << "delWindow: delete ' cur ' object ..." << endl;
	delete cur;
}