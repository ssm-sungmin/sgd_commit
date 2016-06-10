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

namespace SpeakerUI
{
	/// <summary>
	/// Interaction logic for Logo.xaml
	/// </summary>
	public partial class Logo : UserControl
	{
		private static Logo _logoPage = null;
		
		public Logo()
		{
			this.InitializeComponent();
		}

        public static Logo setLogopage()
        {
            _logoPage = new Logo();
            return _logoPage;
        }

		public static Logo getLogopage(){
            //if(_logoPage == null){
            //    _logoPage = new Logo();
            //}
			return _logoPage;
		}
		
		private void start_Click(object sender, System.Windows.RoutedEventArgs e)
		{
			Connect _connectPage = Connect.getConnectpage();
			
			this.Visibility = Visibility.Hidden;
			_connectPage.Visibility = Visibility.Visible;
		}

        public void close()
        {
            _logoPage = null;
        }
	}
}