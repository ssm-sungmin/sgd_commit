using System;
using System.Net;
using System.Net.Sockets;
using System.IO;
using System.Diagnostics;
using System.Text;
using System.Threading;

using System.Runtime.InteropServices;
using CSCore.Win32;

using CSCore.CoreAudioAPI;
using CSCore;
using CSCore.SoundIn;
using CSCore.Codecs.WAV;
using CSCore.Streams;           //SoundInSource
using CSCore.Streams.Effects;   //dmo effect

using System.Windows;
using System.Windows.Controls;

using System.Windows.Interop;
//using System.Windows.Forms;

namespace SpeakerUI
{
    class AudioLBCapture
    {
        private static AudioLBCapture _instance = null;
        private WasapiLoopback_M capture;
        private static IPEndPoint audioIP;
        private UdpClient client, volumeClient;
        private Thread mastervolumeControl;
        private int volume_Port;
        public bool check = false;     //사용중인지 판단
        private int volumeValue;


        //private AudioLBCapture()
        //{
        //    _instance = this;
        //    //compulsion = new Button();
        //}
        private AudioLBCapture(IPEndPoint ip)
        {
            audioIP = ip;
            _instance = this;
        }

        //public static AudioLBCapture setAudioLBCapture()
        //{
        //    //if (_instance == null)
        //    //{
        //    _instance = new AudioLBCapture();
        //    //}
        //    return _instance;
        //}
        public static AudioLBCapture setAudioLBCapture(IPEndPoint ip)
        {
            //if (_instance == null)
            //{
            _instance = new AudioLBCapture(ip);
            //}
            return _instance;
        }

        public static AudioLBCapture getAudioLBCapture(IPEndPoint ip)
        {
            //if (_instance == null)
            //{
            //    _instance = new AudioLBCapture(ip);
            //}
            audioIP = ip;
            return _instance;
        }

        public bool InitRecord()
        {
            WaveFormat waveformat = new WaveFormat(44100, 16, 2);
            capture = new WasapiLoopback_M(75, waveformat);
            capture.Initialize();

            //memStream = new MemoryStream();
            client = new UdpClient();
            volume_Port = 12000;
            volumeValue = 0;
            check = true;

            //처음으로 back?? 언제일까요
            if (GetVolume() == -1)
            {
                EndRecord();
            }
            
            return check;
        }

        public bool StartRecord()
        {
            InitRecord();

            capture.DataAvailable += Capture_DataAvailable;
            capture.Start();

            SetVolume(0);
            mastervolumeControl = new Thread(confirm_Volume);
            mastervolumeControl.Start();

            return true;
        }

        public void EndRecord()
        {
            capture.Stop();

            this.Close();
        }


        private void Capture_DataAvailable(object sender, DataAvailableEventArgs e)
        {
            int minsize = e.ByteCount;
            byte[] buffer = new byte[minsize];
            byte multiple = (byte)2;
            short[] shortBuffer = new short[minsize];

            Buffer.BlockCopy(e.Data, e.Offset, shortBuffer, 0, minsize);
            for (int i = 0; i < minsize; i++)
            {
                if (shortBuffer[i] > 16382)
                    shortBuffer[i] = 32767;
                else if (shortBuffer[i] < -16382)
                    shortBuffer[i] = -32768;
                else shortBuffer[i] *= multiple;
            }
            Buffer.BlockCopy(shortBuffer, 0, buffer, 0, minsize);

            client.Send(buffer, minsize, audioIP);
        }

        //const uint WM_LBUTTONDOWN = 0x201;
        //const uint WM_LBUTTONUP = 0x202;
        //[DllImport("user32.dll")]
        //private static extern int SendMessage(IntPtr handle, UInt32 message, int wParam, int lParam);
        //[DllImport("user32.dll")]
        //public static extern int FindWindow(string lpClassName, string lpWindowName);
        //[DllImport("user32.dll", CharSet = CharSet.Auto, SetLastError = true)]
        //static extern IntPtr FindWindowEx(IntPtr hwndParent, IntPtr hwndChildAfter, string lpszClass, string lpszWindow);

        private void confirm_Volume()
        {
            byte[] returndata;

            try
            {
                volumeClient = new UdpClient(volume_Port);
                IPEndPoint volumeEP = new IPEndPoint(IPAddress.Any, 0);

                while(check){

                    returndata = volumeClient.Receive(ref volumeEP);
                    string volumeData = Encoding.UTF8.GetString(returndata);
                    volumeValue = Int32.Parse(volumeData);

                    if (SetVolume(volumeValue * 100 / 15) == -1)
                    {
                        check = false;
                        volumeClient.Close();
                        //EndRecord();
                        MainWindow.getMain().theEnd();
                    }
                }
            }
            catch (IOException e)
            {
                Console.WriteLine(e.ToString());
            }
        }

        public int GetVolume()
        {
            try
            {
                MMDevice dev = capture.Device;
                AudioEndpointVolume caev = AudioEndpointVolume.FromDevice(dev);
                    
                float currentVolume = caev.MasterVolumeLevelScalar * 15;
                
                return (int)currentVolume;
            }
            catch (Exception ex)
            {
                System.Windows.MessageBox.Show("#1 해당 PC의 오디오를 녹음 할 수 없습니다. 자세한 것은 문의해 주세요.");
                return -1;
            }
        }

        public float SetVolume(int level)
        {
            try
            {
                //MMDeviceEnumerator MMDE = new MMDeviceEnumerator();
                //MMDeviceCollection DevCol = MMDE.EnumAudioEndpoints(DataFlow.All, DeviceState.All);
                MMDevice dev = capture.Device;

                if (dev.DeviceState == DeviceState.Active)
                {
                    //연결 해제
                    if (level  < 0)
                    {
                        return -1;
                    }
                    var newVolume = (float)Math.Max(Math.Min(level, 100), 0) / (float)100;
                    AudioEndpointVolume aev = AudioEndpointVolume.FromDevice(dev);

                    //Set at maximum volume
                    aev.MasterVolumeLevelScalar = newVolume;
                    return newVolume;
                }
                else
                {
                    //Console.WriteLine("Ignoring device " + dev.FriendlyName + " with state " + dev.DeviceState);
                    return -1;
                }
            }
            catch (Exception ex)
            {
                System.Windows.MessageBox.Show("#2 해당 PC의 오디오를 녹음 할 수 없습니다. 자세한 것은 문의해 주세요.");
                return -1;

            }
        }

        public void Close()
        {
            if (mastervolumeControl != null)
            {
                mastervolumeControl.Abort();
                mastervolumeControl = null;
            }
            
            client.Close();
            volumeClient.Close();
            capture.Dispose();

            audioIP = null;
            _instance = null;
            check = false;
        }

    }
}
