using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Text.RegularExpressions;
using System.Net;
using System.Net.Sockets;

namespace SpeakerUI
{
    class UDPConnect
    {
        private static UDPConnect _instance = null;
        private Connect connect_page = null;
        private Regex rgx;
        private string speakerIP, myIP, certification_number, sendStr;
        private int inforPort, audioPort;
        private IPAddress speakerIPAdress;
        public IPEndPoint audioIp;
        public bool disconnect;

        public UDPConnect()
            : this(null)
        {
        }
        public UDPConnect(string number)
        {
            if (number == null)
                throw new ArgumentException("Wrong Form of Certification Number.");

            disconnect = false;
            certification_number = number;
            _instance = this;
        }

        public static UDPConnect setUDPConnect(string number)
        {
            _instance = new UDPConnect(number);
            //if (_instance == null)
            //{
            //    _instance = new UDPConnect(number);
            //}

            _instance.certification_number = number;
            return _instance;
        }

        public static UDPConnect getUDPConnect( )
        {
            //if (_instance == null)
            //{
            //    _instance = new UDPConnect(number);
            //}

            //_instance.certification_number = number;
            return _instance;
        }

        public void Init()
        {
            rgx = new Regex(@"\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}");
            connect_page = Connect.getConnectpage();
            
            //_loopbackCapture = AudioLBCapture.getAudioLBCapture();
            audioPort = 2048;
            inforPort = 11000;
        }

        //최초 접속
        public bool Connection()
        {
            int test = 0;
            try
            {
                WebClient webClient = new WebClient();
                webClient.Encoding = System.Text.Encoding.Default;

                myIP = webClient.DownloadString("http://ipip.kr");
                Match match_myIP = rgx.Match(myIP);

                speakerIP = webClient.DownloadString("http://ss.issro.net/ServerIp.php?r_number=" + certification_number);
                Match match_speakerIP = rgx.Match(speakerIP);

                ////연결 종료 후
                //if (disconnect)
                //{
                //    return false;
                //}

                //잘못된 인증 번호 입력시
                if (speakerIP == "")
                {
                    System.Windows.MessageBox.Show("인증 번호를 잘못 입력하셨습니다. 다시 시도해주십시오.");
                    return false;
                }

                sendStr = match_myIP.ToString() + "/" + connect_page.name + "%0%pc";
                test = send_Basic();

                webClient = null;
                return receive_portNumber();
            }
            catch (Exception e)
            {
                if (test == 0)
                    System.Windows.MessageBox.Show(e.Message + "#0");
                else
                    System.Windows.MessageBox.Show(e.Message + "#1");

                //System.Windows.MessageBox.Show("인터넷이 원활하지 않습니다. 다시 시도해주십시오.");
                return false;
            }
        }

        //음성 데이터 보낼 port 번호 부여받기
        public bool receive_portNumber()
        {
            UdpClient client = new UdpClient(inforPort);
            IPEndPoint receiveEP = new IPEndPoint(IPAddress.Any, 0);
            try
            {
                byte[] receivePort = client.Receive(ref receiveEP);
                string receiveData = Encoding.UTF8.GetString(receivePort);

                audioPort = Int32.Parse(receiveData);
                audioIp = new IPEndPoint(speakerIPAdress, audioPort);
                client.Close();

                if (audioPort == -1) 
                {
                    System.Windows.MessageBox.Show("이미 접속하고 있습니다.");
                    return false;
                }
                return true;
            }
            catch (Exception e)
            {
                //Console.WriteLine(e.ToString());
                client.Close();
                if (!disconnect)
                    System.Windows.MessageBox.Show("데이터를 수신하는데 실패하였습니다. 잠시후 다시 시도해 주십시오.");
                return false;
            }
        }

        //기본 정보 제공
        public int send_Basic()
        {
            byte[] buffer = Encoding.UTF8.GetBytes(sendStr);
            speakerIPAdress = IPAddress.Parse(speakerIP);
            IPEndPoint inforEP = new IPEndPoint(speakerIPAdress, inforPort);
            UdpClient client = new UdpClient();

            client.Send(buffer, buffer.Length, inforEP);
            client.Close();
            return 1;
        }

        public bool Start()
        {
            Init();
            if (!Connection())
                return false;  
            return true;
        }

        public void close()
        {
            _instance = null;
        }
    }
}
