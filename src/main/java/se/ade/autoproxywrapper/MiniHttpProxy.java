package se.ade.autoproxywrapper;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.littleshoot.proxy.*;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import java.net.InetSocketAddress;
import java.util.Queue;

public class MiniHttpProxy {
    final static boolean DEBUG_LOG = false;
    final static long DNS_LOOKUP_INTERVAL = 5000;
    private final Config config;

    Boolean isProxyResolvable;
    Boolean lastProxyState;
    boolean isResolving;
    long lastDnsQuery = 0;
    InetSocketAddress forwardProxyAddress; //Cache of the proxy address object

    LocationProber locationProber = new LocationProber();

    public MiniHttpProxy() {
        this.config = new Config("AutoProxyWrapper.json").readFromFile().saveToFile();
    }

    private void refreshProxyState() {
        if(!isResolving) {
            if (System.currentTimeMillis() > lastDnsQuery + DNS_LOOKUP_INTERVAL) {
                isResolving = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (DEBUG_LOG) System.out.println("Checking if there is a proxy...");

                            isProxyResolvable = locationProber.isAddressResolvable(config.forwardProxyHost);
                            if (lastProxyState != isProxyResolvable) {
                                System.out.println("New forward proxy state: " + isProxyResolvable);
                                forwardProxyAddress = new InetSocketAddress(config.forwardProxyHost, config.forwardProxyPort);
                            }
                            lastProxyState = isProxyResolvable;
                            lastDnsQuery = System.currentTimeMillis();

                            if (DEBUG_LOG) System.out.println("Forward proxy resolvable: " + isProxyResolvable);
                        } catch (Exception e) {
                            System.out.print("Error in refreshProxyState: " + e.getMessage());
                        } finally {
                            isResolving = false;
                        }
                    }
                }).start();
            }
        }
    }

    public void startProxy() {
        if(this.config.forwardProxyHost == null || this.config.forwardProxyHost.isEmpty() || this.config.forwardProxyPort == 0 || this.config.localListeningPort == 0) {
            System.out.println("Config appears to be wrong. Edit AutoProxyWrapper.json.");
            return;
        }

        refreshProxyState();

        final ChainedProxy forwardProxy = new ChainedProxyAdapter() {
            @Override
            public InetSocketAddress getChainedProxyAddress() {
                return forwardProxyAddress;
            }

            @Override
            public TransportProtocol getTransportProtocol() {
                return TransportProtocol.TCP;
            }

            @Override
            public void connectionFailed(Throwable throwable) {
                System.out.println("Forward proxy connection failed: " + throwable.toString());
            }
        };

        ChainedProxyManager chainedProxyManager = new ChainedProxyManager() {
            @Override
            public void lookupChainedProxies(HttpRequest httpRequest, Queue<ChainedProxy> chainedProxies) {
                refreshProxyState();
                if(isProxyResolvable) {
                    chainedProxies.add(forwardProxy);
                }
                chainedProxies.add(ChainedProxyAdapter.FALLBACK_TO_DIRECT_CONNECTION);

                //Add proxy again last if direct fails and times out.
                chainedProxies.add(forwardProxy);
            }
        };

        HttpProxyServerBootstrap bootstrap = DefaultHttpProxyServer.bootstrap()
                .withPort(3128)
                .withChainProxyManager(chainedProxyManager);

        if(DEBUG_LOG) {
            bootstrap.withFiltersSource(new HttpFiltersSourceAdapter() {
                public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                    return new HttpFiltersAdapter(originalRequest) {
                        @Override
                        public HttpResponse clientToProxyRequest(HttpObject httpObject) {
                            if (httpObject instanceof DefaultHttpRequest) {
                                DefaultHttpRequest defaultHttpRequest = (DefaultHttpRequest) httpObject;
                                System.out.println("Requesting: " + defaultHttpRequest.getMethod()
                                                + " "
                                                + defaultHttpRequest.getUri()
                                );
                            }
                            return null;
                        }

                        @Override
                        public HttpResponse proxyToServerRequest(HttpObject httpObject) {
                            return null;
                        }

                        @Override
                        public HttpObject serverToProxyResponse(HttpObject httpObject) {
                            return httpObject;
                        }

                        @Override
                        public HttpObject proxyToClientResponse(HttpObject httpObject) {
                            return httpObject;
                        }
                    };
                }
            });
        }

        bootstrap.start();
    }
}
