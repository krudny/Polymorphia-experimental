import { API_STATIC_HOST_PATTERN } from "./services/api";
import path from "node:path";
import withBundleAnalyzer from "@next/bundle-analyzer";
import type { NextConfig } from "next";
import { RemotePattern } from "next/dist/shared/lib/image-config";

const nextConfig: NextConfig = {
  reactStrictMode: false,
  async redirects() {
    return [
      {
        source: "/knowledge-base",
        destination: "/knowledge-base/evolution-stages/",
        permanent: true,
      },
    ];
  },
  images: {
    qualities: [75],
    dangerouslyAllowLocalIP: true,
    remotePatterns: [
      API_STATIC_HOST_PATTERN as RemotePattern,
      {
        protocol: "https",
        hostname: "raw.githubusercontent.com",
      },
    ],
    formats: ["image/webp"],
    deviceSizes: [640, 750, 828, 1080, 1200, 1920, 2048, 3840],
    imageSizes: [16, 32, 48, 64, 96, 128, 256, 384],
    minimumCacheTTL: 60,
  },
  turbopack: {
    root: path.resolve(__dirname, "./"),
  },
  experimental: {
    optimizePackageImports: [
      "@mui/material",
      "react-hot-toast",
      "@tanstack/react-query",
      "@tanstack/react-form",
      "gsap",
      "zod",
    ],
  },
};

export default withBundleAnalyzer({
  enabled: process.env.ANALYZE === "true",
})(nextConfig);
