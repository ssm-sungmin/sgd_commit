using System;
using System.Collections.Generic;
using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using System.Net;
using System.Collections;
using System.Diagnostics;
//using System.Windows.Forms;

namespace SpeakerUI
{
	/// <summary>
	/// Interaction logic for SendSound.xaml
	/// </summary>
    public partial class SendSound : UserControl
	{
		private static SendSound _sendsoundPage;
        private AudioLBCapture _loopbackCapture;

		public SendSound()
		{
			this.InitializeComponent();
		}

        public static SendSound setSendSoundpage()
        {
            _sendsoundPage = new SendSound();

            return _sendsoundPage;
        }

		public static SendSound getSendSoundpage(){
			return _sendsoundPage;
		}

        public void Record(IPEndPoint audioIP)
        {
            _loopbackCapture = AudioLBCapture.getAudioLBCapture(audioIP);
            _loopbackCapture.StartRecord();
        }

        //main에서 작업
        //private void disconnect_Click(object sender, RoutedEventArgs e)
        //{
        //    close();
        //    UDPConnect _udp = UDPConnect.getUDPConnect();
        //    _udp.disconnect = true;
        //    _udp.Start();
        //}

        public void close()
        {
            if (_loopbackCapture != null)
            {
                if (_loopbackCapture.check)
                    _loopbackCapture.EndRecord();
                else
                {
                    _loopbackCapture = null;
                }
            }

            UDPConnect _udp = UDPConnect.getUDPConnect();
            if (_udp != null && _udp.disconnect == false)
            {
                _udp.disconnect = true;

                _udp.send_Basic();
                _udp.close();
                //_udp.Start();
            }

            //_sendsoundPage.Visibility = Visibility.Hidden;
            _sendsoundPage = null;
        }

        
	}
}