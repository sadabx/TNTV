/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useEffect, useRef, useState, MouseEvent, ChangeEvent } from "react";
import Hls from "hls.js";
import {
  Play,
  Pause,
  Volume2,
  VolumeX,
  Maximize,
  Minimize,
  Settings,
  Tv,
  Subtitles,
  Info,
  RotateCcw,
  RefreshCw,
  X,
  Eye,
  Activity,
  Zap
} from "lucide-react";
import { Channel, Stream } from "../types";
import { getProxiedUrl, getYouTubeId, isTsUrl, generateSCPN } from "../utils/playerUtils";

interface PremiumPlayerProps {
  channel: Channel;
  selectedStreamIdx: number;
  onSelectStream: (idx: number) => void;
  onVideoError?: (errMessage: string) => void;
}

export default function PremiumPlayer({
  channel,
  selectedStreamIdx,
  onSelectStream,
  onVideoError
}: PremiumPlayerProps) {
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const containerRef = useRef<HTMLDivElement | null>(null);
  const hlsRef = useRef<Hls | null>(null);

  const [isPlaying, setIsPlaying] = useState(false);
  const [isMuted, setIsMuted] = useState(false);
  const [volume, setVolume] = useState(1);
  const [isControlsVisible, setIsControlsVisible] = useState(true);
  const [showQualityMenu, setShowQualityMenu] = useState(false);
  const [showStats, setShowStats] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [errorText, setErrorText] = useState<string | null>(null);
  const [subtitleEnabled, setSubtitleEnabled] = useState(false);
  const [isFullscreen, setIsFullscreen] = useState(false);

  // Dynamic values for Stats for Nerds
  const [scpnValue] = useState(() => generateSCPN());
  const [bufferHealth, setBufferHealth] = useState(0);
  const [bandwidthEst, setBandwidthEst] = useState("0.00 Mbps");
  const [droppedFrames, setDroppedFrames] = useState({ dropped: 0, total: 0 });
  const [resolutionText, setResolutionText] = useState("0x0");
  const [playerEngine, setPlayerEngine] = useState("Native HTML5");

  // Determine stream and YouTube Embed
  const activeStream = channel.streams[selectedStreamIdx] || channel.streams[0];
  const youtubeId = activeStream ? getYouTubeId(activeStream.url) : null;

  // Watchdog Load Timeout
  const watchdogRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    // Reset states on channel switch
    setIsLoading(true);
    setErrorText(null);
    setIsPlaying(false);
    setShowQualityMenu(false);

    const video = videoRef.current;
    if (!video || youtubeId) {
      setIsLoading(false);
      return;
    }

    // Clean up any old HSL instance
    destroyHls();

    // Setup Load watchdog
    startWatchdog();

    // Build URL
    const streamUrl = getProxiedUrl(activeStream.url);

    if (Hls.isSupported()) {
      setPlayerEngine("HLS.js Live Engine");
      const hls = new Hls({
        enableWorker: true,
        maxBufferSize: 30 * 1024 * 1024, // 30MB
        maxBufferLength: 15,
        manifestLoadingTimeOut: 8000,
      });
      hlsRef.current = hls;

      hls.on(Hls.Events.MANIFEST_PARSED, () => {
        setIsLoading(false);
        cancelWatchdog();
        setErrorText(null);
        video.play()
          .then(() => setIsPlaying(true))
          .catch(() => setIsPlaying(false));
      });

      hls.on(Hls.Events.LEVEL_SWITCHED, () => {
        if (hls.levels && hls.levels[hls.currentLevel]) {
          const l = hls.levels[hls.currentLevel];
          setResolutionText(`${l.width || 0}x${l.height || 0}`);
        }
      });

      hls.on(Hls.Events.ERROR, (_, data) => {
        console.warn("HLS Error:", data);
        if (data.fatal) {
          cancelWatchdog();
          switch (data.type) {
            case Hls.ErrorTypes.NETWORK_ERROR:
              // Try next backup stream automatically if available
              if (selectedStreamIdx + 1 < channel.streams.length) {
                onSelectStream(selectedStreamIdx + 1);
              } else {
                setErrorText("Stream connection lost. End of available servers.");
                onVideoError?.("HLS Network Error: " + data.details);
              }
              break;
            case Hls.ErrorTypes.MEDIA_ERROR:
              hls.recoverMediaError();
              break;
            default:
              setErrorText("Unknown playback interruption. Try reloading.");
              destroyHls();
              onVideoError?.("HLS Fatal Error: " + data.details);
              break;
          }
        }
      });

      hls.attachMedia(video);
      hls.loadSource(streamUrl);
    } else if (video.canPlayType("application/vnd.apple.mpegurl")) {
      // Native Apple HLS (Safari etc)
      setPlayerEngine("Native Apple Safari Engine");
      video.src = streamUrl;
      video.load();
      video.addEventListener("loadedmetadata", onNativeLoaded);
      video.addEventListener("error", onNativeError);
    } else {
      setIsLoading(false);
      cancelWatchdog();
      setErrorText("Your browser does not support HLS streaming.");
    }

    // Controls slide bar idle hiding logic
    let idleTimer: NodeJS.Timeout;
    const resetIdleTimer = () => {
      setIsControlsVisible(true);
      clearTimeout(idleTimer);
      if (isPlaying) {
        idleTimer = setTimeout(() => {
          setIsControlsVisible(false);
        }, 3500);
      }
    };

    const container = containerRef.current;
    if (container) {
      container.addEventListener("mousemove", resetIdleTimer);
      container.addEventListener("touchstart", resetIdleTimer);
    }

    resetIdleTimer();

    return () => {
      destroyHls();
      cancelWatchdog();
      clearTimeout(idleTimer);
      if (video) {
        video.removeEventListener("loadedmetadata", onNativeLoaded);
        video.removeEventListener("error", onNativeError);
      }
      if (container) {
        container.removeEventListener("mousemove", resetIdleTimer);
        container.removeEventListener("touchstart", resetIdleTimer);
      }
    };
  }, [channel, selectedStreamIdx]);

  // Buffer and frames ticking interval (Stats for Nerds)
  useEffect(() => {
    const video = videoRef.current;
    if (!video || youtubeId) return;

    const interval = setInterval(() => {
      // Calculate active buffer length
      let activeBuf = 0;
      const t = video.currentTime;
      try {
        for (let i = 0; i < video.buffered.length; i++) {
          if (t >= video.buffered.start(i) && t <= video.buffered.end(i)) {
            activeBuf = video.buffered.end(i) - t;
            break;
          }
        }
      } catch {}
      setBufferHealth(Number(activeBuf.toFixed(1)));

      // Fetch actual bandwidth from hls.js
      if (hlsRef.current) {
        const est = hlsRef.current.bandwidthEstimate;
        if (est) {
          setBandwidthEst(`${(est / 1_000_000).toFixed(2)} Mbps`);
        } else {
          setBandwidthEst("Analyzing...");
        }
      } else {
        setBandwidthEst("Native Stream Link");
      }

      // Read resolution
      if (video.videoWidth && video.videoHeight) {
        setResolutionText(`${video.videoWidth}x${video.videoHeight}`);
      }

      // Dropped frames count
      if ((video as any).getVideoPlaybackQuality) {
        const q = (video as any).getVideoPlaybackQuality();
        setDroppedFrames({
          dropped: q.droppedVideoFrames || 0,
          total: q.totalVideoFrames || 0
        });
      }
    }, 1000);

    return () => clearInterval(interval);
  }, [youtubeId]);

  const onNativeLoaded = () => {
    setIsLoading(false);
    cancelWatchdog();
    setErrorText(null);
    const video = videoRef.current;
    if (video) {
      video.play()
        .then(() => setIsPlaying(true))
        .catch(() => setIsPlaying(false));
    }
  };

  const onNativeError = () => {
    cancelWatchdog();
    setIsLoading(false);
    if (selectedStreamIdx + 1 < channel.streams.length) {
      onSelectStream(selectedStreamIdx + 1);
    } else {
      setErrorText("Failed to play the stream on native browser player.");
    }
  };

  const startWatchdog = () => {
    cancelWatchdog();
    watchdogRef.current = setTimeout(() => {
      if (isLoading) {
        console.warn("Stream load timed out, switching stream or trying backup.");
        setIsLoading(false);
        if (selectedStreamIdx + 1 < channel.streams.length) {
          onSelectStream(selectedStreamIdx + 1);
        } else {
          setErrorText("Primary source timeout. Stream currently offline.");
        }
      }
    }, 12000);
  };

  const cancelWatchdog = () => {
    if (watchdogRef.current) {
      clearTimeout(watchdogRef.current);
      watchdogRef.current = null;
    }
  };

  const destroyHls = () => {
    if (hlsRef.current) {
      hlsRef.current.destroy();
      hlsRef.current = null;
    }
  };

  // Play Actions
  const handlePlayPause = (e?: MouseEvent<any> | any) => {
    e?.stopPropagation();
    const video = videoRef.current;
    if (!video || youtubeId) return;

    if (isPlaying) {
      video.pause();
      setIsPlaying(false);
    } else {
      video.play()
        .then(() => setIsPlaying(true))
        .catch(() => setIsPlaying(false));
    }
  };

  // Mute Actions
  const handleMuteToggle = (e?: MouseEvent<any> | any) => {
    e?.stopPropagation();
    const video = videoRef.current;
    if (!video || youtubeId) return;

    const nextMute = !isMuted;
    setIsMuted(nextMute);
    video.muted = nextMute;
  };

  const handleVolumeChange = (e: ChangeEvent<HTMLInputElement>) => {
    const val = parseFloat(e.target.value);
    setVolume(val);
    const video = videoRef.current;
    if (video) {
      video.volume = val;
      video.muted = val === 0;
      setIsMuted(val === 0);
    }
  };

  // Subtitle / Accessibility CC toggle
  const handleSubtitleToggle = (e: MouseEvent<HTMLButtonElement>) => {
    e.stopPropagation();
    const video = videoRef.current;
    if (!video) return;

    const nextCC = !subtitleEnabled;
    setSubtitleEnabled(nextCC);

    if (hlsRef.current) {
      hlsRef.current.subtitleTrack = nextCC ? 0 : -1;
    } else if (video.textTracks) {
      for (let i = 0; i < video.textTracks.length; i++) {
        const track = video.textTracks[i];
        if (track.kind === "subtitles" || track.kind === "captions") {
          track.mode = nextCC ? "showing" : "disabled";
        }
      }
    }
  };

  // Full Screen Actions
  const handleFullscreenToggle = (e: MouseEvent<HTMLButtonElement>) => {
    e.stopPropagation();
    const container = containerRef.current;
    if (!container) return;

    if (!document.fullscreenElement) {
      container.requestFullscreen()
        .then(() => setIsFullscreen(true))
        .catch((err) => console.error(err));
    } else {
      document.exitFullscreen()
        .then(() => setIsFullscreen(false));
    }
  };

  // Trigger retry on active channel stream
  const handleRetryStream = (e: MouseEvent<HTMLButtonElement>) => {
    e.stopPropagation();
    setIsLoading(true);
    setErrorText(null);
    onSelectStream(selectedStreamIdx);
  };

  return (
    <div
      ref={containerRef}
      className={`relative w-full aspect-video bg-black rounded-3xl overflow-hidden shadow-2xl transition-all duration-300 ${
        isFullscreen ? "h-screen rounded-none" : ""
      }`}
    >
      {/* Ambience Soft Colored Back Glow Grid */}
      <div className="absolute inset-0 bg-radial-to-t from-purple-500/10 via-transparent to-neutral-950/40 pointer-events-none z-0" />

      {/* YouTube Live Embed Iframe */}
      {youtubeId ? (
        <iframe
          src={`https://www.youtube.com/embed/${youtubeId}?autoplay=1&mute=${isMuted ? 1 : 0}&rel=0&controls=1&modestbranding=1`}
          className="absolute inset-0 w-full h-full border-none z-10"
          allow="autoplay; encrypted-media; picture-in-picture"
          allowFullScreen
          referrerPolicy="no-referrer"
        />
      ) : (
        /* Native HTML5 Live Streaming Node */
        <video
          ref={videoRef}
          playsInline
          crossOrigin="anonymous"
          className="w-full h-full object-contain relative z-10"
          onClick={() => handlePlayPause()}
        />
      )}

      {/* Loading Splash Spinner */}
      {isLoading && !youtubeId && (
        <div className="absolute inset-0 bg-neutral-950/80 backdrop-blur-xs flex flex-col items-center justify-center z-30 gap-3">
          <div className="w-12 h-12 rounded-full border-4 border-purple-500/20 border-t-purple-500 animate-spin" />
          <span className="text-purple-500 text-xs font-bold uppercase tracking-widest font-mono">
            Buffering Stream...
          </span>
        </div>
      )}

      {/* Stream Failure / Errors Display Overlay */}
      {errorText && !isLoading && (
        <div className="absolute inset-0 bg-neutral-950/95 flex flex-col items-center justify-center z-30 p-6 text-center gap-4">
          <div className="w-12 h-12 rounded-full bg-purple-600/10 flex items-center justify-center border border-purple-500/20">
            <Settings className="w-6 h-6 text-purple-500 animate-spin duration-[6s]" />
          </div>
          <div className="flex flex-col gap-1">
            <h3 className="text-white font-bold text-base">Stream Offline or Restricted</h3>
            <p className="text-zinc-500 text-xs max-w-sm leading-relaxed">{errorText}</p>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={handleRetryStream}
              className="px-4 py-2 bg-purple-600 hover:bg-purple-500 rounded-full font-bold text-xs uppercase tracking-wider flex items-center gap-1.5 active:scale-95 transition-transform text-white cursor-pointer"
            >
              <RefreshCw className="w-3.5 h-3.5" />
              Reconnect
            </button>
            {channel.streams.length > 1 && (
              <button
                onClick={() => onSelectStream((selectedStreamIdx + 1) % channel.streams.length)}
                className="px-4 py-2 bg-neutral-800 hover:bg-neutral-700 border border-white/5 rounded-full font-bold text-xs uppercase tracking-wider active:scale-95 transition-all text-white"
              >
                Try Backup Server
              </button>
            )}
          </div>
        </div>
      )}

      {/* YouTube-Like Control Interface Bar */}
      {!youtubeId && isControlsVisible && !errorText && (
        <div className="absolute inset-0 z-20 bg-gradient-to-t from-black/90 via-transparent to-black/40 flex flex-col justify-between p-4 pointer-events-none">
          {/* Controls Top Rail Info */}
          <div className="flex items-center justify-between w-full pointer-events-auto">
            <div className="flex items-center gap-3">
              <span className="font-bold text-sm text-white drop-shadow-md">
                {channel.name} / {activeStream.label}
              </span>
            </div>

            <div className="flex items-center gap-2">
              {/* CC Button Toggle */}
              <button
                onClick={handleSubtitleToggle}
                className={`p-2 rounded-full cursor-pointer transition-colors ${
                  subtitleEnabled ? "bg-purple-600 text-white" : "bg-black/30 hover:bg-black/60 text-white/80"
                }`}
                title="CC Subtitles"
              >
                <Subtitles className="w-4 h-4" />
              </button>

              {/* Stats for nerds toggler */}
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  setShowStats(!showStats);
                }}
                className={`p-2 rounded-full cursor-pointer transition-colors ${
                  showStats ? "bg-purple-500 text-neutral-950" : "bg-black/30 hover:bg-black/60 text-white/80"
                }`}
                title="Stats for Nerds"
              >
                <Activity className="w-4 h-4" />
              </button>
            </div>
          </div>

          {/* Controls Bottom Navigation / Slider Rail */}
          <div className="flex items-center justify-between w-full pointer-events-auto mt-auto">
            {/* Play, Volume controls */}
            <div className="flex items-center gap-3.5">
              <button
                onClick={handlePlayPause}
                className="w-10 h-10 rounded-full bg-purple-600 hover:bg-purple-500 flex items-center justify-center cursor-pointer transition-all active:scale-90"
              >
                {isPlaying ? <Pause className="w-5 h-5 fill-white text-white" /> : <Play className="w-5 h-5 fill-white text-white ml-0.5" />}
              </button>

              {/* Volume Slider Block */}
              <div className="flex items-center gap-2 group/volume bg-black/40 px-3 py-1.5 rounded-full border border-white/5">
                <button onClick={handleMuteToggle} className="text-white">
                  {isMuted ? <VolumeX className="w-4 h-4" /> : <Volume2 className="w-4 h-4" />}
                </button>
                <input
                  type="range"
                  min="0"
                  max="1"
                  step="0.05"
                  value={isMuted ? 0 : volume}
                  onChange={handleVolumeChange}
                  className="w-14 h-1 rounded-lg bg-zinc-700 accent-purple-600 cursor-pointer outline-none"
                />
              </div>
            </div>

            {/* Quality list dropdown & full viewport toggles */}
            <div className="flex items-center gap-3">
              {/* Quality Settings Selector */}
              {channel.streams.length > 1 && (
                <div className="relative">
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      setShowQualityMenu(!showQualityMenu);
                    }}
                    className="flex items-center gap-1.5 bg-black/40 px-3 py-1.5 rounded-full border border-white/5 hover:bg-black/60 text-white transition-all text-xs font-bold cursor-pointer"
                  >
                    <Settings className="w-3.5 h-3.5 animate-spin duration-[10s]" />
                    <span>{channel.streams[selectedStreamIdx]?.label.toUpperCase()}</span>
                  </button>

                  {showQualityMenu && (
                    <div className="absolute bottom-12 right-0 bg-[#121212] border border-white/10 rounded-2xl p-2 w-48 shadow-xl z-50 backdrop-blur-md">
                      <div className="px-3 py-1.5 border-b border-white/5 text-[10px] uppercase font-black tracking-widest text-zinc-500 mb-1">
                        Select Stream Server
                      </div>
                      <div className="flex flex-col gap-0.5">
                        {channel.streams.map((stream, idx) => (
                          <button
                            key={idx}
                            onClick={() => {
                              onSelectStream(idx);
                              setShowQualityMenu(false);
                            }}
                            className={`flex items-center justify-between px-3 py-2 rounded-xl text-left text-xs font-bold cursor-pointer transition-colors ${
                              idx === selectedStreamIdx
                                ? "bg-purple-600 text-white"
                                : "hover:bg-white/5 text-zinc-400"
                            }`}
                          >
                            <span>{stream.label}</span>
                            {idx === selectedStreamIdx && <span className="w-1.5 h-1.5 rounded-full bg-white" />}
                          </button>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              )}

              {/* Target screen viewport expansion */}
              <button
                onClick={handleFullscreenToggle}
                className="p-2 bg-black/40 hover:bg-black/60 rounded-full text-white cursor-pointer transition-all active:scale-95"
              >
                <Maximize className="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>
      )}

      {/* YouTube's YouTube-Style "Stats for Nerds" Overlay Panel */}
      {showStats && !youtubeId && (
        <div className="absolute top-4 left-4 z-40 bg-zinc-950/95 border border-white/10 rounded-2xl p-4 w-72 md:w-80 font-mono text-[10px] text-zinc-300 shadow-2xl flex flex-col gap-2 backdrop-blur-md shadow-purple-900/10">
          {/* Header row */}
          <div className="flex items-center justify-between border-b border-white/5 pb-2 text-purple-400 font-bold">
            <span className="flex items-center gap-1">
              <Zap className="w-3.5 h-3.5 text-purple-500 fill-purple-500" />
              STATS FOR NERDS
            </span>
            <button
              onClick={() => setShowStats(false)}
              className="p-1 hover:bg-white/5 rounded-md cursor-pointer"
            >
              <X className="w-3.5 h-3.5 text-zinc-500 hover:text-white" />
            </button>
          </div>

          {/* Stats matrix list */}
          <div className="flex flex-col gap-1.5">
            <div className="flex justify-between">
              <span className="text-zinc-500">Channel ID:</span>
              <span className="text-white font-medium">{channel.id}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-zinc-500">Sub-ID sCPN:</span>
              <span className="text-white truncate max-w-[140px]">{scpnValue}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-zinc-500">Resolution:</span>
              <span className="text-white font-medium">{resolutionText}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-zinc-500">Buffer Health:</span>
              <span className={`font-bold ${bufferHealth > 6 ? "text-emerald-400" : "text-amber-400"}`}>
                {bufferHealth} sec
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-zinc-500">Est. Bandwidth:</span>
              <span className="text-purple-400 font-bold">{bandwidthEst}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-zinc-500">Dropped Frames:</span>
              <span className="text-zinc-400">
                {droppedFrames.dropped} of {droppedFrames.total}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-zinc-500">Engine Core:</span>
              <span className="text-purple-400 italic">{playerEngine}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-zinc-500">Audio Codec:</span>
              <span className="text-zinc-400">aac / mp4a.40.2</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
