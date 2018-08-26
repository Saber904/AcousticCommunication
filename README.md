# AcousticCommunication
使用Android手机设备的扬声器和麦克风实现音频通信
## 通信原理
发送端：使用调制技术将数字信号转换为音频模拟信号通过扬声器发送；

接收端：麦克风接受音频信号，并通过解调技术将音频模拟信号转换为数字信号；

具体可参考本人论文：http://kns.cnki.net/KCMS/detail/detail.aspx?dbcode=CMFD&dbname=CMFD201701&filename=1016770569.nh&v=MDgyMDkrUnBGeXJtV3J2S1ZGMjZHTFMvSHRUS3BwRWJQSVI4ZVgxTHV4WVM3RGgxVDNxVHJXTTFGckNVUkxLZmI=
## SDK
SDK在项目的AcousticCommunicationLibrary模块
### 信号发送类（SignalSender）
实例化：
```
SignalSender signalSender = SignalSender.getDefault();//得到默认实现
```
发送数据：
```
int[] numSignal = {1, 2, 3, 4, 5};
signalSender.sendSignal(num_signal);//将int数组转化为音频信号发送出去
```
### 信号接收类（SignalReceiver）
实例化：
```
SignalReceiver signalReceiver = SignalReceiver.getDefault();//得到默认实现
```
设置数据接收回调：
```
SignalReceiver signalReceiver.setOnReceivedListener(
                            new SignalReceiver.OnSignalReceivedListener() {
                                @Override
                                public void onReceived(int[] result) {
                                    //接收到数据
                                }

                                @Override
                                public void onNotReceived() {
                                    //未接收到数据
                                }
                            }
                    );
```
处理数据：
```
signalReceiver.startReceiving();//开始录音
signalReceiver.stopReceiving();//停止录音，并对音频数据进行处理，并将处理结果通过回调返回
```
### 扩展
您可以使用其他通信算法实现SignalGenerator和SignalDemodulator接口来生成和处理信号，并通过
```
signalSender.setSignalGenerator(SignalGenerator generator);
signalReceiver.setSignalDemodulator(SignalDemodulator demodulator);
```
来设置相应的SignalGenerator和SignalDemodulator，目前提供了DPSK和OFDM信号的实现（默认实现是OFDM）。



