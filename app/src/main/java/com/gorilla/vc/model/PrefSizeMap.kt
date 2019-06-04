package com.gorilla.vc.model

import android.util.Size
import org.doubango.tinyWRAP.tmedia_pref_video_size_t

object PrefSizeMap {
    private val map: HashMap<Size, tmedia_pref_video_size_t> = hashMapOf(
            Size(3840, 2160) to tmedia_pref_video_size_t.tmedia_pref_video_size_2160p,
            Size(1920, 1080) to tmedia_pref_video_size_t.tmedia_pref_video_size_1080p,
            Size(1408, 1152) to tmedia_pref_video_size_t.tmedia_pref_video_size_16cif,
            Size(1280, 720) to tmedia_pref_video_size_t.tmedia_pref_video_size_720p,
            Size(1024, 768) to tmedia_pref_video_size_t.tmedia_pref_video_size_xga,
            Size(852, 480) to tmedia_pref_video_size_t.tmedia_pref_video_size_480p,
            Size(800, 480) to tmedia_pref_video_size_t.tmedia_pref_video_size_wvga,
            Size(800, 600) to tmedia_pref_video_size_t.tmedia_pref_video_size_svga,
            Size(704, 576) to tmedia_pref_video_size_t.tmedia_pref_video_size_4cif,
            Size(640, 480) to tmedia_pref_video_size_t.tmedia_pref_video_size_vga,
            Size(480, 320) to tmedia_pref_video_size_t.tmedia_pref_video_size_hvga,
            Size(352, 288) to tmedia_pref_video_size_t.tmedia_pref_video_size_cif,
            Size(320, 240) to tmedia_pref_video_size_t.tmedia_pref_video_size_qvga,
            Size(176, 144) to tmedia_pref_video_size_t.tmedia_pref_video_size_qcif,
            Size(128, 96) to tmedia_pref_video_size_t.tmedia_pref_video_size_sqcif
    )

    fun getPrefSize(size: Size): tmedia_pref_video_size_t? {
        return map[size]
    }
}