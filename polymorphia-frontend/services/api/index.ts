export const API_HOST =
  process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8101";

const STATIC_BASE_URL = process.env.NEXT_PUBLIC_STATIC_BASE_URL;

export const API_STATIC_URL =
  STATIC_BASE_URL !== undefined
    ? STATIC_BASE_URL + "/static"
    : API_HOST + "/static";

const url = new URL(API_STATIC_URL);
export const API_STATIC_HOST_PATTERN = {
  protocol: url.protocol.replace(":", ""),
  hostname: url.hostname,
  port: url.port,
  pathname: url.pathname.endsWith("/")
    ? url.pathname + "**"
    : url.pathname + "/**",
  search: "",
};
