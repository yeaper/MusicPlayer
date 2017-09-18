package com.yyp.musicplayer.interfaces;

/**
 * 专辑图片旋转动画接口
 */
public interface RecordAnimationListener {
    /**
     * 初始化动画
     */
    void initAnimation();

    /**
     * 开启动画
     */
    void startAnimation();

    /**
     * 暂停动画
     */
    void pauseAnimation();

    /**
     * 停止动画
     */
    void stopAnimation();
}
