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
import se.ade.autoproxywrapper.model.ForwardProxy;

import java.net.InetSocketAddress;
import java.util.Queue;

import static se.ade.autoproxywrapper.Config.config;
import static se.ade.autoproxywrapper.events.GenericLogEvent.GenericLogEventType.INFO;
import static se.ade.autoproxywrapper.events.GenericLogEvent.GenericLogEventType.VERBOSE;

public class MiniHttpProxy implements Runnable{
    private final static long DNS_LOOKUP_INTERVAL = 5000;

    private Object eventListener = new Object() {
        @Subscribe
        public void onEvent(RestartEvent e) {
            stopProxy();
            startProxy();
        }

        @Subscribe
        public void onEvent(ShutDownEvent e) {
            stopProxy();
        }
    };

    private Boolean isProxyResolvable;
    private boolean isResolving;
    private long lastDnsQuery = 0;
    private InetSocketAddress forwardProxyAddress; //Cache of the proxy address object

    private LocationProber locationProber = new LocationProber();

    private HttpProxyServer proxyServer;

    public MiniHttpProxy() {
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
                            EventBus.get().post(new GenericLogEvent("Checking if there is a proxy...", VERBOSE));

                            boolean isProxyFound = false;
                            for(ForwardProxy proxy: config().getForwardProxies()) {
                                EventBus.get().post(new GenericLogEvent("Resolving proxy " + proxy.getHost() + ":" + proxy.getPort() + " ...", VERBOSE));

                                isProxyResolvable = locationProber.isAddressResolvable(proxy.getHost());
                                if (isProxyResolvable) {
                                    isProxyFound = true;
                                    EventBus.get().post(new GenericLogEvent("New forward proxy state: true", INFO));
                                    forwardProxyAddress = new InetSocketAddress(proxy.getHost(), proxy.getPort());
                                    EventBus.get().post(new DetectModeEvent(ProxyMode.USE_PROXY));
                                    EventBus.get().post(new GenericLogEvent("Forward proxy resolved", VERBOSE));
                                    break;
                                }
                                lastDnsQuery = System.currentTimeMillis();
                            }
                            if(!isProxyFound) {
                                EventBus.get().post(new DetectModeEvent(ProxyMode.DIRECT));
                                EventBus.get().post(new GenericLogEvent("Forward proxy unresolved, using DIRECT", VERBOSE));
                            }
                        } catch (Exception e) {
                            EventBus.get().post(new GenericLogEvent("Error in refreshProxyState: " + e.getMessage(), INFO));
                        } finally {
                            isResolving = false;
                        }
                    }
                }).start();
            }
        }
    }

    public void startProxy() {
        if(config().getForwardProxies().isEmpty() || config().getLocalPort() == 0) {
            EventBus.get().post(new GenericLogEvent("Please review your properties and proxies.", INFO));
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
                .withPort(config().getLocalPort())
                .withChainProxyManager(chainedProxyManager);

        bootstrap.withFiltersSource(new HttpFiltersSourceAdapter() {
            public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                return new HttpFiltersAdapter(originalRequest) {
                    @Override
                    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
                        if (config().isVerboseLogging()) {
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

        proxyServer = bootstrap.start();
    }

    public void stopProxy() {
        proxyServer.stop();
        proxyServer = null;
    }

    @Override
    public void run() {
        startProxy();
    }
}
