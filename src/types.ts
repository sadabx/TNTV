/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

export interface Stream {
  label: string;
  url: string;
  bitrate?: string;
}

export interface Channel {
  id: string;
  name: string;
  shortName: string;
  logo: string;
  quality: "FHD" | "HD" | "SD" | string;
  streams: Stream[];
  category?: string;
  description?: string;
}

export interface Category {
  name: string;
  channels: Channel[];
}

export interface ChatMessage {
  id: string;
  user: string;
  text: string;
  color: string;
  timestamp: number;
  isSelf?: boolean;
}

export type AppTheme = "toffee-orange" | "dark-neon" | "royal-red" | "nordic-blue" | "forest-green";

export interface UserPreference {
  username: string;
  volume: number;
  muted: boolean;
  theme: AppTheme;
  hideOffline: boolean;
  favorites: string[]; // channel IDs
  watchHistory: string[]; // channel IDs (recents)
}
