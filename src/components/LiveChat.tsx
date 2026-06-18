/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useEffect, useRef, useState } from "react";
import { Send, Users, ShieldAlert, Sparkles, Flame } from "lucide-react";
import { ChatMessage } from "../types";

interface LiveChatProps {
  channelId: string;
}

const AVATAR_COLORS = [
  "bg-purple-600",
  "bg-indigo-600",
  "bg-violet-600",
  "bg-fuchsia-600",
  "bg-pink-600",
  "bg-blue-600",
  "bg-teal-600",
  "bg-plum-600",
];

const MOCK_MESSAGES_TEMPLATES = [
  "OMG! What a boundary! 🏏🔥",
  "This stream is super clean today, crystal clear! 🙌",
  "Who is watching this live feed from Dhaka? 🇧🇩",
  "This channel video stream is running incredibly smooth!",
  "Gazi TV drama series is on fire right now 😂",
  "Can you guys stream Gopal Bhar 24/7?",
  "Premium purple design is gorgeous!",
  "Who is watching this on their phone? 📱😂",
  "FIFA matches are going to be wild!",
  "Outstanding speed on this OTT player, zero lag!",
  "Love the glassy stream lobby controls!",
];

const MOCK_USERNAMES = [
  "Rashed77",
  "SakibFanboy",
  "Nishat_Islam",
  "Ahsan_Dhaka",
  "Tisha_Fly",
  "Karim_Ali",
  "Tasnim_Z",
  "Siddique_X",
  "Zubayer_🏏",
  "Anika_Sky",
  "Farhan_F",
  "NeonRider",
];

