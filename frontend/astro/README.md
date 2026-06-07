# Astro

## Impressions

Feels great to use for static content with some interactivity. 
It's easy to build the website in a modular manner while adding some minor interactivity using the island system.

However, greater interactivity, i.e., forms, feel very wrong as they are wholly interactive, meaning the whole page
is basically one large island. While this is fine for sparse usage, if it occurs for a large part of the application
one may wonder, why even use Astro in the first place.

Statically generated dynamic routes feel good to use. On the other hand, on-demand dynamic routes feel very wonky 
to implement. Mainly because this is the only real use case forcing SSR, which then implies some further consequences.
Not only does it require handling authentication also via cookie for the use in SSR, other features like caching
must now be controlled via headers. And most obvious, deployment now requires a server environment instead of static
bundle to be served from anywhere.

I would use it for any kind of website that relies on static content where interactivity is largely contained on the
client and is mainly used in order to enhance UX. Though some sporadic form-like elements are largely fine as well.

Even some applications that theoretically rely on on-demand dynamic routes may be fine depending on the nature of the
demand. E.g., a personal blog that allows one to add more blog pages. The specific blog pages would be dynamic routes,
but due to long-lived nature of a blog, we could re-generate the static bundle on publish and re-deploy it.