/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { useState, useRef, useEffect } from "react";
import { Heart, MessageSquare, Share2, Play, Pause, ChevronUp, ChevronDown, Volume2, VolumeX, Radio } from "lucide-react";

interface ShortVideo {
  id: string;
  title: string;
  creator: string;
  videoUrl: string;
  likes: string;
  comments: number;
}

const SHORTS_DATA: ShortVideo[] = [
  {
    id: "short-1",
    title: "Unbelievable Goal from Midfield! ⚽🔥 #fifa #sports #goals",
    creator: "@SportsArena_BD",
    videoUrl: "https://assets.mixkit.co/videos/preview/mixkit-playing-football-in-the-rain-41582-large.mp4",
    likes: "24.5K",
    comments: 512,
  },
  {
    id: "short-2",
    title: "Childhood memories with the best cartoons Gopal Bhar! 🍉 #nostalgia",
    creator: "@KolkataFun_Reels",
    videoUrl: "https://assets.mixkit.co/videos/preview/mixkit-little-cartoon-robot-dancing-40018-large.mp4",
    likes: "18.2K",
    comments: 290,
  },
  {
    id: "short-3",
    title: "A majestic view of the green tea gardens in Sylhet 🍵🍃 #bangladesh #travel",
    creator: "@GreenTrails",
    videoUrl: "https://assets.mixkit.co/videos/preview/mixkit-river-surrounded-by-forest-seen-from-above-41578-large.mp4",
    likes: "32.1K",
    comments: 780,
  },
  {
    id: "short-4",
    title: "Dynamic racing action behind the wheel! 🏎️🏁 #formula1 #f1 #fast",
    creator: "@F1_Dynamics",
    videoUrl: "https://assets.mixkit.co/videos/preview/mixkit-kart-racing-action-on-a-sunny-day-40292-large.mp4",
    likes: "45.0K",
    comments: 1105,
  }
];

