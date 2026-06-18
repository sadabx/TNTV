/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { Channel } from "../types";

export const DEFAULT_PROXY_URL = "https://iptv-proxy.trionine.workers.dev/";

/**
 * Attaches the SSL / CORS termination proxy prefix to URLs
 */
export function getProxiedUrl(url: string): string {
  if (!url) return "";
  const proxySetting = localStorage.getItem("iptv-proxy-url") || DEFAULT_PROXY_URL;
  if (proxySetting && url.startsWith("http://")) {
    if (proxySetting.includes("?url=")) {
      return proxySetting + encodeURIComponent(url);
    }
    return proxySetting + url;
  }
  return url;
}

/**
 * Checks if the stream points to a raw MPEG-TS (.ts) url
 */
export function isTsUrl(url: string): boolean {
  if (!url) return false;
  return /\.(ts|mpegts|m2ts)(\?|$)/i.test(url);
}

/**
 * Extract YouTube ID if link points to YouTube live or regular video
 */
export function getYouTubeId(url: string): string | null {
  if (!url) return null;
  const regExp = /^.*(youtu.be\/|v\/|u\/\w\/|embed\/|watch\?v=|\&v=|live\/)([^#\&\?]*).*/;
  const match = url.match(regExp);
  return match && match[2].length === 11 ? match[2] : null;
}

/**
 * Generate a random sCPN (similar to YouTube's Stats for Nerds identifier)
 */
export function generateSCPN(): string {
  const chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  let result = "";
  for (let i = 0; i < 4; i++) {
    for (let j = 0; j < 4; j++) {
      result += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    if (i < 3) result += " ";
  }
  return result;
}

/**
 * Fetches the feed headers to verify if the server is active.
 * Handles CORS errors gracefully.
 */
export async function checkChannelStatus(ch: Channel): Promise<boolean> {
  const youtubeId = getYouTubeId(ch.streams[0]?.url);
  if (youtubeId) return true; // YouTube links are presumed active

  const url = ch.streams[0]?.url;
  if (!url) return false;

  const proxiedUrl = getProxiedUrl(url);
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), 6000);

  try {
    const res = await fetch(proxiedUrl, {
      method: "GET",
      signal: controller.signal,
      headers: { Accept: "*/*" },
    });
    clearTimeout(timeoutId);
    return res.status >= 200 && res.status < 400;
  } catch (e: any) {
    clearTimeout(timeoutId);
    if (e.name !== "AbortError") {
      // If it's a CORS restriction but not a timeout, the server is responsive!
      return true;
    }
  }
  return false;
}
