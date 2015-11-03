ProxyWrapper
====================
Local middle-man proxy that automatically switches between using another proxy or direct internet access. Useful if your computer switches environments a lot between non-proxy or a proxy set up, for example when going to work or home.

Usage
====================
- ProxyWrapper requires the Java 1.8 runtime to be installed. This project uses gradle. To build the project, simply run "./gradlew assemble" in the project directory.
- Open ProxyWrapper and add the forward proxy (the proxy you would regularly use) in the settings of the app, and select a port to listen on.
- Then change all your applications' proxy setting once to the proxy wrapper, e.g. 127.0.0.1:3128 (you can configure the port), and you won't need to change proxy settings again when switching networks.

Details
====================
ProxyWrapper detects if the specified proxy/proxies is available on the current network (by dns resolve), and uses it, or switches to direct access mode if not.

Wifi log in screens
====================
ProxyWrapper will get confused if the local network resolves all DNS in order to forward you to a browser log-in page, for example on a public WiFi network. In that case, you must manually override ProxyWrapper to use direct mode. You can do it by clicking the tray icon or going to the preferences screen in the app gui.
