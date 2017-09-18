package com.yyp.musicplayer.util

import android.content.res.Resources

/**
 * 毫秒转换为播放时间
 */
fun msecToPlayTime(time: Int): String{
    var min = time.div(60000).toString()
    var second = time.rem(60000).div(1000).toString()
    if(min.toInt() < 10){
        min = "0"+min
    }
    if(second.toInt() < 10){
        second = "0"+second
    }

    return min+":"+second
}

fun dpToPx(dp: Int): Int = (dp * Resources.getSystem().displayMetrics.density).toInt()