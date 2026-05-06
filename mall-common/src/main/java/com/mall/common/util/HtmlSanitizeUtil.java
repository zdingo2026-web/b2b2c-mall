package com.mall.common.util;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

/**
 * HTML sanitization utility to prevent stored XSS attacks.
 * Strips dangerous tags (script, iframe, etc.) and event handlers
 * while preserving safe rich-text formatting.
 */
public final class HtmlSanitizeUtil {

    private HtmlSanitizeUtil() {
    }

    private static final PolicyFactory POLICY;

    static {
        PolicyFactory basePolicy = new HtmlPolicyBuilder()
                .allowElements(
                        "p", "br", "b", "i", "em", "strong", "a", "img",
                        "ul", "ol", "li",
                        "h1", "h2", "h3", "h4", "h5", "h6",
                        "table", "tr", "td", "th", "thead", "tbody",
                        "span", "div", "blockquote", "pre", "code",
                        "hr", "sub", "sup", "dl", "dt", "dd"
                )
                .allowAttributes("href").onElements("a")
                .allowAttributes("src").onElements("img")
                .allowAttributes("alt", "title").onElements("img")
                .allowAttributes("class", "style").globally()
                .allowAttributes("colspan", "rowspan").onElements("td", "th")
                .requireRelNofollowOnLinks()
                .allowUrlProtocols("http", "https", "mailto")
                .toFactory();

        POLICY = Sanitizers.FORMATTING.and(basePolicy);
    }

    /**
     * Sanitize HTML content to prevent XSS attacks.
     * Removes script tags, event handlers, and javascript: URLs
     * while preserving safe rich-text formatting.
     *
     * @param html the raw HTML content (may be null)
     * @return sanitized HTML, or null if input was null
     */
    public static String sanitize(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        return POLICY.sanitize(html);
    }
}
