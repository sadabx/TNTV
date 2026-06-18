/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { useEffect, useState } from "react";
import { Play } from "lucide-react";

interface SplashScreenProps {
  onFinish: () => void;
}

export default function SplashScreen({ onFinish }: SplashScreenProps) {
  const [fading, setFading] = useState(false);

  useEffect(() => {
    // Show splash screen for 2.2 seconds before fading out
    const timer = setTimeout(() => {
      setFading(true);
      // Wait for the fade out animations to end (300ms) before committing the finish callback
      const callbackTimer = setTimeout(onFinish, 350);
      return () => clearTimeout(callbackTimer);
    }, 2200);

    return () => clearTimeout(timer);
  }, [onFinish]);

  return (
    <div
      className={`absolute inset-0 bg-neutral-950 z-[999] flex flex-col items-center justify-between py-16 px-6 transition-all duration-[350ms] ease-out ${
        fading ? "opacity-0 scale-105 pointer-events-none" : "opacity-100 scale-100"
      }`}
    >
      {/* Dynamic top logo text */}
      <div className="flex flex-col items-center animate-fade-in">
        <span className="text-[10px] text-red-500 font-bold uppercase tracking-[0.3em] font-mono leading-none bg-red-500/10 px-3 py-1 rounded-full border border-red-500/15">
          T9 Premium OTT
        </span>
      </div>

      {/* Main app emblem */}
      <div className="flex flex-col items-center gap-4">
        {/* Animated Pulsing Toffee-like Play Icon wrapper */}
        <div className="relative group select-none">
          {/* Pulsing ring 1 */}
          <div className="absolute -inset-4 bg-red-600/20 rounded-full blur-xl animate-pulse duration-[2500ms]" />
          {/* Pulsing ring 2 */}
          <div className="absolute -inset-1.5 bg-linear-to-tr from-red-600 to-red-500 rounded-full animate-spin duration-[15s]" />

          {/* Central Logo Disk */}
          <div className="relative w-20 h-20 bg-neutral-900 rounded-full flex items-center justify-center border border-white/10 shadow-[0_4px_24px_rgba(220,38,38,0.25)]">
            <Play className="w-9 h-9 fill-red-600 text-red-600 ml-1 translate-x-[2px]" />
          </div>
        </div>

        {/* Brand Name with sleek color accent gradient */}
        <h1 className="text-4xl font-black tracking-tighter text-white">
          T9<span className="text-linear-to-r from-red-600 to-red-400 bg-clip-text text-transparent italic font-black ml-1">TV</span>
        </h1>
        <p className="text-zinc-500 text-sm font-medium tracking-wide">
          Premium OTT Live TV Stream
        </p>
      </div>

      {/* Footer loading indicator */}
      <div className="flex flex-col items-center gap-3 w-full max-w-[200px]">
        {/* Progress loop line */}
        <div className="w-full h-1 bg-white/5 rounded-full overflow-hidden relative">
          <div className="absolute top-0 left-0 h-full w-2/5 bg-linear-to-r from-red-600 to-red-500 rounded-full animate-[progress-slide_1.2s_infinite_ease-in-out]" />
        </div>
        <span className="text-[10px] text-zinc-600 font-semibold uppercase tracking-widest animate-pulse">
          Launching T9 OTT Engine
        </span>
      </div>

      {/* Inject custom classes keyframes for splash animations cleanly */}
      <style>{`
        @keyframes progress-slide {
          0% { left: -40%; width: 40%; }
          50% { width: 60%; }
          100% { left: 100%; width: 40%; }
        }
      `}</style>
    </div>
  );
}
