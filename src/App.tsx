/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useEffect, useState, MouseEvent } from "react";
import {
  Home,
  Tv,
  Film,
  User,
  Heart,
  Search,
  ChevronRight,
  Flame,
  Radio,
  History,
  Star,
  Globe,
  HelpCircle,
  TrendingUp,
  Volume2,
  ListRestart,
  AppWindow,
  Compass,
  ArrowLeft,
  Share2,
  X,
  MessageCircle,
  Bell,
  Sparkles,
  Pocket,
  BadgeCheck,
  CheckCircle2,
  Lock
} from "lucide-react";
import { AnimatePresence, motion } from "motion/react";

import AndroidFrame from "./components/AndroidFrame";
import SplashScreen from "./components/SplashScreen";
import PremiumPlayer from "./components/PremiumPlayer";
import LiveChat from "./components/LiveChat";
import { Channel, Category, AppTheme } from "./types";
import { CHANNELS_DATA, ALL_CHANNELS } from "./data/channelsData";

interface HeroBanner {
  title: string;
  subtitle: string;
  tag: string;
  channelId: string;
}

const HERO_BANNERS: HeroBanner[] = [
  {
    title: "LIVE CRICKET EXTRAVAGANZA",
    subtitle: "Watch qualification battles live from high bitrate FHD stream nodes.",
    tag: "LIVE SPORTS",
    channelId: "beinsports1"
  },
  {
    title: "PREMIUM CINE MANIA",
    subtitle: "Sit back and enjoy high-action blockbuster movies non-stop.",
    tag: "MOVIE BONANZA",
    channelId: "sonymax-hd"
  },
  {
    title: "24/7 GLOBE REPORTING",
    subtitle: "Transparent updates and real-time coverage from worldwide reporters.",
    tag: "NEWS REELS",
    channelId: "al-jazeera"
  }
];

const LANDING_TABS = ["Live TV", "Sports", "News", "Entertainment", "Kids", "Infotainment", "Religious", "Indian"];
const MAIN_CATEGORIES = ["Sports", "News", "Entertainment", "Kids", "Infotainment", "Religious", "Indian"];

