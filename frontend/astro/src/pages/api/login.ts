import type { APIRoute } from "astro";
export const prerender = false;

export const POST: APIRoute = async ({ request, cookies }) => {
    try {
        const { token } = await request.json();

        if (!token) {
            return new Response(JSON.stringify({ error: "No token provided" }), { status: 400 });
        }

        // Securely set the HttpOnly cookie from the server side
        cookies.set("auth_token", token, {
            path: "/",
            httpOnly: true, // 🔒 Client-side JS can no longer read or modify this
            secure: true, // Enforces HTTPS
            sameSite: "lax",
            maxAge: 60 * 60 * 24 * 3, // 72 hours
        });

        return new Response(JSON.stringify({ success: true }), { status: 200 });
    } catch (error) {
        return new Response(JSON.stringify({ error: "Internal server error" }), { status: 500 });
    }
};
