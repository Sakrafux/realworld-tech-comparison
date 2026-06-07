import type { APIRoute } from "astro";

export const POST: APIRoute = ({ cookies }) => {
    // Delete the cookie from the server side
    cookies.delete("auth_token", { path: "/" });

    return new Response(JSON.stringify({ success: true }), { status: 200 });
};
