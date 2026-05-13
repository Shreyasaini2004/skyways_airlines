const fs = require("fs");
const http = require("http");
const https = require("https");
const path = require("path");

const port = Number(process.env.PORT || process.env.FRONTEND_PORT || 4200);
const apiBaseUrl = process.env.API_BASE_URL || "http://localhost:8080";
const publicDir = path.join(__dirname, "public");

const mimeTypes = {
  ".html": "text/html; charset=utf-8",
  ".css": "text/css; charset=utf-8",
  ".js": "application/javascript; charset=utf-8",
  ".json": "application/json; charset=utf-8",
  ".svg": "image/svg+xml",
  ".png": "image/png",
  ".jpg": "image/jpeg",
  ".jpeg": "image/jpeg",
  ".ico": "image/x-icon"
};

const hopByHopHeaders = new Set([
  "connection",
  "keep-alive",
  "proxy-authenticate",
  "proxy-authorization",
  "te",
  "trailer",
  "transfer-encoding",
  "upgrade"
]);

function sendJson(res, status, payload) {
  res.writeHead(status, { "Content-Type": "application/json; charset=utf-8" });
  res.end(JSON.stringify(payload));
}

function proxyApiRequest(req, res) {
  const target = new URL(apiBaseUrl);
  const transport = target.protocol === "https:" ? https : http;
  const headers = { ...req.headers, host: target.host };

  for (const headerName of Object.keys(headers)) {
    if (hopByHopHeaders.has(headerName.toLowerCase())) {
      delete headers[headerName];
    }
  }

  const proxyReq = transport.request(
    {
      protocol: target.protocol,
      hostname: target.hostname,
      port: target.port,
      method: req.method,
      path: req.url,
      headers
    },
    (proxyRes) => {
      const responseHeaders = { ...proxyRes.headers };
      for (const headerName of Object.keys(responseHeaders)) {
        if (hopByHopHeaders.has(headerName.toLowerCase())) {
          delete responseHeaders[headerName];
        }
      }
      res.writeHead(proxyRes.statusCode || 502, responseHeaders);
      proxyRes.pipe(res);
    }
  );

  proxyReq.on("error", (error) => {
    sendJson(res, 502, {
      timestamp: new Date().toISOString(),
      status: 502,
      error: "API_GATEWAY_UNAVAILABLE",
      message: `Unable to reach backend gateway at ${apiBaseUrl}: ${error.message}`,
      path: req.url,
      fieldErrors: []
    });
  });

  req.pipe(proxyReq);
}

function serveStatic(req, res) {
  const requestUrl = new URL(req.url, `http://localhost:${port}`);
  const requestedPath = requestUrl.pathname === "/" ? "/index.html" : requestUrl.pathname;
  const safePath = path
    .normalize(decodeURIComponent(requestedPath))
    .replace(/^[/\\]+/, "")
    .replace(/^(\.\.[/\\])+/, "");
  const filePath = path.join(publicDir, safePath);
  const resolvedPath = path.resolve(filePath);

  if (!resolvedPath.startsWith(path.resolve(publicDir))) {
    res.writeHead(403, { "Content-Type": "text/plain; charset=utf-8" });
    res.end("Forbidden");
    return;
  }

  fs.stat(resolvedPath, (statError, stats) => {
    if (statError || !stats.isFile()) {
      fs.readFile(path.join(publicDir, "index.html"), (indexError, data) => {
        if (indexError) {
          res.writeHead(404, { "Content-Type": "text/plain; charset=utf-8" });
          res.end("Not found");
          return;
        }
        res.writeHead(200, { "Content-Type": mimeTypes[".html"] });
        res.end(data);
      });
      return;
    }

    const extension = path.extname(resolvedPath).toLowerCase();
    res.writeHead(200, {
      "Content-Type": mimeTypes[extension] || "application/octet-stream",
      "Cache-Control": "no-cache"
    });
    fs.createReadStream(resolvedPath).pipe(res);
  });
}

const server = http.createServer((req, res) => {
  if (req.url === "/config") {
    sendJson(res, 200, { apiBaseUrl, frontendPort: port });
    return;
  }

  if (req.url.startsWith("/api/")) {
    proxyApiRequest(req, res);
    return;
  }

  serveStatic(req, res);
});

server.listen(port, () => {
  console.log(`SkyWays frontend running at http://localhost:${port}`);
  console.log(`Proxying API requests to ${apiBaseUrl}`);
});
