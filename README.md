ProxyWrapper
================

Middle-man proxy that automatically switches between using another proxy or direct internet access.

Useful for computers that switches enviroments a lot between non-proxy or a proxy set up.

Detects if the specified proxy is available on the current network (by dns resolve), and uses it, or switches to direct access mode if not.
