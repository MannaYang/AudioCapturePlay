# AudioCapturePlay
基于AudioRecord录制原始pcm音频，使用开源库lame实时转换pcm音频为MP3格式音频，采用Service、MediaPlayer播放MP3，提供录制音频计时器显示，音频音量分贝值显示，音频频谱显示，录制、播放状态控制等
## 功能简介
  目前包含基本的音频录制、播放操作,功能如下:
  1. 基于AudioRecord录制原始PCM格式音频数据
  2. 基于lame库实时转换PCM音频为MP3格式音频
  3. 基于原始lame项目中C文件编译生成对应so文件、提供调用lame编码封装类
  4. 基于FFT格式化PCM数据并实时显示音频频谱
  5. 提供录制音频计时器显示、音量分贝值显示、录制开始、暂停、继续等状态控制与文件写入
  6. 提供AudioPlayManager对象控制MediaPlayer播放、暂停、继续状态、Timer定时更新SeekBar进度条
  7. 提供ObjectAnimator方式实现唱针、唱片旋转、复原动画操作
 
  其它音频格式:
  1. wav、m4a、aac可在录制PCM格式实时回调中添加相应头文件、转换操作
## lame编解码
  1. lame_encode_buffer_interleaved 该方法为传入双声道音频buffer,如果AudioCapture中使用AudioFormat.CHANNEL_IN_STEREO
  2. lame_encode_buffer 该方法为传入单声道音频buffer,如果AudioCapture中使用AudioFormat.CHANNEL_IN_MONO
## Chronometer、RoundedBitmapDrawable控件类
  1. Chronometer为原生计时器,提供计时、倒计时等功能,初始格式为00:00,通过setFormat格式化为00:00:00,暂停、继续计时需减掉已计时时间戳
  2. RoundedBitmapDrawable可作为圆角Bitmap使用,通过setCornerRadius、setCircular可实现圆角设置、圆型
## 公共库
  1. 包含录音控制类、lame编解码cpp文件、编译so文件、Service播放控制类,使用方式参见app中AudioCaptureActivity.class
## 截图展示
  录制开始、暂停、完成 ：
    
  ![image](https://github.com/MannaYang/AudioCapturePlay/blob/master/screenshot/ic_start.png)
  
  音频文件 ：
    
  ![image](https://github.com/MannaYang/AudioCapturePlay/blob/master/screenshot/ic_audio_file.png)
  
  播放准备 ：
    
  ![image](https://github.com/MannaYang/AudioCapturePlay/blob/master/screenshot/ic_play_start.png)
  
  播放中 ：
    
  ![image](https://github.com/MannaYang/AudioCapturePlay/blob/master/screenshot/ic_playing.png)
  
## 感谢开源
  1. 音频频谱柱状图  https://github.com/zhaolewei/MusicVisualizer
  2. lame编解码库   https://sourceforge.net/projects/lame/files/lame 

## 我的个人新球

  免费加入星球一起讨论项目、研究新技术,共同成长!
  
![image](https://github.com/MannaYang/AudioCapturePlay/blob/master/screenshot/xiaomiquan.png)
