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
import se.ade.autoproxywrapper.statistics.*;

import java.net.InetSocketAddress;
import java.util.Queue;

import static se.ade.autoproxywrapper.config.Config.get;

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

    private ProxyMode currentMode = ProxyMode.AUTO;
    private HttpProxyServer proxyServer;

	private ProxyActivityTracker proxyActivityTracker = new ProxyActivityTracker();

    public MiniHttpProxy() {
        EventBus.get().register(eventListener);
    }

    private void refreshProxyState() {
        if(!get().isEnabled()) {
            forwardProxyAddress = null;
            isProxyResolvable = false;
            if(currentMode != ProxyMode.DISABLED) {
                EventBus.get().post(GenericLogEvent.info("In bypass mode"));
                currentMode = ProxyMode.DISABLED;
            }
            return;
        }
        if(!isResolving) {
            if (System.currentTimeMillis() > lastDnsQuery + DNS_LOOKUP_INTERVAL) {
                isResolving = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            boolean isProxyFound = false;
                            for(ForwardProxy proxy: get().getForwardProxies()) {
                                isProxyResolvable = locationProber.isAddressResolvable(proxy.getHost());
                                if (isProxyResolvable) {
                                    isProxyFound = true;
                                    InetSocketAddress inetSocketAddress = new InetSocketAddress(proxy.getHost(), proxy.getPort());
                                    if(forwardProxyAddress == null || !forwardProxyAddress.equals(inetSocketAddress)) {
                                        forwardProxyAddress = inetSocketAddress;
                                        currentMode = ProxyMode.USE_PROXY;
                                        EventBus.get().post(new DetectModeEvent(ProxyMode.USE_PROXY, forwardProxyAddress));
                                    }
                                    break;
                                }
                                lastDnsQuery = System.currentTimeMillis();
                            }
                            if(!isProxyFound) {
                                forwardProxyAddress = null;
                                if(currentMode != ProxyMode.DIRECT) {
                                    EventBus.get().post(new DetectModeEvent(ProxyMode.DIRECT, null));
                                    currentMode = ProxyMode.DIRECT;
                                }
                            }
                        } catch (Exception e) {
                            EventBus.get().post(GenericLogEvent.info("Error in refreshProxyState: " + e.getMessage()));
                        } finally {
                            isResolving = false;
                        }
                    }
                }).start();
            }
        }
    }

    public void startProxy() {
        if(get().getForwardProxies().isEmpty() || get().getLocalPort() == 0) {
            EventBus.get().post(GenericLogEvent.info("No proxies and/or no local listening port. Please review your properties and proxies."));
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
                if (isProxyResolvable) {
                    chainedProxies.add(forwardProxy);
                }
            }
        };

        HttpProxyServerBootstrap bootstrap = DefaultHttpProxyServer.bootstrap()
                .withPort(get().getLocalPort())
                .withConnectTimeout(10000)
                .withChainProxyManager(chainedProxyManager)
                .plusActivityTracker(proxyActivityTracker);

        bootstrap.withFiltersSource(new HttpFiltersSourceAdapter() {
            public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                return new HttpFiltersAdapter(originalRequest) {
                    @Override
                    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
                        if (httpObject instanceof DefaultHttpRequest) {
                            DefaultHttpRequest defaultHttpRequest = (DefaultHttpRequest) httpObject;
                            EventBus.get().post(new RequestEvent(defaultHttpRequest.getMethod().toString(), defaultHttpRequest.getUri()));
                        }
                        return null;
                    }
                };
            }
        });

        proxyServer = bootstrap.start();
    }

    public void stopProxy() {
        if (proxyServer != null) {
            proxyServer.abort();

			StatisticsStorage statisticsStorage = StatisticsStorage.instance();
			statisticsStorage.insertStatistics(proxyActivityTracker.getStatistics());
			statisticsStorage.close();

            forwardProxyAddress = null;
            currentMode = ProxyMode.AUTO;
            proxyServer = null;
        }
    }

	public Statistics getStatistics() {
		return proxyActivityTracker.getStatistics();
	}

	@Override
    public void run() {
        startProxy();
    }
}
