void initWindow (int handle, int transparency);
void delWindow (int handle);
struct fader_windows * createNewFaderWindows (const int windowID, struct fader_windows *prev);
struct fader_windows * findFaderWindow (int handle, short createIfNotFound);