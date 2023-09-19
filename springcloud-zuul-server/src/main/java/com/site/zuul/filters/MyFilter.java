package com.site.zuul.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.stereotype.Component;

// Set custom zuul filters
@Component
public class MyFilter extends ZuulFilter {

    /**
     * filterType
     *      pre：    before routing
     *      route：  when routing
     *      post：   after routing
     *      error：  if error
     * @return
     */
    @Override
    public String filterType() {
        return "pre";
    }

    // order: form smaller one to big one
    @Override
    public int filterOrder() {
        return 1;
    }

    // If enable filter
    @Override
    public boolean shouldFilter() {
        return true;
    }

    // Filter implementation
    @Override
    public Object run() throws ZuulException {
        System.out.println("display pre zuul filter...");
        return null;
    }
}
