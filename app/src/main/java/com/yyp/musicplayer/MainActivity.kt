package com.yyp.musicplayer

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import com.squareup.picasso.Picasso
import com.yyp.musicplayer.bean.MusicList
import com.yyp.musicplayer.interfaces.RecordAnimationListener
import com.yyp.musicplayer.server.MusicPlayerService
import com.yyp.musicplayer.util.dpToPx
import com.yyp.musicplayer.util.msecToPlayTime
import kotlinx.android.synthetic.main.activity_music_play.*

class MainActivity : Activity(), RecordAnimationListener {

    var serviceIntent: Intent? = null

    var serviceConnection: ServiceConnection? = null
    private var binder: MusicPlayerService.MyBinder? = null
    var isFinished = false // 是否结束当前activity的标志
    var isFirstPlay = true // 是否首次播放
    var isPause = true

    val data = arrayListOf<MusicList>() // 播放列表

    var musicUrl = ""
    var playIndex = -1

    private var currentValue = 0f
    private var objAnim: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_play)

        data.add(MusicList(
                "斑马斑马",
                "http://bmob-cdn-5565.b0.upaiyun.com/2017/05/13/2c794568409de62b8097f9c7768749d1.jpg",
                "http://bmob-cdn-5565.b0.upaiyun.com/2017/05/13/35f172484029b265801c2720175a3f8c.jpg",
                "http://bmob-cdn-5565.b0.upaiyun.com/2017/02/21/0cecbbd1405b824280bc79352a607d83.mp3"
        ))
        data.add(MusicList(
                "k Eyed Peas",
                "http://img3.imgtn.bdimg.com/it/u=1069641098,697723783&fm=27&gp=0.jpg",
                "http://bmob-cdn-5565.b0.upaiyun.com/2017/05/13/58cf441240e7f5cd8039f3e10a5d923a.jpg",
                "http://bmob-cdn-5565.b0.upaiyun.com/2017/02/18/f1e039e640d89a3f803fef30b5bd932d.mp3"
        ))

        init()
    }

    fun init(){
        serviceIntent = Intent(this, MusicPlayerService::class.java)

        initAnimation()
        initMediaPlayer()
        setListener()

        setPlayView(data[++playIndex])
    }

    /**
     * 初始化动画
     */
    override fun initAnimation(){
        // 设置动画，从上次停止位置开始,这里是顺时针旋转360度
        objAnim = ObjectAnimator.ofFloat(programPlayRecordImage, "Rotation",
                currentValue - 360, currentValue)
        // 设置持续时间
        objAnim!!.duration = 30000
        // 设置循环播放
        objAnim!!.repeatCount = ObjectAnimator.INFINITE
        // 设置匀速播放
        val lin: LinearInterpolator = LinearInterpolator()
        objAnim!!.interpolator = lin
        // 设置动画监听
        objAnim!!.addUpdateListener({ animation ->
            // 监听动画执行的位置，以便下次开始时，从当前位置开始
            currentValue = animation.animatedValue as Float
        })
    }

    /**
     * 开启动画
     */
    override fun startAnimation() {
        if(!objAnim!!.isStarted) {
            // 每次暂停动画后，都得初始化一次
            initAnimation()
            objAnim!!.start()
        }
    }

    /**
     * 停止动画
     */
    override fun stopAnimation() {
        if(objAnim!!.isStarted){
            objAnim!!.end()
            currentValue = 0f // 重置起始位置
        }
    }

    /**
     * 暂停动画
     */
    override fun pauseAnimation() {
        if(objAnim!!.isRunning){
            objAnim!!.cancel()
        }
    }

    fun initMediaPlayer(){
        serviceIntent!!.putExtra("action", "init")
        serviceIntent!!.putExtra("musicUrl", "")
        startService(serviceIntent)
    }

    fun setListener(){
        programPlayStart.setOnClickListener { playPause() }
        programPlayPrevious.setOnClickListener {
            // 播放到第一首歌时，下标移动到最后一首
            if(playIndex == 0){
                playIndex = data.size
            }

            resetPlayer()
            setPlayView(data[--playIndex])
            playPause()
        }
        programPlayNext.setOnClickListener {
            // 播放到最后一首歌时，下标移动到第一首
            if(playIndex == data.size.minus(1)){
                playIndex = -1
            }

            resetPlayer()
            setPlayView(data[++playIndex])
            playPause()
        }
    }

    private var handler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            when(msg!!.what){
                INIT_SEEKBAR -> {
                    // 设置进度条的最大长度
                    programPlayProgressBar.max = binder!!.musicDuration
                    programPlayProgressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                            // 手动控制进度：需要先控制播放，再设置进度
                            if(fromUser){
                                isPause = true // 改变进度条的值，即可播放
                                playPause()
                                binder!!.seekTo(progress)

                            }
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar) {

                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar) {

                        }
                    })
                }
                UPDATE_SEEKBAR -> {
                    // 改变当前进度条的值、起始时间
                    programPlayProgressBar.progress = binder!!.musicCurrentPosition
                    programPlayStartTime.text = msecToPlayTime(binder!!.musicCurrentPosition)
                }
                UPDATE_SEEKBAR_BUFFER -> {
                    // 设置缓存进度
                    programPlayProgressBar.secondaryProgress = msg.arg1
                }
                NEXT_MUSIC -> { // 循环播放
                    resetPlayer()
                    if(playIndex == data.size.minus(1)){
                        playIndex = -1
                    }
                    setPlayView(data[++playIndex])
                    playPause()
                }
                MUSIC_DURATION -> {
                    // 设置歌曲总时长
                    programPlayEndTime.text = msecToPlayTime(binder!!.musicDuration)
                }
            }
        }
    }

    /**
     * 绑定服务
     */
    fun bindService(){
        if (serviceConnection == null) {
            serviceConnection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {

                    binder = service as MusicPlayerService.MyBinder

                    val msg1 = handler.obtainMessage()
                    msg1.what = INIT_SEEKBAR
                    handler.sendMessage(msg1)

                    // 连接之后启动子线程获取当前进度
                    object : Thread() {
                        override fun run() {
                            while (true) {

                                if(isFinished){
                                    break
                                }

                                val msg2 = handler.obtainMessage()
                                msg2.what = UPDATE_SEEKBAR
                                handler.sendMessage(msg2)

                                binder!!.setOnMusicBufferingListener{ progress ->
                                    val msg3 = handler.obtainMessage()
                                    msg3.what = UPDATE_SEEKBAR_BUFFER
                                    msg3.arg1 = progress
                                    handler.sendMessage(msg3)
                                }

                                binder!!.setOnCompleteListener {
                                    val msg4 = handler.obtainMessage()
                                    msg4.what = NEXT_MUSIC
                                    handler.sendMessage(msg4)
                                }

                                val msg5 = handler.obtainMessage()
                                msg5.what = MUSIC_DURATION
                                handler.sendMessage(msg5)

                                // 100ms更新一次
                                try {
                                    Thread.sleep(100)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }.start()
                }

                override fun onServiceDisconnected(name: ComponentName) {
                }
            }

            // 以绑定方式连接服务
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    /**
     * 设置播放界面
     */
    fun setPlayView(item: MusicList){
        // 设置背景、专辑图
        Picasso.with(this)
                .load(item.bg_img)
                .resize(dpToPx(400), dpToPx(330))
                .config(Bitmap.Config.RGB_565)
                .into(programPlayHeaderBg)
        Picasso.with(this)
                .load(item.record_img)
                .resize(dpToPx(150), dpToPx(150))
                .config(Bitmap.Config.RGB_565)
                .into(programPlayRecordImage)
        // 设置歌单、节目名
        programPlayName.text = item.name
        // 设置歌曲总时长
        programPlayEndTime.text = "00:00"
        // 歌曲Url
        musicUrl = item.audio_url
    }

    /**
     * 播放/停止操作
     */
    fun playPause(){
        if(isPause){
            isPause = false
            programPlayStart.setImageResource(R.drawable.program_play_pause)
            // 开启图片旋转动画
            startAnimation()

            // 是否第一次播放
            if(!isFirstPlay){
                serviceIntent!!.putExtra("action", "rePlay")
                serviceIntent!!.putExtra("musicUrl", "")
                startService(serviceIntent)
            }else{
                isFirstPlay = false
                serviceIntent!!.putExtra("action", "play")
                serviceIntent!!.putExtra("musicUrl", musicUrl)
                startService(serviceIntent)
            }
        }else {
            isPause = true
            programPlayStart.setImageResource(R.drawable.program_play_start)
            // 暂停动画
            pauseAnimation()

            serviceIntent!!.putExtra("action", "pause")
            serviceIntent!!.putExtra("musicUrl", "")
            startService(serviceIntent)
        }

        // 播放后，再绑定服务
        bindService()
    }

    /**
     * 重置播放器
     */
    fun resetPlayer(){
        // 停止服务
        stopService(serviceIntent)
        // 关闭动画
        stopAnimation()

        isPause = true
        isFirstPlay = true
        programPlayStart.setImageResource(R.drawable.program_play_start)
        programPlayProgressBar.progress = 0
        programPlayStartTime.text = "00:00"
    }

    companion object{
        val INIT_SEEKBAR = 1    // 初始化seekbar信息
        val UPDATE_SEEKBAR = 2  // 更新seekbar
        val UPDATE_SEEKBAR_BUFFER = 4  // 更新seekbar缓存进度
        val NEXT_MUSIC = 5      // 播放下一首
        val MUSIC_DURATION = 6  // 获取音乐总时长
    }

    override fun onDestroy() {
        super.onDestroy()
        // 停止更新UI
        isFinished = true
        // 释放播放器资源
        serviceIntent!!.putExtra("action", "release")
        serviceIntent!!.putExtra("musicUrl", "")
        startService(serviceIntent)
        // 解绑服务
        if(serviceConnection != null){
            unbindService(serviceConnection)
        }
    }
}
