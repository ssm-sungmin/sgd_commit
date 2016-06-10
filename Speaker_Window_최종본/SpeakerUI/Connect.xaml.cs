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
using System.Runtime.InteropServices;       //DLL
//using System.Windows.Forms;             //Keys_참조 추가 해야함

namespace SpeakerUI
{
	/// <summary>
	/// Interaction logic for Connect.xaml
	/// </summary>
	public partial class Connect : UserControl
	{
		private static Connect _connectPage;
        private AudioLBCapture _loopbackCapture;
        private UDPConnect _udpConnect;
        private bool[] Number_check = { false, false, false, false };
        private string certification_number;
        public string name;

		public Connect()
		{
			this.InitializeComponent();
            name = "Computer";
		}

        public static Connect setConnectpage()
        {
            _connectPage = new Connect();
            return _connectPage;
        }
		
		public static Connect getConnectpage(){
            //if(_connectPage == null){
            //    _connectPage = new Connect();
            //}
			return _connectPage;
		}

		private void connect_Click(object sender, System.Windows.RoutedEventArgs e)
		{
			SendSound _sendsoundPage = SendSound.getSendSoundpage();
            _udpConnect = UDPConnect.setUDPConnect(getCertification_Number());
            
            //모든 준비 완료 됐으면
            if (_udpConnect.Start())
            {
                _loopbackCapture = AudioLBCapture.setAudioLBCapture(_udpConnect.audioIp);
                if (_loopbackCapture.InitRecord())
                {
                    this.Visibility = Visibility.Hidden;
                    _sendsoundPage.Visibility = Visibility.Visible;
                    _sendsoundPage.Record(_udpConnect.audioIp);

                    MainWindow.getMain().disconnectBtn.Visibility = Visibility.Visible;

                    _sendsoundPage = null;      //다 씀
                }
                else
                {
                    _udpConnect.close();
                    _loopbackCapture.Close();
                }
            }
            else
            {
                _udpConnect.close();
            }
		}

        //인증 번호 입력
        #region certifinumber keyEvent
        [DllImport("user32.dll", SetLastError = true)]
        public static extern void keybd_event(byte bVk, byte bScan, uint dwFlags, int dwExtraInfo);
        private void keyDown1(object sender, System.Windows.Input.KeyEventArgs e)
        {
            if (!e.Key.ToString().Equals(System.Windows.Forms.Keys.Tab.ToString()))
                CertifiNum1.Text = "";
        }

        private void keyDown2(object sender, System.Windows.Input.KeyEventArgs e)
        {
            if (!e.Key.ToString().Equals(System.Windows.Forms.Keys.Tab.ToString()))
                CertifiNum2.Text = "";
        }

        private void keyDown3(object sender, System.Windows.Input.KeyEventArgs e)
        {
            if (!e.Key.ToString().Equals(System.Windows.Forms.Keys.Tab.ToString()))
                CertifiNum3.Text = "";
        }

        private void keyDown4(object sender, System.Windows.Input.KeyEventArgs e)
        {
            if (!e.Key.ToString().Equals(System.Windows.Forms.Keys.Tab.ToString()))
                CertifiNum4.Text = "";
        }
        #endregion

        //숫자만 입력 받기
        #region only Number
        private void textChanged1(object sender, System.Windows.Controls.TextChangedEventArgs e)
        {
            if (CertifiNum1.Text.Equals(""))
            {
                return;
            }
            else if (isNumber(CertifiNum1.Text))
            {
                Number_check[0] = true;
                keybd_event(0x09, 0, 1 | 0, 0);     //ctrl down
                keybd_event(0x09, 0, 1 | 2, 0);     //ctrl up
            }
            else
            {
                Number_check[0] = false;
                System.Windows.MessageBox.Show("숫자만 입력하실 수 있습니다");
                CertifiNum1.Text = "";
            }
        }

        private void textChanged2(object sender, System.Windows.Controls.TextChangedEventArgs e)
        {
            if (CertifiNum2.Text.Equals(""))
            {
                return;
            }
            else if (isNumber(CertifiNum2.Text))
            {
                Number_check[1] = true;
                keybd_event(0x09, 0, 1 | 0, 0);//ctrl down
                keybd_event(0x09, 0, 1 | 2, 0);//ctrl up
            }
            else
            {
                Number_check[1] = false;
                System.Windows.MessageBox.Show("숫자만 입력하실 수 있습니다");
                CertifiNum2.Text = "";
            }
        }

        private void textChanged3(object sender, System.Windows.Controls.TextChangedEventArgs e)
        {
            if (CertifiNum3.Text.Equals(""))
            {
                return;
            }
            else if (isNumber(CertifiNum3.Text))
            {
                Number_check[2] = true;
                keybd_event(0x09, 0, 1 | 0, 0);//ctrl down
                keybd_event(0x09, 0, 1 | 2, 0);//ctrl up
            }
            else
            {
                Number_check[2] = false;
                System.Windows.MessageBox.Show("숫자만 입력하실 수 있습니다");
                CertifiNum3.Text = "";
            }
        }

        private void textChanged4(object sender, System.Windows.Controls.TextChangedEventArgs e)
        {
            if (CertifiNum4.Text.Equals(""))
            {
                return;
            }
            else if(isNumber(CertifiNum4.Text))
            {
                Number_check[3] = true;
                keybd_event(0x09, 0, 1 | 0, 0);//ctrl down
                keybd_event(0x09, 0, 1 | 2, 0);//ctrl up
            }
            else
            {
                Number_check[3] = false;
                System.Windows.MessageBox.Show("숫자만 입력하실 수 있습니다");
                CertifiNum4.Text = "";
            }
        }
        #endregion

        public bool isNumber(string str)
        {
            if (str.CompareTo("0") >= 0 && str.CompareTo("9") <= 0)
            {
                return true;
            }
            return false;
        }

        public string getCertification_Number()
        {
            bool allNumber = true;
            certification_number = CertifiNum1.Text + CertifiNum2.Text + CertifiNum3.Text + CertifiNum4.Text;
            if (ConnectName.Text != "")
            {
                name = ConnectName.Text;
            }
                

            for(int i=0; i<4; i++){
                if(!isNumber(certification_number[i].ToString())){
                    allNumber = false;
                }
            }
            if(allNumber)
                return certification_number;
            else
            {
                System.Windows.MessageBox.Show("인증번호를 정확히 입력해 주세요.");
                return null;
            }
        }

        private void name_Changed(object sender, TextChangedEventArgs e)
        {
            if (ConnectName.Text == "")
            {
                DefaultName.Visibility = Visibility.Visible;
            }
            else
            {
                DefaultName.Visibility = Visibility.Hidden;
            }
        }

        public void close()
        {
            if(_udpConnect != null) 
                _udpConnect.close();
            if (_loopbackCapture != null)
            {
                if (_loopbackCapture.check)
                    _loopbackCapture.EndRecord();
                else
                    _loopbackCapture = null;
            }
            _connectPage = null;
        }
    }
}