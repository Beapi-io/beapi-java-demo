package demo.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.server.Compression;
import java.nio.charset.StandardCharsets;
import org.apache.coyote.http2.Http2Protocol;
//import lombok.extern.slf4j.Slf4j;

//@Slf4j
@Configuration
public class TomcatConfig  {


    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> servletContainerCustomizer() {
        return new WebServerFactoryCustomizer<TomcatServletWebServerFactory>() {

            @Override
            public void customize(TomcatServletWebServerFactory factory) {
                Compression compression = new Compression();
                compression.setEnabled(false);
                factory.setCompression(compression);
                factory.setUriEncoding(StandardCharsets.UTF_8);
                factory.addConnectorCustomizers(new TomcatConnectorCustomizer() {

                    @Override
                    public void customize(Connector connector) {
                        connector.addUpgradeProtocol(new Http2Protocol());
                        AbstractHttp11Protocol<?> httpHandler = ((AbstractHttp11Protocol<?>) connector.getProtocolHandler());
                        httpHandler.setMaxKeepAliveRequests(-1);
                        httpHandler.setRejectIllegalHeader(true);
                        httpHandler.setMaxThreads(200);
                        httpHandler.setMinSpareThreads(100);
                        httpHandler.setMaxConnections(10000);
                        //httpHandler.setUseKeepAliveResponseHeader(true);
                        //httpHandler.setKeepAliveTimeout(keepAliveTimeout);
                    }
                });
            }
        };
    }

/*
    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.addConnectorCustomizers(connector -> {
            AbstractHttp11Protocol protocol = (AbstractHttp11Protocol) connector.getProtocolHandler();

            protocol.setMaxKeepAliveRequests(50);


            System.out.println("####################################################################################");
            System.out.println("#");
            System.out.println("# TomcatCustomizer");
            System.out.println("#");
            System.out.println("# custom maxKeepAliveRequests {"+connector.getMaxKeepAliveRequests()+"}");
            System.out.println("# origin keepalive timeout: {"+ connector.getKeepAliveTimeout()+"} ms");
            System.out.println("# keepalive timeout: {"+ connector.getKeepAliveTimeout()+"} ms ");
            System.out.println("# connection timeout: {"+ connector.getConnectionTimeout()+"} ms ");
            System.out.println("# max connections: {"+ connector.getMaxConnections()+"} ");
            System.out.println("#");
            System.out.println(
                    "####################################################################################");

        });
    }
 */

}
