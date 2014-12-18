package se.ade.autoproxywrapper;

import com.google.common.eventbus.Subscribe;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.littleshoot.proxy.*;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import se.ade.autoproxywrapper.events.*;

import java.awt.*;
import java.net.InetSocketAddress;
import java.util.Queue;

public class MiniHttpProxy {
    final static long DNS_LOOKUP_INTERVAL = 5000;

    boolean enableLogging = false;
    private final Config config;

    private Object eventListener = new Object() {
        @Subscribe
        public void onEvent(SetLoggingEnabledEvent e) {
            enableLogging = e.enabled;
        }

        @Subscribe
        public void onEvent(ShutDownEvent e) {
            EventBus.get().unregister(eventListener);
        }
    };

    Boolean isProxyResolvable;
    Boolean lastProxyState;
    boolean isResolving;
    long lastDnsQuery = 0;
    InetSocketAddress forwardProxyAddress; //Cache of the proxy address object

    LocationProber locationProber = new LocationProber();

    public MiniHttpProxy() {
        this.config = new Config("AutoProxyWrapper.json").readFromFile().saveToFile();
        EventBus.get().register(eventListener);
    }

    private void refreshProxyState() {
        if(!isResolving) {
            if (System.currentTimeMillis() > lastDnsQuery + DNS_LOOKUP_INTERVAL) {
                isResolving = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (enableLogging) System.out.println("Checking if there is a proxy...");

                            isProxyResolvable = locationProber.isAddressResolvable(config.forwardProxyHost);
                            if (lastProxyState != isProxyResolvable) {
                                System.out.println("New forward proxy state: " + isProxyResolvable);

                                forwardProxyAddress = new InetSocketAddress(config.forwardProxyHost, config.forwardProxyPort);

                                ProxyMode mode = isProxyResolvable ? ProxyMode.USE_PROXY : ProxyMode.DIRECT;

                                EventBus.get().post(new DetectModeEvent(mode));
                            }
                            lastProxyState = isProxyResolvable;
                            lastDnsQuery = System.currentTimeMillis();

                            if (enableLogging) System.out.println("Forward proxy resolvable: " + isProxyResolvable);
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
                EventBus.get().post(new ForwardProxyConnectionFailureEvent(throwable));
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
                .withPort(config.localListeningPort)
                .withChainProxyManager(chainedProxyManager);


        bootstrap.withFiltersSource(new HttpFiltersSourceAdapter() {
            public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                return new HttpFiltersAdapter(originalRequest) {
                    @Override
                    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
                        if (enableLogging) {
                            if (httpObject instanceof DefaultHttpRequest) {
                                DefaultHttpRequest defaultHttpRequest = (DefaultHttpRequest) httpObject;
                                EventBus.get().post(new RequestEvent(defaultHttpRequest.getMethod().toString(), defaultHttpRequest.getUri()));

                                /*
                                System.out.println("Requesting: " + defaultHttpRequest.getMethod()
                                                + " "
                                                + defaultHttpRequest.getUri()
                                );
                                */
                            }
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


        bootstrap.start();
    }
}
