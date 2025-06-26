const http = require('http');
const https = require('https');
const express = require('express');
const app = express();

const proxy = http.createServer(app);

app.use((req, res) => {
    const options = {
        hostname: 'localhost',
        port: 8443,
        path: req.url,
        method: req.method,
        headers: req.headers,
        rejectUnauthorized: false 
    };

    const proxyRequest = https.request(options, (proxyResponse) => {
        // sechub server /login redirect enables ssl but webview needs http bc of self singed certificates
        // http://localhost:8000 -> https://localhost:8000
        if (proxyResponse.headers.location) {
          proxyResponse.headers.location = proxyResponse.headers.location.replace('https://localhost:8000', 'http://localhost:8000');
      }

      res.writeHead(proxyResponse.statusCode, proxyResponse.headers);
      proxyResponse.pipe(res, { end: true });
    });

    req.pipe(proxyRequest, { end: true });
});

proxy.listen(8000, () => {
    console.log('Proxy server listening on port 8000');
});