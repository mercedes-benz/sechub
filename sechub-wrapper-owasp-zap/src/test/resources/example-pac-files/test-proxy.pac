// SPDX-License-Identifier: MIT
function FindProxyForURL(url, host) {
    if (shExpMatch(host, "*.example.com")) {
        return "DIRECT";
    }
    return "PROXY proxy.host.com:9090";
}
