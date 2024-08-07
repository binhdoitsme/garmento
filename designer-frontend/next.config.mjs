/** @type {import('next').NextConfig} */
const nextConfig = {
  async rewrites() {
    return [
      { source: "/api/:path*", destination: "http://garmento.io/api/:path*" },
    ];
  },
  trailingSlash: false,
  reactStrictMode: false,
};

export default nextConfig;
