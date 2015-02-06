/* use standard namespace */
#include <iostream>
using namespace std;

#include <jni.h>

/* windows headers */
#include "windows.h"
#include "Winuser.h"

/* fader headers */
#include "fader_thread.h"
#include "handle_windows.h"

/* typeDef */
typedef DWORD (WINAPI *PSLWA)(HWND, DWORD, BYTE, DWORD);

/* Header for class de_neue_phase_asterisk_ClickDial_widgets_CallWindow */

#ifndef _Included_de_neue_phase_asterisk_ClickDial_widgets_CallWindow
#define _Included_de_neue_phase_asterisk_ClickDial_widgets_CallWindow
#ifdef __cplusplus
extern "C" {
#endif

#define LWA_COLORKEY 0x00000001
#define LWA_ALPHA 0x00000002

#define CHANGE_COLOR 0
#define CHANGE_TRANSPARENCY_FADE_IN 5
#define CHANGE_TRANSPARENCY_FADE_OUT 10

/*
 * Class:     de_neue_phase_asterisk_ClickDial_widgets_CallWindow
 * Method:    initModule
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_de_neue_1phase_asterisk_ClickDial_widgets_CallWindow_initModule
  (JNIEnv *, jclass, jint, jint);

/*
 * Class:     de_neue_phase_asterisk_ClickDial_widgets_CallWindow
 * Method:    destroyModule
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_de_neue_1phase_asterisk_ClickDial_widgets_CallWindow_destroyModule
  (JNIEnv *, jclass, jint);

/*
 * Class:     de_neue_phase_asterisk_ClickDial_widgets_CallWindow
 * Method:    fadeIn
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_de_neue_1phase_asterisk_ClickDial_widgets_CallWindow_fadeIn
  (JNIEnv *, jclass, jint, jint, jint);

/*
 * Class:     de_neue_phase_asterisk_ClickDial_widgets_CallWindow
 * Method:    fadeOut
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_de_neue_1phase_asterisk_ClickDial_widgets_CallWindow_fadeOut
  (JNIEnv *, jclass, jint, jint, jint);

/*
 * Class:     de_neue_phase_asterisk_ClickDial_widgets_CallWindow
 * Method:    getTransparency
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_de_neue_1phase_asterisk_ClickDial_widgets_CallWindow_getTransparency
  (JNIEnv *, jclass, jint);

/*
 * Class:     de_neue_phase_asterisk_ClickDial_widgets_CallWindow
 * Method:    changeColor
 * Signature: (IIII)I
 */
JNIEXPORT jint JNICALL Java_de_neue_1phase_asterisk_ClickDial_widgets_CallWindow_changeColor
  (JNIEnv *, jclass, jint, jint, jint, jint);


/**
 * used to keep track of the windows
 */
struct fader_windows {
	struct fader_windows * next;
	struct fader_windows * prev;
	
	int	   windowID;
	struct fader_data * data;
};

/**
 * used to store fader data 
 */
struct fader_data {
	int transparency;
	int rgb[3];
	HWND hWnd;  /* window handle */
	HANDLE ghMutex;
};

/**
 * used to start a fader thread
 */
struct fader_start_data {
	int new_transparency;
	int new_rgb[3];
	int stepWidth;
	char change;			/* 0 - change transparency ; 1 - change RGB */
	struct fader_data* fader;
	DWORD ThreadID;
};

#ifdef __cplusplus
}
#endif
#endif