export default function App() {
  const [showSplash, setShowSplash] = useState(true);
  const [activeTab, setActiveTab] = useState<"home" | "fifa" | "favorites">("home");
  const [landingTab, setLandingTab] = useState("Live TV");
  const [activeChannel, setActiveChannel] = useState<Channel | null>(null);
  const [activeStreamIdx, setActiveStreamIdx] = useState(0);
  const [searchQuery, setSearchQuery] = useState("");
  const [isSearchExpanded, setIsSearchExpanded] = useState(false);
  
  // Tab inside active streaming channel Detail View: "chat" or "related"
  const [streamDetailTab, setStreamDetailTab] = useState<"chat" | "related">("chat");

  const [username, setUsername] = useState(() => {
    return localStorage.getItem("chat_username") || "Toffee Guest";
  });

  // Client-side favorites list mapped to channel ids
  const [favorites, setFavorites] = useState<string[]>(() => {
    try {
      return JSON.parse(localStorage.getItem("iptv-favorites") || '["sonymax-hd", "beinsports1", "loli-kids"]');
    } catch {
      return ["sonymax-hd", "beinsports1", "loli-kids"];
    }
  });

  // Client-side watch recents history list
  const [watchHistory, setWatchHistory] = useState<string[]>(() => {
    try {
      return JSON.parse(localStorage.getItem("iptv-history") || "[]");
    } catch {
      return [];
    }
  });

  // Sync favorites & history changes
  useEffect(() => {
    localStorage.setItem("iptv-favorites", JSON.stringify(favorites));
  }, [favorites]);

  useEffect(() => {
    localStorage.setItem("iptv-history", JSON.stringify(watchHistory));
  }, [watchHistory]);

  const handleSelectChannel = (ch: Channel) => {
    setActiveChannel(ch);
    setActiveStreamIdx(0);
    setStreamDetailTab("chat"); // default to channel-isolated live fan chat

    // Append to simple watch history
    setWatchHistory((prev) => {
      const filtered = prev.filter((id) => id !== ch.id);
      return [ch.id, ...filtered].slice(0, 15);
    });
  };

  const handleToggleFavorite = (id: string, e?: MouseEvent<any> | any) => {
    e?.stopPropagation();
    if (favorites.includes(id)) {
      setFavorites(favorites.filter((x) => x !== id));
    } else {
      setFavorites([...favorites, id]);
    }
  };

  const continueWatchingList = ALL_CHANNELS.filter((ch) => watchHistory.includes(ch.id));

  // Category Shelf Filter (News, Movies, Entertainment, Infotainment, Kids, Religious, Indian)
  const getChannelsByCategory = (catName: string) => {
    return ALL_CHANNELS.filter((ch) => {
      if (catName === "News") return ch.category?.toLowerCase().includes("news");
      if (catName === "Sports") return ch.category?.toLowerCase().includes("sport");
      if (catName === "Entertainment") return ch.category?.toLowerCase().includes("entertainment") || ch.category?.toLowerCase().includes("drama") || ch.category?.toLowerCase().includes("general");
      if (catName === "Kids") return ch.category?.toLowerCase().includes("kid") || ch.category?.toLowerCase().includes("cartoon") || ch.category?.toLowerCase().includes("family");
      if (catName === "Infotainment") return ch.category?.toLowerCase().includes("infotainment") || ch.category?.toLowerCase().includes("science") || ch.category?.toLowerCase().includes("discovery");
      if (catName === "Religious") return ch.category?.toLowerCase().includes("religious") || ch.category?.toLowerCase().includes("islamic") || ch.category?.toLowerCase().includes("faith");
      if (catName === "Indian") return ch.category?.toLowerCase().includes("indian") || ch.category?.toLowerCase().includes("hindi");
      return ch.category?.toLowerCase() === catName.toLowerCase();
    });
  };

  return (
    <div className="w-full h-full flex flex-col bg-[#0F0F0F] relative overflow-hidden font-sans text-white/90 select-none">
      {/* Splash Screen */}
      <AnimatePresence>
        {showSplash && (
          <motion.div
            key="splash"
            initial={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.3 }}
            className="absolute inset-0 z-[1000]"
          >
            <SplashScreen onFinish={() => setShowSplash(false)} />
          </motion.div>
        )}
      </AnimatePresence>

      {/* Main Core Frame container */}
      {!showSplash && (
        <AndroidFrame
          onHomePress={() => {
            setActiveChannel(null);
            setActiveTab("home");
            setSearchQuery("");
          }}
          onBackPress={() => {
            if (activeChannel) {
              setActiveChannel(null);
            } else if (activeTab !== "home" || searchQuery) {
              setActiveTab("home");
              setSearchQuery("");
            }
          }}
        >
          {/* Active channel video stream layout */}
          {activeChannel ? (
            <div className="flex-1 flex flex-col h-full bg-[#090909] relative overflow-hidden animate-fade-in">
              
              {/* Back navigation header overlay on top of aspect frame */}
              <div className="absolute top-2.5 left-3.5 z-40 flex items-center gap-2 bg-black/60 px-3.5 py-1.5 rounded-full border border-white/10 backdrop-blur-md shadow-lg select-none">
                <button
                  onClick={() => setActiveChannel(null)}
                  className="p-0.5 cursor-pointer flex items-center justify-center transition-transform active:-translate-x-1"
                >
                  <ArrowLeft className="w-4 h-4 text-white" />
                </button>
                <div className="w-[1px] h-3 bg-white/20" />
                <span className="text-[10px] text-purple-300 font-extrabold tracking-wider uppercase font-mono">
                  {activeChannel.category || "Live OTT"}
                </span>
              </div>

              {/* PERFECT FIT ASPECT VIDEO PLAYER WINDOW - Compact, snug, zero chunky borders! */}
              <div className="w-full bg-black aspect-video relative z-30 shadow-[0_4px_30px_rgba(0,0,0,0.8)] border-b border-white/5">
                <PremiumPlayer
                  channel={activeChannel}
                  selectedStreamIdx={activeStreamIdx}
                  onSelectStream={(idx) => setActiveStreamIdx(idx)}
                />
              </div>

              {/* STREAM METADATA GRID & SPLIT VIEWPORT TAB BED */}
              <div className="flex-1 flex flex-col overflow-hidden bg-[#0A0A0A]">
                
                {/* Meta details header slot */}
                <div className="p-4 bg-gradient-to-b from-[#111] to-transparent border-b border-white/5 space-y-1.5 shrink-0">
                  <div className="flex items-center justify-between">
                    <span className="text-[10px] bg-purple-500/10 text-purple-400 border border-purple-500/20 px-2.5 py-0.5 rounded-full font-bold uppercase tracking-wider font-mono">
                      FHD Stream Server
                    </span>
                    <button
                      onClick={() => handleToggleFavorite(activeChannel.id)}
                      className="flex items-center gap-1 text-xs text-zinc-400 hover:text-white transition-colors"
                    >
                      <Heart className={`w-3.5 h-3.5 ${favorites.includes(activeChannel.id) ? "fill-purple-500 text-purple-500" : ""}`} />
                      <span className="text-[10px] font-bold">FAVORITE</span>
                    </button>
                  </div>
                  <h2 className="text-sm font-black text-white leading-snug">{activeChannel.name}</h2>
                  <p className="text-[10px] text-zinc-500 leading-normal line-clamp-1">
                    {activeChannel.description || "Active high-bitrate live network node array stream feed."}
                  </p>
                </div>

                {/* DYNAMIC META TABS SWITCHER PANEL (Isolated Chat vs Related Streams) */}
                <div className="flex border-b border-white/5 bg-zinc-950/20 px-4 shrink-0">
                  <button
                    onClick={() => setStreamDetailTab("chat")}
                    className={`flex-1 py-3 text-xs font-bold flex items-center justify-center gap-2 border-b-2 transition-all ${
                      streamDetailTab === "chat"
                        ? "border-purple-500 text-purple-400 font-extrabold"
                        : "border-transparent text-zinc-500 hover:text-zinc-300"
                    }`}
                  >
                    <MessageCircle className="w-4 h-4" />
                    <span>Live Chat</span>
                  </button>
                  <button
                    onClick={() => setStreamDetailTab("related")}
                    className={`flex-1 py-3 text-xs font-bold flex items-center justify-center gap-2 border-b-2 transition-all ${
                      streamDetailTab === "related"
                        ? "border-purple-500 text-purple-400 font-extrabold"
                        : "border-transparent text-zinc-500 hover:text-zinc-300"
                    }`}
                  >
                    <Radio className="w-4 h-4" />
                    <span>More Channels</span>
                  </button>
                </div>

                {/* TAB CONTROLLERS PANEL VIEWPORT CONTAINER */}
                <div className="flex-1 overflow-hidden relative flex flex-col p-3">
                  {streamDetailTab === "chat" ? (
                    <div className="flex-1 flex flex-col h-full overflow-hidden">
                      {/* LiveChat isolated strictly by channelId */}
                      <LiveChat channelId={activeChannel.id} />
                    </div>
                  ) : (
                    /* Related streams scroll menu list */
                    <div className="flex-1 overflow-y-auto space-y-2.5 pr-1 scrollbar-none">
                      {ALL_CHANNELS.filter((item) => item.id !== activeChannel.id).map((ch) => {
                        const isFav = favorites.includes(ch.id);
                        return (
                          <div
                            key={ch.id}
                            onClick={() => handleSelectChannel(ch)}
                            className="bg-[#141414]/90 hover:bg-[#1C1C1C] border border-white/5 hover:border-purple-500/20 p-2.5 rounded-2xl flex items-center justify-between cursor-pointer transition-all active:scale-[0.99] group shadow-md"
                          >
                            <div className="flex items-center gap-3 min-w-0">
                              <div className="w-10 h-10 rounded-xl bg-white overflow-hidden flex items-center justify-center border border-white/10 shadow-sm shrink-0 font-mono text-[10px] font-black text-zinc-850">
                                {ch.shortName}
                              </div>
                              <div className="min-w-0">
                                <span className="block text-xs font-extrabold text-zinc-300 group-hover:text-white truncate">
                                  {ch.name}
                                </span>
                                <span className="block text-[8px] text-purple-400 font-mono uppercase tracking-widest mt-0.5">
                                  {ch.category || "TV Channel"}
                                </span>
                              </div>
                            </div>
                            <button
                              onClick={(e) => {
                                e.stopPropagation();
                                handleToggleFavorite(ch.id);
                              }}
                              className="p-1 px-1.5 text-zinc-500 hover:text-white transition-colors"
                            >
                              <Heart className={`w-3.5 h-3.5 ${isFav ? "fill-purple-500 text-purple-500" : ""}`} />
                            </button>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>
              </div>
            </div>
          ) : (
            /* LOBBY / DISCOVERY CONTENT MAIN VIEWPORTS */
            <div className="flex-1 flex flex-col h-full bg-[#0B0B0C] overflow-hidden relative">
              
              {/* GLASSY STICKY HEAD NAVBAR with unified sticky Search field */}
              <div className="px-4 py-3 bg-[#111113]/30 backdrop-blur-xl border-b border-white/5 flex items-center justify-between select-none shrink-0 z-50 shadow-[0_2px_15px_rgba(0,0,0,0.5)]">
                <div className="flex items-center gap-2">
                  <div className="w-7 h-7 rounded-xl bg-gradient-to-tr from-purple-600 to-indigo-500 flex items-center justify-center shadow-lg shadow-purple-600/30">
                    <Radio className="w-4 h-4 text-white hover:animate-ping animate-pulse" />
                  </div>
                  <h1 className="text-base font-black tracking-tighter text-white leading-none">
                    T9<span className="text-purple-500 italic font-black text-xs leading-none ml-0.5">TV</span>
                  </h1>
                </div>

                {/* SLEEK GLASSY SEARCH CONTAINER IN NAVBAR THAT EXPANDS ON CLICK */}
                <div className="flex items-center gap-2">
                  {!isSearchExpanded && !searchQuery ? (
                    <button
                      onClick={() => setIsSearchExpanded(true)}
                      className="p-2 hover:bg-white/[0.06] rounded-xl text-zinc-400 hover:text-white transition-all cursor-pointer"
                      id="navbar-search-trigger"
                    >
                      <Search className="w-4 h-4 text-white hover:scale-110 transition-transform" />
                    </button>
                  ) : (
                    <motion.div
                      initial={{ width: 44, opacity: 0 }}
                      animate={{ width: 180, opacity: 1 }}
                      transition={{ type: "spring", stiffness: 300, damping: 25 }}
                      className="relative flex items-center"
                    >
                      <input
                        type="text"
                        autoFocus
                        placeholder="Search..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        className="w-full bg-white/[0.06] border border-white/10 rounded-xl py-1.5 pl-8 pr-7 text-[11px] font-semibold text-white/90 placeholder-zinc-500 outline-none focus:border-purple-500/50 transition-all font-sans"
                      />
                      <Search className="w-3 h-3 text-zinc-400 absolute left-2.5" />
                      <button
                        onClick={() => {
                          setSearchQuery("");
                          setIsSearchExpanded(false);
                        }}
                        className="absolute right-2 text-zinc-400 hover:text-white p-0.5"
                      >
                        <X className="w-3 h-3" />
                      </button>
                    </motion.div>
                  )}
                </div>
              </div>

              {/* RENDER VIEWPORTS BASED ON ACTIVE_TAB */}
              <div className="flex-1 overflow-hidden relative flex flex-col">
                {searchQuery ? (
                  /* DYNAMIC COHESIVE REAL-TIME SEARCH RESULTS PANEL */
                  <div className="flex-1 flex flex-col p-4 overflow-y-auto scrollbar-none gap-4 pb-24 animate-fade-in select-none">
                    <div className="flex items-center justify-between px-1">
                      <span className="text-[10px] text-purple-400 uppercase tracking-widest font-black font-mono">
                        Search Results ({ALL_CHANNELS.filter(ch => ch.name.toLowerCase().includes(searchQuery.toLowerCase())).length})
                      </span>
                    </div>

                    {ALL_CHANNELS.filter(ch => ch.name.toLowerCase().includes(searchQuery.toLowerCase())).length === 0 ? (
                      <div className="flex flex-col items-center justify-center p-12 text-center border border-white/5 bg-neutral-950/20 rounded-3xl gap-2 mt-2">
                        <Tv className="w-8 h-8 text-zinc-700 animate-bounce" />
                        <span className="text-zinc-400 font-bold text-xs">No Channel Matches</span>
                        <span className="text-zinc-600 text-[10px]">Try searching for other network titles.</span>
                      </div>
                    ) : (
                      <div className="grid grid-cols-2 gap-3.5">
                        {ALL_CHANNELS.filter(ch => ch.name.toLowerCase().includes(searchQuery.toLowerCase())).map((ch) => {
                          const isFav = favorites.includes(ch.id);
                          return (
                            <div
                              key={ch.id}
                              onClick={() => handleSelectChannel(ch)}
                              className="group bg-[#141416] border border-white/5 hover:border-purple-500/30 hover:bg-[#1A1A1D] rounded-3xl p-3 flex flex-col justify-between aspect-video gap-3 cursor-pointer shadow-lg active:scale-95 transition-all relative"
                            >
                              <div className="flex items-start justify-between">
                                <div className="w-10 h-10 rounded-2xl bg-white overflow-hidden flex items-center justify-center border border-white/5 shrink-0">
                                  <span className="font-mono text-zinc-800 text-[10px] font-black uppercase text-center">{ch.shortName}</span>
                                </div>
                                <button
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    handleToggleFavorite(ch.id);
                                  }}
                                  className="p-1.5 bg-black/40 hover:bg-black/80 rounded-xl cursor-pointer text-white/50 hover:text-white transition-all border border-white/5"
                                >
                                  <Heart className={`w-3 h-3 ${isFav ? "fill-purple-500 text-purple-500" : ""}`} />
                                </button>
                              </div>

                              <div className="flex flex-col gap-0.5 leading-none">
                                <span className="text-white text-xs font-extrabold truncate leading-tight">{ch.name}</span>
                                <div className="flex items-center gap-1.5 mt-1 text-zinc-400">
                                  <span className="text-[9px] uppercase font-bold tracking-wider font-mono">
                                    {ch.quality} • {ch.category}
                                  </span>
                                </div>
                              </div>
                            </div>
                          );
                        })}
                      </div>
                    )}
                  </div>
                ) : (
                  /* TAB NAVIGATION RENDERING */
                  <>
                    {activeTab === "home" && (
                      <div className="flex-1 flex flex-col overflow-hidden animate-fade-in">
                        
                        {/* DYNAMIC TOP LANDING CATEGORY Pills TAB BAR (With smooth sliding underline like screenshot!) */}
                        <div className="flex items-center gap-6 overflow-x-auto scrollbar-none border-b border-white/5 px-4 bg-zinc-950/20 select-none shrink-0 pointer-events-auto">
                          {LANDING_TABS.map((tab) => {
                            const isSelected = landingTab === tab;
                            return (
                              <button
                                key={tab}
                                onClick={() => setLandingTab(tab)}
                                className="relative py-3 px-1 text-xs font-bold transition-all whitespace-nowrap shrink-0 group cursor-pointer"
                              >
                                <span className={isSelected ? "text-purple-400 font-extrabold" : "text-zinc-400 hover:text-zinc-200"}>
                                  {tab}
                                </span>
                                {isSelected ? (
                                  <motion.div
                                    layoutId="landing-active-underline"
                                    className="absolute bottom-0 left-0 right-0 h-0.5 bg-purple-500 rounded-full"
                                    transition={{ type: "spring", stiffness: 350, damping: 30 }}
                                  />
                                ) : (
                                  <div className="absolute bottom-0 left-0 right-0 h-[2px] bg-transparent group-hover:bg-purple-500/30 rounded-full transition-all" />
                                )}
                              </button>
                            );
                          })}
                        </div>

                        {/* LANDING TAB MAIN CONTENT FLOW */}
                        <div className="flex-1 overflow-y-auto p-4 space-y-6 pb-24 scrollbar-none">
                          
                          {landingTab === "Live TV" ? (
                            /* ALL CHANNEL CATEGORIES FITTED DIRECTLY IN RESPONSIVE GRIDS (No Sliders / No View All) */
                            <>
                              {/* Continue History list scroll */}
                              {continueWatchingList.length > 0 && (
                                <div className="flex flex-col gap-3">
                                  <span className="text-[10px] text-zinc-500 uppercase tracking-widest font-black font-mono">
                                    Continue Watching
                                  </span>
                                  <div className="flex items-center gap-3.5 overflow-x-auto scrollbar-none py-1">
                                    {continueWatchingList.slice(0, 5).map((ch, idx) => {
                                      const progressPct = [74, 42, 90, 55, 80][idx % 5];
                                      return (
                                        <div
                                          key={ch.id}
                                          onClick={() => handleSelectChannel(ch)}
                                          className="w-[130px] shrink-0 bg-[#161618] border border-white/5 hover:border-purple-500/20 rounded-2xl p-2.5 flex flex-col justify-between h-[90px] relative overflow-hidden cursor-pointer active:scale-95 transition-all"
                                        >
                                          <div className="flex items-start justify-between">
                                            <div className="w-8 h-8 rounded-xl bg-white overflow-hidden flex items-center justify-center font-mono text-[9px] font-black text-zinc-805 text-zinc-800">
                                              {ch.shortName}
                                            </div>
                                            <span className="text-[8px] font-mono text-zinc-400">
                                              {progressPct}%
                                            </span>
                                          </div>
                                          <span className="text-zinc-200 font-bold text-[9px] truncate leading-tight mt-1">
                                            {ch.name}
                                          </span>
                                          <div className="absolute bottom-0 left-0 w-full h-[3px] bg-white/5">
                                            <div
                                              className="h-full bg-purple-500 transition-all rounded-r"
                                              style={{ width: `${progressPct}%` }}
                                            />
                                          </div>
                                        </div>
                                      );
                                    })}
                                  </div>
                                </div>
                              )}

                              {MAIN_CATEGORIES.map((cat) => {
                                const channels = getChannelsByCategory(cat);
                                if (channels.length === 0) return null;
                                return (
                                  <div key={cat} className="flex flex-col gap-3">
                                    <span className="text-xs text-white font-extrabold tracking-tight border-l-2 border-purple-500 pl-2">
                                      {cat} Channels
                                    </span>
                                    <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
                                      {channels.map((ch) => {
                                        const isFav = favorites.includes(ch.id);
                                        return (
                                          <div
                                            key={ch.id}
                                            onClick={() => handleSelectChannel(ch)}
                                            className="group bg-[#141416]/90 border border-white/5 hover:border-purple-500/20 rounded-2xl p-3 flex flex-col justify-between aspect-video gap-3 cursor-pointer shadow-md select-none transition-all active:scale-95 relative overflow-hidden"
                                          >
                                            <div className="flex items-start justify-between">
                                              <div className="w-10 h-10 rounded-2xl bg-white overflow-hidden flex items-center justify-center border border-white/5 shrink-0 shadow-inner">
                                                <span className="font-mono text-zinc-800 text-[10px] font-black uppercase text-center leading-none px-1">
                                                  {ch.shortName}
                                                </span>
                                              </div>
                                              <button
                                                onClick={(e) => {
                                                  e.stopPropagation();
                                                  handleToggleFavorite(ch.id);
                                                }}
                                                className="p-1 px-[5px] bg-black/40 hover:bg-black/80 rounded-lg text-white/55 hover:text-purple-400 border border-white/5 cursor-pointer z-10"
                                              >
                                                <Heart className={`w-3.5 h-3.5 ${isFav ? "fill-purple-500 text-purple-500" : ""}`} />
                                              </button>
                                            </div>

                                            <div className="flex flex-col gap-0.5 leading-none">
                                              <span className="text-white text-xs font-semibold truncate group-hover:text-purple-400">{ch.name}</span>
                                              <span className="text-[9px] text-zinc-500 font-bold mt-1 tracking-wider uppercase font-mono">
                                                {ch.quality}
                                              </span>
                                            </div>
                                          </div>
                                        );
                                      })}
                                    </div>
                                  </div>
                                );
                              })}
                            </>
                          ) : (
                            /* RENDERS OTHER TABS (Sports, News etc.) IN A CLEAN GLASSY GRID SPECIFIC WITH LABELS */
                            <div className="flex flex-col gap-4">
                              <div className="flex items-center justify-between">
                                <span className="text-xs text-white font-extrabold tracking-tight border-l-2 border-purple-500 pl-2">
                                  {landingTab} Library
                                </span>
                              </div>

                              <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
                                {getChannelsByCategory(landingTab).length === 0 ? (
                                  <div className="col-span-full bg-[#121212]/30 p-10 rounded-3xl text-center text-zinc-400 border border-white/5">
                                    <span className="block text-zinc-400 font-bold text-xs">Category Coming Soon</span>
                                    <span className="block text-[10px] text-zinc-600 mt-1">Expanding live server arrays.</span>
                                  </div>
                                ) : (
                                  getChannelsByCategory(landingTab).map((ch) => {
                                    const isFav = favorites.includes(ch.id);
                                    return (
                                      <div
                                        key={ch.id}
                                        onClick={() => handleSelectChannel(ch)}
                                        className="group bg-[#141416]/90 border border-white/5 hover:border-purple-500/20 rounded-2xl p-3 flex flex-col justify-between aspect-video gap-3 cursor-pointer shadow-md select-none transition-all active:scale-95 relative overflow-hidden"
                                      >
                                        <div className="flex items-start justify-between">
                                          <div className="w-10 h-10 rounded-2xl bg-white overflow-hidden flex items-center justify-center border border-white/5 shrink-0 shadow-inner">
                                            <span className="font-mono text-zinc-800 text-[10px] font-black uppercase text-center leading-none px-1">
                                              {ch.shortName}
                                            </span>
                                          </div>
                                          <button
                                            onClick={(e) => {
                                              e.stopPropagation();
                                              handleToggleFavorite(ch.id);
                                            }}
                                            className="p-1 px-[5px] bg-black/40 hover:bg-black/80 rounded-lg text-white/55 hover:text-purple-400 border border-white/5 cursor-pointer z-10"
                                          >
                                            <Heart className={`w-3.5 h-3.5 ${isFav ? "fill-purple-500 text-purple-500" : ""}`} />
                                          </button>
                                        </div>

                                        <div className="flex flex-col gap-0.5 leading-none">
                                          <span className="text-white text-xs font-semibold truncate group-hover:text-purple-400">{ch.name}</span>
                                          <span className="text-[9px] text-zinc-500 font-bold mt-1 tracking-wider uppercase font-mono">
                                            {ch.quality}
                                          </span>
                                        </div>
                                      </div>
                                    );
                                  })
                                )}
                              </div>
                            </div>
                          )}
                        </div>
                      </div>
                    )}                    {/* SPECIAL STADIUM MODE: FIFA 26 (Trophy layout page directly matching the icon center bottom!) */}
                    {activeTab === "fifa" && (
                      <div className="flex-1 flex flex-col p-4 overflow-y-auto scrollbar-none gap-6 pb-24 animate-fade-in select-none">
                        
                        {/* Immersive high resolution stadium banner */}
                        <div className="w-full bg-gradient-to-tr from-yellow-700/60 to-purple-950 border border-yellow-500/20 rounded-3xl p-5 relative overflow-hidden">
                          <div className="absolute top-0 right-0 w-44 h-44 bg-yellow-500/10 rounded-full blur-2xl pointer-events-none" />
                          <div className="relative z-10 flex flex-col items-start gap-2">
                            <div className="flex items-center gap-1.5 bg-yellow-500/20 text-yellow-300 border border-yellow-500/30 px-2.5 py-0.5 rounded-full text-[9px] font-black font-mono tracking-widest">
                              ★ WORLD ARENA PASS ★
                            </div>
                            <h2 className="text-lg font-black tracking-tighter text-white leading-tight">
                              FIFA World Cup 2026 Live
                            </h2>
                            <p className="text-zinc-300 text-[10px] leading-relaxed max-w-[85%]">
                              Broadcasting official high-speed qualifying streams and classic matches in Full HD 1080p.
                            </p>
                          </div>
                        </div>

                        {/* FIFA Match nodes */}
                        <div className="flex flex-col gap-3">
                          <span className="text-[10px] text-yellow-500 uppercase tracking-widest font-black font-mono">
                            FIFA Live Broadcasting Streams
                          </span>

                          <div className="flex flex-col gap-2.5">
                            {ALL_CHANNELS.filter((ch) => ch.id === "fifa-wc-2026" || ch.category?.toLowerCase() === "sports").map((ch) => (
                              <div
                                key={ch.id}
                                onClick={() => handleSelectChannel(ch)}
                                className="bg-[#141416] hover:bg-[#1A1A1D] border border-white/5 hover:border-yellow-500/15 p-3 rounded-2xl flex items-center justify-between cursor-pointer active:scale-98 transition-all"
                              >
                                <div className="flex items-center gap-3">
                                  <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-yellow-500/50 to-amber-500 flex items-center justify-center border border-yellow-400/20 font-mono text-[9px] font-black uppercase text-zinc-950 text-center shadow">
                                    {ch.shortName}
                                  </div>
                                  <div className="flex flex-col">
                                    <span className="text-white font-extrabold text-xs">{ch.name}</span>
                                    <span className="text-[9px] text-zinc-400 font-semibold tracking-wider font-mono">
                                      High Bitrate • {ch.quality}
                                    </span>
                                  </div>
                                </div>

                                <div className="p-2 bg-yellow-500/10 text-yellow-400 font-bold text-[9px] rounded-lg tracking-wider">
                                  ENTER STADIUM
                                </div>
                              </div>
                            ))}
                          </div>
                        </div>
                      </div>
                    )}

                    {/* MY LIST (FAVORITES) VIEW */}
                    {activeTab === "favorites" && (
                      <div className="flex-1 flex flex-col p-4 overflow-y-auto scrollbar-none gap-6 pb-24 animate-fade-in select-none">
                        <span className="text-[10px] text-purple-400 uppercase tracking-widest font-black font-mono">
                          My Premium Favorites list ({favorites.length})
                        </span>

                        {favorites.length === 0 ? (
                          <div className="flex flex-col items-center justify-center p-12 text-center border border-white/5 bg-neutral-950/20 rounded-3xl gap-2 mt-4">
                            <Heart className="w-8 h-8 text-zinc-700 animate-pulse" />
                            <span className="text-zinc-400 font-bold text-xs">No Favorites Added</span>
                            <span className="text-zinc-650 text-[10px]">Mark channels with heart inside lists.</span>
                          </div>
                        ) : (
                          <div className="grid grid-cols-2 gap-3.5">
                            {ALL_CHANNELS.filter((ch) => favorites.includes(ch.id)).map((ch) => (
                              <div
                                key={ch.id}
                                onClick={() => handleSelectChannel(ch)}
                                className="group bg-[#141416] hover:bg-[#1A1A1D] border border-white/5 hover:border-purple-500/20 rounded-3xl p-3 flex flex-col justify-between aspect-video gap-3 cursor-pointer shadow-lg active:scale-95 transition-all relative"
                              >
                                <div className="flex items-start justify-between">
                                  <div className="w-10 h-10 rounded-2xl bg-white overflow-hidden flex items-center justify-center border border-white/5 shrink-0 animate-fade-in">
                                    <span className="font-mono text-zinc-800 text-[10px] font-black uppercase text-center">{ch.shortName}</span>
                                  </div>
                                  <button
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      handleToggleFavorite(ch.id);
                                    }}
                                    className="p-1 px-[5px] bg-black/40 hover:bg-black/80 rounded-lg text-purple-400 border border-white/5 cursor-pointer z-10"
                                  >
                                    <X className="w-3 h-3 text-white" />
                                  </button>
                                </div>

                                <div className="flex flex-col gap-0.5 leading-none">
                                  <span className="text-white text-xs font-extrabold truncate">{ch.name}</span>
                                  <span className="text-[9px] text-zinc-500 font-bold mt-1 tracking-wider uppercase font-mono">
                                    {ch.quality} • {ch.category}
                                  </span>
                                </div>
                              </div>
                            ))}
                          </div>
                        )}
                      </div>
                    )}
                  </>
                )}
              </div>

              {/* DYNAMIC BOTTOM HORIZONTAL TAB CONTROL PANEL - Highly Translucent Glassmorphism with exactly 3 tabs */}
              <div className="absolute bottom-3 inset-x-4 bg-black/40 backdrop-blur-xl rounded-2xl border border-white/10 py-3.5 px-1.5 flex items-center justify-around shadow-[0_8px_32px_0_rgba(0,0,0,0.6)] z-40 select-none">
                
                {/* 1. Home Tab button */}
                <button
                  onClick={() => {
                    setActiveTab("home");
                    setSearchQuery("");
                  }}
                  className={`flex flex-col items-center gap-1 cursor-pointer transition-all duration-350 hover:text-white ${
                    activeTab === "home" ? "text-purple-400 scale-105 font-bold" : "text-zinc-500"
                  }`}
                >
                  <Home className="w-4 h-4" />
                  <span className="text-[8px] font-extrabold uppercase tracking-wide">Home</span>
                </button>

                {/* 2. FIFA 26 Special Trophy Tab (Exact like mobile layout screenshot!) */}
                <button
                  onClick={() => {
                    setActiveTab("fifa");
                    setSearchQuery("");
                  }}
                  className={`flex flex-col items-center gap-1 cursor-pointer transition-all duration-350 hover:text-white -mt-4 relative group ${
                    activeTab === "fifa" ? "text-purple-400 font-bold" : "text-zinc-500"
                  }`}
                >
                  <div className="w-11 h-11 bg-gradient-to-tr from-purple-600 to-indigo-500 rounded-full flex items-center justify-center shadow-lg border border-white/20 scale-110 group-hover:scale-115 transition-transform">
                    {/* Golden Trophy Unicode represent */}
                    <span className="text-xl">🏆</span>
                  </div>
                  <span className="text-[8px] font-extrabold uppercase tracking-wide mt-1">FIFA 26</span>
                </button>

                {/* 3. My List Tab button */}
                <button
                  onClick={() => {
                    setActiveTab("favorites");
                    setSearchQuery("");
                  }}
                  className={`flex flex-col items-center gap-1 cursor-pointer transition-all duration-350 hover:text-white ${
                    activeTab === "favorites" ? "text-purple-400 scale-105 font-bold" : "text-zinc-500"
                  }`}
                >
                  <Heart className="w-4 h-4" />
                  <span className="text-[8px] font-extrabold uppercase tracking-wide">My List</span>
                </button>
              </div>
            </div>
          )}
        </AndroidFrame>
      )}
    </div>
  );
}
