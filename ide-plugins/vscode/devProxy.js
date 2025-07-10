const http = require('http');
const https = require('https');

const server = http.createServer((req, res) => {
    const options = {
        hostname: 'localhost',
        port: 8443,
        path: req.url,
        method: req.method,
        headers: req.headers,
        // allow self-signed certificates
        rejectUnauthorized: false
    };

    const proxyRequest = https.request(options, (proxyRes) => {
        res.writeHead(proxyRes.statusCode, proxyRes.headers);
        proxyRes.pipe(res, { end: true });
    });

    proxyRequest.on('error', (err) => {
        console.error('Proxy request error:', err);
        res.writeHead(500);
        res.end('Internal Server Error');
    });

    req.pipe(proxyRequest, { end: true });
});

server.listen(8000, () => {
    console.log('Proxy server listening on port 8000');
});