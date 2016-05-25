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
import se.ade.autoproxywrapper.loopback.LoopBackConfig;
import se.ade.autoproxywrapper.loopback.LoopBackServer;
import se.ade.autoproxywrapper.loopback.LoopBackService;
import se.ade.autoproxywrapper.model.ForwardProxy;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Queue;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static se.ade.autoproxywrapper.config.Config.getConfig;
import static se.ade.autoproxywrapper.config.Config.save;

public class MiniHttpProxy implements Runnable, ModeSelector {
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

    private ProxyMode currentMode = ProxyMode.DIRECT;
    private HttpProxyServer proxyServer;

	private LoopBackService loopBackService;

	public MiniHttpProxy() {
        EventBus.get().register(eventListener);
    }

    private void refreshProxyState() {
        if(!getConfig().isEnabled()) {
            forwardProxyAddress = null;
            isProxyResolvable = false;
            if(currentMode != ProxyMode.DIRECT) {
                setMode(ProxyMode.DIRECT);
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
                            for(ForwardProxy proxy: getConfig().getForwardProxies()) {
                                isProxyResolvable = locationProber.isAddressResolvable(proxy.getHost());
                                if (isProxyResolvable) {
                                    isProxyFound = true;
                                    InetSocketAddress inetSocketAddress = new InetSocketAddress(proxy.getHost(), proxy.getPort());
                                    if(forwardProxyAddress == null || !forwardProxyAddress.equals(inetSocketAddress)) {
                                        forwardProxyAddress = inetSocketAddress;
                                        setMode(ProxyMode.USE_PROXY);
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
                                    setMode(ProxyMode.DIRECT);
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

	public void setMode(ProxyMode mode) {
		EventBus.get().post(GenericLogEvent.info("In mode " + mode.name()));
		EventBus.get().post(new ModeChangedEvent(mode));
		currentMode = mode;
	}

	public void startProxy() {
        if(getConfig().getForwardProxies().isEmpty() || getConfig().getLocalPort() == 0) {
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
				if(getConfig().isBlockedHostsEnabled() && isBlockedHost(httpRequest.getUri())) {
					EventBus.get().post(GenericLogEvent.verbose("Blocked request: " + httpRequest.getUri()));
					return;
				}

				if(getConfig().isDirectModeHostsEnabled() && isDirectModeHost(httpRequest.getUri())) {
					EventBus.get().post(GenericLogEvent.verbose("Using direct mode for request: " + httpRequest.getUri()));
					chainedProxies.add(ChainedProxyAdapter.FALLBACK_TO_DIRECT_CONNECTION);
					return;
				}

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
                .withPort(getConfig().getLocalPort())
                .withConnectTimeout(10000)
                .withChainProxyManager(chainedProxyManager);

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

		startLoopBackService();

        proxyServer = bootstrap.start();
    }

	private void startLoopBackService() {
		getConfig().getLoopBackConfigs().add(new LoopBackConfig("ade.se", 8080, 25565, "ade minecraft"));
		save();

		List<LoopBackConfig> loopBackConfigs = getConfig().getLoopBackConfigs();
		if(loopBackConfigs == null || loopBackConfigs.size() == 0) {
			return;
		}

		loopBackService = new LoopBackService(this, loopBackConfigs);
		loopBackService.start();
	}

	private void stopLoopBackService() {
		if(loopBackService != null) {
			loopBackService.destroyService();
			loopBackService = null;
		}
	}

	private boolean isBlockedHost(String uri) {
		String host = getHost(uri);
		for(String regex : getConfig().getBlockedHosts()) {
			if(regexMatches(host, regex)) {
				return true;
			}
		}

		return false;
	}

	private boolean regexMatches(String host, String regex) {
		if(host == null || regex == null) {
			return false;
		}

		Pattern p;
		try {
			p = Pattern.compile(regex);
		} catch (PatternSyntaxException e) {
			EventBus.get().post(GenericLogEvent.error("Invalid host regex pattern: " + regex));
			return false;
		}
		return p.matcher(host).matches();
	}

	private boolean isDirectModeHost(String uri) {
		String host = getHost(uri);
		for(String regex : getConfig().getDirectModeHosts()) {
			if(regexMatches(host, regex)) {
				return true;
			}
		}

		return false;
	}

	private String getHost(String uri) {
		if(uri == null) {
			return null;
		}

		uri = uri.toLowerCase();

		if(uri.startsWith("http")) {
			try {
				uri = new URI(uri).getHost();
			} catch (URISyntaxException e) {
				return null;
			}
		}

		if(uri.contains(":")) {
			return uri.split(":")[0];
		} else {
			return uri;
		}
	}

	public void stopProxy() {
        if (proxyServer != null) {
			proxyServer.abort();
			forwardProxyAddress = null;
            proxyServer = null;
        }
		stopLoopBackService();
    }

	public ProxyMode getMode() {
		return currentMode;
	}

	@Override
    public void run() {
        startProxy();
    }
}
