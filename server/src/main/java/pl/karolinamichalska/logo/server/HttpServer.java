package pl.karolinamichalska.logo.server;

import com.google.common.net.InetAddresses;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.servlet.DispatcherType;
import java.util.EnumSet;

public class HttpServer {
    private static final int DEFAULT_MAX_THREADS = 100;
    private static final int DEFAULT_HTTP_PORT = 8080;

    private final Server server;
    private final ServerConnector httpConnector;

    @Inject
    public HttpServer(GuiceResteasyBootstrapServletContextListener guiceResteasyBootstrapServletContextListener) {
        QueuedThreadPool threadPool = new QueuedThreadPool(DEFAULT_MAX_THREADS);
        threadPool.setMinThreads(10);
        threadPool.setName("http-worker");
        server = new Server(threadPool);

        httpConnector = new ServerConnector(server);
        httpConnector.setName("http");
        httpConnector.setHost(getLocalhost());
        httpConnector.setPort(DEFAULT_HTTP_PORT);
        server.addConnector(httpConnector);

        HandlerCollection handlers = new HandlerCollection();
        handlers.addHandler(createServletContext(guiceResteasyBootstrapServletContextListener));

        HandlerList rootHandlers = new HandlerList();
        rootHandlers.addHandler(handlers);
        server.setHandler(rootHandlers);
    }

    private static String getLocalhost() {
        return InetAddresses.fromInteger(0).getHostAddress();
    }

    private static Handler createServletContext(GuiceResteasyBootstrapServletContextListener guiceResteasyBootstrapServletContextListener) {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setBaseResource(Resource.newClassPathResource("/static/web"));
        context.addEventListener(guiceResteasyBootstrapServletContextListener);

        ErrorPageErrorHandler errorHandler = new ErrorPageErrorHandler();
        errorHandler.addErrorPage(404, "/");
        context.setErrorHandler(errorHandler);

        context.addFilter(
                SinglePageAppHttpFilter.class,
                "/*",
                EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD));
        context.addServlet(DefaultServlet.class, "/*");
        context.addServlet(HttpServletDispatcher.class, "/api/*");

        return context;
    }

    @PostConstruct
    public void start() throws Exception {
        server.start();
    }

    @PreDestroy
    public void stop() throws Exception {
        server.stop();
        server.destroy();
    }

    public String getHttpAddress() {
        return "http://%s:%s".formatted(getHttpHost(), getHttpPort());
    }

    public String getHttpHost() {
        return httpConnector.getHost();
    }

    public int getHttpPort() {
        return httpConnector.getPort();
    }
}
