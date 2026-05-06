/**
 * HTML sanitization utility to prevent stored XSS attacks.
 * Defense-in-depth: the backend also sanitizes via OWASP HTML Sanitizer,
 * but this client-side layer strips dangerous content before rendering.
 */

/**
 * Sanitize HTML by removing script tags, event handlers, and javascript: URLs.
 * This is a client-side defense-in-depth measure; the primary sanitization
 * happens server-side with OWASP Java HTML Sanitizer.
 */
export function sanitizeHtml(html: string): string {
  if (!html) return ''

  return html
    // Remove <script> tags and their content
    .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '')
    // Remove event handler attributes (onclick, onload, onerror, etc.)
    .replace(/\s+on\w+\s*=\s*(?:"[^"]*"|'[^']*'|[^\s>]+)/gi, '')
    // Remove javascript: URLs in href/src attributes
    .replace(/(href|src)\s*=\s*(?:"javascript:[^"]*"|'javascript:[^']*')/gi, '')
}