export default function LiveChat({ channelId }: LiveChatProps) {
  const [username, setUsername] = useState(() => localStorage.getItem("chat_username") || "");
  const [avatarColor] = useState(() => AVATAR_COLORS[Math.floor(Math.random() * AVATAR_COLORS.length)]);
  const [inputNick, setInputNick] = useState("");
  const [inputText, setInputText] = useState("");
  const [messages, setMessages] = useState<ChatMessage[]>([]);

  const scrollRef = useRef<HTMLDivElement | null>(null);

  // Load chat profile name
  useEffect(() => {
    if (username) {
      localStorage.setItem("chat_username", username);
    }
  }, [username]);

  // Isolate chat messages by channelId using localStorage persistence
  useEffect(() => {
    const storageKey = `chat_msgs_${channelId}`;
    const cached = localStorage.getItem(storageKey);
    let initialMsgs: ChatMessage[] = [];

    if (cached) {
      try {
        initialMsgs = JSON.parse(cached);
      } catch (e) {
        initialMsgs = [];
      }
    }

    setMessages(initialMsgs);
    scrollToBottom();
  }, [channelId]);

  const scrollToBottom = () => {
    setTimeout(() => {
      if (scrollRef.current) {
        scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
      }
    }, 80);
  };

  const handleJoinChat = (e: React.FormEvent) => {
    e.preventDefault();
    const cleanNick = inputNick.trim();
    if (cleanNick) {
      setUsername(cleanNick);
      localStorage.setItem("chat_username", cleanNick);
      scrollToBottom();
    }
  };

  const handleSendMessageSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const cleanMsg = inputText.trim();
    if (cleanMsg) {
      const userMsg: ChatMessage = {
        id: `user-${channelId}-${Date.now()}`,
        user: username,
        text: cleanMsg,
        color: avatarColor,
        timestamp: Date.now(),
        isSelf: true,
      };

      const storageKey = `chat_msgs_${channelId}`;
      setMessages((prev) => {
        const next = [...prev, userMsg].slice(-50);
        localStorage.setItem(storageKey, JSON.stringify(next));
        return next;
      });

      setInputText("");
      scrollToBottom();
    }
  };

  return (
    <div className="flex-1 flex flex-col h-full bg-[#121212]/20 rounded-3xl overflow-hidden border border-white/5 backdrop-blur-xl">
      {/* Live Chat Ribbons bar */}
      <div className="px-4 py-3 bg-white/[0.02] border-b border-white/5 flex items-center justify-between select-none">
        <div className="flex items-center gap-2">
          <div className="flex items-center gap-1.5 bg-purple-500/10 text-purple-400 px-2.5 py-0.5 rounded-full border border-purple-500/20">
            <Flame className="w-3.5 h-3.5 fill-purple-500 text-purple-400 animate-pulse" />
            <span className="text-[10px] uppercase font-bold tracking-wider font-mono">LIVE ARENA</span>
          </div>
        </div>
      </div>

      {/* Message List Grid viewport */}
      <div
        ref={scrollRef}
        className="flex-1 overflow-y-auto p-4 flex flex-col gap-3 scroll-smooth scrollbar-none"
      >
        {/* Connection Notice banner info */}
        <div className="flex items-start gap-3 bg-purple-500/5 p-3 rounded-2xl border border-purple-500/15 mb-2 select-text">
          <ShieldAlert className="w-4 h-4 text-purple-400 shrink-0 mt-0.5" />
          <div className="flex flex-col gap-0.5">
            <span className="text-white font-bold text-xs flex items-center gap-1">
              Isolated Live Workspace Fan Chat
              <Sparkles className="w-3.5 h-3.5 text-purple-400 fill-purple-400 animate-pulse" />
            </span>
            <span className="text-zinc-500 text-[9px] leading-relaxed">
              Premium sandbox chat feed. Chat history is secure, isolated, and saved independently to this channel.
            </span>
          </div>
        </div>

        {/* Dynamic chat bubbles cascade in */}
        {messages.map((msg) => (
          <div
            key={msg.id}
            className={`flex items-start gap-2.5 max-w-[90%] transition-transform hover:translate-x-1 ${
              msg.isSelf ? "self-end flex-row-reverse" : "self-start"
            }`}
          >
            {/* User Avatar Circle */}
            <div
              className={`w-7 h-7 shrink-0 rounded-xl flex items-center justify-center font-black ${msg.color} text-white shadow-md text-xs uppercase select-none`}
            >
              {msg.user[0]}
            </div>

            {/* Message Bubble disk */}
            <div
              className={`flex flex-col rounded-2xl p-2.5 ${
                msg.isSelf
                  ? "bg-purple-600 rounded-tr-none text-white shadow-lg shadow-purple-900/10"
                  : "bg-[#161616] rounded-tl-none text-zinc-200 border border-white/5"
              }`}
            >
              {!msg.isSelf && (
                <span className="text-[10px] font-bold text-purple-400 tracking-wide select-none">
                  {msg.user}
                </span>
              )}
              <p className="text-xs leading-normal break-words font-medium pr-1">{msg.text}</p>
            </div>
          </div>
        ))}
      </div>

      {/* Footer Text Action / Join Forms bar */}
      <div className="p-3 bg-white/[0.01] border-t border-white/5">
        {!username ? (
          /* NICKNAME REGISTER FORM SCREEN */
          <form onSubmit={handleJoinChat} className="flex flex-col gap-2">
            <span className="text-[9px] text-zinc-500 font-bold uppercase tracking-wider px-1">
              Select username to enter chat
            </span>
            <div className="flex gap-2">
              <input
                type="text"
                maxLength={15}
                required
                value={inputNick}
                onChange={(e) => setInputNick(e.target.value)}
                placeholder="Enter Nickname..."
                className="flex-1 bg-zinc-900 border border-white/5 rounded-xl px-4 py-2.5 text-xs text-white outline-none focus:border-purple-500 font-semibold"
              />
              <button
                type="submit"
                className="px-5 py-2.5 bg-purple-600 hover:bg-purple-500 rounded-xl font-extrabold text-xs text-white uppercase tracking-wider active:scale-95 transition-transform cursor-pointer shadow-lg shadow-purple-900/20"
              >
                Join
              </button>
            </div>
          </form>
        ) : (
          /* MESSAGE WRITING FORM INPUT */
          <form onSubmit={handleSendMessageSubmit} className="flex items-center gap-2">
            <div className="w-8 h-8 shrink-0 rounded-xl bg-purple-500/10 flex items-center justify-center border border-purple-500/20 font-bold text-purple-400 text-xs">
              {username[0]?.toUpperCase()}
            </div>
            <input
              type="text"
              maxLength={120}
              required
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              placeholder="Start sharing, join live chat..."
              autoComplete="off"
              className="flex-1 bg-zinc-900 border border-white/5 text-white/90 placeholder-zinc-500 px-4 py-2.5 rounded-xl text-xs outline-none focus:border-purple-500/50 focus:bg-zinc-900/60 transition-all font-medium"
            />
            <button
              type="submit"
              className="p-2.5 shrink-0 bg-purple-600 hover:bg-purple-500 text-white rounded-xl transition-all active:scale-90 cursor-pointer shadow-lg shadow-purple-900/20"
            >
              <Send className="w-4 h-4 fill-white" />
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
