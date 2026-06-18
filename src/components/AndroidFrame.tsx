/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useEffect, useState } from "react";
import { Battery, Wifi, Signal, Maximize2, Minimize2, AppWindow } from "lucide-react";

interface AndroidFrameProps {
  children: React.ReactNode;
  onHomePress?: () => void;
  onBackPress?: () => void;
}

export default function AndroidFrame({ children, onHomePress, onBackPress }: AndroidFrameProps) {
  const [time, setTime] = useState("");
  const [battery, setBattery] = useState(87);
  const [isFullscreen, setIsFullscreen] = useState(false);

  useEffect(() => {
    const updateTime = () => {
      const now = new Date();
      setTime(
        now.toLocaleTimeString([], {
          hour: "2-digit",
          minute: "2-digit",
          hour12: true,
        }).replace(/AM|PM/i, "")
      );
    };
    updateTime();
    const interval = setInterval(updateTime, 30000);

    // Minor battery drain simulation just for fun!
    const batteryInterval = setInterval(() => {
      setBattery((b) => (b > 5 ? b - 1 : 100));
    }, 180000);

    return () => {
      clearInterval(interval);
      clearInterval(batteryInterval);
    };
  }, []);

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#090909] via-[#0F0F0F] to-[#141414] flex flex-col items-center justify-center p-0 md:p-6 text-white overflow-hidden relative selection:bg-red-600/30">
      {/* Background decoration */}
      <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-red-600/5 rounded-full blur-[120px] pointer-events-none" />
      <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-red-600/3 rounded-full blur-[140px] pointer-events-none" />

      {/* Frame / Responsive wrap */}
      <div
        className={`w-full transition-all duration-500 ease-[cubic-bezier(0.2,0.8,0.2,1)] ${
          isFullscreen
            ? "max-w-full h-screen p-0 m-0"
            : "max-w-md h-[860px] md:h-[840px] rounded-[48px] border-8 border-neutral-900 shadow-[0_24px_80px_rgba(0,0,0,0.95)] relative flex flex-col bg-[#0F0F0F] overflow-hidden ring-1 ring-white/10"
        }`}
      >
        {/* Android Notch / Dynamic Island (Only when not fullscreen) */}
        {!isFullscreen && (
          <div className="absolute top-0 left-1/2 -translate-x-1/2 w-32 h-[30px] bg-black rounded-b-[18px] z-50 flex items-center justify-center border-l border-r border-b border-white/5 shadow-inner">
            <div className="w-3 h-3 rounded-full bg-zinc-900 flex items-center justify-center border border-zinc-800">
              <div className="w-1 h-1 rounded-full bg-red-500/50" />
            </div>
            <div className="w-8 h-1 bg-zinc-900 rounded-full ml-3" />
          </div>
        )}

        {/* Dynamic Android Status Bar (Responsive mobile styling) */}
        <div className={`w-full bg-black/40 backdrop-blur-2xl px-6 flex items-center justify-between text-xs font-semibold select-none z-50 relative ${
          isFullscreen ? "py-1.5 border-b border-white/5" : "pt-4 pb-2"
        }`}>
          {/* Status Bar Left: Clock & App Logo */}
          <div className="flex items-center gap-1.5">
            <span className="font-sans text-[13px] tracking-tight">{time}</span>
            <div className="w-1.5 h-1.5 rounded-full bg-red-600 animate-pulse ml-0.5" />
          </div>

          {/* Status Bar Right: Wifi, Signal, Battery */}
          <div className="flex items-center gap-2">
            <Signal className="w-3.5 h-3.5 text-white/90" strokeWidth={2.5} />
            <Wifi className="w-3.5 h-3.5 text-white/90" strokeWidth={2.5} />
            <div className="flex items-center gap-1 bg-white/10 px-1.5 py-0.5 rounded-full scale-95 border border-white/5 text-[10px]">
              <Battery className="w-3.5 h-3.5 text-red-500" strokeWidth={2.5} />
              <span>{battery}%</span>
            </div>
          </div>
        </div>

        {/* Frame Actions / Header controls (only visible on frame container view) */}
        {!isFullscreen && (
          <div className="absolute right-4 top-16 z-50 flex flex-col gap-2 pointer-events-auto">
            <button
              onClick={() => setIsFullscreen(true)}
              title="Go full window"
              className="p-2.5 bg-black/80 hover:bg-[#121212] border border-white/10 rounded-full shadow-lg transition-transform active:scale-95 cursor-pointer backdrop-blur-md group"
            >
              <Maximize2 className="w-4 h-4 text-white/85 group-hover:text-red-500 transition-colors" />
            </button>
          </div>
        )}

        {/* Dynamic Fullscreen Exit trigger top toolbar */}
        {isFullscreen && (
          <div className="absolute top-2 right-4 z-50 flex items-center gap-3">
            <span className="text-[10px] bg-red-600/20 text-red-400 px-2 py-0.5 rounded-full border border-red-500/30 uppercase tracking-widest font-bold font-mono">
              TV Mode
            </span>
            <button
              onClick={() => setIsFullscreen(false)}
              className="p-2 bg-black/80 border border-white/10 rounded-full flex items-center justify-center transition-all hover:bg-[#121212] active:scale-95 cursor-pointer"
              title="Return to Device Frame"
            >
              <Minimize2 className="w-4 h-4 text-white hover:text-red-500" />
            </button>
          </div>
        )}

        {/* Core Screen Context (The embedded mobile app viewport) */}
        <div className="flex-1 flex flex-col bg-[#0F0F0F] overflow-hidden relative">
          {children}
        </div>

        {/* Simulated Android Hardware/Software Navigation Keys */}
        {!isFullscreen && (
          <div className="w-full h-11 bg-zinc-950 flex items-center justify-around border-t border-white/5 px-10 relative z-40 select-none">
            {/* Back Arrow Key */}
            <button
              onClick={onBackPress}
              className="py-2 px-6 hover:bg-white/5 rounded-full active:scale-90 transition-all cursor-pointer flex items-center justify-center group"
            >
              <div className="w-2.5 h-2.5 border-b-2 border-l-2 border-white/60 rotate-45 group-hover:border-rose-500 transition-colors" />
            </button>

            {/* Home Pill Key */}
            <button
              onClick={onHomePress}
              className="py-2 px-8 hover:bg-white/5 rounded-full active:scale-90 transition-all cursor-pointer flex items-center justify-center group"
            >
              <div className="w-14 h-1.5 bg-white/60 rounded-full group-hover:bg-rose-500 transition-colors" />
            </button>

            {/* Overview Square Key */}
            <button
              className="py-2 px-6 hover:bg-white/5 rounded-full cursor-pointer flex items-center justify-center group"
              onClick={onHomePress}
            >
              <div className="w-2.5 h-2.5 border-2 border-white/60 rounded-[3px] group-hover:border-rose-500 transition-colors" />
            </button>
          </div>
        )}
      </div>

      {/* Quick toggle hint for desktop frame users */}
      {!isFullscreen && (
        <p className="hidden md:flex text-neutral-500 text-[11px] font-medium tracking-wide mt-4 items-center gap-1.5">
          <AppWindow className="w-3.5 h-3.5" />
          Click the <strong className="text-white">top right button</strong> to play in full window size.
        </p>
      )}
    </div>
  );
}
