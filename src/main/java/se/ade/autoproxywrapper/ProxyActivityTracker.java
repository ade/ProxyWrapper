package se.ade.autoproxywrapper;

import io.netty.handler.codec.http.*;
import org.littleshoot.proxy.*;

import javax.net.ssl.SSLSession;
import java.net.InetSocketAddress;

public class ProxyActivityTracker implements ActivityTracker{

    @Override
    public void clientConnected(InetSocketAddress clientAddress) {}

    @Override
    public void clientSSLHandshakeSucceeded(InetSocketAddress clientAddress, SSLSession sslSession) {}

    @Override
    public void clientDisconnected(InetSocketAddress clientAddress, SSLSession sslSession) {}

    @Override
    public void bytesReceivedFromClient(FlowContext flowContext, int numberOfBytes) {
        System.out.println("Bytes bytesReceivedFromClient: " + numberOfBytes);
    }

    @Override
    public void requestReceivedFromClient(FlowContext flowContext, HttpRequest httpRequest) {
        System.out.println("Request requestReceivedFromClient: " + httpRequest.getUri());
    }

    @Override
    public void bytesSentToServer(FullFlowContext flowContext, int numberOfBytes) {
        System.out.println("Bytes bytesSentToServer: " + numberOfBytes);
    }

    @Override
    public void requestSentToServer(FullFlowContext flowContext, HttpRequest httpRequest) {
        System.out.println("Request requestSentToServer: " + httpRequest.getUri());
    }

    @Override
    public void bytesReceivedFromServer(FullFlowContext flowContext, int numberOfBytes) {
        System.out.println("Bytes bytesReceivedFromServer: " + numberOfBytes);
    }

    @Override
    public void responseReceivedFromServer(FullFlowContext flowContext, HttpResponse httpResponse) {
        System.out.println("Request responseReceivedFromServer: " + httpResponse.getStatus().code());
    }

    @Override
    public void bytesSentToClient(FlowContext flowContext, int numberOfBytes) {
        System.out.println("Bytes bytesSentToClient: " + numberOfBytes);
    }

    @Override
    public void responseSentToClient(FlowContext flowContext, HttpResponse httpResponse) {
        System.out.println("Request responseSentToClient: " + httpResponse.getStatus().code());
    }
}
