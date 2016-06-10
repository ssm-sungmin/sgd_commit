using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using CSCore.CoreAudioAPI;

namespace CSCore.SoundIn
{
    public class WasapiLoopback_M : WasapiCapture
    { 
        //public WasapiLoopback_M()
        //    : base(false, AudioClientShareMode.Shared, 100, new WaveFormat())
        //{
        //}
        public WasapiLoopback_M(int latency, WaveFormat waveFormat)
            : base(false, AudioClientShareMode.Shared, latency, waveFormat)
        {
        }

        protected override MMDevice GetDefaultDevice()
        {
            return MMDeviceEnumerator.DefaultAudioEndpoint(DataFlow.Render, Role.Console);
        }

        protected override AudioClientStreamFlags GetStreamFlags()
        {
            return AudioClientStreamFlags.StreamFlagsLoopback;
        }

        //internal void Close()
        //{
        //    throw new NotImplementedException();
        //}
    }
}
