const { createProxyMiddleware } = require("http-proxy-middleware");

const proxy = {
  target: "http://garmento.io/api",
  changeOrigin: true,
};
module.exports = function (app) {
  app.use("/api", createProxyMiddleware(proxy));
};
