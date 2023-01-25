package pl.karolinamichalska.logo.server;

import com.google.common.collect.ImmutableSet;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

public class SinglePageAppHttpFilter extends HttpFilter {

    private static final Set<String> STATIC_SUFFIXES = ImmutableSet.of(
            ".png",
            ".woff2",
            ".ttf",
            ".ico",
            ".html",
            ".js",
            ".css",
            ".txt"
    );

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        String path = req.getRequestURI();
        if (!path.startsWith("/api") && !path.startsWith("/index.html")
                && STATIC_SUFFIXES.stream().noneMatch(path::endsWith)) {
            req.getRequestDispatcher("/index.html").forward(req, res);
        } else {
            super.doFilter(req, res, chain);
        }
    }
}
