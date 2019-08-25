package guru.springframework.msscbreweryclient.web.config;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.IOReactorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Created by jt on 2019-08-07.
 */

@Component
public class NIORestTemplateCustomizer implements RestTemplateCustomizer {

    private final int connectionTimeOut;
    private final int IOThreadCount;
    private final int SoTimeOut;
    private final int DefaultMaxPerRoute;
    private final int MaxTotal;

    public NIORestTemplateCustomizer(@Value("${sfg.connectiontimeout}") int connectionTimeOut,
                                     @Value("${sfg.iothreadcount}") int IOThreadCount,
                                     @Value("${sfg.sotimeout}") int soTimeOut,
                                     @Value("${sfg.defaultmaxperroute}") int defaultMaxPerRoute,
                                     @Value("${sfg.maxtotal}") int maxTotal) {
        this.connectionTimeOut = connectionTimeOut;
        this.IOThreadCount = IOThreadCount;
        this.SoTimeOut = soTimeOut;
        this.DefaultMaxPerRoute = defaultMaxPerRoute;
        this.MaxTotal = maxTotal;
    }

    public ClientHttpRequestFactory clientHttpRequestFactory() throws IOReactorException {
        final DefaultConnectingIOReactor ioreactor = new DefaultConnectingIOReactor(IOReactorConfig.custom().
                setConnectTimeout(connectionTimeOut).
                setIoThreadCount(IOThreadCount).
                setSoTimeout(SoTimeOut).
                build());

        final PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(ioreactor);
        connectionManager.setDefaultMaxPerRoute(DefaultMaxPerRoute);
        connectionManager.setMaxTotal(MaxTotal);

        CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        return new HttpComponentsAsyncClientHttpRequestFactory(httpAsyncClient);

    }

    @Override
    public void customize(RestTemplate restTemplate) {
        try {
            restTemplate.setRequestFactory(clientHttpRequestFactory());
        } catch (IOReactorException e) {
            e.printStackTrace();
        }
    }
}