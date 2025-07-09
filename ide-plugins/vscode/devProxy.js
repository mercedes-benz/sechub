const http = require('http');
const https = require('https');

// Create an HTTP server that listens on port 8000
const server = http.createServer((req, res) => {
    // Set up options for the HTTPS request
    const options = {
        hostname: 'localhost',
        port: 8443,
        path: req.url,
        method: req.method,
        headers: req.headers,
        rejectUnauthorized: false // Allow self-signed certificates
    };

    // Make the HTTPS request
    const proxyRequest = https.request(options, (proxyRes) => {
        // Pipe the response from the proxy request back to the original response
        res.writeHead(proxyRes.statusCode, proxyRes.headers);
        proxyRes.pipe(res, { end: true });
    });

    // Handle errors
    proxyRequest.on('error', (err) => {
        console.error('Proxy request error:', err);
        res.writeHead(500);
        res.end('Internal Server Error');
    });

    // Pipe the original request to the proxy request
    req.pipe(proxyRequest, { end: true });
});

// Start the server
server.listen(8000, () => {
    console.log('Proxy server listening on port 8000');
});