export default function ShortsFeed() {
  const [activeIndex, setActiveIndex] = useState(0);
  const [isPlaying, setIsPlaying] = useState(true);
  const [isMuted, setIsMuted] = useState(true);
  const [likedList, setLikedList] = useState<string[]>([]);
  const videoRefs = useRef<(HTMLVideoElement | null)[]>([]);

  const activeVideo = SHORTS_DATA[activeIndex];

  useEffect(() => {
    // Play active video, pause others
    videoRefs.current.forEach((ref, idx) => {
      if (ref) {
        ref.muted = isMuted;
        if (idx === activeIndex) {
          if (isPlaying) {
            ref.play().catch((err) => console.log("Reels play blocked:", err));
          } else {
            ref.pause();
          }
        } else {
          ref.pause();
          ref.currentTime = 0;
        }
      }
    });
  }, [activeIndex, isPlaying, isMuted]);

  const handleNextShort = () => {
    if (activeIndex + 1 < SHORTS_DATA.length) {
      setActiveIndex(activeIndex + 1);
      setIsPlaying(true);
    }
  };

  const handlePrevShort = () => {
    if (activeIndex > 0) {
      setActiveIndex(activeIndex - 1);
      setIsPlaying(true);
    }
  };

  const handleToggleLike = (id: string) => {
    if (likedList.includes(id)) {
      setLikedList(likedList.filter((x) => x !== id));
    } else {
      setLikedList([...likedList, id]);
    }
  };

  return (
    <div className="flex-1 flex flex-col h-full bg-black relative select-none">
      {/* Dynamic top bar header on shorts player screen */}
      <div className="absolute top-4 left-4 z-30 flex items-center gap-1 bg-black/40 px-3 py-1 rounded-full border border-white/5 backdrop-blur-md">
        <Radio className="w-3.5 h-3.5 text-rose-500 animate-pulse" />
        <span className="text-[10px] text-zinc-300 font-extrabold tracking-widest font-mono uppercase">
          Shorts Arena
        </span>
      </div>

      <div className="absolute top-4 right-4 z-30">
        <button
          onClick={() => setIsMuted(!isMuted)}
          className="p-2.5 bg-black/50 hover:bg-black/80 rounded-full border border-white/5 backdrop-blur-md cursor-pointer text-white transition-all active:scale-95"
        >
          {isMuted ? <VolumeX className="w-4 h-4" /> : <Volume2 className="w-4 h-4 text-rose-400" />}
        </button>
      </div>

      {/* Main Swiper Video Block */}
      <div className="flex-1 relative flex items-center justify-center overflow-hidden">
        {SHORTS_DATA.map((short, idx) => {
          const isCurrent = idx === activeIndex;
          return (
            <div
              key={short.id}
              className={`absolute inset-0 w-full h-full flex flex-col justify-end transition-all duration-[450ms] cubic-bezier(0.2,0.8,0.2,1) ${
                isCurrent
                  ? "opacity-100 translate-y-0 scale-100 z-10"
                  : idx < activeIndex
                  ? "opacity-0 -translate-y-full scale-95 -z-10"
                  : "opacity-0 translate-y-full scale-105 -z-10"
              }`}
            >
              {/* Loop Video node */}
              <video
                ref={(el) => {
                  videoRefs.current[idx] = el;
                }}
                src={short.videoUrl}
                loop
                playsInline
                webkit-playsinline="true"
                className="absolute inset-0 w-full h-full object-cover z-0"
                onClick={() => setIsPlaying(!isPlaying)}
              />

              {/* Ambient bottom shadow cover */}
              <div className="absolute inset-x-0 bottom-0 h-44 bg-gradient-to-t from-black/80 via-black/40 to-transparent pointer-events-none z-10" />

              {/* Central Trigger Play button overlays on click pause */}
              {!isPlaying && isCurrent && (
                <button
                  onClick={() => setIsPlaying(true)}
                  className="absolute inset-0 m-auto w-16 h-14 rounded-3xl bg-black/55 backdrop-blur-md flex items-center justify-center z-20 cursor-pointer shadow-xl border border-white/10"
                >
                  <Play className="w-7 h-7 text-white fill-white translate-x-[2px]" />
                </button>
              )}

              {/* Bottom titles info panel */}
              <div className="relative z-20 p-6 flex flex-col gap-1 max-w-[80%] text-white pointer-events-auto select-text">
                <span className="text-sm font-black bg-rose-600/90 w-max px-2.5 py-0.5 rounded-md drop-shadow-md select-none group cursor-pointer">
                  {short.creator}
                </span>
                <p className="text-xs text-zinc-100 leading-relaxed font-semibold drop-shadow-md select-all pr-4">
                  {short.title}
                </p>
              </div>
            </div>
          );
        })}

        {/* Sidebar Interactions Panel Floating */}
        <div className="absolute right-5 bottom-24 z-30 flex flex-col items-center gap-5 pointer-events-auto">
          {/* Like Heart Trigger */}
          <button
            onClick={() => handleToggleLike(activeVideo.id)}
            className="flex flex-col items-center gap-1 group cursor-pointer"
          >
            <div
              className={`w-12 h-12 rounded-full border flex items-center justify-center shadow-lg transition-transform active:scale-75 ${
                likedList.includes(activeVideo.id)
                  ? "bg-rose-500 border-rose-400 text-white"
                  : "bg-black/50 border-white/5 text-white/90 hover:bg-black/75"
              }`}
            >
              <Heart
                className={`w-5 h-5 ${likedList.includes(activeVideo.id) ? "fill-white" : "group-hover:text-rose-500"}`}
              />
            </div>
            <span className="text-[10px] font-bold font-mono tracking-wide text-zinc-400">
              {likedList.includes(activeVideo.id) ? "LIKED" : activeVideo.likes}
            </span>
          </button>

          {/* Comments Panel */}
          <div className="flex flex-col items-center gap-1">
            <div className="w-12 h-12 rounded-full bg-black/50 border border-white/5 flex items-center justify-center text-white/90 hover:bg-black/75 cursor-pointer shadow-lg active:scale-95 transition-transform">
              <MessageSquare className="w-5 h-5" />
            </div>
            <span className="text-[10px] font-bold font-mono tracking-wide text-zinc-400">
              {activeVideo.comments}
            </span>
          </div>

          {/* Share Action */}
          <div className="flex flex-col items-center gap-1">
            <div className="w-12 h-12 rounded-full bg-black/50 border border-white/5 flex items-center justify-center text-white/90 hover:bg-black/75 cursor-pointer shadow-lg active:scale-95 transition-transform">
              <Share2 className="w-5 h-5" />
            </div>
            <span className="text-[10px] font-bold font-mono tracking-wide text-zinc-400">SHARE</span>
          </div>
        </div>

        {/* Video Scroller vertical navigational pointers */}
        <div className="absolute right-5 top-1/2 -translate-y-1/2 z-30 flex flex-col gap-2.5">
          <button
            onClick={handlePrevShort}
            disabled={activeIndex === 0}
            className={`p-2 rounded-full border border-white/5 backdrop-blur-md cursor-pointer transition-all active:scale-95 ${
              activeIndex === 0
                ? "opacity-30 bg-black/35 text-zinc-600"
                : "bg-black/60 text-white hover:bg-black/85"
            }`}
          >
            <ChevronUp className="w-4 h-4" />
          </button>

          <button
            onClick={handleNextShort}
            disabled={activeIndex === SHORTS_DATA.length - 1}
            className={`p-2 rounded-full border border-white/5 backdrop-blur-md cursor-pointer transition-all active:scale-95 ${
              activeIndex === SHORTS_DATA.length - 1
                ? "opacity-30 bg-black/35 text-zinc-600"
                : "bg-black/60 text-white hover:bg-black/85"
            }`}
          >
            <ChevronDown className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  );
}
