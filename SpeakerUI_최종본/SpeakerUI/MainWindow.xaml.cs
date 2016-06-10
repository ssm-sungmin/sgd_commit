using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

using System.Windows.Threading;

namespace SpeakerUI
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
		public static Logo logo_page;
		public static Connect connect_page;
		public static SendSound sendsound_page;
        private static MainWindow main_window;
        private static AudioLBCapture audio_lbcapture;

        public MainWindow()
        {
            InitializeComponent();
			Init();
        }

        //public static MainWindow setMain()
        //{
        //    main_window = new MainWindow();
        //    return main_window;
        //}
        public static MainWindow getMain()
        {
            //if (main_window == null)
            //{
            //    main_window = new MainWindow();
            //}
            return main_window;
        }

		public void Init(){
            main_window = this;
            logo_page = Logo.setLogopage();
            connect_page = Connect.setConnectpage();
            sendsound_page = SendSound.setSendSoundpage();

			this.mainpanel.Children.Add(logo_page);
			this.mainpanel.Children.Add(connect_page);
            this.mainpanel.Children.Add(sendsound_page);

			logo_page.Visibility = Visibility.Visible;
			connect_page.Visibility = Visibility.Hidden;
			sendsound_page.Visibility = Visibility.Hidden;
		}

        public void Restart()
        {
            restartBtn.Visibility = Visibility.Hidden;
            Information_restart.Visibility = Visibility.Hidden;

            logo_page = Logo.setLogopage();
            connect_page = Connect.setConnectpage();
            sendsound_page = SendSound.setSendSoundpage();

            this.mainpanel.Children.Add(logo_page);
            this.mainpanel.Children.Add(connect_page);
            this.mainpanel.Children.Add(sendsound_page);

            //sendsound_page.disconnecttBtn.Click += disconnect_Click;
            logo_page.Visibility = Visibility.Visible;
            connect_page.Visibility = Visibility.Hidden;
            sendsound_page.Visibility = Visibility.Hidden;
        }

		private void TitleBar_Down(object sender, System.Windows.Input.MouseButtonEventArgs e)
		{
			this.DragMove();
		}

		private void close_Click(object sender, System.Windows.RoutedEventArgs e)
		{
            //this.mainpanel.Children.Clear();
            if (sendsound_page != null)
            {
                sendsound_page.close();
            }

            if (connect_page != null)
            {
                connect_page.close();
            }

            if (logo_page != null)
            {
                logo_page.close();
            }

            this.Close();
		}

		private void minimize_Click(object sender, System.Windows.RoutedEventArgs e)
		{
			this.WindowState = WindowState.Minimized;
		}

        private void restart_Click(object sender, RoutedEventArgs e)
        {
            Restart();
        }

        public void theEnd()
        {
            disconnect_Click(this, null);
            //tempBtn.Click += temp_Click;
            //this.Dispatcher.Invoke(DispatcherPriority.Normal, new RoutedEvent(Button.Click), temp)
            //tempBtn.RaiseEvent(new RoutedEventArgs(Button.ClickEvent, tempBtn));
        }

        //public void see()
        //{
        //    disconnectBtn.Visibility = Visibility.Visible;
        //}

        public void Disconnect()
        {
            if (sendsound_page != null)
            {
                sendsound_page.close();
            }

            if (connect_page != null)
            {
                connect_page.close();
            }

            if (logo_page != null)
            {
                logo_page.close();
            }

            Dispatcher.Invoke(DispatcherPriority.Normal, new Action(delegate
            {
                //사용할 메서드 및 동작
                this.mainpanel.Children.Remove(logo_page);
                this.mainpanel.Children.Remove(connect_page);
                this.mainpanel.Children.Remove(sendsound_page);

                disconnectBtn.Visibility = Visibility.Hidden;
                restartBtn.Visibility = Visibility.Visible;
                Information_restart.Visibility = Visibility.Visible;
            }));
        }

        private void disconnect_Click(object sender, RoutedEventArgs e)
        {
            //SendSound .getSendSoundpage().close();
            //UDPConnect _udp = UDPConnect.getUDPConnect();
            //_udp.disconnect = true;
            //_udp.Start();

            //if (sendsound_page != null)
            //{
            //    sendsound_page.close();
            //}

            //if (connect_page != null)
            //{
            //    connect_page.close();
            //}

            //if (logo_page != null)
            //{
            //    logo_page.close();
            //}
            //this.mainpanel.Children.Remove(logo_page);
            //this.mainpanel.Children.Remove(connect_page);
            //this.mainpanel.Children.Remove(sendsound_page);

            //restartBtn.Visibility = Visibility.Visible;
            //Information_restart.Visibility = Visibility.Visible;
            Disconnect();
        }

        private void compulsionEnd_Click(object sender, RoutedEventArgs e)
        {
            Disconnect();
        }

        public void Dispose()
        {
            //Dispose(true);
            //Close();
            GC.SuppressFinalize(this);
        }

        //protected virtual void Dispose(bool disposing)
        //{
        //    if (disposing)
        //    {
        //        // Free other state (managed objects).
        //    }
        //    // Free your own state (unmanaged objects).
        //    // Set large fields to null.
        //}

        ~MainWindow()
        {
            Dispose();
        }

        //public void Diospose()
        //{
        //    Close();
        //    GC.SuppressFinalize(this);
        //}

        
    }
}
