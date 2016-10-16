#include "com_apress_movieplayer_AviPlayer.h"

#include <limits.h>
#include <android/bitmap.h>
#include <android/log.h>

#include <avilib.h>

#define LOG_TAG "AviPlayer"

#define LOG_PRINT(level,fmt,...) \
		__android_log_print(level, LOG_TAG, "%s: " fmt, __PRETTY_FUNCTION__, ##__VA_ARGS__)

#define LOG_DEBUG(fmt,...) \
		LOG_PRINT(ANDROID_LOG_DEBUG, fmt, ##__VA_ARGS__)

#define LOG_WARNING(fmt,...) \
		LOG_PRINT(ANDROID_LOG_WARN, fmt, ##__VA_ARGS__)

#define LOG_ERROR(fmt,...) \
		LOG_PRINT(ANDROID_LOG_ERROR, fmt, ##__VA_ARGS__)

#define LOG_INFO(fmt,...) \
		LOG_PRINT(ANDROID_LOG_INFO, fmt, ##__VA_ARGS__)

/**
 * AVI player instance fields.
 */
typedef struct avi_player {
	JNIEnv* env;
	jobject obj;
	jclass clazz;

	/* SurfaceHolder */
	jmethodID lockCanvasMethodId;
	jmethodID unlockCanvasAndPostMethodId;

	/* Canvas */
	jmethodID drawBitmapMethodId;

	jfieldID isPlayingFieldId;
	jobject surfaceHolder;
	jobject bitmap;

} avi_player_t;

/**
 * Calls the new bitmap method with the given width and height.
 *
 * @param p avi player.
 * @param width bitmap width.
 * @param height bitmap height.
 * @return bitmap instance.
 */
jobject newBitmap(avi_player_t* p, int width, int height) {
	jobject bitmap = 0;
	jmethodID newBitmapMethodId = 0;

	LOG_DEBUG("BEGIN p=%p width=%d height=%d", p, width, height);

	newBitmapMethodId = (*p->env)->GetStaticMethodID(p->env, p->clazz,
			"newBitmap", "(II)Landroid/graphics/Bitmap;");
	if (0 == newBitmapMethodId) {
		LOG_ERROR("Unable to find newBitmap method");
		goto exit;
	}

	bitmap = (*p->env)->CallStaticObjectMethod(p->env, p->clazz,
			newBitmapMethodId, width, height);

exit:
	LOG_DEBUG("END bitmap=%p", bitmap);
	return bitmap;
}

/**
 * Caches the AVI player.
 *
 * @param p avi player.
 * @return result code.
 */
int cacheAviPlayer(avi_player_t* p) {
	int result = 0;
	jclass clazz = 0;
	jfieldID isPlayingFieldId = 0;

	/* Get object class instance. */
	clazz = (*p->env)->GetObjectClass(p->env, p->obj);
	if (0 == clazz) {
		LOG_ERROR("Unable to get class");
		goto exit;
	}

	/* Get is playing field id. */
	isPlayingFieldId = (*p->env)->GetFieldID(p->env, clazz, "isPlaying", "Z");
	if (0 == isPlayingFieldId) {
		LOG_ERROR("Unable to get isPlaying field id");
		(*p->env)->DeleteLocalRef(p->env, clazz);
		goto exit;
	}

	result = 1;

	p->clazz = clazz;
	p->isPlayingFieldId = isPlayingFieldId;

exit:
	return result;
}

/**
 * Cache surface holder method ids.
 *
 * @param p avi player.
 * @return result code.
 */
int cacheSurfaceHolderMethods(avi_player_t* p) {
	int result = 0;

	jclass surfaceHolderClazz = 0;
	jmethodID lockCanvasMethodId = 0;
	jmethodID unlockCanvasAndPostMethodId = 0;

	surfaceHolderClazz = (*p->env)->FindClass(p->env,
			"android/view/SurfaceHolder");
	if (0 == surfaceHolderClazz) {
		LOG_ERROR("Unable to find surfaceHolder class");
		goto exit;
	}

	lockCanvasMethodId = (*p->env)->GetMethodID(p->env, surfaceHolderClazz,
			"lockCanvas", "()Landroid/graphics/Canvas;");
	if (0 == lockCanvasMethodId) {
		LOG_ERROR("Unable to find lockCanvas method");
		goto exit;
	}

	unlockCanvasAndPostMethodId = (*p->env)->GetMethodID(p->env,
			surfaceHolderClazz, "unlockCanvasAndPost",
			"(Landroid/graphics/Canvas;)V");
	if (0 == unlockCanvasAndPostMethodId) {
		LOG_ERROR("Unable to find unlockCanvasAndPost method");
		goto exit;
	}

	p->lockCanvasMethodId = lockCanvasMethodId;
	p->unlockCanvasAndPostMethodId = unlockCanvasAndPostMethodId;

	result = 1;

exit:
	return result;
}

/**
 * Cache canvas method ids.
 *
 * @param p avi player.
 * @return result code
 */
int cacheCanvasMethods(avi_player_t* p) {
	int result = 0;

	jclass canvasClazz = 0;
	jmethodID drawBitmapMethodId = 0;

	canvasClazz = (*p->env)->FindClass(p->env, "android/graphics/Canvas");
	if (0 == canvasClazz) {
		LOG_ERROR("Unable to find canvas class");
		goto exit;
	}

	drawBitmapMethodId = (*p->env)->GetMethodID(p->env, canvasClazz,
			"drawBitmap",
			"(Landroid/graphics/Bitmap;FFLandroid/graphics/Paint;)V");
	if (0 == drawBitmapMethodId) {
		LOG_ERROR("Unable to get drawBitmap method");
		goto exit;
	}

	p->drawBitmapMethodId = drawBitmapMethodId;

	result = 1;

exit:
	return result;
}

/**
 * Draw bitmap.
 *
 * @param p avi player.
 * @return result code
 */
int drawBitmap(avi_player_t* p) {
	int result = 0;
	jobject canvas = 0;

	/* Lock and get canvas */
	canvas = (*p->env)->CallObjectMethod(p->env, p->surfaceHolder,
			p->lockCanvasMethodId);
	if (0 == canvas) {
		LOG_ERROR("Unable to lock canvas");
		goto exit;
	}

	/* Draw bitmap */
	(*p->env)->CallVoidMethod(p->env, canvas, p->drawBitmapMethodId, p->bitmap,
			0.0, 0.0, 0);

	/* Unlock and post canvas */
	(*p->env)->CallVoidMethod(p->env, p->surfaceHolder,
			p->unlockCanvasAndPostMethodId, canvas);

	result = 1;

exit:
	(*p->env)->DeleteLocalRef(p->env, canvas);

	return result;
}

/**
 * Opens the given AVI movie file.
 *
 * @param movieFile movie file.
 * @return avi file.
 */
avi_t* openAvi(avi_player_t* p, jstring movieFile) {
	avi_t* avi = 0;
	const char* fileName = 0;

	/* Get movie file as chars. */
	fileName = (*p->env)->GetStringUTFChars(p->env, movieFile, 0);
	if (0 == fileName) {
		LOG_ERROR("Unable to get movieFile as chars");
		goto exit;
	}

	/* Open AVI input file. */
	avi = AVI_open_input_file(fileName, 1);

	/* No need to have the file name. */
	(*p->env)->ReleaseStringUTFChars(p->env, movieFile, fileName);

exit:
	return avi;
}

void Java_com_apress_movieplayer_AviPlayer_render(JNIEnv* env, jobject obj,
		jobject surfaceHolder, jstring movieFile) {
	avi_player_t ap;
	avi_t* avi = 0;
	jboolean isPlaying = 0;
	double frameRate = 0;
	long frameDelay = 0;
	long frameSize = 0;
	char* frame = 0;
	int keyFrame = 0;

	/* Cache environment and object. */
	memset(&ap, 0, sizeof(avi_player_t));
	ap.env = env;
	ap.obj = obj;
	ap.surfaceHolder = surfaceHolder;

	/* Cache surface holder and canvas method ids. */
	if (!cacheAviPlayer(&ap) || !cacheSurfaceHolderMethods(&ap)
			|| !cacheCanvasMethods(&ap)) {
		LOG_ERROR("Unable to cache the method ids");
		goto exit;
	}

	/* Open AVI input file. */
	avi = openAvi(&ap, movieFile);
	if (0 == avi) {
		LOG_ERROR("Unable to open AVI file.");
		goto exit;
	}

	/* New bitmap. */
	ap.bitmap = newBitmap(&ap, AVI_video_width(avi), AVI_video_height(avi));
	if (0 == ap.bitmap) {
		LOG_ERROR("Unable to generate a bitmap");
		goto exit;
	}

	/* Frame rate. */
	frameRate = AVI_frame_rate(avi);
	LOG_DEBUG("frameRate=%f", frameRate);

	frameDelay = (long) (1000 / frameRate);
	LOG_DEBUG("frameDelay=%ld", frameDelay);

	/* Play file. */
	while (1) {
		/* Lock the bitmap and get access to raw data. */
		AndroidBitmap_lockPixels(env, ap.bitmap, (void**) &frame);

		/* Copy the next frame from AVI file to bitmap data. */
		frameSize = AVI_read_frame(avi, frame, &keyFrame);
		LOG_DEBUG("frame=%p keyFrame=%d frameSize=%d error=%s",
				frame, keyFrame, frameSize, AVI_strerror());

		/* Unlock bitmap. */
		AndroidBitmap_unlockPixels(env, ap.bitmap);

		/* Synchornize on the current object. */
		if (0 != (*env)->MonitorEnter(env, obj)) {
			LOG_ERROR("Unable to monitor enter");
			goto exit;
		}

		isPlaying = (*env)->GetBooleanField(env, obj, ap.isPlayingFieldId);

		/* Done synchronizing. */
		if (0 != (*env)->MonitorExit(env, obj)) {
			LOG_ERROR("Unable to monitor exit");
			goto exit;
		}

		/* If there is no frame or player stoped. */
		if ((-1 == frameSize) || (0 == isPlaying)) {
			break;
		}

		/* Draw bitmap. */
		drawBitmap(&ap);

		/* Wait for the next frame. */
		usleep(frameDelay);
	}

exit:
	if (0 != avi) {
		AVI_close(avi);
	}
}
